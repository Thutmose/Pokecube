package pokecube.adventures.blocks.cloner.recipe;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import pokecube.adventures.blocks.cloner.ClonerHelper;
import pokecube.adventures.blocks.cloner.ClonerHelper.DNAPack;
import pokecube.adventures.blocks.cloner.recipe.RecipeSelector.ItemBasedSelector;
import pokecube.adventures.blocks.cloner.recipe.RecipeSelector.SelectorValue;
import pokecube.core.entity.pokemobs.genetics.genes.SpeciesGene.SpeciesInfo;
import pokecube.core.utils.Tools;
import thut.api.entity.genetics.Alleles;
import thut.api.entity.genetics.IMobGenetics;
import thut.lib.CompatWrapper;

public class RecipeExtract implements IPoweredRecipe
{
    public static int ENERGYCOST  = 10000;

    ItemStack         output      = CompatWrapper.nullStack;
    ItemStack         source      = CompatWrapper.nullStack;
    ItemStack         destination = CompatWrapper.nullStack;
    ItemStack         selector    = CompatWrapper.nullStack;

    public boolean    fixed       = false;

    public RecipeExtract()
    {
    }

    public RecipeExtract(boolean fixed)
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
        IMobGenetics genes;
        source:
        if ((genes = ClonerHelper.getGenes(source)) == null)
        {
            List<ItemStack> stacks = Lists.newArrayList(ClonerHelper.DNAITEMS.keySet());
            Collections.shuffle(stacks);
            if (CompatWrapper.isValid(source)) for (ItemStack stack : stacks)
            {
                if (Tools.isSameStack(stack, source))
                {
                    DNAPack pack = ClonerHelper.DNAITEMS.get(stack);
                    if (Math.random() > pack.chance) continue;
                    Alleles alleles = pack.alleles;
                    SpeciesInfo info = alleles.getExpressed().getValue();
                    System.out.println(info.entry + " " + pack.chance);
                    genes = IMobGenetics.GENETICS_CAP.getDefaultInstance();
                    genes.getAlleles().put(alleles.getExpressed().getKey(), alleles);
                    break source;
                }
            }
            source = CompatWrapper.nullStack;
        }
        System.out.println(destination);
        output = destination.copy();
        CompatWrapper.setStackSize(output, 1);
        if (!CompatWrapper.isValid(source)) return output;
        if (output.getTagCompound() == null) output.setTagCompound(new NBTTagCompound());
        ClonerHelper.mergeGenes(genes, output, new ItemBasedSelector(selector));
        CompatWrapper.setStackSize(output, 1);
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
        destination = inv.getStackInSlot(0);
        source = inv.getStackInSlot(2);
        if (!fixed) selector = inv.getStackInSlot(1);
        IMobGenetics genes;
        source:
        if ((genes = ClonerHelper.getGenes(source)) == null)
        {
            List<ItemStack> stacks = Lists.newArrayList(ClonerHelper.DNAITEMS.keySet());
            Collections.shuffle(stacks);
            if (CompatWrapper.isValid(source)) for (ItemStack stack : stacks)
            {
                if (Tools.isSameStack(stack, source))
                {
                    DNAPack pack = ClonerHelper.DNAITEMS.get(stack);
                    Alleles alleles = pack.alleles;
                    genes = IMobGenetics.GENETICS_CAP.getDefaultInstance();
                    genes.getAlleles().put(alleles.getExpressed().getKey(), alleles);
                    break source;
                }
            }
            source = CompatWrapper.nullStack;
        }
        if (!(ClonerHelper.isDNAContainer(destination)))
        {
            destination = CompatWrapper.nullStack;
        }
        if (ClonerHelper.getGeneSelectors(selector).isEmpty()) selector = CompatWrapper.nullStack;
        if (CompatWrapper.isValid(selector) && CompatWrapper.isValid(source)
                && CompatWrapper.isValid(destination)) { return true; }
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
        boolean keepDNA = false;
        boolean keepSelector = false;

        SelectorValue value = ClonerHelper.getSelectorValue(selector);
        if (value.dnaDestructChance < Math.random()) keepDNA = true;
        if (value.selectorDestructChance < Math.random()) keepSelector = true;

        if (slot == 2 && keepDNA) return stackIn;
        if (slot == 1 && keepSelector) return stackIn;

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
        if (tile.getCraftMatrix().eventHandler != null)
        {
            tile.getCraftMatrix().eventHandler.detectAndSendChanges();
        }
        return true;
    }
}
