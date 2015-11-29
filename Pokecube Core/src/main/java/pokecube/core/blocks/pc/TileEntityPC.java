package pokecube.core.blocks.pc;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IChatComponent;
import pokecube.core.blocks.TileEntityOwnable;

public class TileEntityPC extends TileEntityOwnable implements IInventory
{
    private boolean     bound   = false;
    private String      boundId = "";
    public List<String> visible = new ArrayList<String>();

    public TileEntityPC()
    {

    }

    /** Reads a tile entity from NBT. */
    @Override
    public void readFromNBT(NBTTagCompound par1NBTTagCompound)
    {
        super.readFromNBT(par1NBTTagCompound);
        this.bound = par1NBTTagCompound.getBoolean("bound");
        boundId = par1NBTTagCompound.getString("boundID");
        if (boundId == null || boundId.isEmpty())
        {
            boundId = new UUID(1234, 4321).toString();
        }
    }

    /** Writes a tile entity to NBT. */
    @Override
    public void writeToNBT(NBTTagCompound par1NBTTagCompound)
    {
        super.writeToNBT(par1NBTTagCompound);
        par1NBTTagCompound.setBoolean("bound", bound);
        par1NBTTagCompound.setString("boundID", boundId);
    }

    /** Overriden in a sign to provide the text. */
    @SuppressWarnings("rawtypes")
    @Override
    public Packet getDescriptionPacket()
    {

        NBTTagCompound nbttagcompound = new NBTTagCompound();

        this.writeToNBT(nbttagcompound);
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
        NBTTagCompound nbt = pkt.getNbtCompound();
        this.readFromNBT(nbt);
    }

    @Override
    public int getSizeInventory()
    {
        if (getPC() != null) return getPC().getSizeInventory();
        return 0;
    }

    @Override
    public ItemStack getStackInSlot(int i)
    {
        if (getPC() != null) { return getPC().getStackInSlot(i); }
        return null;
    }

    @Override
    public ItemStack decrStackSize(int i, int j)
    {
        if (getPC() != null) { return getPC().decrStackSize(i, j); }
        return null;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int i)
    {
        if (getPC() != null) { return getPC().getStackInSlotOnClosing(i); }
        return null;
    }

    @Override
    public void setInventorySlotContents(int i, ItemStack itemstack)
    {
        if (getPC() != null)
        {
            getPC().setInventorySlotContents(i, itemstack);
        }
    }

    @Override
    public int getInventoryStackLimit()
    {
        if (getPC() != null) return getPC().getInventoryStackLimit();
        return 0;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer entityplayer)
    {
        if (getPC() != null) return getPC().isUseableByPlayer(entityplayer);
        return false;
    }

    @Override
    public boolean isItemValidForSlot(int i, ItemStack itemstack)
    {
        if (getPC() != null)
        {
            // if(pc.getPage()!=box) return false;
            return getPC().isItemValidForSlot(i, itemstack);
        }
        return false;
    }

    public void toggleBound()
    {
        TileEntity te = worldObj.getTileEntity(getPos().down());
        this.bound = !this.bound;

        if (te != null && te instanceof TileEntityPC) ((TileEntityPC) te).toggleBound();

        if (bound)
        {
            UUID id = InventoryPC.defaultId;
            boundId = id.toString();
        }
        else
        {
            boundId = "";
        }
        worldObj.markBlockForUpdate(getPos());
    }

    public void setBoundOwner(String uuid)
    {
        UUID id = InventoryPC.defaultId;

        TileEntity te = worldObj.getTileEntity(getPos().down());
        if (te != null && te instanceof TileEntityPC) ((TileEntityPC) te).setBoundOwner(uuid);

        boolean canEdit = boundId.isEmpty() || boundId.equals(id.toString());
        if (!canEdit)
        {
            if (uuid.equals(boundId))
            {
                toggleBound();
            }
        }
        else
        {
            boundId = uuid;
        }

        worldObj.markBlockForUpdate(getPos());

    }

    public boolean isBound()
    {
        return bound;
    }

    public InventoryPC getPC()
    {
        if (bound) { return InventoryPC.getPC(boundId); }
        return null;
    }

    @Override
    public String getCommandSenderName()
    {
        if (getPC() != null) return getPC().getCommandSenderName();
        return null;
    }

    @Override
    public boolean hasCustomName()
    {
        if (getPC() != null) return getPC().hasCustomName();
        return false;
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
    public void setField(int id, int value)
    {
    }

    @Override
    public int getFieldCount()
    {
        return 0;
    }

    @Override
    public void clear()
    {
    }
}
