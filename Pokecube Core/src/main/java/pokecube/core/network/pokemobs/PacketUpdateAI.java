package pokecube.core.network.pokemobs;

import java.io.IOException;

import javax.annotation.Nullable;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import pokecube.core.PokecubeCore;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.AICapWrapper;
import thut.api.entity.ai.IAIMob;
import thut.api.entity.ai.IAIRunnable;
import thut.api.entity.ai.ILogicRunnable;

public class PacketUpdateAI implements IMessage, IMessageHandler<PacketUpdateAI, IMessage>
{
    public int            entityId;
    public NBTTagCompound data = new NBTTagCompound();

    public static void sendUpdatePacket(IPokemob pokemob, @Nullable String ai, @Nullable String logic)
    {
        NBTTagCompound tag = new NBTTagCompound();
        NBTTagCompound savedAI = new NBTTagCompound();
        NBTTagCompound savedLogic = new NBTTagCompound();
        if (ai != null) for (IAIRunnable runnable : pokemob.getAI().aiTasks)
        {
            if (runnable instanceof INBTSerializable && runnable.getIdentifier().equals(ai))
            {
                NBTBase base = INBTSerializable.class.cast(runnable).serializeNBT();
                savedAI.setTag(runnable.getIdentifier(), base);
                break;
            }
        }
        if (logic != null) for (ILogicRunnable runnable : pokemob.getAI().aiLogic)
        {
            if (runnable instanceof INBTSerializable && runnable.getIdentifier().equals(logic))
            {
                NBTBase base = INBTSerializable.class.cast(runnable).serializeNBT();
                savedLogic.setTag(runnable.getIdentifier(), base);
                break;
            }
        }
        tag.setTag("ai", savedAI);
        tag.setTag("logic", savedLogic);
        PacketUpdateAI packet = new PacketUpdateAI();
        packet.data = tag;
        packet.entityId = pokemob.getEntity().getEntityId();
        PokecubeMod.packetPipeline.sendToServer(packet);
    }

    public PacketUpdateAI()
    {
    }

    @Override
    public IMessage onMessage(final PacketUpdateAI message, final MessageContext ctx)
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

    void processMessage(MessageContext ctx, PacketUpdateAI message)
    {
        EntityPlayer player = ctx.getServerHandler().player;
        int id = message.entityId;
        NBTTagCompound data = message.data;
        Entity e = PokecubeMod.core.getEntityProvider().getEntity(player.getEntityWorld(), id, true);
        IAIMob ai = e.getCapability(IAIMob.THUTMOBAI, null);
        if (ai instanceof AICapWrapper)
        {
            AICapWrapper wrapper = (AICapWrapper) ai;
            wrapper.deserializeNBT(data);
            wrapper.init();
        }
    }
}
