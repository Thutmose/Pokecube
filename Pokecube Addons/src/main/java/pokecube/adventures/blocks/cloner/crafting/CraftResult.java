package pokecube.adventures.blocks.cloner.crafting;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.item.ItemStack;

public class CraftResult extends InventoryCraftResult
{
    final IInventory inventory;
    final int        shift;

    public CraftResult(IInventory inventory, int shift)
    {
        this.inventory = inventory;
        this.shift = shift;
    }

    @Override
    public void clear()
    {
        inventory.setInventorySlotContents(9, ItemStack.EMPTY);
    }

    @Override
    public void closeInventory(EntityPlayer player)
    {
    }

    /** Removes up to a specified number of items from an inventory slot and
     * returns them in a new stack.
     * 
     * @param index
     *            The slot to remove from.
     * @param count
     *            The maximum amount of items to remove. */
    @Override
    public ItemStack decrStackSize(int index, int count)
    {
        return inventory.decrStackSize(index + shift, count);
    }

    @Override
    public int getField(int id)
    {
        return 0;
    }

    @Override
    public int getFieldCount()
    {
        return 0;
    }

    /** Returns the maximum stack size for a inventory slot. Seems to always be
     * 64, possibly will be extended. */
    @Override
    public int getInventoryStackLimit()
    {
        return 64;
    }

    /** Returns the stack in the given slot.
     * 
     * @param index
     *            The slot to retrieve from. */
    @Override
    public ItemStack getStackInSlot(int index)
    {
        return inventory.getStackInSlot(index + shift);
    }

    /** Returns true if automation is allowed to insert the given stack
     * (ignoring stack size) into the given slot. */
    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack)
    {
        return true;
    }

    /** Do not make give this method the name canInteractWith because it clashes
     * with Container */
    @Override
    public boolean isUsableByPlayer(EntityPlayer player)
    {
        return true;
    }

    /** For tile entities, ensures the chunk containing the tile entity is saved
     * to disk later - the game won't think it hasn't changed and skip it. */
    @Override
    public void markDirty()
    {
    }

    @Override
    public void openInventory(EntityPlayer player)
    {
    }

    /** Removes a stack from the given slot and returns it.
     * 
     * @param index
     *            The slot to remove a stack from. */
    @Override
    public ItemStack removeStackFromSlot(int index)
    {
        return inventory.removeStackFromSlot(index + shift);
    }

    @Override
    public void setField(int id, int value)
    {
    }

    /** Sets the given item stack to the specified slot in the inventory (can be
     * crafting or armor sections). */
    @Override
    public void setInventorySlotContents(int index, ItemStack stack)
    {
        inventory.setInventorySlotContents(index + shift, stack);
    }
}