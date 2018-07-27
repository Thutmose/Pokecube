package pokecube.core.network.pokemobs;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import pokecube.core.PokecubeCore;
import pokecube.core.handlers.Config;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.network.PokecubePacketHandler;
import pokecube.core.network.packets.PacketSyncRoutes;

public class PacketPokemobGui implements IMessage, IMessageHandler<PacketPokemobGui, IMessage>
{
    public static final byte MAIN    = 0;
    public static final byte AI      = 1;
    public static final byte STORAGE = 2;
    public static final byte ROUTES  = 3;

    byte                     message;
    int                      id;

    public static void sendPagePacket(byte page, int id)
    {
        PacketPokemobGui packet = new PacketPokemobGui(page, id);
        PokecubePacketHandler.sendToServer(packet);
    }

    public PacketPokemobGui()
    {
    }

    public PacketPokemobGui(byte message, int id)
    {
        this.message = message;
        this.id = id;
    }

    @Override
    public IMessage onMessage(final PacketPokemobGui message, final MessageContext ctx)
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
        message = buf.readByte();
        id = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeByte(message);
        buf.writeInt(id);
    }

    void processMessage(MessageContext ctx, PacketPokemobGui message)
    {
        Entity entity = ctx.getServerHandler().player.getEntityWorld().getEntityByID(message.id);

        int id = -1;
        switch (message.message)
        {
        case AI:
            id = Config.GUIPOKEMOBAI_ID;
            break;
        case MAIN:
            id = Config.GUIPOKEMOB_ID;
            break;
        case STORAGE:
            id = Config.GUIPOKEMOBSTORE_ID;
            break;
        case ROUTES:
            PacketSyncRoutes.sendUpdateClientPacket(entity, ctx.getServerHandler().player, true);
            return;
        }
        if (id > 0) ctx.getServerHandler().player.openGui(PokecubeMod.core, id, entity.getEntityWorld(),
                entity.getEntityId(), 0, 0);
    }
}
