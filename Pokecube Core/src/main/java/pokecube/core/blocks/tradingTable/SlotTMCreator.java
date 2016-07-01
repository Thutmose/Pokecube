package pokecube.core.blocks.tradingTable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import pokecube.core.items.ItemTM;

public class SlotTMCreator extends Slot {

	public SlotTMCreator(IInventory par1iInventory, int par2, int par3, int par4) {
		super(par1iInventory, par2, par3, par4);
	}

    /**
     * Return whether this slot's stack can be taken from this slot.
     */
    @Override
	public boolean canTakeStack(EntityPlayer par1EntityPlayer)
    {
        return true;
    }

    @Override
    public boolean isItemValid(ItemStack itemstack)
    {
    	return (itemstack.getItem() instanceof ItemTM);
    }
}
