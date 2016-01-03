package pokecube.core.blocks.pc;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;

import li.cil.oc.api.Network;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.Environment;
import li.cil.oc.api.network.Message;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.network.Visibility;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import pokecube.core.blocks.TileEntityOwnable;

public class TileEntityPC extends TileEntityOwnable implements IInventory, Environment
{
    private boolean     bound   = false;
    private String      boundId = "";
    public List<String> visible = new ArrayList<String>();

    public TileEntityPC()
    {
        super();
        try
        {
            node = Network.newNode(this, Visibility.Network).withConnector()
                    .withComponent("pokecubepc", Visibility.Network).create();
        }
        catch (Exception e)
        {
            // e.printStackTrace();
        }
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
        if (node != null && node.host() == this)
        {
            // This restores the node's address, which is required for networks
            // to continue working without interruption across loads. If the
            // node is a power connector this is also required to restore the
            // internal energy buffer of the node.
            node.load(par1NBTTagCompound.getCompoundTag("oc:node"));
        }
    }

    /** Writes a tile entity to NBT. */
    @Override
    public void writeToNBT(NBTTagCompound par1NBTTagCompound)
    {
        super.writeToNBT(par1NBTTagCompound);
        par1NBTTagCompound.setBoolean("bound", bound);
        par1NBTTagCompound.setString("boundID", boundId);
        if (node != null && node.host() == this)
        {
            final NBTTagCompound nodeNbt = new NBTTagCompound();
            node.save(nodeNbt);
            par1NBTTagCompound.setTag("oc:node", nodeNbt);
        }
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
    public ItemStack removeStackFromSlot(int i)
    {
        if (getPC() != null) { return getPC().removeStackFromSlot(i); }
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
    public String getName()
    {
        if (getPC() != null) return getPC().getName();
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

    protected Node node;

    // See updateEntity().
    protected boolean addedToNetwork = false;

    // -----------------------------------------------------------------------
    // //

    @Override
    public Node node()
    {
        return node;
    }

    @Override
    public void onConnect(final Node node)
    {
        // This is called when the call to Network.joinOrCreateNetwork(this) in
        // updateEntity was successful, in which case `node == this`.
        // This is also called for any other node that gets connected to the
        // network our node is in, in which case `node` is the added node.
        // If our node is added to an existing network, this is called for each
        // node already in said network.
    }

    @Override
    public void onDisconnect(final Node node)
    {
        // This is called when this node is removed from its network when the
        // tile entity is removed from the world (see onChunkUnload() and
        // invalidate()), in which case `node == this`.
        // This is also called for each other node that gets removed from the
        // network our node is in, in which case `node` is the removed node.
        // If a net-split occurs this is called for each node that is no longer
        // connected to our node.
    }

    @Override
    public void onMessage(final Message message)
    {
        // This is used to deliver messages sent via node.sendToXYZ. Handle
        // messages at your own discretion. If you do not wish to handle a
        // message you should *not* throw an exception, though.
    }

    @Override
    public void onChunkUnload()
    {
        super.onChunkUnload();
        // Make sure to remove the node from its network when its environment,
        // meaning this tile entity, gets unloaded.
        if (node != null) node.remove();
    }

    @Override
    public void invalidate()
    {
        super.invalidate();
        // Make sure to remove the node from its network when its environment,
        // meaning this tile entity, gets unloaded.
        if (node != null) node.remove();
    }

    @Override
    public void onLoad()
    {
        new Init(this);
    }

    @Callback
    public Object[] getItemList(Context context, Arguments args)
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
        else
        {
            return new Object[0];
        }
    }

    public static class Init
    {
        final TileEntityPC pc;

        public Init(TileEntityPC tile)
        {
            pc = tile;
            MinecraftForge.EVENT_BUS.register(this);
        }

        @SubscribeEvent
        public void convert(WorldTickEvent event)
        {
            if (event.world.isRemote)
            {
                MinecraftForge.EVENT_BUS.unregister(this);
                return;
            }

            if (event.phase == Phase.END)
            {
                MinecraftForge.EVENT_BUS.unregister(this);
                Network.joinOrCreateNetwork(pc);
            }
        }
    }
}
