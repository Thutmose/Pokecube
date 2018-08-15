package pokecube.core.blocks.pc;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;

import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.SimpleComponent;
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
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.network.packets.PacketPC;
import thut.api.network.PacketHandler;

@Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "opencomputers")
public class TileEntityPC extends TileEntityOwnable implements IInventory, SimpleComponent
{
    private boolean     bound     = false;
    private UUID        boundId   = null;
    private String      boundName = "";
    public List<String> visible   = new ArrayList<String>();

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

    @Override
    public String getComponentName()
    {
        return "pokecubepc";
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
    @Optional.Method(modid = "opencomputers")
    public Object[] getItemList(Context context, Arguments args) throws Exception
    {
        if (isBound())
        {
            InventoryPC inv = getPC();
            ArrayList<Object> items = Lists.newArrayList();
            for (int i = 0; i < inv.getSizeInventory(); i++)
            {
                ItemStack stack = inv.getStackInSlot(i);
                if (stack != ItemStack.EMPTY) items.add(stack.getDisplayName());
            }
            return items.toArray();
        }
        throw new Exception("PC not bound");
    }

    @Override
    public String getName()
    {
        if (getPC() != null) { return boundName; }
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

    /** Overriden in a sign to provide the text. */
    @Override
    public SPacketUpdateTileEntity getUpdatePacket()
    {
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        this.writeToNBT(nbttagcompound);
        return new SPacketUpdateTileEntity(this.getPos(), 3, nbttagcompound);
    }

    @Override
    public NBTTagCompound getUpdateTag()
    {
        NBTTagCompound nbt = new NBTTagCompound();
        return writeToNBT(nbt);
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
        if (getPC() != null) { return getPC().isItemValidForSlot(i, itemstack); }
        return false;
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer entityplayer)
    {
        if (getPC() != null) return getPC().isUsableByPlayer(entityplayer);
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
        try
        {
            if (par1NBTTagCompound.hasKey("boundID"))
                boundId = UUID.fromString(par1NBTTagCompound.getString("boundID"));
        }
        catch (Exception e)
        {
        }
        boundName = par1NBTTagCompound.getString("boundName");
        if (boundId == null)
        {
            boundId = InventoryPC.defaultId;
            boundName = "Public Box";
        }
    }

    @Override
    public ItemStack removeStackFromSlot(int i)
    {
        if (getPC() != null) { return getPC().removeStackFromSlot(i); }
        return null;
    }

    public void setBoundOwner(EntityPlayer player)
    {
        if (!canEdit(player)) return;
        if (this.world.isRemote)
        {
            PacketPC packet = new PacketPC(PacketPC.BIND);
            packet.data.setBoolean("O", false);
            PokecubeMod.packetPipeline.sendToServer(packet);
            return;
        }
        TileEntity te = world.getTileEntity(getPos().down());
        if (te != null && te instanceof TileEntityPC) ((TileEntityPC) te).setBoundOwner(player);
        boundId = player.getUniqueID();
        boundName = player.getDisplayNameString();
        if (!world.isRemote)
        {
            PacketHandler.sendTileUpdate(this);
        }
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
        if (this.world.isRemote)
        {
            PacketPC packet = new PacketPC(PacketPC.BIND);
            packet.data.setBoolean("O", true);
            PokecubeMod.packetPipeline.sendToServer(packet);
            return;
        }

        TileEntity te = world.getTileEntity(getPos().down());
        this.bound = !this.bound;

        if (te != null && te instanceof TileEntityPC) ((TileEntityPC) te).toggleBound();

        if (bound)
        {
            boundId = InventoryPC.defaultId;
            boundName = "Public";
        }
        else
        {
            boundId = null;
            boundName = "";
        }
        if (!world.isRemote)
        {
            PacketHandler.sendTileUpdate(this);
        }
    }

    /** Writes a tile entity to NBT.
     * 
     * @return */
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound par1NBTTagCompound)
    {
        super.writeToNBT(par1NBTTagCompound);
        par1NBTTagCompound.setBoolean("bound", bound);
        if (boundId != null) par1NBTTagCompound.setString("boundID", boundId.toString());
        par1NBTTagCompound.setString("boundName", boundName);
        return par1NBTTagCompound;
    }

    @Override
    public boolean isEmpty()
    {
        return true;
    }
}
