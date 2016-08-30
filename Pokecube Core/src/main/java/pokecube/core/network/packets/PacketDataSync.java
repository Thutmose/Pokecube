package pokecube.core.network.packets;

import java.io.IOException;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.core.PokecubeCore;
import pokecube.core.handlers.PlayerDataHandler;
import pokecube.core.handlers.PlayerDataHandler.PlayerData;
import pokecube.core.handlers.PlayerDataHandler.PlayerDataManager;
import pokecube.core.interfaces.PokecubeMod;

public class PacketDataSync implements IMessage, IMessageHandler<PacketDataSync, IMessage>
{
    public NBTTagCompound data = new NBTTagCompound();

    public static void sendSyncPacket(EntityPlayer player, String dataType)
    {
        PlayerDataManager manager = PlayerDataHandler.getInstance().getPlayerData(player);
        PlayerData data = manager.getData(dataType, PlayerData.class);
        PacketDataSync packet = new PacketDataSync();
        packet.data.setString("type", dataType);
        NBTTagCompound tag1 = new NBTTagCompound();
        data.writeToNBT(tag1);
        packet.data.setTag("data", tag1);
        PokecubeMod.packetPipeline.sendTo(packet, (EntityPlayerMP) player);
        System.out.println("Saving Data for " + player);
        PlayerDataHandler.getInstance().save(player.getCachedUniqueIdString());
    }

    public PacketDataSync()
    {
    }

    @Override
    public IMessage onMessage(final PacketDataSync message, final MessageContext ctx)
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
        PacketBuffer buffer = new PacketBuffer(buf);
        buffer.writeNBTTagCompoundToBuffer(data);
    }

    void processMessage(MessageContext ctx, PacketDataSync message)
    {
        EntityPlayer player;
        if (ctx.side == Side.CLIENT)
        {
            player = PokecubeCore.getPlayer(null);
            PlayerDataManager manager = PlayerDataHandler.getInstance().getPlayerData(player);
            manager.getData(data.getString("type"), PlayerData.class).readFromNBT(data.getCompoundTag("data"));
        }
    }
}
