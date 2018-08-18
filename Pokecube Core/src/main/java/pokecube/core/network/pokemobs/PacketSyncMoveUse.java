package pokecube.core.network.pokemobs;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import pokecube.core.PokecubeCore;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;

public class PacketSyncMoveUse implements IMessage, IMessageHandler<PacketSyncMoveUse, IMessage>
{

    private static void processMessage(MessageContext ctx, PacketSyncMoveUse message)
    {
        EntityPlayer player = PokecubeCore.getPlayer(null);
        int id = message.entityId;
        int index = message.index;
        Entity e = PokecubeMod.core.getEntityProvider().getEntity(player.getEntityWorld(), id, true);
        IPokemob mob = CapabilityPokemob.getPokemobFor(e);
        if (mob != null)
        {
            mob.getMoveStats().lastMove = mob.getMove(index);
        }
    }

    public static void sendUpdate(IPokemob pokemob)
    {
        if (pokemob.getEntity().getEntityWorld().isRemote || !(pokemob.getPokemonOwner() instanceof EntityPlayer))
            return;
        PacketSyncMoveUse packet = new PacketSyncMoveUse();
        packet.entityId = pokemob.getEntity().getEntityId();
        packet.index = pokemob.getMoveIndex();
        PokecubeMod.packetPipeline.sendTo(packet, (EntityPlayerMP) pokemob.getPokemonOwner());
    }

    int entityId;
    int index;

    public PacketSyncMoveUse()
    {
    }

    @Override
    public IMessage onMessage(final PacketSyncMoveUse message, final MessageContext ctx)
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
        index = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(entityId);
        buf.writeInt(index);
    }

}
