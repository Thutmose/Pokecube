package pokecube.core.blocks.tradingTable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import thut.lib.CompatWrapper;

public class ContainerTMCreator extends Container
{
    /** Returns true if the item is a filled pokecube.
     *
     * @param itemstack
     *            the itemstack to test
     * @return true if the id is a filled pokecube one, false otherwise */
    protected static boolean isItemValid(ItemStack itemstack)
    {
        return true;
    }

    TileEntityTradingTable tile;

    public ContainerTMCreator(TileEntityTradingTable tile, InventoryPlayer playerInv)
    {
        this.tile = tile;
        if (tile != null) tile.moves(playerInv.player);
        bindInventories(playerInv);
    }

    public void bindInventories(InventoryPlayer playerInv)
    {
        clearSlots();
        addSlotToContainer(new SlotTMCreator(tile == null ? new InventoryBasic("", false, 1) : tile, 0, 15, 12));
        bindPlayerInventory(playerInv);
    }

    private void bindPlayerInventory(InventoryPlayer playerInventory)
    {
        // Inventory
        for (int y = 0; y < 3; y++)
            for (int x = 0; x < 9; x++)
                addSlotToContainer(new Slot(playerInventory, x + y * 9 + 9, 8 + x * 18, 84 + y * 18));

        // Action Bar
        for (int x = 0; x < 9; x++)
            addSlotToContainer(new Slot(playerInventory, x, 8 + x * 18, 142));
    }

    @Override
    public boolean canInteractWith(EntityPlayer entityPlayer)
    {
        return tile.isUseableByPlayer(entityPlayer);
    }

    protected void clearSlots()
    {
        this.inventorySlots.clear();
    }

    public TileEntityTradingTable getTile()
    {
        return tile;
    }

    @Override
    public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player)
    {
        return super.slotClick(slotId, dragType, clickTypeIn, player);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index)
    {
        ItemStack itemstack = CompatWrapper.nullStack;
        Slot slot = this.inventorySlots.get(index);
        if (slot != null && slot.getHasStack())
        {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();
            if (index < 1)
            {
                if (!this.mergeItemStack(itemstack1, 1, this.inventorySlots.size(),
                        false)) { return CompatWrapper.nullStack; }
            }
            else if (!this.mergeItemStack(itemstack1, 0, 1, false)) { return CompatWrapper.nullStack; }
            if (!CompatWrapper.isValid(itemstack1))
            {
                slot.putStack(CompatWrapper.nullStack);
            }
            else
            {
                slot.onSlotChanged();
            }
        }
        return itemstack;
    }

}
