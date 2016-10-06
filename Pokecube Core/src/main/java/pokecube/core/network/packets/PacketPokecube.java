package pokecube.core.network.packets;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import pokecube.core.PokecubeCore;
import pokecube.core.items.pokecubes.EntityPokecube;
import pokecube.core.network.PokecubePacketHandler;

public class PacketPokecube implements IMessage, IMessageHandler<PacketPokecube, IMessage>
{
    public static void sendMessage(EntityPlayer player, int id, long renderTime)
    {
        PacketPokecube toSend = new PacketPokecube(id, renderTime);
        PokecubePacketHandler.sendToClient(toSend, player);
    }

    int  id;
    long time;

    public PacketPokecube()
    {
    }

    public PacketPokecube(int id, long renderTime)
    {
        time = renderTime;
        this.id = id;
    }

    @Override
    public IMessage onMessage(final PacketPokecube message, final MessageContext ctx)
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
        time = buf.readLong();
        id = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeLong(time);
        buf.writeInt(id);
    }

    void processMessage(MessageContext ctx, PacketPokecube message)
    {
        Entity e = PokecubeCore.proxy.getWorld().getEntityByID(message.id);
        if (e instanceof EntityPokecube) ((EntityPokecube) e).reset = message.time;
    }
}