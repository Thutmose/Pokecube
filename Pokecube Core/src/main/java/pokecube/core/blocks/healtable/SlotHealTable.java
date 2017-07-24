/**
 *
 */
package pokecube.core.blocks.healtable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import pokecube.core.items.pokecubes.PokecubeManager;

/** @author Manchou */
public class SlotHealTable extends Slot
{
    /** @param par1iInventory
     * @param par2
     * @param par3
     * @param par4 */
    public SlotHealTable(EntityPlayer par1EntityPlayer, IInventory inventory, int slotIndex, int xDisplay,
            int yDisplay)
    {
        super(inventory, slotIndex, xDisplay, yDisplay);
    }

    public void heal()
    {
        ItemStack stack = this.getStack();
        PokecubeManager.heal(stack);
    }

    @Override
    public boolean isItemValid(ItemStack itemstack)
    {
        return ContainerHealTable.isItemValid(itemstack);
    }

    @Override
    public void onSlotChanged()
    {
        super.onSlotChanged();
    }
}
