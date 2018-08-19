package pokecube.adventures.blocks.afa;

import java.lang.reflect.Constructor;
import java.util.UUID;
import java.util.logging.Level;

import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.SimpleComponent;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.blocks.TileEntityOwnable;
import pokecube.core.interfaces.IMoveConstants.AIRoutine;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.IHasCommands;
import pokecube.core.interfaces.pokemob.IHasCommands.Command;
import pokecube.core.interfaces.pokemob.IHasCommands.IMobCommandHandler;
import thut.api.maths.Vector3;

@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "opencomputers")
public class TileEntityCommander extends TileEntityOwnable implements SimpleComponent
{
    protected boolean          addedToNetwork = false;
    private UUID               pokeID         = null;
    private Command            command        = null;
    private IMobCommandHandler handler        = null;
    public String              args           = "";
    protected int              power          = 0;

    public TileEntityCommander()
    {
        super();
    }

    public void setCommand(Command command, String args) throws Exception
    {
        this.command = command;
        this.args = args;
        if (command != null) initCommand();
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
        if (getWorld().isRemote) return new SPacketUpdateTileEntity(this.getPos(), 3, nbttagcompound);
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
        if (getWorld().isRemote)
        {
            NBTTagCompound nbt = pkt.getNbtCompound();
            readFromNBT(nbt);
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);
        if (nbt.hasKey("pokeIDMost")) pokeID = nbt.getUniqueId("pokeID");
        if (nbt.hasKey("cmd")) this.command = Command.valueOf(nbt.getString("cmd"));
        this.args = nbt.getString("args");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);
        if (getPokeID() != null) nbt.setUniqueId("pokeID", getPokeID());
        nbt.setString("args", args);
        if (this.command != null) nbt.setString("cmd", this.command.name());
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

    protected void initCommand() throws Exception
    {
        setCommand(command, getArgs());
    }

    private Object[] getArgs() throws Exception
    {
        Class<? extends IMobCommandHandler> clazz = IHasCommands.COMMANDHANDLERS.get(command);
        for (Constructor<?> c : clazz.getConstructors())
        {
            if (c.getParameterCount() != 0) { return getArgs(c); }
        }
        return null;
    }

    private Object[] getArgs(Constructor<?> constructor)
    {
        String[] args = this.args.split(",");
        Class<?>[] argTypes = constructor.getParameterTypes();
        int index = 0;
        Object[] ret = new Object[argTypes.length];
        for (int i = 0; i < ret.length; i++)
        {
            Class<?> type = argTypes[i];
            if (type == Vector3.class)
            {
                Vector3 arg = Vector3.getNewVector();
                arg.set(Double.parseDouble(args[index]) + getPos().getX(),
                        Double.parseDouble(args[index + 1]) + getPos().getY(),
                        Double.parseDouble(args[index + 2]) + getPos().getZ());
                index += 3;
                ret[i] = arg;
            }
            else if (type == float.class || type == Float.class)
            {
                float arg = (float) Double.parseDouble(args[index]);
                index += 1;
                ret[i] = arg;
            }
            else if (type == byte.class || type == Byte.class)
            {
                byte arg = (byte) Integer.parseInt(args[index]);
                index += 1;
                ret[i] = arg;
            }
            else if (type == int.class || type == Integer.class)
            {
                int arg = Integer.parseInt(args[index]);
                index += 1;
                ret[i] = arg;
            }
            else if (type == boolean.class || type == Boolean.class)
            {
                boolean arg = Boolean.parseBoolean(args[index]);
                index += 1;
                ret[i] = arg;
            }
            else if (type == String.class)
            {
                String arg = args[index];
                index += 1;
                ret[i] = arg;
            }
        }
        return ret;
    }

    public void setCommand(Command command, Object... args) throws Exception
    {
        this.command = command;
        Class<? extends IMobCommandHandler> clazz = IHasCommands.COMMANDHANDLERS.get(command);
        if (args == null)
        {
            handler = clazz.newInstance();
            return;
        }
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
        if (this.command != null && handler == null) initCommand();
        if (handler == null) throw new Exception("No CommandHandler has been set");
        if (pokeID == null) throw new Exception("No Pokemob Set, please set a UUID first.");
        WorldServer world = (WorldServer) w;
        IPokemob pokemob = CapabilityPokemob.getPokemobFor(world.getEntityFromUuid(pokeID));
        if (pokemob == null) throw new Exception("Pokemob for given ID is not found.");
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

    ////////////////////////// Open computers stuff below

    @Override
    @Optional.Method(modid = "opencomputers")
    public String getComponentName()
    {
        return "poke_commander";
    }

    @Callback(doc = "function(uuid:string) - gets the uuid of the pokemob to command.")
    @Optional.Method(modid = "opencomputers")
    public Object[] getPokeID(Context context, Arguments args) throws Exception
    {
        return new Object[] { pokeID };
    }

    @Callback(doc = "function(uuid:string) - Sets the uuid of the pokemob to command.")
    @Optional.Method(modid = "opencomputers")
    public Object[] setPokeID(Context context, Arguments args) throws Exception
    {
        String var = args.checkString(0);
        pokeID = UUID.fromString(var);
        return new Object[] { true, pokeID };
    }

    @Callback(doc = "function(command:string, args...) - Sets the command and the arguments for it to run, positions are relative to the controller")
    @Optional.Method(modid = "opencomputers")
    public Object[] setCommand(Context context, Arguments args) throws Exception
    {
        Command command = Command.valueOf(args.checkString(0));
        Object[] commandArgs = getArgs(command, args);
        setCommand(command, commandArgs);
        return new Object[] { true, command };
    }

    @Callback(doc = "function() - Executes the set command, setCommand must be called beforehand.")
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
        if (pokeID == null) throw new Exception("No Pokemob set");
        WorldServer world = (WorldServer) getWorld();
        IPokemob pokemob = CapabilityPokemob.getPokemobFor(world.getEntityFromUuid(pokeID));
        if (pokemob == null) throw new Exception("No Pokemob found for set ID");
        return pokemob.getMoves();
    }

    @Callback(doc = "function() - Gets the current move index for the pokemob.")
    @Optional.Method(modid = "opencomputers")
    public Object[] getMoveIndex(Context context, Arguments args) throws Exception
    {
        if (pokeID == null) throw new Exception("No Pokemob set");
        WorldServer world = (WorldServer) getWorld();
        IPokemob pokemob = CapabilityPokemob.getPokemobFor(world.getEntityFromUuid(pokeID));
        if (pokemob == null) throw new Exception("No Pokemob found for set ID");
        return new Object[] { pokemob.getMoveIndex() };
    }

    @Callback(doc = "function(index:number) - Sets the current move index for the pokemob.")
    @Optional.Method(modid = "opencomputers")
    public Object[] setMoveIndex(Context context, Arguments args) throws Exception
    {
        if (pokeID == null) throw new Exception("No Pokemob set");
        WorldServer world = (WorldServer) getWorld();
        IPokemob pokemob = CapabilityPokemob.getPokemobFor(world.getEntityFromUuid(pokeID));
        if (pokemob == null) throw new Exception("No Pokemob found for set ID");
        pokemob.setMoveIndex(args.checkInteger(0));
        return new Object[] { pokemob.getMoveIndex() };
    }

    @Callback(doc = "function(routine:string) - Gets the state of the given routine.")
    @Optional.Method(modid = "opencomputers")
    public Object[] getRoutineState(Context context, Arguments args) throws Exception
    {
        if (pokeID == null) throw new Exception("No Pokemob set");
        WorldServer world = (WorldServer) getWorld();
        IPokemob pokemob = CapabilityPokemob.getPokemobFor(world.getEntityFromUuid(pokeID));
        if (pokemob == null) throw new Exception("No Pokemob found for set ID");
        AIRoutine routine = AIRoutine.valueOf(args.checkString(0));
        return new Object[] { pokemob.isRoutineEnabled(routine) };
    }

    @Callback(doc = "function(routine:string, state:boolean) - Sets the state of the given routine.")
    @Optional.Method(modid = "opencomputers")
    public Object[] setRoutineState(Context context, Arguments args) throws Exception
    {
        if (pokeID == null) throw new Exception("No Pokemob set");
        WorldServer world = (WorldServer) getWorld();
        IPokemob pokemob = CapabilityPokemob.getPokemobFor(world.getEntityFromUuid(pokeID));
        if (pokemob == null) throw new Exception("No Pokemob found for set ID");
        AIRoutine routine = AIRoutine.valueOf(args.checkString(0));
        pokemob.setRoutineState(routine, args.checkBoolean(1));
        return new Object[] { true, pokemob.isRoutineEnabled(routine) };
    }

    @Callback(doc = "function() - Gets the home location for the pokemob.")
    @Optional.Method(modid = "opencomputers")
    public Object[] getHome(Context context, Arguments args) throws Exception
    {
        if (pokeID == null) throw new Exception("No Pokemob set");
        WorldServer world = (WorldServer) getWorld();
        IPokemob pokemob = CapabilityPokemob.getPokemobFor(world.getEntityFromUuid(pokeID));
        if (pokemob == null) throw new Exception("No Pokemob found for set ID");
        return new Object[] { pokemob.getHome() };
    }

    @Callback(doc = "function(x:number, y:number, z:number, d:homeDistance) - Sets home location, relative to the controller.")
    @Optional.Method(modid = "opencomputers")
    public Object[] setHome(Context context, Arguments args) throws Exception
    {
        if (pokeID == null) throw new Exception("No Pokemob set");
        WorldServer world = (WorldServer) getWorld();
        IPokemob pokemob = CapabilityPokemob.getPokemobFor(world.getEntityFromUuid(pokeID));
        if (pokemob == null) throw new Exception("No Pokemob found for set ID");
        pokemob.setHome(args.checkInteger(0) + getPos().getX(), args.checkInteger(1) + getPos().getY(),
                args.checkInteger(2) + getPos().getZ(), 16);
        return new Object[] { true };
    }

    @Optional.Method(modid = "opencomputers")
    private Object[] getArgs(Command command, Arguments args) throws Exception
    {
        Class<? extends IMobCommandHandler> clazz = IHasCommands.COMMANDHANDLERS.get(command);
        for (Constructor<?> c : clazz.getConstructors())
        {
            if (c.getParameterCount() != 0) { return getArgs(c, args); }
        }
        return null;
    }

    @Optional.Method(modid = "opencomputers")
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
                arg.set(args.checkDouble(index) + getPos().getX(), args.checkDouble(index + 1) + getPos().getY(),
                        args.checkDouble(index + 2) + getPos().getZ());
                index += 3;
                ret[i] = arg;
            }
            else if (type == float.class || type == Float.class)
            {
                float arg = (float) args.checkDouble(index);
                index += 1;
                ret[i] = arg;
            }
            else if (type == byte.class || type == Byte.class)
            {
                byte arg = (byte) args.checkInteger(index);
                index += 1;
                ret[i] = arg;
            }
            else if (type == int.class || type == Integer.class)
            {
                int arg = args.checkInteger(index);
                index += 1;
                ret[i] = arg;
            }
            else if (type == boolean.class || type == Boolean.class)
            {
                boolean arg = args.checkBoolean(index);
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
