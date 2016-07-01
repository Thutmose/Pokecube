package pokecube.adventures.network.packets;

import java.io.IOException;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.adventures.entity.trainers.EntityTrainer;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Database;
import pokecube.core.events.handlers.SpawnHandler;
import thut.api.maths.Vector3;
import thut.api.network.PacketHandler;

public class PacketTrainer implements IMessage, IMessageHandler<PacketTrainer, IMessage>
{
    public static final byte MESSAGEUPDATETRAINER = 0;

    byte                     message;
    public NBTTagCompound    data                 = new NBTTagCompound();

    public PacketTrainer()
    {
    }

    public PacketTrainer(byte message)
    {
        this.message = message;
    }

    @Override
    public IMessage onMessage(final PacketTrainer message, final MessageContext ctx)
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

    void processMessage(MessageContext ctx, PacketTrainer message)
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
        if (message.message == MESSAGEUPDATETRAINER)
        {
            int id = message.data.getInteger("I");
            EntityTrainer trainer = (EntityTrainer) player.getEntityWorld().getEntityByID(id);
            boolean reset = message.data.getBoolean("R");
            boolean stationary = message.data.getBoolean("S");
            if (reset)
            {
                int maxXp = SpawnHandler.getSpawnLevel(trainer.getEntityWorld(), Vector3.getNewVector().set(trainer),
                        Database.getEntry(1));
                trainer.initTrainer(trainer.type, maxXp);
            }
            else
            {
                trainer.readEntityFromNBT(message.data.getCompoundTag("T"));
                boolean rename = message.data.hasKey("N");
                if (rename)
                {
                    String name = message.data.getString("N");
                    trainer.setCustomNameTag(name);
                }
            }
            if (player.isServerWorld())
            {
                if (stationary) trainer.setStationary(stationary);
                PacketHandler.sendEntityUpdate(trainer);
            }
        }
    }

}
