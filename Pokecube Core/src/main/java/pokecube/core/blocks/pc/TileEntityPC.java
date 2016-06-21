package pokecube.core.blocks.pc;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;

import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.Optional.Interface;
import pokecube.core.blocks.TileEntityOwnable;

@Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "OpenComputers")
public class TileEntityPC extends TileEntityOwnable implements IInventory//, SimpleComponent
{
    private boolean     bound   = false;
    private String      boundId = "";
    public List<String> visible = new ArrayList<String>();

    public TileEntityPC()
    {
        super();
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
    public ItemStack decrStackSize(int i, int j)
    {
        if (getPC() != null) { return getPC().decrStackSize(i, j); }
        return null;
    }

//  @Override //TODO re-add SimpleComponent when it is fixed.
    public String getComponentName()
    {
        return "pokecubepc";
    }

    /** Overriden in a sign to provide the text. */
    @Override
    public SPacketUpdateTileEntity getUpdatePacket()
    {
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        this.writeToNBT(nbttagcompound);
        return new SPacketUpdateTileEntity(this.getPos(), 3, nbttagcompound);
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
        if (getPC() != null) return getPC().getInventoryStackLimit();
        return 0;
    }

    @Callback(doc = "Returns the items in the PC")
    @Optional.Method(modid = "OpenComputers")
    public Object[] getItemList(Context context, Arguments args) throws Exception
    {
        if (isBound())
        {
            InventoryPC inv = getPC();
            ArrayList<Object> items = Lists.newArrayList();
            for (int i = 0; i < inv.getSizeInventory(); i++)
            {
                ItemStack stack = inv.getStackInSlot(i);
                if (stack != null) items.add(stack.getDisplayName());
            }
            return items.toArray();
        }
        throw new Exception("PC not bound");
    }

    @Override
    public String getName()
    {
        if (getPC() != null) return getPC().getName();
        return null;
    }

    public InventoryPC getPC()
    {
        if (bound) { return InventoryPC.getPC(boundId); }
        return null;
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
    public boolean hasCustomName()
    {
        if (getPC() != null) return getPC().hasCustomName();
        return false;
    }

    public boolean isBound()
    {
        return bound;
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

    @Override
    public boolean isUseableByPlayer(EntityPlayer entityplayer)
    {
        if (getPC() != null) return getPC().isUseableByPlayer(entityplayer);
        return false;
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
        NBTTagCompound nbt = pkt.getNbtCompound();
        this.readFromNBT(nbt);
    }

    @Override
    public void openInventory(EntityPlayer player)
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

    @Override
    public ItemStack removeStackFromSlot(int i)
    {
        if (getPC() != null) { return getPC().removeStackFromSlot(i); }
        return null;
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

        // worldObj.markBlockForUpdate(getPos());

    }

    @Override
    public void setField(int id, int value)
    {
    }

    @Override
    public void setInventorySlotContents(int i, ItemStack itemstack)
    {
        if (getPC() != null)
        {
            getPC().setInventorySlotContents(i, itemstack);
        }
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
        // worldObj.upda
        // worldObj.markBlockForUpdate(getPos());
    }

    /** Writes a tile entity to NBT.
     * 
     * @return */
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound par1NBTTagCompound)
    {
        super.writeToNBT(par1NBTTagCompound);
        par1NBTTagCompound.setBoolean("bound", bound);
        par1NBTTagCompound.setString("boundID", boundId);
        return par1NBTTagCompound;
    }
}
