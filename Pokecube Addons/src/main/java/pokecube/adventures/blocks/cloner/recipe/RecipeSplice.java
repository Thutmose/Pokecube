package pokecube.adventures.blocks.cloner.recipe;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import pokecube.adventures.blocks.cloner.ClonerHelper;
import pokecube.adventures.blocks.cloner.recipe.RecipeSelector.ItemBasedSelector;
import pokecube.adventures.blocks.cloner.recipe.RecipeSelector.SelectorValue;
import pokecube.core.database.PokedexEntry;
import thut.lib.CompatWrapper;

public class RecipeSplice implements IPoweredRecipe
{
    public static int ENERGYCOST = 10000;

    ItemStack         output     = ItemStack.EMPTY;
    ItemStack         dna        = ItemStack.EMPTY;
    ItemStack         egg        = ItemStack.EMPTY;
    ItemStack         selector   = ItemStack.EMPTY;

    public boolean    fixed      = false;

    public RecipeSplice()
    {
    }

    public RecipeSplice(boolean fixed)
    {
        this.fixed = fixed;
    }

    public void setSelector(ItemStack selector)
    {
        this.selector = selector;
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
        if (!CompatWrapper.isValid(output)) return ItemStack.EMPTY;
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
        output = ItemStack.EMPTY;
        dna = inv.getStackInSlot(0);
        egg = inv.getStackInSlot(2);
        if (!fixed) selector = inv.getStackInSlot(1);
        if (ClonerHelper.getGenes(dna) == null)
        {
            dna = ItemStack.EMPTY;
        }
        if (ClonerHelper.getGenes(egg) == null)
        {
            egg = ItemStack.EMPTY;
        }
        if (ClonerHelper.getGeneSelectors(selector).isEmpty()) selector = ItemStack.EMPTY;

        if (CompatWrapper.isValid(selector) && CompatWrapper.isValid(dna) && CompatWrapper.isValid(egg))
        {
            PokedexEntry entry = ClonerHelper.getFromGenes(dna);
            if (entry == null) entry = ClonerHelper.getFromGenes(egg);
            if (entry == null) return false;
            egg = egg.copy();
            if (egg.getTagCompound() == null) egg.setTagCompound(new NBTTagCompound());
            ClonerHelper.spliceGenes(ClonerHelper.getGenes(dna), egg, new ItemBasedSelector(selector));
            egg.setCount(1);
            output = egg;
            return true;
        }
        return false;
    }

    @Override
    public ItemStack toKeep(int slot, ItemStack stackIn, InventoryCrafting inv)
    {
        boolean keepDNA = false;
        boolean keepSelector = false;
        SelectorValue value = ClonerHelper.getSelectorValue(selector);
        if (value.dnaDestructChance < Math.random()) keepDNA = true;
        if (value.selectorDestructChance < Math.random()) keepSelector = true;
        if (slot == 0 && keepDNA) return stackIn;
        if (slot == 1 && keepSelector) return stackIn;
        if (slot == 0 && CompatWrapper.isValid(stackIn) && stackIn.getItem() == Items.POTIONITEM)
            return new ItemStack(Items.GLASS_BOTTLE);
        return IPoweredRecipe.super.toKeep(slot, stackIn, inv);
    }

    @Override
    public boolean complete(IPoweredProgress tile)
    {
        List<ItemStack> remaining = Lists.newArrayList(getRemainingItems(tile.getCraftMatrix()));
        tile.setInventorySlotContents(tile.getOutputSlot(), getRecipeOutput());
        for (int i = 0; i < remaining.size(); i++)
        {
            ItemStack stack = remaining.get(i);
            if (CompatWrapper.isValid(stack)) tile.setInventorySlotContents(i, stack);
            else tile.decrStackSize(i, 1);
        }
        if (tile.getCraftMatrix().eventHandler != null)
        {
            tile.getCraftMatrix().eventHandler.detectAndSendChanges();
        }
        return true;
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
