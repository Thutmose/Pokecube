package pokecube.adventures.blocks.warppad;

import cofh.api.energy.EnergyStorage;
import cofh.api.energy.IEnergyReceiver;
import io.netty.buffer.Unpooled;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.SimpleComponent;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.Optional.Interface;
import pokecube.adventures.comands.Config;
import pokecube.adventures.network.PacketPokeAdv.MessageClient;
import pokecube.core.blocks.TileEntityOwnable;
import pokecube.core.network.PokecubePacketHandler;
import pokecube.core.utils.PokecubeSerializer.TeleDest;
import pokecube.core.utils.Vector4;
import thut.api.entity.Transporter;
import thut.api.maths.Vector3;

@Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "OpenComputers")
public class TileEntityWarpPad extends TileEntityOwnable implements SimpleComponent, IEnergyReceiver
{
    public Vector4          link;
    private Vector3         linkPos;
    public Vector3          here;
    protected long          lastStepped = Long.MIN_VALUE;
    boolean                 noEnergy    = false;
    public static double    MAXRANGE    = 64;
    public static int       COOLDOWN    = 1000;

    protected EnergyStorage storage     = new EnergyStorage(32000);

    public TileEntityWarpPad()
    {
    }

    public void onStepped(Entity stepper)
    {
        if (worldObj.isRemote || link == null) return;

        if (here == null) here = Vector3.getNewVector().set(this);
        if (linkPos == null)
        {
            linkPos = Vector3.getNewVector().set(link.x, link.y, link.z);
        }

        double distSq = 0;
        long time = System.currentTimeMillis();
        boolean tele = link != null && !link.isEmpty() && lastStepped + COOLDOWN <= time
                && (MAXRANGE < 0 || (distSq = here.distToSq(linkPos)) < MAXRANGE * MAXRANGE);

        if (tele && Config.instance.warpPadEnergy && !noEnergy)
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

    @Callback
    @Optional.Method(modid = "OpenComputers")
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
    @Optional.Method(modid = "OpenComputers")
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

    @Override
    public String getComponentName()
    {
        return "warppad";
    }
}
