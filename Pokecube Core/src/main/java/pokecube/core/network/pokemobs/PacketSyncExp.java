package pokecube.core.network.pokemobs;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import pokecube.core.PokecubeCore;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;

public class PacketSyncExp implements IMessage, IMessageHandler<PacketSyncExp, IMessage>
{

    private static void processMessage(MessageContext ctx, PacketSyncExp message)
    {
        EntityPlayer player = PokecubeCore.getPlayer(null);
        int id = message.entityId;
        int exp = message.exp;
        Entity e = PokecubeMod.core.getEntityProvider().getEntity(player.getEntityWorld(), id, true);
        IPokemob mob = CapabilityPokemob.getPokemobFor(e);
        if (mob != null)
        {
            mob.getMoveStats().exp = exp;
        }
    }

    public static void sendUpdate(IPokemob pokemob)
    {
        if (!pokemob.getEntity().isServerWorld()) return;
        PacketSyncExp packet = new PacketSyncExp();
        packet.entityId = pokemob.getEntity().getEntityId();
        packet.exp = pokemob.getExp();
        PokecubeMod.packetPipeline.sendToDimension(packet, pokemob.getEntity().dimension);
    }

    int entityId;
    int exp;

    public PacketSyncExp()
    {
    }

    @Override
    public IMessage onMessage(final PacketSyncExp message, final MessageContext ctx)
    {
        PokecubeCore.proxy.getMainThreadListener().addScheduledTask(new Runnable()
        {
            @Override
            public void run()
            {
                processMessage(ctx, message);
            }
        });
        return null;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        entityId = buf.readInt();
        exp = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(entityId);
        buf.writeInt(exp);
    }

}
