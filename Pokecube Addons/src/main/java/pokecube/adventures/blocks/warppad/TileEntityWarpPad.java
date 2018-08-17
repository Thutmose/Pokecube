package pokecube.adventures.blocks.warppad;

import org.nfunk.jep.JEP;

import io.netty.buffer.Unpooled;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.SimpleComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.Optional.Interface;
import net.minecraftforge.fml.common.Optional.InterfaceList;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.commands.Config;
import pokecube.adventures.network.PacketPokeAdv.MessageClient;
import pokecube.core.blocks.TileEntityOwnable;
import pokecube.core.interfaces.pokemob.commandhandlers.TeleportHandler;
import pokecube.core.network.PokecubePacketHandler;
import pokecube.core.utils.PokecubeSerializer.TeleDest;
import thut.api.entity.Transporter;
import thut.api.maths.Vector3;
import thut.api.maths.Vector4;

@InterfaceList({ @Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "opencomputers") })
public class TileEntityWarpPad extends TileEntityOwnable implements SimpleComponent
{
    public static JEP parser;

    public static void initParser(String function)
    {
        parser = new JEP();
        parser.initFunTab(); // clear the contents of the function table
        parser.addStandardFunctions();
        parser.initSymTab(); // clear the contents of the symbol table
        parser.addStandardConstants();
        parser.addComplex(); // among other things adds i to the symbol
                             // table
        parser.addVariable("dw", 0);
        parser.addVariable("dx", 0);
        parser.addVariable("dy", 0);
        parser.addVariable("dz", 0);
        parser.parseExpression(function);
    }

    public static double MAXRANGE = 64;
    public static int    COOLDOWN = 20;
    public Vector4       link;
    private Vector3      linkPos;
    public Vector3       here;
    boolean              admin    = false;
    boolean              noEnergy = false;
    public int           energy   = 0;

    public TileEntityWarpPad()
    {
    }

    @Override
    public String getComponentName()
    {
        return "warppad";
    }

    @Callback(doc = "Returns the current 4-vector destination")
    @Optional.Method(modid = "opencomputers")
    public Object[] getDestination(Context context, Arguments args) throws Exception
    {
        if (link != null) { return new Object[] { link.x, link.y, link.z, link.w }; }
        throw new Exception("no link");
    }

    /** Overriden in a sign to provide the text. */
    @Override
    public SPacketUpdateTileEntity getUpdatePacket()
    {
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        if (world.isRemote) return new SPacketUpdateTileEntity(this.getPos(), 3, nbttagcompound);
        this.writeToNBT(nbttagcompound);
        return new SPacketUpdateTileEntity(this.getPos(), 3, nbttagcompound);
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
        if (world.isRemote)
        {
            NBTTagCompound nbt = pkt.getNbtCompound();
            readFromNBT(nbt);
        }
    }

    public void onStepped(Entity stepper)
    {
        if (world.isRemote || link == null) return;
        if (here == null) here = Vector3.getNewVector().set(this);
        if (linkPos == null)
        {
            linkPos = Vector3.getNewVector().set(link.x, link.y, link.z);
        }

        double distSq = 0;
        long time = world.getTotalWorldTime();
        long lastStepped = stepper.getEntityData().getLong("lastWarpPadUse");
        boolean tele = link != null && !link.isEmpty() && lastStepped + COOLDOWN <= time
                && (MAXRANGE < 0 || (distSq = here.distToSq(linkPos)) < MAXRANGE * MAXRANGE);
        if (tele)
        {
            Integer dimHere = stepper.dimension;
            Integer dimThere = (int) link.w;
            if (TeleportHandler.invalidDests.contains(dimHere) || TeleportHandler.invalidDests.contains(dimThere))
            {
                tele = false;
            }
        }

        if (tele && Config.instance.warpPadEnergy && !noEnergy)
        {
            parser.setVarValue("dx", (link.x - here.x));
            parser.setVarValue("dy", (link.y - here.y));
            parser.setVarValue("dz", (link.z - here.z));
            parser.setVarValue("dw", (link.w - getWorld().provider.getDimension()));
            distSq = parser.getValue();
            tele = energy > distSq;
            if (!tele)
            {
                energy = 0;
                stepper.playSound(SoundEvents.BLOCK_NOTE_BASEDRUM, 1.0F, 1.0F);
            }
            else
            {
                energy -= distSq;
            }
            stepper.getEntityData().setLong("lastWarpPadUse", time);
        }
        if (tele)
        {
            stepper.playSound(SoundEvents.ENTITY_ENDERMEN_TELEPORT, 1.0F, 1.0F);
            PacketBuffer buff = new PacketBuffer(Unpooled.buffer());
            buff.writeByte(MessageClient.TELEPORTEFFECTS);
            here.writeToBuff(buff);
            MessageClient packet = new MessageClient(buff);
            PokecubePacketHandler.sendToAllNear(packet, here, stepper.dimension, 20);
            TeleDest d = new TeleDest(link);
            Vector3 loc = d.getLoc();
            int dim = d.getDim();
            if (stepper instanceof EntityPlayer)
            {
                stepper = Transporter.teleportEntity(stepper, loc, dim, false);
            }
            else if (dim == d.getDim())
            {
                stepper.setPositionAndUpdate(loc.x, loc.y, loc.z);
            }
            else
            {
                return;
            }
            if (stepper != null) stepper.playSound(SoundEvents.ENTITY_ENDERMEN_TELEPORT, 1.0F, 1.0F);
            buff = new PacketBuffer(Unpooled.buffer());
            buff.writeByte(MessageClient.TELEPORTEFFECTS);
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
        admin = tagCompound.getBoolean("admin");
        energy = tagCompound.getInteger("energy");
    }

    public int receiveEnergy(EnumFacing facing, int maxReceive, boolean simulate)
    {
        int receive = Math.min(maxReceive, PokecubeAdv.conf.warpPadMaxEnergy - energy);
        if (!simulate && receive > 0)
        {
            energy += receive;
        }
        return receive;
    }

    @Callback(doc = "function(x:number, y:number, z:number, w:number) - Sets the 4-vector destination, w is the dimension")
    @Optional.Method(modid = "opencomputers")
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
                link = new Vector4(x, y, z, w);
            }
            else
            {
                link.set(x, y, z, w);
            }
            return new Object[] { link.x, link.y, link.z, link.w };
        }
        throw new Exception("invalid arguments, expected number,number,number,number");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound)
    {
        super.writeToNBT(tagCompound);
        if (link != null)
        {
            NBTTagCompound linkTag = new NBTTagCompound();
            link.writeToNBT(linkTag);
            tagCompound.setTag("link", linkTag);
        }
        tagCompound.setBoolean("noEnergy", noEnergy);
        tagCompound.setBoolean("admin", admin);
        tagCompound.setInteger("energy", energy);
        return tagCompound;
    }
}
