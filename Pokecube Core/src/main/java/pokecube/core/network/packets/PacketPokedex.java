package pokecube.core.network.packets;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.village.Village;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.core.PokecubeCore;
import pokecube.core.handlers.Config;
import pokecube.core.handlers.PlayerDataHandler;
import pokecube.core.network.PokecubePacketHandler;
import pokecube.core.utils.PokecubeSerializer;
import thut.api.maths.Vector3;
import thut.api.maths.Vector4;

public class PacketPokedex implements IMessage, IMessageHandler<PacketPokedex, IMessage>
{
    public static final byte VILLAGE = -3;
    public static final byte REMOVE  = -2;
    public static final byte RENAME  = -1;

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

    public static void sendVillageInfoPacket(EntityPlayer player)
    {
        List<Village> villages = player.getEntityWorld().getVillageCollection().getVillageList();
        PacketPokedex packet = new PacketPokedex();
        if (villages.size() > 0)
        {
            final BlockPos pos = player.getPosition();
            Collections.sort(villages, new Comparator<Village>()
            {
                @Override
                public int compare(Village o1, Village o2)
                {
                    return (int) (pos.distanceSq(o1.getCenter()) - pos.distanceSq(o2.getCenter()));
                }
            });
            Vector3 temp = Vector3.getNewVector().set(villages.get(0).getCenter());
            temp.writeToNBT(packet.data, "village");
        }
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
        System.out.println(message.message+" "+player);
        if (message.message == REMOVE)
        {
            Vector4 location = new Vector4(message.data);
            PokecubeSerializer.getInstance().unsetTeleport(location, player.getCachedUniqueIdString());
            player.addChatMessage(new TextComponentString("Removed The location " + location.toIntString()));
            PlayerDataHandler.getInstance().save(player.getCachedUniqueIdString());
            PacketDataSync.sendInitPacket(player, "pokecube-data");
        }
        else if (message.message == RENAME)
        {
            String name = message.data.getString("N");
            Vector4 location = new Vector4(message.data);
            PokecubeSerializer.getInstance().setTeleport(location, player.getCachedUniqueIdString(), name);
            player.addChatMessage(
                    new TextComponentString("Set The location " + location.toIntString() + " as " + name));
            PlayerDataHandler.getInstance().save(player.getCachedUniqueIdString());
            PacketDataSync.sendInitPacket(player, "pokecube-data");
        }
        else if (message.message == VILLAGE)
        {
            Vector3 temp = Vector3.readFromNBT(message.data, "village");
            if (temp != null) pokecube.core.client.gui.GuiPokedex.closestVillage.set(temp);
            else pokecube.core.client.gui.GuiPokedex.closestVillage.clear();
            player.openGui(PokecubeCore.instance, Config.GUIPOKEDEX_ID, player.getEntityWorld(), 0, 0, 0);
        }
        else
        {
            player.getHeldItemMainhand().setItemDamage(message.message);
        }
    }
}
