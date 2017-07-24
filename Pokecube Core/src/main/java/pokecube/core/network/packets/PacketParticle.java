package pokecube.core.network.packets;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import pokecube.core.PokecubeCore;
import pokecube.core.network.PokecubePacketHandler;
import thut.api.maths.Vector3;

public class PacketParticle implements IMessage, IMessageHandler<PacketParticle, IMessage>
{
    public static void sendMessage(World world, Vector3 location, Vector3 velocity, String sound, int... args)
    {
        PacketParticle toSend = new PacketParticle(location, velocity, sound, args);
        PokecubePacketHandler.sendToAllNear(toSend, location, world.provider.getDimension(), 32);
    }

    Vector3 velocity;
    Vector3 location;
    String  particle;
    int[]   args;

    public PacketParticle()
    {
    }

    public PacketParticle(Vector3 location, Vector3 velocity, String sound, int... args)
    {
        this.location = location;
        this.velocity = velocity;
        if (this.velocity == null) this.velocity = Vector3.getNewVector();
        this.particle = sound;
        this.args = args;
    }

    @Override
    public IMessage onMessage(final PacketParticle message, final MessageContext ctx)
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
        velocity = Vector3.readFromBuff(buffer);
        try
        {
            particle = buffer.readString(30);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        args = buffer.readVarIntArray();
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        PacketBuffer buffer = new PacketBuffer(buf);
        location.writeToBuff(buffer);
        velocity.writeToBuff(buffer);
        buffer.writeString(particle);
        buffer.writeVarIntArray(args);
    }

    void processMessage(MessageContext ctx, PacketParticle message)
    {
        PokecubeCore.proxy.spawnParticle(PokecubeCore.proxy.getWorld(), message.particle, message.location,
                message.velocity, message.args);
    }
}