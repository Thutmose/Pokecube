package pokecube.adventures.blocks.afa;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import pokecube.core.items.pokecubes.PokecubeManager;

public class ContainerAFA extends Container
{
    public TileEntityAFA   tile;
    public World    worldObj;
    public BlockPos pos;

    public ContainerAFA(TileEntityAFA tile, InventoryPlayer playerInv)
    {
        super();
        this.tile = tile;
        this.worldObj = tile.getWorld();
        this.pos = tile.getPos();
        bindInventories(playerInv);
    }

    protected void clearSlots()
    {
        this.inventorySlots.clear();
    }

    public void bindInventories(InventoryPlayer playerInv)
    {
        clearSlots();
        bindPlayerInventory(playerInv);
        addSlotToContainer(new AFASlot(tile, 0, 15, 12));
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
    public boolean canInteractWith(EntityPlayer playerIn)
    {
        return playerIn.getUniqueID().equals(tile.placer);
    }

    @Override
    public ItemStack slotClick(int i, int j, int flag, EntityPlayer entityplayer)
    {
        if (i == -999) return null;
        if (flag != 0 && flag != 5)
        {
            ItemStack itemstack = null;
            Slot slot = (Slot) inventorySlots.get(i);

            if (slot != null && slot.getHasStack())
            {
                ItemStack itemstack1 = slot.getStack();
                itemstack = itemstack1.copy();

                if (i < 6)
                {
                    if (!mergeItemStack(itemstack1, 1, 37, true)) { return null; }
                }
                else
                {
                    if (itemstack != null && !tile.isItemValidForSlot(36, itemstack1)) { return null; }

                    if (!mergeItemStack(itemstack1, 0, 1, false)) { return null; }
                }

                if (itemstack1.stackSize == 0)
                {
                    slot.putStack(null);
                }
                else
                {
                    slot.onSlotChanged();
                }

                if (itemstack1.stackSize != itemstack.stackSize)
                {
                    // slot.onPickupFromSlot(itemstack1);
                }
                else
                {
                    return null;
                }
            }

            if (itemstack != null)
            {
                return itemstack;
            }
            else
            {
                return null;
            }
        }
        else
        {
            return super.slotClick(i, j, flag, entityplayer);
        }
    }

    private static class AFASlot extends Slot
    {

        public AFASlot(IInventory inventoryIn, int index, int xPosition, int yPosition)
        {
            super(inventoryIn, index, xPosition, yPosition);
        }
        @Override
        public boolean isItemValid(ItemStack itemstack)
        {
            return PokecubeManager.isFilled(itemstack);
        }
    }
}
