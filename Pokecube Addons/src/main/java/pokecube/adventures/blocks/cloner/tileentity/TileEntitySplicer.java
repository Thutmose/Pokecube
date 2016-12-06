package pokecube.adventures.blocks.cloner.tileentity;

import li.cil.oc.api.network.SimpleComponent;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Optional.Interface;
import net.minecraftforge.fml.common.Optional.InterfaceList;
import pokecube.adventures.blocks.cloner.ClonerHelper;
import pokecube.adventures.blocks.cloner.crafting.CraftMatrix;
import pokecube.adventures.blocks.cloner.recipe.IPoweredRecipe;
import pokecube.adventures.blocks.cloner.recipe.RecipeSplice;
import pokecube.core.items.pokemobeggs.ItemPokemobEgg;

@InterfaceList({ @Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "OpenComputers") })
public class TileEntitySplicer extends TileClonerBase implements SimpleComponent
{
    public TileEntitySplicer()
    {
        /** 1 slot for egg, 1 slot for gene container,1 slot for output, 1 slot
         * for stabiliser. */
        super(4, 3);
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack)
    {
        switch (index)
        {
        case 0:// DNA Container
            return ClonerHelper.getGenes(stack) != null;
        case 1:// DNA Selector
            return !ClonerHelper.getGeneSelectors(stack).isEmpty();
        case 2:// DNA Destination
            return ItemPokemobEgg.getEntry(stack) != null;
        }
        return false;
    }

    @Override
    public String getName()
    {
        return "splicer";
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
        return recipe == RecipeSplice.class;
    }

    @Override
    public String getComponentName()
    {
        return "dnaSplicer";
    }
    
    
}
