package pokecube.core.network.pokemobs;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import pokecube.core.PokecubeCore;
import pokecube.core.interfaces.IMoveConstants.AIRoutine;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;

public class PacketAIRoutine implements IMessage, IMessageHandler<PacketAIRoutine, IMessage>
{

    int       entityId;
    AIRoutine routine;
    boolean   state;

    public static void sentCommand(IPokemob pokemob, AIRoutine routine, boolean state)
    {
        PacketAIRoutine packet = new PacketAIRoutine();
        packet.entityId = pokemob.getEntity().getEntityId();
        packet.routine = routine;
        packet.state = state;
        PokecubeMod.packetPipeline.sendToServer(packet);
    }

    public PacketAIRoutine()
    {
    }

    @Override
    public IMessage onMessage(PacketAIRoutine message, MessageContext ctx)
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
        routine = AIRoutine.values()[buf.readByte()];
        state = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(entityId);
        buf.writeByte(routine.ordinal());
        buf.writeBoolean(state);
    }

    private static final void processMessage(World world, PacketAIRoutine message)
    {
        Entity user = PokecubeMod.core.getEntityProvider().getEntity(world, message.entityId, true);
        IPokemob pokemob = CapabilityPokemob.getPokemobFor(user);
        if (pokemob == null) return;
        pokemob.setRoutineState(message.routine, message.state);
    }

}
