package pokecube.adventures.blocks.afa;

import java.lang.reflect.Constructor;
import java.util.UUID;
import java.util.logging.Level;

import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.SidedComponent;
import li.cil.oc.api.network.SimpleComponent;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.Optional.Interface;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.blocks.TileEntityOwnable;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.IHasCommands;
import pokecube.core.interfaces.pokemob.IHasCommands.Command;
import pokecube.core.interfaces.pokemob.IHasCommands.IMobCommandHandler;
import thut.api.maths.Vector3;

@Optional.InterfaceList(value = { @Interface(iface = "li.cil.oc.api.network.SidedComponent", modid = "opencomputers"),
        @Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "opencomputers") })
public class TileEntityCommander extends TileEntityOwnable implements ITickable, SimpleComponent, SidedComponent
{
    protected boolean          addedToNetwork = false;
    private UUID               pokeID         = null;
    private Command            command        = null;
    private IMobCommandHandler handler        = null;

    public TileEntityCommander()
    {
        super();
    }

    @Override
    public boolean canConnectNode(EnumFacing side)
    {
        return side == EnumFacing.DOWN;
    }

    @Override
    public String getComponentName()
    {
        return "poke_commander";
    }

    @Override
    public ITextComponent getDisplayName()
    {
        return new TextComponentString("Pokemob Commander");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox()
    {
        AxisAlignedBB bb = INFINITE_EXTENT_AABB;
        return bb;
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

    @Override
    public NBTTagCompound getUpdateTag()
    {
        NBTTagCompound nbt = new NBTTagCompound();
        return writeToNBT(nbt);
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

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);
        if (nbt.hasKey("pokeID")) pokeID = nbt.getUniqueId("pokeID");
    }

    @Override
    public void update()
    {
    }

    @Override
    public void validate()
    {
        super.validate();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);
        if (getPokeID() != null) nbt.setUniqueId("pokeID", getPokeID());
        return nbt;
    }

    public UUID getPokeID()
    {
        return pokeID;
    }

    public void setPokeID(UUID pokeID)
    {
        this.pokeID = pokeID;
    }

    public Command getCommand()
    {
        return command;
    }

    public void setCommand(Command command, Object... args) throws Exception
    {
        this.command = command;
        Class<? extends IMobCommandHandler> clazz = IHasCommands.COMMANDHANDLERS.get(command);
        Class<?>[] argTypes = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++)
        {
            argTypes[i] = args[i].getClass();
        }
        Constructor<? extends IMobCommandHandler> constructor = clazz.getConstructor(argTypes);
        handler = constructor.newInstance(args);
    }

    public void sendCommand() throws Exception
    {
        World w = getWorld();
        if (!(w instanceof WorldServer)) return;
        if (handler == null) throw new NullPointerException("No CommandHandler has been set");
        if (pokeID == null) throw new NullPointerException("No Pokemob Set, please set a UUID first.");
        WorldServer world = (WorldServer) w;
        IPokemob pokemob = CapabilityPokemob.getPokemobFor(world.getEntityFromUuid(pokeID));
        if (pokemob == null) throw new NullPointerException("Pokemob for given ID is not found.");
        try
        {
            handler.handleCommand(pokemob);
        }
        catch (Exception e)
        {
            PokecubeMod.log(Level.SEVERE, "Error executing a command for a pokemob", e);
            throw new Exception("Error handling the command", e);
        }
    }

    @Callback(doc = "function(uuid:string) - Sets the uuid of the pokemob to command.")
    @Optional.Method(modid = "opencomputers")
    public Object[] setPokeID(Context context, Arguments args) throws Exception
    {
        String var = args.checkString(0);
        pokeID = UUID.fromString(var);
        return new Object[] { true, pokeID };
    }

    @Callback(doc = "function(command:string, args...) - Sets the command and the arguments for it to run.")
    @Optional.Method(modid = "opencomputers")
    public Object[] setCommand(Context context, Arguments args) throws Exception
    {
        Command command = Command.valueOf(args.checkString(0));
        Object[] commandArgs = getArgs(command, args);
        setCommand(command, commandArgs);
        return new Object[] { true, command };
    }

    @Callback(doc = "function(command:string, args...) - Sets the command and the arguments for it to run.")
    @Optional.Method(modid = "opencomputers")
    public Object[] executeCommand(Context context, Arguments args) throws Exception
    {
        sendCommand();
        return new Object[] { true, command };
    }

    @Callback(doc = "function() - Gets the moves known by the pokemob.")
    @Optional.Method(modid = "opencomputers")
    public Object[] getMoves(Context context, Arguments args) throws Exception
    {
        if (pokeID == null) throw new NullPointerException("No Pokemob set");
        WorldServer world = (WorldServer) getWorld();
        IPokemob pokemob = CapabilityPokemob.getPokemobFor(world.getEntityFromUuid(pokeID));
        if (pokemob == null) throw new NullPointerException("No Pokemob found for set ID");
        return pokemob.getMoves();
    }

    private Object[] getArgs(Command command, Arguments args) throws Exception
    {
        Class<? extends IMobCommandHandler> clazz = IHasCommands.COMMANDHANDLERS.get(command);
        for (Constructor<?> c : clazz.getConstructors())
        {
            if (c.getParameterCount() != 0) { return getArgs(c, args); }
        }
        return null;
    }

    private Object[] getArgs(Constructor<?> constructor, Arguments args) throws Exception
    {
        Class<?>[] argTypes = constructor.getParameterTypes();
        int index = 1;
        Object[] ret = new Object[argTypes.length];
        for (int i = 0; i < ret.length; i++)
        {
            Class<?> type = argTypes[i];
            if (type == Vector3.class)
            {
                Vector3 arg = Vector3.getNewVector();
                arg.set(args.checkDouble(index), args.checkDouble(index + 1), args.checkDouble(index + 2));
                index += 3;
                ret[i] = arg;
            }
            else if (type == float.class)
            {
                float arg = (float) args.checkDouble(index);
                index += 1;
                ret[i] = arg;
            }
            else if (type == byte.class)
            {
                byte arg = (byte) args.checkInteger(index);
                index += 1;
                ret[i] = arg;
            }
            else if (type == int.class)
            {
                int arg = args.checkInteger(index);
                index += 1;
                ret[i] = arg;
            }
            else if (type == String.class)
            {
                String arg = args.checkString(index);
                index += 1;
                ret[i] = arg;
            }
        }
        return ret;
    }
}
