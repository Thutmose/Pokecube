package pokecube.compat.jei.pokemobs.interactions;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.PokedexEntry.InteractionLogic.Interaction;
import pokecube.core.utils.Tools;
import thut.lib.IDefaultRecipe;

public class PokemobInteractRecipe implements IDefaultRecipe
{
    final PokedexEntry entry;
    final ItemStack    key;
    final ItemStack    outputStack;
    final PokedexEntry outputForme;
    final Interaction  logic;

    public PokemobInteractRecipe(PokedexEntry entry, Interaction logic, ItemStack outputStack)
    {
        this.entry = entry;
        this.key = logic.key;
        this.logic = logic;
        this.outputForme = logic.forme;
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
        return outputStack != null ? outputStack : ItemStack.EMPTY;
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

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof PokemobInteractRecipe)
        {
            PokemobInteractRecipe other = (PokemobInteractRecipe) obj;
            return other.entry == entry && Tools.isSameStack(key, other.key);
        }
        return false;
    }
}
