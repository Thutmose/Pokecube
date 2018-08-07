package pokecube.adventures.items.bags;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotBag extends Slot
{
    public SlotBag(IInventory inventory, int slotIndex, int xDisplay, int yDisplay)
    {
        super(inventory, slotIndex, xDisplay, yDisplay);
    }
    @Override
    public boolean isItemValid(ItemStack itemstack)
    {
        return ContainerBag.isItemValid(itemstack);
    }
}
