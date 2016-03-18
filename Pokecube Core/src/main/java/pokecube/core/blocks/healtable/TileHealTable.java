package pokecube.core.blocks.healtable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import pokecube.core.interfaces.PokecubeMod;
import thut.api.maths.Vector3;

public class TileHealTable extends TileEntity implements IInventory, ITickable
{
    public static boolean noSound = false;
    private ItemStack[] inventory;

    Vector3             here = Vector3.getNewVector();

    int                   ticks   = 0;
    boolean               stopped = false;
    public TileHealTable()
    {
        this.inventory = new ItemStack[9];
    }

    @Override
    public void clear()
    {
    }

    @Override
    public void closeInventory(EntityPlayer player)
    {
    }

    @Override
    public ItemStack decrStackSize(int slotIndex, int amount)
    {
        ItemStack stack = getStackInSlot(slotIndex);

        if (stack != null)
        {
            if (stack.stackSize <= amount)
            {
                setInventorySlotContents(slotIndex, null);
            }
            else
            {
                stack = stack.splitStack(amount);

                if (stack.stackSize == 0)
                {
                    setInventorySlotContents(slotIndex, null);
                }
            }
        }

        return stack;
    }

    /** Overriden in a sign to provide the text. */
    @Override
    @SuppressWarnings("rawtypes")
    public Packet getDescriptionPacket()
    {
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        if (worldObj.isRemote) return new S35PacketUpdateTileEntity(pos, 3, nbttagcompound);
        this.writeToNBT(nbttagcompound);
        return new S35PacketUpdateTileEntity(pos, 3, nbttagcompound);
    }

    @Override
    public IChatComponent getDisplayName()
    {
        return null;
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

    @Override
    public int getInventoryStackLimit()
    {
        return 64;
    }

    @Override
    public String getName()
    {
        return "TileEntityHealTable";
    }

    @Override
    public int getSizeInventory()
    {
        return this.inventory.length;
    }

    @Override
    public ItemStack getStackInSlot(int slotIndex)
    {
        return this.inventory[slotIndex];
    }

    @Override
    public boolean hasCustomName()
    {
        return true;
    }

    /** invalidates a tile entity */
    @Override
    public void invalidate()
    {
        super.invalidate();
        if (worldObj.isRemote && !PokecubeMod.getProxy().isSoundPlaying(here))
        {
            worldObj.playRecord(pos, null);
        }
    }

    @Override
    public boolean isItemValidForSlot(int i, ItemStack itemstack)
    {
        return ContainerHealTable.isItemValid(itemstack);
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player)
    {
        return worldObj.getTileEntity(pos) == this && player.getDistanceSq(pos) < 64;
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
        }
    }

    @Override
    public void openInventory(EntityPlayer player)
    {
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound)
    {
        super.readFromNBT(tagCompound);
        NBTTagList tagList = (NBTTagList) tagCompound.getTag("Inventory");
        // ticks = tagCompound.getInteger("time");
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

    @Override
    public ItemStack removeStackFromSlot(int slotIndex)
    {
        ItemStack stack = getStackInSlot(slotIndex);

        if (stack != null)
        {
            setInventorySlotContents(slotIndex, null);
        }

        return stack;
    }

    @Override
    public void setField(int id, int value)
    {
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack)
    {
        this.inventory[slot] = stack;

        if (stack != null && stack.stackSize > getInventoryStackLimit())
        {
            stack.stackSize = getInventoryStackLimit();
        }
    }

    @Override
    public void update()
    {
        int power = worldObj.getStrongPower(pos);
        if (power == 0)
        {
            if (worldObj.isRemote && PokecubeMod.getProxy().isSoundPlaying(here)) worldObj.playRecord(pos, null);
            return;
        }
        here.set(this);
        if (!noSound && worldObj.isRemote && !PokecubeMod.getProxy().isSoundPlaying(here))
        {
            worldObj.playRecord(pos, null);
            worldObj.playRecord(pos, "pokecube:pokecenterloop");
        }
        ticks++;
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound)
    {
        super.writeToNBT(tagCompound);
        NBTTagList itemList = new NBTTagList();
        tagCompound.setInteger("time", ticks);
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

        tagCompound.setTag("Inventory", itemList);
    }
}