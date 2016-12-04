package pokecube.adventures.blocks.cloner.crafting;

import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;

public class CraftMatrix extends InventoryCrafting
{
    /** Class containing the callbacks for the events on_GUIClosed and
     * on_CraftMaxtrixChanged. */
    public final Container eventHandler;
    final int              height;
    final int              width;
    final IInventory       inventory;

    public CraftMatrix(Container eventHandlerIn, IInventory inventory, int width, int height)
    {
        super(eventHandlerIn, width, height);
        this.eventHandler = eventHandlerIn;
        this.inventory = inventory;
        this.width = width;
        this.height = height;
    }

    @Override
    /** Removes up to a specified number of items from an inventory slot and
     * returns them in a new stack.
     * 
     * @param index
     *            The slot to remove from.
     * @param count
     *            The maximum amount of items to remove. */
    public ItemStack decrStackSize(int index, int count)
    {
        ItemStack ret = inventory.decrStackSize(index, count);
        if (eventHandler != null) this.eventHandler.onCraftMatrixChanged(this);
        return ret;
    }

    @Override
    public int getHeight()
    {
        return height;
    }

    @Override
    /** Returns the itemstack in the slot specified (Top left is 0, 0). Args:
     * row, column */
    public ItemStack getStackInRowAndColumn(int row, int column)
    {
        return row >= 0 && row < height && column >= 0 && column <= width ? this.getStackInSlot(row + column * width)
                : null;
    }

    @Override
    /** Returns the stack in the given slot.
     * 
     * @param index
     *            The slot to retrieve from. */
    public ItemStack getStackInSlot(int index)
    {
        return index >= this.getSizeInventory() ? null : inventory.getStackInSlot(index);
    }

    @Override
    public int getWidth()
    {
        return width;
    }

    @Override
    /** Removes a stack from the given slot and returns it.
     * 
     * @param index
     *            The slot to remove a stack from. */
    public ItemStack removeStackFromSlot(int index)
    {
        return inventory.removeStackFromSlot(index);
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack)
    {
        return inventory.isItemValidForSlot(index, stack);
    }

    @Override
    /** Sets the given item stack to the specified slot in the inventory (can be
     * crafting or armor sections). */
    public void setInventorySlotContents(int index, ItemStack stack)
    {
        inventory.setInventorySlotContents(index, stack);
        if (eventHandler != null) eventHandler.onCraftMatrixChanged(this);
    }
}