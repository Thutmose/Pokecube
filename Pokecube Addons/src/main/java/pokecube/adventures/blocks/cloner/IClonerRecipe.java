package pokecube.adventures.blocks.cloner;

import net.minecraft.item.crafting.IRecipe;

public interface IClonerRecipe extends IRecipe
{
    int getEnergyCost();
    
    boolean splicerRecipe();
}
