package pokecube.core.blocks.healtable;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.ITextComponent;
import pokecube.core.PokecubeCore;
import pokecube.core.blocks.TileEntityOwnable;
import thut.api.maths.Vector3;
import thut.lib.CompatWrapper;

public class TileHealTable extends TileEntityOwnable implements IInventory, ITickable
{
    public static SoundEvent MUSICLOOP;
    private List<ItemStack>  inventory = NonNullList.<ItemStack> withSize(9, ItemStack.EMPTY);

    Vector3                  here      = Vector3.getNewVector();

    int                      ticks     = 0;
    boolean                  stopped   = false;

    public TileHealTable()
    {
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
    public ItemStack decrStackSize(int slot, int count)
    {
        if (CompatWrapper.isValid(getStackInSlot(slot)))
        {
            ItemStack itemStack;
            itemStack = getStackInSlot(slot).splitStack(count);
            if (!CompatWrapper.isValid(getStackInSlot(slot)))
            {
                setInventorySlotContents(slot, ItemStack.EMPTY);
            }
            return itemStack;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ITextComponent getDisplayName()
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
        return this.inventory.size();
    }

    @Override
    public ItemStack getStackInSlot(int slotIndex)
    {
        return this.inventory.get(slotIndex);
    }

    /** Overriden in a sign to provide the text. */
    @Override
    public SPacketUpdateTileEntity getUpdatePacket()
    {
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        if (getWorld().isRemote) return new SPacketUpdateTileEntity(pos, 3, nbttagcompound);
        this.writeToNBT(nbttagcompound);
        return new SPacketUpdateTileEntity(pos, 3, nbttagcompound);
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
        if (getWorld().isRemote && PokecubeCore.proxy.isSoundPlaying(here))
        {
            PokecubeCore.proxy.toggleSound(MUSICLOOP, getPos());
        }
    }

    @Override
    public boolean isItemValidForSlot(int i, ItemStack itemstack)
    {
        return ContainerHealTable.isItemValid(itemstack);
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player)
    {
        return getWorld().getTileEntity(pos) == this && player.getDistanceSq(pos) < 64;
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
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt)
    {
        if (getWorld().isRemote)
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
        if (tagList != null) for (int i = 0; i < tagList.tagCount(); i++)
        {
            NBTTagCompound tag = tagList.getCompoundTagAt(i);
            byte slot = tag.getByte("Slot");
            if (slot >= 0 && slot < inventory.size())
            {
                inventory.set(slot, new ItemStack(tag));
            }
        }
    }

    @Override
    public ItemStack removeStackFromSlot(int slot)
    {
        if (CompatWrapper.isValid(getStackInSlot(slot)))
        {
            ItemStack stack = getStackInSlot(slot);
            setInventorySlotContents(slot, ItemStack.EMPTY);
            return stack;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void setField(int id, int value)
    {
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack)
    {
        if (CompatWrapper.isValid(stack)) inventory.set(index, ItemStack.EMPTY);
        inventory.set(index, stack);
    }

    @Override
    public void update()
    {
        if (!getWorld().isRemote || MUSICLOOP == null) return;
        int power = getWorld().getStrongPower(pos);
        here.set(this);
        if (power == 0)
        {
            if (getWorld().isRemote && PokecubeCore.proxy.isSoundPlaying(here))
                PokecubeCore.proxy.toggleSound(MUSICLOOP, getPos());
            return;
        }
        if (ticks <= 0 && !PokecubeCore.proxy.isSoundPlaying(here))
        {
            PokecubeCore.proxy.toggleSound(MUSICLOOP, getPos());
            ticks = 1;
        }
        ticks--;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound)
    {
        super.writeToNBT(tagCompound);
        NBTTagList itemList = new NBTTagList();
        tagCompound.setInteger("time", ticks);
        for (int i = 0; i < inventory.size(); i++)
        {
            ItemStack stack = inventory.get(i);
            if (stack != ItemStack.EMPTY)
            {
                NBTTagCompound tag = new NBTTagCompound();
                tag.setByte("Slot", (byte) i);
                stack.writeToNBT(tag);
                itemList.appendTag(tag);
            }
        }
        tagCompound.setTag("Inventory", itemList);
        return tagCompound;
    }

    // 1.11
    public boolean isEmpty()
    {
        return true;
    }
}