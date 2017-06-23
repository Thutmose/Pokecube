package pokecube.core.blocks.pc;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import thut.lib.CompatWrapper;

public class SlotPC extends Slot
{

    public boolean release = false;

    public SlotPC(IInventory inventory, int slotIndex, int xDisplay, int yDisplay)
    {
        super(inventory, slotIndex, xDisplay, yDisplay);
    }

    /** Return whether this slot's stack can be taken from this slot. */
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

    /** Called when the stack in a Slot changes */
    @Override
    public void onSlotChanged()
    {
        if (this.getStack() == CompatWrapper.nullStack)
            inventory.setInventorySlotContents(getSlotIndex(), CompatWrapper.nullStack);
        this.inventory.markDirty();
    }

    /** Helper method to put a stack in the slot. */
    @Override
    public void putStack(ItemStack par1ItemStack)
    {
        this.inventory.setInventorySlotContents(this.getSlotIndex(), par1ItemStack);
        this.onSlotChanged();
    }
}
