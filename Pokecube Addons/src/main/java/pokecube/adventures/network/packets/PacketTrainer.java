package pokecube.adventures.network.packets;

import java.io.IOException;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.adventures.entity.helper.capabilities.CapabilityHasPokemobs;
import pokecube.adventures.entity.helper.capabilities.CapabilityHasPokemobs.IHasPokemobs;
import pokecube.adventures.entity.trainers.EntityTrainer;
import pokecube.adventures.entity.trainers.TypeTrainer;
import pokecube.core.PokecubeCore;
import thut.api.network.PacketHandler;

public class PacketTrainer implements IMessage, IMessageHandler<PacketTrainer, IMessage>
{
    public static final byte MESSAGEUPDATETRAINER = 0;
    public static final byte MESSAGENOTIFYDEFEAT  = 1;
    public static final byte MESSAGEKILLTRAINER   = 2;

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
            NBTBase tag = message.data.getTag("T");
            int id = message.data.getInteger("I");
            Entity mob = player.getEntityWorld().getEntityByID(id);
            IHasPokemobs cap = CapabilityHasPokemobs.getHasPokemobs(mob);
            if (cap != null)
            {
                CapabilityHasPokemobs.storage.readNBT(CapabilityHasPokemobs.HASPOKEMOBS_CAP, cap, null, tag);
                TypeTrainer old = cap.getType();
                TypeTrainer type = TypeTrainer.getTrainer(message.data.getString("K"));
                if (old != type)
                {

                    cap.setType(type);
                }
                PacketHandler.sendEntityUpdate(mob);
            }
            return;
        }
        if (message.message == MESSAGENOTIFYDEFEAT)
        {
            int id = message.data.getInteger("I");
            EntityLivingBase mob = (EntityLivingBase) player.getEntityWorld().getEntityByID(id);
            if (mob instanceof EntityTrainer) ((EntityTrainer) mob).visibleTime = message.data.getLong("L");
            return;
        }
        if (message.message == MESSAGEKILLTRAINER)
        {
            int id = message.data.getInteger("I");
            EntityTrainer trainer = (EntityTrainer) player.getEntityWorld().getEntityByID(id);
            trainer.setDead();
        }
    }

}
