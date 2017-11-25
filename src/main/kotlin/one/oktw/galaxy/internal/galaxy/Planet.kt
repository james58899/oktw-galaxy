package one.oktw.galaxy.internal.galaxy

import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.Updates.set
import one.oktw.galaxy.Main
import one.oktw.galaxy.Main.Companion.planetManager
import org.bson.Document
import org.spongepowered.api.Sponge
import org.spongepowered.api.world.World
import org.spongepowered.api.world.storage.WorldProperties
import java.util.*

class Planet internal constructor(uuid: UUID) {
    private val server = Sponge.getServer()
    private val database = Main.databaseManager.database
    private val planets = database.getCollection("Planet")
    private val planet: Document

    val uniqueId: UUID
        get() = planet["UUID"] as UUID

    var name: String
        get() = planet.getString("Name")
        set(name) {
            planets.findOneAndUpdate(eq("UUID", uniqueId), set("name", name))
        }

    var size: Int
        get() = planet.getInteger("Size")
        set(size) {
            val properties = worldProp.get()

            properties.worldBorderTargetDiameter = (size * 16).toDouble()
            if (server.saveWorldProperties(properties)) {
                planets.findOneAndUpdate(eq("UUID", uniqueId), set("Size", size))
            }
        }

    var security: SecurityLevel
        get() = SecurityLevel.fromInt(planet.getInteger("Security"))
        set(level) {
            planets.findOneAndUpdate(eq("UUID", uniqueId), set("Security", level))
        }

    val world: Optional<World>
        get() = planetManager.loadWorld(uniqueId)

    val worldProp: Optional<WorldProperties>
        get() = server.getWorldProperties(uniqueId)

    init {
        this.planet = planets.find(eq("UUID", uuid)).first()
    }
}
