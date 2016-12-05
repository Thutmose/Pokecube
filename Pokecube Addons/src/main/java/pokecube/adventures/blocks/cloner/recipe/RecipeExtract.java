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

    public RecipeExtract()
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
        destination = inv.getStackInSlot(0);
        source = inv.getStackInSlot(2);
        selector = inv.getStackInSlot(1);
        IMobGenetics genes;
        source:
        if ((genes = ClonerHelper.getGenes(source)) == null)
        {
            if (CompatWrapper.isValid(source)) for (ItemStack stack : ClonerHelper.DNAITEMS.keySet())
            {
                if (Tools.isSameStack(stack, source))
                {
                    Alleles alleles = ClonerHelper.DNAITEMS.get(stack);
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
        if (CompatWrapper.isValid(selector) && CompatWrapper.isValid(source) && CompatWrapper.isValid(destination))
        {
            output = destination.copy();
            if (output.getTagCompound() == null) output.setTagCompound(new NBTTagCompound());
            ClonerHelper.mergeGenes(genes, output, new ItemBasedSelector(selector));
            CompatWrapper.setStackSize(output, 1);
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
        if (tile.getCraftMatrix().eventHandler != null)
        {
            tile.getCraftMatrix().eventHandler.detectAndSendChanges();
        }
        return true;
    }
}
