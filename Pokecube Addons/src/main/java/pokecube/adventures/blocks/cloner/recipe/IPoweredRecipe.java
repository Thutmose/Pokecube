package pokecube.adventures.blocks.cloner.recipe;

import thut.lib.IDefaultRecipe;

public interface IPoweredRecipe extends IDefaultRecipe
{
    int getEnergyCost();

    boolean complete(IPoweredProgress tile);
}
