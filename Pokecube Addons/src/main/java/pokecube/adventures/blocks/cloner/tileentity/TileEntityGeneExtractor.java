package pokecube.adventures.blocks.cloner.tileentity;

import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.SimpleComponent;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.Optional.Interface;
import net.minecraftforge.fml.common.Optional.InterfaceList;
import pokecube.adventures.blocks.cloner.ClonerHelper;
import pokecube.adventures.blocks.cloner.crafting.CraftMatrix;
import pokecube.adventures.blocks.cloner.recipe.IPoweredRecipe;
import pokecube.adventures.blocks.cloner.recipe.RecipeExtract;
import pokecube.core.utils.Tools;
import thut.api.entity.genetics.IMobGenetics;
import thut.lib.CompatWrapper;

@InterfaceList({ @Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "OpenComputers") })
public class TileEntityGeneExtractor extends TileClonerBase implements SimpleComponent
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

    @Override
    public String getComponentName()
    {
        return "dnaExtractor";
    }

    @Optional.Method(modid = "OpenComputers")
    public Object[] getSourceInfo(Context context, Arguments args) throws Exception
    {
        return null;
    }
}
