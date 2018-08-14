package pokecube.compat.jei.pokemobs.evolutions;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import pokecube.core.database.PokedexEntry.EvolutionData;
import thut.lib.IDefaultRecipe;

public class PokemobRecipe implements IDefaultRecipe
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
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack getRecipeOutput()
    {
        return ItemStack.EMPTY;
    }

    ResourceLocation registryName;

    @Override
    public IRecipe setRegistryName(ResourceLocation name)
    {
        registryName = name;
        return this;
    }

    @Override
    public ResourceLocation getRegistryName()
    {
        return registryName;
    }

    @Override
    public Class<IRecipe> getRegistryType()
    {
        return IRecipe.class;
    }

}
