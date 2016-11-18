package pokecube.compat.jei.pokemobs;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import pokecube.core.database.PokedexEntry.EvolutionData;
import thut.lib.CompatWrapper;

public class PokemobRecipe implements IRecipe
{
    EvolutionData data;

    public PokemobRecipe(EvolutionData data)
    {
        this.data = data;
    }

    @Override
    public boolean matches(InventoryCrafting inv, World worldIn)
    {
        return true;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv)
    {
        return CompatWrapper.nullStack;
    }

    @Override
    public int getRecipeSize()
    {
        return 2;
    }

    @Override
    public ItemStack getRecipeOutput()
    {
        return CompatWrapper.nullStack;
    }

    @Override
    public ItemStack[] getRemainingItems(InventoryCrafting inv)
    {
        return null;
    }

}
