package pokecube.core.network.pokemobs;

import java.io.IOException;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import pokecube.core.PokecubeCore;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.utils.TagNames;

public class PacketSyncNewMoves implements IMessage, IMessageHandler<PacketSyncNewMoves, IMessage>
{
    public int            entityId;
    public NBTTagCompound data = new NBTTagCompound();

    public static void sendUpdatePacket(IPokemob pokemob)
    {
        if (pokemob.getPokemonOwner() instanceof EntityPlayerMP)
        {
            EntityPlayerMP player = (EntityPlayerMP) pokemob.getPokemonOwner();
            NBTTagList newMoves = new NBTTagList();
            for (String s : pokemob.getMoveStats().newMoves)
            {
                newMoves.appendTag(new NBTTagString(s));
            }
            PacketSyncNewMoves packet = new PacketSyncNewMoves();
            packet.data.setTag(TagNames.NEWMOVES, newMoves);
            packet.entityId = pokemob.getEntity().getEntityId();
            PokecubeMod.packetPipeline.sendTo(packet, player);
        }
    }

    public PacketSyncNewMoves()
    {
    }

    @Override
    public IMessage onMessage(final PacketSyncNewMoves message, final MessageContext ctx)
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

    void processMessage(MessageContext ctx, PacketSyncNewMoves message)
    {
        EntityPlayer player = PokecubeCore.getPlayer(null);
        int id = message.entityId;
        NBTTagCompound data = message.data;
        Entity e = PokecubeMod.core.getEntityProvider().getEntity(player.getEntityWorld(), id, true);
        IPokemob pokemob = CapabilityPokemob.getPokemobFor(e);
        if (pokemob != null)
        {
            NBTTagList newMoves = (NBTTagList) data.getTag(TagNames.NEWMOVES);
            pokemob.getMoveStats().newMoves.clear();
            for (int i = 0; i < newMoves.tagCount(); i++)
                if (!pokemob.getMoveStats().newMoves.contains(newMoves.getStringTagAt(i)))
                    pokemob.getMoveStats().newMoves.add(newMoves.getStringTagAt(i));
        }
    }
}
