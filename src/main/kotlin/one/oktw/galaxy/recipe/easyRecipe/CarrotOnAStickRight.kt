/*
 * OKTW Galaxy Project
 * Copyright (C) 2018-2020
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

package one.oktw.galaxy.recipe.easyRecipe

import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.inventory.CraftingInventory
import net.minecraft.item.Items
import net.minecraft.recipe.CraftingRecipe
import net.minecraft.recipe.RecipeSerializer
import net.minecraft.util.Identifier
import net.minecraft.world.World
import one.oktw.galaxy.recipe.utils.Ingredient
import one.oktw.galaxy.recipe.utils.RecipeUtils

class CarrotOnAStickRight : CraftingRecipe {
    private val item = Items.CARROT_ON_A_STICK.defaultStack
    private val list = listOf(
        Ingredient(item = Items.AIR), Ingredient(item = Items.AIR), Ingredient(item = Items.STICK),
        Ingredient(item = Items.AIR), Ingredient(item = Items.STICK), Ingredient(item = Items.STRING),
        Ingredient(item = Items.STICK), Ingredient(item = Items.CARROT), Ingredient(item = Items.STRING)
    )

    override fun matches(inv: CraftingInventory, world: World): Boolean = RecipeUtils.isItemShapedMatches(inv, 3, 3, list)

    override fun craft(inv: CraftingInventory) = item.copy()

    @Environment(EnvType.CLIENT)
    override fun fits(width: Int, height: Int): Boolean {
        throw NotImplementedError()
    }

    override fun getOutput() = item

    override fun getId() = Identifier("galaxy", "easy_recipe/carrot_on_a_stick_right")

    override fun getSerializer(): RecipeSerializer<*> {
        TODO("Not yet implemented, support client mod.")
    }
}