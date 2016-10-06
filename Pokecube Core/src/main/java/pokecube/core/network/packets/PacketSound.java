package pokecube.core.network.packets;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import pokecube.core.PokecubeCore;
import pokecube.core.network.PokecubePacketHandler;
import thut.api.maths.Vector3;

public class PacketSound implements IMessage, IMessageHandler<PacketSound, IMessage>
{
    public static void sendMessage(Vector3 location, int dimension, int id, String sound, float volume, float pitch)
    {
        PacketSound toSend = new PacketSound(sound, id, volume, pitch);
        PokecubePacketHandler.sendToAllNear(toSend, location, dimension, 32);
    }

    String sound;
    int    id;
    float  volume;
    float  pitch;

    public PacketSound()
    {
    }

    public PacketSound(String sound, int id, float volume, float pitch)
    {
        this.sound = sound;
        this.id = id;
        this.volume = volume;
        this.pitch = pitch;
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
        id = buffer.readInt();
        volume = buffer.readFloat();
        pitch = buffer.readFloat();
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
        buffer.writeInt(id);
        buffer.writeFloat(volume);
        buffer.writeFloat(pitch);
        buffer.writeString(sound);
    }

    void processMessage(MessageContext ctx, PacketSound message)
    {
        Entity e = PokecubeCore.proxy.getWorld().getEntityByID(message.id);
        if (e != null)
        {
            SoundEvent event = new SoundEvent(new ResourceLocation(message.sound));
            e.worldObj.playRecord(e.getPosition(), event);
        }
    }
}