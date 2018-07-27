package pokecube.core.network.packets;

import java.io.IOException;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.properties.IGuardAICapability;
import pokecube.core.client.gui.helper.RouteEditHelper;
import pokecube.core.events.handlers.EventsHandler;
import pokecube.core.handlers.Config;
import pokecube.core.interfaces.PokecubeMod;

public class PacketSyncRoutes implements IMessage, IMessageHandler<PacketSyncRoutes, IMessage>
{
    public int            entityId;
    public NBTTagCompound data = new NBTTagCompound();

    public static void sendUpdateClientPacket(Entity mob, EntityPlayerMP player, boolean gui)
    {
        IGuardAICapability guard = mob.getCapability(EventsHandler.GUARDAI_CAP, null);
        PacketSyncRoutes packet = new PacketSyncRoutes();
        packet.data.setTag("R", guard.serializeTasks());
        packet.data.setBoolean("O", gui);
        packet.entityId = mob.getEntityId();
        PokecubeMod.packetPipeline.sendTo(packet, player);
    }

    public static void sendServerPacket(Entity mob, NBTBase tag)
    {
        PacketSyncRoutes packet = new PacketSyncRoutes();
        packet.entityId = mob.getEntityId();
        if (tag instanceof NBTTagCompound) packet.data = (NBTTagCompound) tag;
        PokecubeMod.packetPipeline.sendToServer(packet);
    }

    public PacketSyncRoutes()
    {
    }

    @Override
    public IMessage onMessage(final PacketSyncRoutes message, final MessageContext ctx)
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
        PacketBuffer buffer = new PacketBuffer(buf);
        entityId = buffer.readInt();
        try
        {
            data = buffer.readCompoundTag();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        PacketBuffer buffer = new PacketBuffer(buf);
        buffer.writeInt(entityId);
        buffer.writeCompoundTag(data);
    }

    void processMessage(MessageContext ctx, PacketSyncRoutes message)
    {
        EntityPlayer player;
        int id = message.entityId;
        NBTTagCompound data = message.data;
        if (ctx.side == Side.CLIENT)
        {
            player = PokecubeCore.getPlayer(null);
        }
        else
        {
            player = ctx.getServerHandler().player;
        }
        Entity e = PokecubeMod.core.getEntityProvider().getEntity(player.getEntityWorld(), id, true);
        if (e == null) return;
        IGuardAICapability guard = e.getCapability(EventsHandler.GUARDAI_CAP, null);

        if (guard != null)
        {
            if (ctx.side == Side.CLIENT)
            {
                guard.loadTasks((NBTTagList) data.getTag("R"));
                if (data.getBoolean("O"))
                {
                    sendServerPacket(e, null);
                }
            }
            else
            {
                if (data.hasNoTags())
                {
                    player.openGui(PokecubeMod.core, Config.GUIPOKEMOBROUTE_ID, e.getEntityWorld(), e.getEntityId(), 0,
                            0);
                }
                else RouteEditHelper.applyServerPacket(data.getTag("T"), e, guard);
            }
        }
    }
}
