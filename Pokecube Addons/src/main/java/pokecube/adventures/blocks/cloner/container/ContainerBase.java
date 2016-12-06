package pokecube.adventures.blocks.cloner.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thut.lib.CompatWrapper;

public abstract class ContainerBase extends Container
{
    final IInventory             tile;
    final public InventoryPlayer inv;
    protected final int[]        vals;

    public ContainerBase(InventoryPlayer inv, IInventory tile)
    {
        super();
        this.inv = inv;
        this.tile = tile;
        vals = new int[tile.getFieldCount()];
    }

    @Override
    public void addListener(IContainerListener listener)
    {
        super.addListener(listener);
        listener.sendAllWindowProperties(this, this.tile);
    }

    @Override
    public boolean canInteractWith(EntityPlayer p)
    {
        return true;
    }

    @Override
    /** Looks for changes made in the container, sends them to every
     * listener. */
    public void detectAndSendChanges()
    {
        for (int i = 0; i < this.listeners.size(); ++i)
        {
            IContainerListener icrafting = this.listeners.get(i);
            for (int j = 0; j < vals.length; j++)
            {
                if (vals[j] != tile.getField(j))
                {
                    icrafting.sendProgressBarUpdate(this, j, this.tile.getField(j));
                }
                vals[j] = tile.getField(j);
            }
        }
        updateCrafting();
        super.detectAndSendChanges();
    }

    /** Called when the container is closed. */
    @Override
    public void onContainerClosed(EntityPlayer playerIn)
    {
        super.onContainerClosed(playerIn);
        tile.closeInventory(playerIn);
    }

    @Override
    /** Handles slot click.
     * 
     * @param mode
     *            0 = basic click, 1 = shift click, 2 = hotbar, 3 = pick block,
     *            4 = drop, 5 = ?, 6 = double click */
    public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player)
    {
        return super.slotClick(slotId, dragType, clickTypeIn, player);
    }

    @Override
    /** This is called on shift click Take a stack from the specified inventory
     * slot. */
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index)
    {
        ItemStack itemstack = CompatWrapper.nullStack;
        Slot slot = this.inventorySlots.get(index);
        System.out.println(index + " " + slot);
        if (slot != null && slot.getHasStack())
        {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();
            int num = tile.getSizeInventory();
            if (index == 0)
            {
                if (!this.mergeItemStack(itemstack1, num, 36 + num, true)) { return CompatWrapper.nullStack; }

                slot.onSlotChange(itemstack1, itemstack);
            }
            else if (index >= num && index < 27 + num)
            {
                if (!this.mergeItemStack(itemstack1, 1, num, false)) { return CompatWrapper.nullStack; }
            }
            else if (index >= 27 + num && index < 36 + num)
            {
                if (!this.mergeItemStack(itemstack1, num, 27 + num, false)) { return CompatWrapper.nullStack; }
            }
            else if (!this.mergeItemStack(itemstack1, num, 36 + num, false)) { return CompatWrapper.nullStack; }

            if (!CompatWrapper.isValid(itemstack1))
            {
                slot.putStack(CompatWrapper.nullStack);
            }
            else
            {
                slot.onSlotChanged();
            }
            if (CompatWrapper.getStackSize(itemstack1) != CompatWrapper
                    .getStackSize(itemstack)) { return CompatWrapper.nullStack; }
            slot.func_190901_a(playerIn, itemstack1);
        }
        return itemstack;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void updateProgressBar(int id, int data)
    {
        this.tile.setField(id, data);
    }

    protected abstract void updateCrafting();
}
