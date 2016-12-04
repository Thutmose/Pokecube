package pokecube.adventures.blocks.cloner.recipe;

import net.minecraft.item.crafting.IRecipe;

public interface IPoweredRecipe extends IRecipe
{
    int getEnergyCost();
}
