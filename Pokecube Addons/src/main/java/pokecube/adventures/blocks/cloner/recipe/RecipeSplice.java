package pokecube.adventures.blocks.cloner.recipe;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import pokecube.adventures.blocks.cloner.ClonerHelper;
import pokecube.adventures.blocks.cloner.recipe.RecipeSelector.ItemBasedSelector;
import pokecube.core.database.PokedexEntry;
import pokecube.core.items.pokemobeggs.ItemPokemobEgg;
import thut.lib.CompatWrapper;

public class RecipeSplice implements IPoweredRecipe
{
    public static int ENERGYCOST = 10000;

    ItemStack         output     = CompatWrapper.nullStack;
    ItemStack         dna        = CompatWrapper.nullStack;
    ItemStack         egg        = CompatWrapper.nullStack;
    ItemStack         selector   = CompatWrapper.nullStack;

    public RecipeSplice()
    {
    }

    @Override
    public ItemStack getRecipeOutput()
    {
        return this.output;
    }

    /** Returns an Item that is the result of this recipe */
    @Override
    @Nullable
    public ItemStack getCraftingResult(InventoryCrafting inv)
    {
        if (!CompatWrapper.isValid(output)) return CompatWrapper.nullStack;
        return this.output.copy();
    }

    @Override
    public int getEnergyCost()
    {
        return ENERGYCOST;
    }

    /** Used to check if a recipe matches current crafting inventory */
    @Override
    public boolean matches(InventoryCrafting inv, World worldIn)
    {
        output = CompatWrapper.nullStack;
        dna = inv.getStackInSlot(0);
        egg = inv.getStackInSlot(2);
        selector = inv.getStackInSlot(1);
        if (ClonerHelper.getGenes(dna) == null)
        {
            dna = CompatWrapper.nullStack;
        }
        if (!CompatWrapper.isValid(egg) || !(egg.getItem() instanceof ItemPokemobEgg))
        {
            egg = CompatWrapper.nullStack;
        }
        if (ClonerHelper.getGeneSelectors(selector).isEmpty()) selector = CompatWrapper.nullStack;

        if (CompatWrapper.isValid(selector) && CompatWrapper.isValid(dna) && CompatWrapper.isValid(egg))
        {
            PokedexEntry entry = ClonerHelper.getFromGenes(dna);
            if (entry == null) entry = ClonerHelper.getFromGenes(egg);
            if (entry == null) return false;
            egg = egg.copy();
            if (egg.getTagCompound() == null) egg.setTagCompound(new NBTTagCompound());
            egg.getTagCompound().setString("pokemob", entry.getName());
            ClonerHelper.mergeGenes(ClonerHelper.getGenes(dna), egg, new ItemBasedSelector(selector));
            CompatWrapper.setStackSize(egg, 1);
            output = egg;
            return true;
        }
        return false;
    }

    @Override
    public int getRecipeSize()
    {
        return 3;
    }

    @Override
    public ItemStack toKeep(int slot, ItemStack stackIn, InventoryCrafting inv)
    {
        return IPoweredRecipe.super.toKeep(slot, stackIn, inv);
    }

    @Override
    public boolean complete(IPoweredProgress tile)
    {
        List<ItemStack> remaining = Lists.newArrayList(getRemainingItems(tile.getCraftMatrix()));
        for (int i = 0; i < remaining.size(); i++)
        {
            ItemStack stack = remaining.get(i);
            if (CompatWrapper.isValid(stack)) tile.setInventorySlotContents(i, stack);
            else tile.decrStackSize(i, 1);
        }
        tile.setInventorySlotContents(tile.getOutputSlot(), getRecipeOutput());
        return true;
    }
}
