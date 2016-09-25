package pokecube.core.network.packets;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import pokecube.core.PokecubeCore;
import pokecube.core.network.PokecubePacketHandler;
import thut.api.maths.Vector3;

public class PacketSound implements IMessage, IMessageHandler<PacketSound, IMessage>
{
    public static void sendMessage(EntityPlayer sendTo, Vector3 location, String sound)
    {
        PacketSound toSend = new PacketSound(location, sound);
        PokecubePacketHandler.sendToClient(toSend, sendTo);
    }

    Vector3 location;
    String  sound;

    public PacketSound()
    {
    }

    public PacketSound(Vector3 location, String sound)
    {
        this.location = location;
        this.sound = sound;
    }

    @Override
    public IMessage onMessage(final PacketSound message, final MessageContext ctx)
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
        location = Vector3.readFromBuff(buffer);
        try
        {
            sound = buffer.readStringFromBuffer(30);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        PacketBuffer buffer = new PacketBuffer(buf);
        location.writeToBuff(buffer);
        buffer.writeString(sound);
    }

    void processMessage(MessageContext ctx, PacketSound message)
    {
        EntityPlayer player = PokecubeCore.getPlayer(null);
        SoundEvent sound = new SoundEvent(new ResourceLocation(message.sound));
        player.getEntityWorld().playSound(player, message.location.getPos(), sound, SoundCategory.PLAYERS, 1, 1);
    }
}
