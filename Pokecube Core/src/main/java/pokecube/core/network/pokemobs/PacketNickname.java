package pokecube.core.network.pokemobs;

import java.util.UUID;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import pokecube.core.PokecubeCore;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;

public class PacketNickname implements IMessage, IMessageHandler<PacketNickname, IMessage>
{
    public static void sendPacket(Entity mob, String name)
    {
        PacketNickname packet = new PacketNickname();
        packet.entityId = mob.getUniqueID();
        packet.name = name;
        PokecubeMod.packetPipeline.sendToServer(packet);
    }

    UUID   entityId;
    String name;

    public PacketNickname()
    {
    }

    @Override
    public IMessage onMessage(final PacketNickname message, final MessageContext ctx)
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
        entityId = new UUID(buf.readLong(), buf.readLong());
        name = buffer.readStringFromBuffer(20);
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        PacketBuffer buffer = new PacketBuffer(buf);
        buffer.writeLong(entityId.getMostSignificantBits());
        buffer.writeLong(entityId.getLeastSignificantBits());
        buffer.writeString(name);
    }

    void processMessage(MessageContext ctx, PacketNickname message)
    {
        EntityPlayer player;
        player = ctx.getServerHandler().playerEntity;
        Entity orignalMob = ctx.getServerHandler().playerEntity.getServerWorld().getEntityFromUuid(message.entityId);
        if (orignalMob == null) return;
        Entity mob = PokecubeMod.core.getEntityProvider().getEntity(player.worldObj, orignalMob.getEntityId(), true);
        if (mob == null || !(mob instanceof IPokemob)) return;

        IPokemob pokemob = (IPokemob) mob;
        String name = ChatAllowedCharacters.filterAllowedCharacters(new String(message.name));
        if (pokemob.getPokemonDisplayName().getFormattedText().equals(name)) return;
        boolean OT = pokemob.getPokemonOwnerID() == null || pokemob.getOriginalOwnerUUID() == null
                || (pokemob.getPokemonOwnerID().equals(pokemob.getOriginalOwnerUUID()));

        if (!OT && pokemob.getPokemonOwner() != null)
        {
            OT = pokemob.getPokemonOwner().getUniqueID().equals(pokemob.getOriginalOwnerUUID());
        }
        if (!OT)
        {
            if (pokemob.getPokemonOwner() != null)
            {
                pokemob.getPokemonOwner().addChatMessage(new TextComponentTranslation("pokemob.rename.deny"));
            }
        }
        else
        {
            pokemob.getPokemonOwner().addChatMessage(new TextComponentTranslation("pokemob.rename.success",
                    pokemob.getPokemonDisplayName().getFormattedText(), name));
            pokemob.setPokemonNickname(name);
        }

    }
}