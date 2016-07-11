package pokecube.core.network.pokemobs;

import java.util.List;

import com.google.common.collect.Lists;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.core.PokecubeCore;
import pokecube.core.interfaces.PokecubeMod;
import thut.api.maths.Vector3;

public class PacketPosSync implements IMessage, IMessageHandler<PacketPosSync, IMessage>
{
    int     entityId;
    Vector3 pos;
    Vector3 motion;
    int[]   riding = {};

    public static void sendToServer(Entity mob)
    {
        PacketPosSync packet = new PacketPosSync();
        packet.entityId = mob.getEntityId();
        packet.pos = Vector3.getNewVector().set(mob);
        packet.motion = Vector3.getNewVector().setToVelocity(mob);
        PokecubeMod.packetPipeline.sendToServer(packet);
    }

    public static void notifyNearby(Entity mob)
    {
        PacketPosSync packet = new PacketPosSync();
        packet.entityId = mob.getEntityId();
        packet.pos = Vector3.getNewVector().set(mob);
        packet.motion = Vector3.getNewVector().setToVelocity(mob);
        packet.riding = new int[mob.getRecursivePassengers().size()];
        List<Entity> mobs = Lists.newArrayList(mob.getRecursivePassengers());
        for (int i = 0; i < packet.riding.length; i++)
        {
            packet.riding[i] = mobs.get(i).getEntityId();
        }
        PokecubeMod.packetPipeline.sendToAllAround(packet,
                new TargetPoint(mob.dimension, packet.pos.x, packet.pos.y, packet.pos.z, 64));
    }

    public PacketPosSync()
    {
    }

    @Override
    public IMessage onMessage(final PacketPosSync message, final MessageContext ctx)
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
        entityId = buf.readInt();
        pos = Vector3.getNewVector().set(buf.readFloat(), buf.readFloat(), buf.readFloat());
        motion = Vector3.getNewVector().set(buf.readFloat(), buf.readFloat(), buf.readFloat());
        int num = buf.readInt();
        riding = new int[num];
        for (int i = 0; i < num; i++)
        {
            riding[i] = buf.readInt();
        }
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(entityId);
        buf.writeFloat((float) pos.x);
        buf.writeFloat((float) pos.y);
        buf.writeFloat((float) pos.z);
        buf.writeFloat((float) motion.x);
        buf.writeFloat((float) motion.y);
        buf.writeFloat((float) motion.z);
        buf.writeInt(riding.length);
        for (int i = 0; i < riding.length; i++)
        {
            buf.writeInt(riding[i]);
        }
    }

    private void apply(Entity mob)
    {
        mob.setPosition(pos.x, pos.y, pos.z);
        mob.motionX = motion.x;
        mob.motionY = motion.y;
        mob.motionZ = motion.z;

        if (mob.worldObj.isRemote)
        {
            for (int i = 0; i < riding.length; i++)
            {
                Entity e = mob.worldObj.getEntityByID(riding[i]);
                e.startRiding(mob, true);
            }
        }

        for (Entity e : mob.getRecursivePassengers())
        {
            e.onGround = true;
            e.fallDistance = 0;
        }
    }

    void processMessage(MessageContext ctx, PacketPosSync message)
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
        Entity mob = player.getEntityWorld().getEntityByID(message.entityId);
        if (mob != null)
        {
            if (ctx.side == Side.CLIENT)
            {
                if (!mob.getRecursivePassengers().contains(player))
                {
                    message.apply(mob);
                }
            }
            else
            {
                message.apply(mob);
                notifyNearby(mob);
            }
        }
    }

}
