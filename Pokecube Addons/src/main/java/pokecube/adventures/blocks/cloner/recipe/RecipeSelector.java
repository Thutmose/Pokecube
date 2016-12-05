package pokecube.adventures.blocks.cloner.recipe;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import pokecube.adventures.blocks.cloner.ClonerHelper;
import pokecube.adventures.blocks.cloner.IGeneSelector;
import thut.api.entity.genetics.Alleles;
import thut.api.entity.genetics.Gene;
import thut.lib.CompatWrapper;
import thut.lib.IDefaultRecipe;

public class RecipeSelector implements IDefaultRecipe
{
    public static class ItemBasedSelector implements IGeneSelector
    {
        final ItemStack selector;

        public ItemBasedSelector(ItemStack selector)
        {
            this.selector = selector;
        }

        @Override
        public Alleles merge(Alleles source, Alleles destination)
        {
            Set<Class<? extends Gene>> selected = ClonerHelper.getGeneSelectors(selector);
            if (selected.contains(source.getExpressed().getClass()))
            {
                if (destination == null) return source;
                return IGeneSelector.super.merge(source, destination);
            }
            return null;
        }
    }

    private static Map<ItemStack, Float> selectorValues = Maps.newHashMap();

    public static void addSelector(ItemStack stack, Float value)
    {
        selectorValues.put(stack, value);
    }

    ItemStack output = CompatWrapper.nullStack;

    @Override
    public boolean matches(InventoryCrafting inv, World worldIn)
    {
        if (inv.getSizeInventory() < getRecipeSize()) return false;
        ItemStack book = inv.getStackInSlot(0);
        ItemStack modifier = inv.getStackInSlot(1);
        if (ClonerHelper.getGeneSelectors(book).isEmpty() || !CompatWrapper.isValid(modifier)) return false;

        System.out.println(inv.getSizeInventory() + " " + inv.getStackInSlot(0));
        return false;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv)
    {
        return output;
    }

    @Override
    public int getRecipeSize()
    {
        return 2;
    }

    @Override
    public ItemStack getRecipeOutput()
    {
        return output;
    }

}
