package pokecube.core.blocks.pc;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotPC extends Slot {

	public boolean release = false;
	
	public SlotPC(IInventory inventory, int slotIndex, int xDisplayPosition, int yDisplayPosition)
    {
        super(inventory, slotIndex, xDisplayPosition, yDisplayPosition);
    }

    /**
     * Return whether this slot's stack can be taken from this slot.
     */
    @Override
	public boolean canTakeStack(EntityPlayer par1EntityPlayer)
    {
    	return !release;
    }
	
    @Override
    public boolean isItemValid(ItemStack itemstack)
    {
    	return ContainerPC.isItemValid(itemstack);
    }


    /**
     * Called when the stack in a Slot changes
     */
    @Override
	public void onSlotChanged()
    {
    //	System.out.println("Slot change "+mod_Pokecube.isOnClientSide()+" "+this.getStack()+" "+this.getSlotIndex());
    	if(this.getStack()==null)
    		inventory.setInventorySlotContents(getSlotIndex(), null);
        this.inventory.markDirty();
    }
    /**
     * Helper method to put a stack in the slot.
     */
    @Override
	public void putStack(ItemStack par1ItemStack)
    {
        this.inventory.setInventorySlotContents(this.getSlotIndex(), par1ItemStack);
//        if(this.getStack()!=null && par1ItemStack!=null)
//        {
            this.onSlotChanged();
//        }
    }
}
