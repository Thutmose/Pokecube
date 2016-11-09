package pokecube.core.network.pokemobs;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import pokecube.core.PokecubeCore;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.IStatsModifiers;
import pokecube.core.interfaces.IPokemob.Stats;
import pokecube.core.interfaces.PokecubeMod;

public class PacketSyncModifier implements IMessage, IMessageHandler<PacketSyncModifier, IMessage>
{

    private static void processMessage(MessageContext ctx, PacketSyncModifier message)
    {
        EntityPlayer player = PokecubeCore.getPlayer(null);
        int id = message.entityId;
        int modifier = message.modifier;
        float[] values = message.values;
        Entity e = PokecubeMod.core.getEntityProvider().getEntity(player.getEntityWorld(), id, false);
        if (e != null && e instanceof IPokemob)
        {
            IPokemob mob = (IPokemob) e;
            IStatsModifiers stats = mob.getModifiers().sortedModifiers.get(modifier);
            for (Stats stat : Stats.values())
                stats.setModifier(stat, values[stat.ordinal()]);
        }
    }

    public static void sendUpdate(String type, IPokemob pokemob)
    {
        if (!((Entity) pokemob).addedToChunk) return;
        PacketSyncModifier packet = new PacketSyncModifier();
        packet.entityId = ((Entity) pokemob).getEntityId();
        packet.modifier = pokemob.getModifiers().indecies.get(type);
        for (Stats stat : Stats.values())
        {
            packet.values[stat.ordinal()] = pokemob.getModifiers().sortedModifiers.get(packet.modifier)
                    .getModifierRaw(stat);
        }
        PokecubeMod.packetPipeline.sendToAllAround(packet, new TargetPoint(((Entity) pokemob).dimension,
                ((Entity) pokemob).posX, ((Entity) pokemob).posY, ((Entity) pokemob).posZ, 64));
    }

    int   entityId;
    int   modifier;
    float values[] = new float[Stats.values().length];

    public PacketSyncModifier()
    {
    }

    @Override
    public IMessage onMessage(final PacketSyncModifier message, final MessageContext ctx)
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
        modifier = buf.readInt();
        for (int i = 0; i < values.length; i++)
            values[i] = buf.readFloat();
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(entityId);
        buf.writeInt(modifier);
        for (int i = 0; i < values.length; i++)
            buf.writeFloat(values[i]);
    }

}
