/*
 * OKTW Galaxy Project
 * Copyright (C) 2018-2019
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package one.oktw.galaxy.command.commands

import com.mojang.authlib.GameProfile
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import io.netty.buffer.Unpooled.wrappedBuffer
import net.minecraft.client.network.packet.CommandSuggestionsS2CPacket
import net.minecraft.client.network.packet.CustomPayloadS2CPacket
import net.minecraft.command.arguments.GameProfileArgumentType
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.LiteralText
import net.minecraft.util.PacketByteBuf
import one.oktw.galaxy.Main.Companion.PROXY_IDENTIFIER
import one.oktw.galaxy.Main.Companion.main
import one.oktw.galaxy.command.Command
import one.oktw.galaxy.event.type.ProxyPacketReceiveEvent
import one.oktw.galaxy.event.type.RequestCommendCompletionsEvent
import one.oktw.galaxy.proxy.api.ProxyAPI.decode
import one.oktw.galaxy.proxy.api.ProxyAPI.encode
import one.oktw.galaxy.proxy.api.packet.CreateGalaxy
import one.oktw.galaxy.proxy.api.packet.SearchPlayer
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class Join : Command {
    private var completeID = ConcurrentHashMap<UUID, Int>()
    override fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        dispatcher.register(
            CommandManager.literal("join")
                .executes { context ->
                    execute(context.source, listOf(context.source.player.gameProfile))
                }
                .then(
                    CommandManager.argument("target", GameProfileArgumentType.gameProfile())
                        //用來移除 ＠ 開頭的自動完成
                        .suggests { context, suggestionsBuilder ->
                            context.source.minecraftServer.playerManager.playerList
                                .forEach { suggestionsBuilder.suggest(it.name.asString()) }
                            return@suggests suggestionsBuilder.buildFuture()
                        }
                        .executes { context ->
                            execute(context.source, GameProfileArgumentType.getProfileArgument(context, "target"))
                        }
                )
        )
        main!!.eventManager.register(RequestCommendCompletionsEvent::class) { event ->
            val command = event.packet.partialCommand
            if (command.toLowerCase().startsWith("/join ")) {
                completeID[event.player.uuid] = event.packet.completionId
                // completeID[event.player.uuid] = event.packet.completionId
                event.player.networkHandler.sendPacket(
                    CustomPayloadS2CPacket(
                        PROXY_IDENTIFIER,
                        PacketByteBuf(wrappedBuffer(encode(SearchPlayer(command.toLowerCase().removePrefix("/join "), 100))))
                    )
                )
            }
        }
        main!!.eventManager.register(ProxyPacketReceiveEvent::class) { event ->
            val data: Any = decode(event.packet.array())
            if (data is SearchPlayer.Result) {
                val id = completeID[event.player.uuid]
                if (id != null) {
                    val suggestion = SuggestionsBuilder("", 0)
                    data.players.forEach { player ->
                        suggestion.suggest(player)
                    }

                    event.player.networkHandler.sendPacket(
                        CommandSuggestionsS2CPacket(
                            id,
                            suggestion.buildFuture().get()
                        )
                    )
                }
            }
        }
    }

    private fun execute(source: ServerCommandSource, collection: Collection<GameProfile>): Int {
        val player = collection.first()

        source.player.networkHandler.sendPacket(
            CustomPayloadS2CPacket(PROXY_IDENTIFIER, PacketByteBuf(wrappedBuffer(encode(CreateGalaxy(player.id)))))
        )
        source.sendFeedback(LiteralText(if (source.player.gameProfile == player) "正在加入您的星系" else "正在加入 ${player.name} 的星系"), false)

        return com.mojang.brigadier.Command.SINGLE_SUCCESS
    }
}
