package pokecube.core.network.pokemobs;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.core.PokecubeCore;
import pokecube.core.commands.CommandTools;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;

public class PacketChangeForme implements IMessage, IMessageHandler<PacketChangeForme, IMessage>
{
    public static void sendPacketToServer(Entity mob, String forme)
    {
        PacketChangeForme packet = new PacketChangeForme();
        packet.entityId = mob.getEntityId();
        packet.forme = forme;
        PokecubeMod.packetPipeline.sendToServer(packet);
    }

    public static void sendPacketToNear(Entity mob, String forme, int distance)
    {
        PacketChangeForme packet = new PacketChangeForme();
        packet.entityId = mob.getEntityId();
        packet.forme = forme;
        PokecubeMod.packetPipeline.sendToAllAround(packet,
                new TargetPoint(mob.dimension, mob.posX, mob.posY, mob.posZ, distance));
    }

    int    entityId;
    String forme;

    public PacketChangeForme()
    {
    }

    @Override
    public IMessage onMessage(final PacketChangeForme message, final MessageContext ctx)
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
        entityId = buf.readInt();
        forme = buffer.readStringFromBuffer(20);
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        PacketBuffer buffer = new PacketBuffer(buf);
        buffer.writeInt(entityId);
        buffer.writeString(forme);
    }

    void processMessage(MessageContext ctx, PacketChangeForme message)
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
        IPokemob pokemob = (IPokemob) mob;
        if (ctx.side == Side.CLIENT)
        {
            pokemob.changeForme(message.forme);
        }
        else
        {
            if (pokemob.getPokemonAIState(IMoveConstants.EVOLVING)) return;
            PokedexEntry megaEntry = pokemob.getPokedexEntry().getEvo(pokemob);
            if (megaEntry != null && megaEntry.getPokedexNb() == pokemob.getPokedexEntry().getPokedexNb())
            {
                String old = pokemob.getPokemonDisplayName().getFormattedText();
                if (pokemob.getPokedexEntry() == megaEntry)
                {
                    pokemob.megaEvolve(pokemob.getPokedexEntry().getBaseName());
                    megaEntry = pokemob.getPokedexEntry().baseForme;
                    player.addChatMessage(CommandTools.makeTranslatedMessage("pokemob.megaevolve.revert", "green", old,
                            megaEntry.getUnlocalizedName()));
                }
                else
                {
                    pokemob.setPokemonAIState(IMoveConstants.MEGAFORME, true);
                    pokemob.megaEvolve(megaEntry.getName());
                    player.addChatMessage(CommandTools.makeTranslatedMessage("pokemob.megaevolve.success", "green", old,
                            megaEntry.getUnlocalizedName()));
                }
            }
            else
            {
                if (pokemob.getPokemonAIState(IMoveConstants.MEGAFORME))
                {
                    String old = pokemob.getPokemonDisplayName().getFormattedText();
                    pokemob.megaEvolve(pokemob.getPokedexEntry().getBaseName());
                    pokemob.setPokemonAIState(IMoveConstants.MEGAFORME, false);
                    megaEntry = pokemob.getPokedexEntry().baseForme;
                    player.addChatMessage(CommandTools.makeTranslatedMessage("pokemob.megaevolve.revert", "green", old,
                            megaEntry.getUnlocalizedName()));
                }
                else
                {
                    player.addChatMessage(CommandTools.makeTranslatedMessage("pokemob.megaevolve.failed", "red",
                            pokemob.getPokemonDisplayName()));
                }
            }
        }
    }
}
