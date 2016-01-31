package pokecube.adventures.blocks.warppad;

import cofh.api.energy.EnergyStorage;
import cofh.api.energy.IEnergyReceiver;
import io.netty.buffer.Unpooled;
import li.cil.oc.api.Network;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.Environment;
import li.cil.oc.api.network.Message;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.network.Visibility;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import pokecube.adventures.handlers.ConfigHandler;
import pokecube.adventures.network.PacketPokeAdv.MessageClient;
import pokecube.core.blocks.TileEntityOwnable;
import pokecube.core.network.PokecubePacketHandler;
import pokecube.core.utils.PokecubeSerializer.TeleDest;
import pokecube.core.utils.Vector4;
import thut.api.entity.Transporter;
import thut.api.maths.Vector3;

public class TileEntityWarpPad extends TileEntityOwnable implements Environment, ITickable, IEnergyReceiver
{
    public Vector4       link;
    private Vector3      linkPos;
    public Vector3       here;
    protected long       lastStepped = Long.MIN_VALUE;
    boolean              noEnergy    = false;
    public static double MAXRANGE    = 64;
    public static int    COOLDOWN    = 1000;

    protected EnergyStorage storage = new EnergyStorage(32000);

    public TileEntityWarpPad()
    {
        // The 'node' of a tile entity is used to connect it to other components
        // including computers. They are connected to nodes of neighboring
        // blocks, forming a network that way. That network is also used for
        // distributing energy among components for the mod.
        try
        {
            node = Network.newNode(this, Visibility.Network).withConnector()
                    .withComponent("warppad", Visibility.Network).create();
        }
        catch (Exception e)
        {
            // e.printStackTrace();
        }
    }

    public void onStepped(Entity stepper)
    {
        if (worldObj.isRemote || link == null) return;

        if (here == null) here = Vector3.getNewVectorFromPool().set(this);
        if (linkPos == null)
        {
            linkPos = Vector3.getNewVectorFromPool().set(link.x, link.y, link.z);
        }

        double distSq = 0;
        long time = System.currentTimeMillis();
        boolean tele = link != null && !link.isEmpty() && lastStepped + COOLDOWN <= time
                && (MAXRANGE < 0 || (distSq = here.distToSq(linkPos)) < MAXRANGE * MAXRANGE);

        if (tele && ConfigHandler.ENERGY && !noEnergy)
        {
            int energy = (int) (distSq);
            tele = storage.extractEnergy(energy, false) == energy;

            if (!tele)
            {
                worldObj.playSoundEffect(getPos().getX(), getPos().getY(), getPos().getZ(), "note.bd", 1.0F, 1.0F);
                lastStepped = time;
            }
        }

        if (tele)
        {
            TileEntity te = linkPos.getTileEntity(getWorld(), EnumFacing.DOWN);

            worldObj.playSoundEffect(getPos().getX(), getPos().getY(), getPos().getZ(), "mob.endermen.portal", 1.0F,
                    1.0F);

            PacketBuffer buff = new PacketBuffer(Unpooled.buffer());
            buff.writeByte(9);
            here.writeToBuff(buff);
            MessageClient packet = new MessageClient(buff);
            PokecubePacketHandler.sendToAllNear(packet, here, stepper.dimension, 20);

            if (te != null && te instanceof TileEntityWarpPad)
            {
                TileEntityWarpPad pad = (TileEntityWarpPad) te;
                pad.lastStepped = time;
            }

            TeleDest d = new TeleDest(link);

            Vector3 loc = d.getLoc();
            int dim = d.getDim();

            Transporter.teleportEntity(stepper, loc, dim, false);

            worldObj.playSoundEffect(loc.x, loc.y, loc.z, "mob.endermen.portal", 1.0F, 1.0F);
            buff = new PacketBuffer(Unpooled.buffer());
            buff.writeByte(9);
            linkPos.writeToBuff(buff);
            packet = new MessageClient(buff);
            PokecubePacketHandler.sendToAllNear(packet, linkPos, stepper.dimension, 20);

        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound)
    {
        super.readFromNBT(tagCompound);
        link = new Vector4(tagCompound.getCompoundTag("link"));
        // The host check may be superfluous for you. It's just there to allow
        // some special cases, where getNode() returns some node managed by
        // some other instance (for example when you have multiple internal
        // nodes in this tile entity).
        if (node != null && node.host() == this)
        {
            // This restores the node's address, which is required for networks
            // to continue working without interruption across loads. If the
            // node is a power connector this is also required to restore the
            // internal energy buffer of the node.
            node.load(tagCompound.getCompoundTag("oc:node"));
        }
        noEnergy = tagCompound.getBoolean("noEnergy");
        storage.readFromNBT(tagCompound);
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound)
    {
        super.writeToNBT(tagCompound);
        if (link != null)
        {
            NBTTagCompound linkTag = new NBTTagCompound();
            link.writeToNBT(linkTag);
            tagCompound.setTag("link", linkTag);
        }
        if (node != null && node.host() == this)
        {
            final NBTTagCompound nodeNbt = new NBTTagCompound();
            node.save(nodeNbt);
            tagCompound.setTag("oc:node", nodeNbt);
        }
        tagCompound.setBoolean("noEnergy", noEnergy);
        storage.writeToNBT(tagCompound);
    }

    /** Overriden in a sign to provide the text. */
    @SuppressWarnings("rawtypes")
    @Override
    public Packet getDescriptionPacket()
    {
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        if (worldObj.isRemote) return new S35PacketUpdateTileEntity(this.getPos(), 3, nbttagcompound);
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
        if (worldObj.isRemote)
        {
            NBTTagCompound nbt = pkt.getNbtCompound();
            readFromNBT(nbt);
        }
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
    public void update()
    {
        // On the first update, try to add our node to nearby networks. We do
        // this in the update logic, not in validate() because we need to access
        // neighboring tile entities, which isn't possible in validate().
        // We could alternatively check node != null && node.network() == null,
        // but this has somewhat better performance, and makes it clearer.
        if (!addedToNetwork)
        {
            addedToNetwork = true;
            Network.joinOrCreateNetwork(this);
        }
    }

    @Callback
    public Object[] setDestination(Context context, Arguments args) throws Exception
    {
        if (args.isDouble(0) && args.isDouble(1) && args.isDouble(2) && args.isDouble(3))
        {
            float x = (float) args.checkDouble(0);
            float y = (float) args.checkDouble(1);
            float z = (float) args.checkDouble(2);
            float w = (float) args.checkDouble(3);
            if (link == null)
            {
                link = new Vector4(x, y, z, (float) w);
            }
            else
            {
                link.set(x, y, z, w);
            }
            return new Object[] {};
        }
        throw new Exception("invalid arguments, expected number,number,number,number");
    }

    @Callback
    public Object[] getDestination(Context context, Arguments args) throws Exception
    {
        if (link != null) { return new Object[] { link.x, link.y, link.z, link.w }; }
        throw new Exception("no link");
    }

    @Override
    public boolean canConnectEnergy(EnumFacing facing)
    {
        return facing == EnumFacing.DOWN;
    }

    @Override
    public int receiveEnergy(EnumFacing facing, int maxReceive, boolean simulate)
    {
        return storage.receiveEnergy(maxReceive, simulate);
    }

    @Override
    public int getEnergyStored(EnumFacing facing)
    {
        return storage.getEnergyStored();
    }

    @Override
    public int getMaxEnergyStored(EnumFacing facing)
    {
        return storage.getMaxEnergyStored();
    }
}
