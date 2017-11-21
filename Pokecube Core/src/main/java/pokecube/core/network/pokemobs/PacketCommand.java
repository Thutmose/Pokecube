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
import pokecube.core.interfaces.pokemob.commandhandlers.SwapMovesHandler;

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
            IHasCommands.COMMANDHANDLERS.put(Command.SWAPMOVES, SwapMovesHandler.class);
        }
    }

    private static class DefaultHandler implements IMobCommandHandler
    {

        @Override
        public void handleCommand(IPokemob pokemob) throws Exception
        {
        }

        @Override
        public void writeToBuf(ByteBuf buf)
        {
        }

        @Override
        public void readFromBuf(ByteBuf buf)
        {
        }

    }

    int                entityId;
    IMobCommandHandler handler;
    Command            command;

    public static void sentCommand(IPokemob pokemob, Command command, IMobCommandHandler handler)
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
                EntityPlayer player = ctx.getServerHandler().playerEntity;
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
