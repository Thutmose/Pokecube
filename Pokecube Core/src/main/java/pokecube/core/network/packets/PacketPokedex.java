package pokecube.core.network.packets;

import java.io.IOException;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.core.PokecubeCore;
import pokecube.core.handlers.PlayerDataHandler;
import pokecube.core.network.PokecubePacketHandler;
import pokecube.core.utils.PokecubeSerializer;
import thut.api.maths.Vector4;

public class PacketPokedex implements IMessage, IMessageHandler<PacketPokedex, IMessage>
{
    public static final byte REMOVE = -2;
    public static final byte RENAME = -1;

    public static void sendRenameTelePacket(String newName, Vector4 location)
    {
        PacketPokedex packet = new PacketPokedex();
        packet.message = RENAME;
        packet.data.setString("N", newName);
        location.writeToNBT(packet.data);
        PokecubePacketHandler.sendToServer(packet);
    }

    public static void sendRemoveTelePacket(Vector4 location)
    {
        PacketPokedex packet = new PacketPokedex();
        packet.message = REMOVE;
        location.writeToNBT(packet.data);
        PokecubePacketHandler.sendToServer(packet);
    }

    public static void sendChangePagePacket(byte page)
    {
        PacketPokedex packet = new PacketPokedex();
        packet.message = page;
        PokecubePacketHandler.sendToServer(packet);
    }

    byte                  message;
    public NBTTagCompound data = new NBTTagCompound();

    public PacketPokedex()
    {
    }

    public PacketPokedex(byte message)
    {
        this.message = message;
    }

    @Override
    public IMessage onMessage(final PacketPokedex message, final MessageContext ctx)
    {
        PokecubeCore.proxy.getMainThreadListener().addScheduledTask(new Runnable()
        {
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
        PacketBuffer buffer = new PacketBuffer(buf);
        try
        {
            data = buffer.readNBTTagCompoundFromBuffer();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeByte(message);
        PacketBuffer buffer = new PacketBuffer(buf);
        buffer.writeNBTTagCompoundToBuffer(data);
    }

    void processMessage(MessageContext ctx, PacketPokedex message)
    {
        EntityPlayer player;
        if (ctx.side == Side.CLIENT)
        {
            player = PokecubeCore.getPlayer(null);
        }
        else
        {
            player = ctx.getServerHandler().playerEntity;
        }
        if (message.message == REMOVE)
        {
            Vector4 location = new Vector4(message.data);
            PokecubeSerializer.getInstance().unsetTeleport(location, player.getCachedUniqueIdString());
            player.addChatMessage(new TextComponentString("Removed The location " + location.toIntString()));
            PlayerDataHandler.getInstance().save(player.getCachedUniqueIdString());
            PacketDataSync.sendSyncPacket(player, "pokecube-data");
        }
        else if (message.message == RENAME)
        {
            String name = message.data.getString("N");
            Vector4 location = new Vector4(message.data);
            PokecubeSerializer.getInstance().setTeleport(location, player.getCachedUniqueIdString(), name);
            player.addChatMessage(
                    new TextComponentString("Set The location " + location.toIntString() + " as " + name));
            PlayerDataHandler.getInstance().save(player.getCachedUniqueIdString());
            PacketDataSync.sendSyncPacket(player, "pokecube-data");
        }
        else
        {
            player.getHeldItemMainhand().setItemDamage(message.message);
        }
    }
}
