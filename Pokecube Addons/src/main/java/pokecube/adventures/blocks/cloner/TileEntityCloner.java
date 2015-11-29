package pokecube.adventures.blocks.cloner;

import cofh.api.energy.TileEnergyHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

public class TileEntityCloner extends TileEnergyHandler implements IInventory
{
    public CraftMatrix          craftMatrix;
    public InventoryCraftResult result;
    private ItemStack[]         inventory = new ItemStack[10];

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);
        NBTBase temp = nbt.getTag("Inventory");
        if (temp instanceof NBTTagList)
        {
            NBTTagList tagList = (NBTTagList) temp;
            for (int i = 0; i < tagList.tagCount(); i++)
            {
                NBTTagCompound tag = tagList.getCompoundTagAt(i);
                byte slot = tag.getByte("Slot");

                if (slot >= 0 && slot < inventory.length)
                {
                    inventory[slot] = ItemStack.loadItemStackFromNBT(tag);
                }
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);
        NBTTagList itemList = new NBTTagList();
        for (int i = 0; i < inventory.length; i++)
        {
            ItemStack stack = inventory[i];

            if (stack != null)
            {
                NBTTagCompound tag = new NBTTagCompound();
                tag.setByte("Slot", (byte) i);
                stack.writeToNBT(tag);
                itemList.appendTag(tag);
            }
        }
        nbt.setTag("Inventory", itemList);
    }

    /** Overriden in a sign to provide the text. */
    @SuppressWarnings("rawtypes")
    @Override
    public Packet getDescriptionPacket()
    {
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        if (worldObj.isRemote) return new S35PacketUpdateTileEntity(this.getPos(), 3, nbttagcompound);
        this.writeToNBT(nbttagcompound);
        if (craftMatrix != null)
        {
            craftMatrix.eventHandler.onCraftMatrixChanged(craftMatrix);
        }
        return new S35PacketUpdateTileEntity(this.getPos(), 3, nbttagcompound);
    }

    /** Called when you receive a TileEntityData packet for the location this
     * TileEntity is currently in. On the client, the NetworkManager will always
     * be the remote server. On the server, it will be whomever is responsible
     * for sending the packet.
     *
     * @param net
     *            The NetworkManager the packet originated from
     * @param pkt
     *            The data packet */
    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt)
    {
        if (worldObj.isRemote)
        {
            NBTTagCompound nbt = pkt.getNbtCompound();
            readFromNBT(nbt);
            if (craftMatrix != null)
            {
                craftMatrix.eventHandler.onCraftMatrixChanged(craftMatrix);
            }
        }
    }

    @Override
    public String getCommandSenderName()
    {
        return "cloner";
    }

    @Override
    public boolean hasCustomName()
    {
        return false;
    }

    @Override
    public IChatComponent getDisplayName()
    {
        return new ChatComponentText("cloner");
    }

    @Override
    public int getSizeInventory()
    {
        return 10;
    }

    @Override
    public ItemStack getStackInSlot(int index)
    {
        if (inventory[index] != null && inventory[index].stackSize <= 0) inventory[index] = null;

        return inventory[index];
    }

    @Override
    public ItemStack decrStackSize(int slot, int count)
    {
        if (this.inventory[slot] != null)
        {
            ItemStack itemStack;

            itemStack = inventory[slot].splitStack(count);

            if (inventory[slot].stackSize <= 0)
            {
                inventory[slot] = null;
            }
            return itemStack;
        }
        return null;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slot)
    {
        if (inventory[slot] != null)
        {
            ItemStack stack = inventory[slot];
            inventory[slot] = null;
            return stack;
        }
        return null;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack)
    {
        if (stack == null || stack.stackSize <= 0) inventory[index] = null;
        else inventory[index] = stack;
    }

    @Override
    public int getInventoryStackLimit()
    {
        return 64;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player)
    {
        return true;
    }

    @Override
    public void openInventory(EntityPlayer player)
    {

    }

    @Override
    public void closeInventory(EntityPlayer player)
    {

    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack)
    {
        return index != 0;
    }

    @Override
    public int getField(int id)
    {
        return storage.getEnergyStored();
    }

    @Override
    public void setField(int id, int value)
    {
        storage.setEnergyStored(value);
    }

    @Override
    public int getFieldCount()
    {
        return 1;
    }

    @Override
    public void clear()
    {
        for (int i = 0; i < 10; i++)
            inventory[i] = null;
    }

    public static class CraftMatrix extends InventoryCrafting
    {
        /** Class containing the callbacks for the events on_GUIClosed and
         * on_CraftMaxtrixChanged. */
        protected final Container eventHandler;
        final TileEntityCloner    cloner;

        public CraftMatrix(Container eventHandlerIn, TileEntityCloner cloner)
        {
            super(eventHandlerIn, 3, 3);
            this.eventHandler = eventHandlerIn;
            this.cloner = cloner;
        }

        @Override
        /** Returns the stack in the given slot.
         * 
         * @param index
         *            The slot to retrieve from. */
        public ItemStack getStackInSlot(int index)
        {
            return index >= this.getSizeInventory() ? null : cloner.getStackInSlot(index);
        }

        @Override
        /** Returns the itemstack in the slot specified (Top left is 0, 0).
         * Args: row, column */
        public ItemStack getStackInRowAndColumn(int row, int column)
        {
            return row >= 0 && row < 3 && column >= 0 && column <= 3 ? this.getStackInSlot(row + column * 3) : null;
        }

        @Override
        /** Removes a stack from the given slot and returns it.
         * 
         * @param index
         *            The slot to remove a stack from. */
        public ItemStack getStackInSlotOnClosing(int index)
        {
            return cloner.getStackInSlotOnClosing(index);
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
            ItemStack ret = cloner.decrStackSize(index, count);
            this.eventHandler.onCraftMatrixChanged(this);
            return ret;
        }

        @Override
        /** Sets the given item stack to the specified slot in the inventory
         * (can be crafting or armor sections). */
        public void setInventorySlotContents(int index, ItemStack stack)
        {
            cloner.setInventorySlotContents(index, stack);
            this.eventHandler.onCraftMatrixChanged(this);
        }

        @Override
        public int getHeight()
        {
            return 3;
        }

        @Override
        public int getWidth()
        {
            return 3;
        }
    }

    public static class CraftResult extends InventoryCraftResult
    {
        final TileEntityCloner cloner;

        public CraftResult(TileEntityCloner cloner)
        {
            this.cloner = cloner;
        }

        /** Returns the stack in the given slot.
         * 
         * @param index
         *            The slot to retrieve from. */
        public ItemStack getStackInSlot(int index)
        {
            return cloner.getStackInSlot(index + 9);
        }

        /** Removes up to a specified number of items from an inventory slot and
         * returns them in a new stack.
         * 
         * @param index
         *            The slot to remove from.
         * @param count
         *            The maximum amount of items to remove. */
        public ItemStack decrStackSize(int index, int count)
        {
            return cloner.decrStackSize(index + 9, count);
        }

        /** Removes a stack from the given slot and returns it.
         * 
         * @param index
         *            The slot to remove a stack from. */
        public ItemStack getStackInSlotOnClosing(int index)
        {
            return cloner.getStackInSlotOnClosing(index + 9);
        }

        /** Sets the given item stack to the specified slot in the inventory
         * (can be crafting or armor sections). */
        public void setInventorySlotContents(int index, ItemStack stack)
        {
            cloner.setInventorySlotContents(index + 9, stack);
        }

        /** Returns the maximum stack size for a inventory slot. Seems to always
         * be 64, possibly will be extended. */
        public int getInventoryStackLimit()
        {
            return 64;
        }

        /** For tile entities, ensures the chunk containing the tile entity is
         * saved to disk later - the game won't think it hasn't changed and skip
         * it. */
        public void markDirty()
        {
        }

        /** Do not make give this method the name canInteractWith because it
         * clashes with Container */
        public boolean isUseableByPlayer(EntityPlayer player)
        {
            return true;
        }

        public void openInventory(EntityPlayer player)
        {
        }

        public void closeInventory(EntityPlayer player)
        {
        }

        /** Returns true if automation is allowed to insert the given stack
         * (ignoring stack size) into the given slot. */
        public boolean isItemValidForSlot(int index, ItemStack stack)
        {
            return true;
        }

        public int getField(int id)
        {
            return 0;
        }

        public void setField(int id, int value)
        {
        }

        public int getFieldCount()
        {
            return 0;
        }

        public void clear()
        {
            System.out.println("clearing");
            cloner.setInventorySlotContents(9, null);
        }

    }
}
