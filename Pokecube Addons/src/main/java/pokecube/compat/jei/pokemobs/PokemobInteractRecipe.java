package pokecube.compat.jei.pokemobs;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.PokedexEntry.InteractionLogic;
import thut.lib.CompatWrapper;
import thut.lib.IDefaultRecipe;

public class PokemobInteractRecipe implements IDefaultRecipe
{
    final PokedexEntry     entry;
    final ItemStack        key;
    final ItemStack        outputStack;
    final PokedexEntry     outputForme;
    final InteractionLogic logic;

    public PokemobInteractRecipe(PokedexEntry entry, ItemStack key, ItemStack outputStack, PokedexEntry outputForme,
            InteractionLogic logic)
    {
        this.entry = entry;
        this.key = key;
        this.logic = logic;
        this.outputForme = outputForme;
        this.outputStack = outputStack;
    }

    @Override
    public boolean matches(InventoryCrafting inv, World worldIn)
    {
        return true;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv)
    {
        return getRecipeOutput();
    }

    @Override
    public ItemStack getRecipeOutput()
    {
        return outputStack != null ? outputStack : CompatWrapper.nullStack;
    }

    ResourceLocation registryName;

    // @Override
    public IRecipe setRegistryName(ResourceLocation name)
    {
        registryName = name;
        return this;
    }

    // @Override
    public ResourceLocation getRegistryName()
    {
        return registryName;
    }

    // @Override
    public Class<IRecipe> getRegistryType()
    {
        return IRecipe.class;
    }

    // @Override
    public int getRecipeSize()
    {
        // TODO Auto-generated method stub
        return 0;
    }
}
