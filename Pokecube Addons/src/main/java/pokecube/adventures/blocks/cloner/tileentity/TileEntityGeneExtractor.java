package pokecube.adventures.blocks.cloner.tileentity;

import net.minecraft.item.ItemStack;
import pokecube.adventures.blocks.cloner.ClonerHelper;
import pokecube.adventures.blocks.cloner.crafting.CraftMatrix;
import pokecube.adventures.blocks.cloner.recipe.IPoweredRecipe;
import pokecube.adventures.blocks.cloner.recipe.RecipeExtract;
import pokecube.core.utils.Tools;
import thut.api.entity.genetics.IMobGenetics;
import thut.lib.CompatWrapper;

public class TileEntityGeneExtractor extends TileClonerBase
{
    public TileEntityGeneExtractor()
    {
        /** 1 slot for object to extract genes from, 1 slot for gene container,
         * 1 slot for output, 1 slot for stabiliser. */
        super(4, 3);
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack)
    {
        switch (index)
        {
        case 0:// DNA Container
            return ClonerHelper.isDNAContainer(stack);
        case 1:// DNA Selector
            return !ClonerHelper.getGeneSelectors(stack).isEmpty();
        case 2:// DNA Source
            IMobGenetics genes = ClonerHelper.getGenes(stack);
            if (genes == null && CompatWrapper.isValid(stack)) for (ItemStack stack1 : ClonerHelper.DNAITEMS.keySet())
            {
                if (Tools.isSameStack(stack1, stack)) { return true; }
            }
            return genes != null;
        }
        return false;
    }

    @Override
    public String getName()
    {
        return "extractor";
    }

    @Override
    public CraftMatrix getCraftMatrix()
    {
        if (craftMatrix == null) this.craftMatrix = new CraftMatrix(null, this, 1, 3);
        return craftMatrix;
    }

    @Override
    public boolean isValid(Class<? extends IPoweredRecipe> recipe)
    {
        return recipe == RecipeExtract.class;
    }
}
