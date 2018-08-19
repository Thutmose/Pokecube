package pokecube.core.network.pokemobs;

import java.util.logging.Level;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import pokecube.core.PokecubeCore;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.IHasCommands;
import pokecube.core.interfaces.pokemob.IHasCommands.Command;
import pokecube.core.interfaces.pokemob.IHasCommands.IMobCommandHandler;
import pokecube.core.interfaces.pokemob.commandhandlers.AttackEntityHandler;
import pokecube.core.interfaces.pokemob.commandhandlers.AttackLocationHandler;
import pokecube.core.interfaces.pokemob.commandhandlers.AttackNothingHandler;
import pokecube.core.interfaces.pokemob.commandhandlers.ChangeFormHandler;
import pokecube.core.interfaces.pokemob.commandhandlers.MoveIndexHandler;
import pokecube.core.interfaces.pokemob.commandhandlers.MoveToHandler;
import pokecube.core.interfaces.pokemob.commandhandlers.StanceHandler;
import pokecube.core.interfaces.pokemob.commandhandlers.SwapMovesHandler;
import pokecube.core.interfaces.pokemob.commandhandlers.TeleportHandler;

public class PacketCommand implements IMessage, IMessageHandler<PacketCommand, IMessage>
{
    // Register default command handlers
    static
    {
        // Only populate this if someone else hasn't override in.
        if (IHasCommands.COMMANDHANDLERS.isEmpty())
        {
            IHasCommands.COMMANDHANDLERS.put(Command.ATTACKENTITY, AttackEntityHandler.class);
            IHasCommands.COMMANDHANDLERS.put(Command.ATTACKLOCATION, AttackLocationHandler.class);
            IHasCommands.COMMANDHANDLERS.put(Command.ATTACKNOTHING, AttackNothingHandler.class);
            IHasCommands.COMMANDHANDLERS.put(Command.CHANGEFORM, ChangeFormHandler.class);
            IHasCommands.COMMANDHANDLERS.put(Command.CHANGEMOVEINDEX, MoveIndexHandler.class);
            IHasCommands.COMMANDHANDLERS.put(Command.MOVETO, MoveToHandler.class);
            IHasCommands.COMMANDHANDLERS.put(Command.STANCE, StanceHandler.class);
            IHasCommands.COMMANDHANDLERS.put(Command.SWAPMOVES, SwapMovesHandler.class);
            IHasCommands.COMMANDHANDLERS.put(Command.TELEPORT, TeleportHandler.class);
        }
    }

    public static class DefaultHandler implements IMobCommandHandler
    {
        boolean byOwner;

        @Override
        public void handleCommand(IPokemob pokemob) throws Exception
        {
        }

        @Override
        public void writeToBuf(ByteBuf buf)
        {
            buf.writeBoolean(fromOwner());
        }

        @Override
        public void readFromBuf(ByteBuf buf)
        {
            setFromOwner(buf.readBoolean());
        }

        @Override
        public IMobCommandHandler setFromOwner(boolean owner)
        {
            this.byOwner = owner;
            return this;
        }

        @Override
        public boolean fromOwner()
        {
            return this.byOwner;
        }

    }

    int                entityId;
    IMobCommandHandler handler;
    Command            command;

    public static void sendCommand(IPokemob pokemob, Command command, IMobCommandHandler handler)
    {
        PacketCommand packet = new PacketCommand();
        packet.entityId = pokemob.getEntity().getEntityId();
        packet.command = command;
        packet.handler = handler;
        PokecubeMod.packetPipeline.sendToServer(packet);
    }

    public PacketCommand()
    {
    }

    @Override
    public IMessage onMessage(PacketCommand message, MessageContext ctx)
    {
        PokecubeCore.proxy.getMainThreadListener().addScheduledTask(new Runnable()
        {
            @Override
            public void run()
            {
                EntityPlayer player = ctx.getServerHandler().player;
                processMessage(player.getEntityWorld(), message);
            }
        });
        return null;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        entityId = buf.readInt();
        command = Command.values()[buf.readByte()];
        try
        {
            handler = IHasCommands.COMMANDHANDLERS.get(command).newInstance();
            handler.readFromBuf(buf);
        }
        catch (Exception e)
        {
            PokecubeMod.log(Level.SEVERE, "Error handling a command to a pokemob", e);
            handler = new DefaultHandler();
        }
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(entityId);
        buf.writeByte(command.ordinal());
        handler.writeToBuf(buf);
    }

    private static final void processMessage(World world, PacketCommand message)
    {
        Entity user = PokecubeMod.core.getEntityProvider().getEntity(world, message.entityId, true);
        IPokemob pokemob = CapabilityPokemob.getPokemobFor(user);
        if (pokemob == null) return;
        pokemob.handleCommand(message.command, message.handler);
    }

}
