package pokecube.adventures.blocks.cloner.tileentity;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import pokecube.adventures.blocks.cloner.ClonerHelper;
import pokecube.adventures.blocks.cloner.crafting.CraftMatrix;
import pokecube.adventures.blocks.cloner.crafting.PoweredProcess;
import pokecube.adventures.blocks.cloner.recipe.IPoweredProgress;
import thut.core.common.blocks.DefaultInventory;
import thut.lib.CompatWrapper;

public class TileEntityGeneExtractor extends TileEntity implements DefaultInventory, IPoweredProgress
{
    public static int     MAXENERGY      = 256;
    public int            energy         = 0;
    public int            progress       = 0;
    public int            total          = 0;
    public PoweredProcess currentProcess = null;

    public TileEntityGeneExtractor()
    {
        /** 1 slot for object to extract genes from, 1 slot for gene container,
         * 1 slot for output, 1 slot for stabiliser. */
        inventory = CompatWrapper.makeList(4);
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
            return ClonerHelper.getGenes(stack) != null;
        }
        return false;
    }

    @Override
    public int getField(int id)
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void setField(int id, int value)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public int getFieldCount()
    {
        // TODO Auto-generated method stub
        return 2;
    }

    @Override
    public String getName()
    {
        // TODO Auto-generated method stub
        return "extractor";
    }

    final List<ItemStack> inventory;

    @Override
    public List<ItemStack> getInventory()
    {
        return inventory;
    }

    @Override
    public int getEnergy()
    {
        return energy;
    }

    @Override
    public void setEnergy(int energy)
    {
        this.energy = energy;
    }

    @Override
    public int getProgress()
    {
        return progress;
    }

    @Override
    public void setProgress(int progress)
    {
        this.progress = progress;
    }

    public CraftMatrix  craftMatrix;
    public EntityPlayer user;

    @Override
    public void setCraftMatrix(CraftMatrix matrix)
    {
        craftMatrix = matrix;
    }

    @Override
    public CraftMatrix getCraftMatrix()
    {
        if (craftMatrix == null) this.craftMatrix = new CraftMatrix(null, this, 3, 1);
        return craftMatrix;
    }

    @Override
    public EntityPlayer getUser()
    {
        return user;
    }

}
