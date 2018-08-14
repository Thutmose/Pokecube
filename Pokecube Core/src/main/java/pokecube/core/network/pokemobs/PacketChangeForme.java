package pokecube.core.network.pokemobs;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.ICanEvolve;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.items.megastuff.MegaCapability;
import thut.core.common.commands.CommandTools;

public class PacketChangeForme implements IMessage, IMessageHandler<PacketChangeForme, IMessage>
{
    public static void sendPacketToServer(Entity mob, PokedexEntry forme)
    {
        PacketChangeForme packet = new PacketChangeForme();
        packet.entityId = mob.getEntityId();
        packet.forme = forme;
        PokecubeMod.packetPipeline.sendToServer(packet);
    }

    public static void sendPacketToNear(Entity mob, PokedexEntry forme, int distance)
    {
        PacketChangeForme packet = new PacketChangeForme();
        packet.entityId = mob.getEntityId();
        packet.forme = forme;
        PokecubeMod.packetPipeline.sendToAllAround(packet,
                new TargetPoint(mob.dimension, mob.posX, mob.posY, mob.posZ, distance));
    }

    int          entityId;
    PokedexEntry forme;

    public PacketChangeForme()
    {
    }

    @Override
    public IMessage onMessage(final PacketChangeForme message, final MessageContext ctx)
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
        entityId = buf.readInt();
        forme = Database.getEntry(buffer.readString(20));
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        PacketBuffer buffer = new PacketBuffer(buf);
        buffer.writeInt(entityId);
        if (forme != null) buffer.writeString(forme.getName());
        else buffer.writeString("");
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
            player = ctx.getServerHandler().player;
        }
        Entity mob = PokecubeMod.core.getEntityProvider().getEntity(player.getEntityWorld(), message.entityId, true);
        IPokemob pokemob = CapabilityPokemob.getPokemobFor(mob);
        if (pokemob == null) return;

        if (ctx.side == Side.CLIENT)
        {
            pokemob.setPokedexEntry(message.forme);
        }
        else
        {
            if (pokemob.getGeneralState(GeneralStates.EVOLVING)) return;
            boolean hasRing = MegaCapability.canMegaEvolve(player, pokemob);
            if (!hasRing)
            {
                player.sendMessage(
                        new TextComponentTranslation("pokecube.mega.noring", pokemob.getPokemonDisplayName()));
                return;
            }
            PokedexEntry newEntry = pokemob.getPokedexEntry().getEvo(pokemob);
            if (newEntry != null && newEntry.getPokedexNb() == pokemob.getPokedexEntry().getPokedexNb())
            {
                String old = pokemob.getPokemonDisplayName().getFormattedText();
                if (pokemob.getPokedexEntry() == newEntry)
                {
                    ITextComponent mess = CommandTools.makeTranslatedMessage("pokemob.megaevolve.command.revert",
                            "green", old);
                    pokemob.displayMessageToOwner(mess);
                    pokemob.setCombatState(CombatStates.MEGAFORME, false);
                    mess = CommandTools.makeTranslatedMessage("pokemob.megaevolve.revert", "green", old,
                            newEntry.getUnlocalizedName());
                    ICanEvolve.setDelayedMegaEvolve(pokemob, newEntry, mess);
                }
                else
                {
                    ITextComponent mess = CommandTools.makeTranslatedMessage("pokemob.megaevolve.command.evolve",
                            "green", old);
                    pokemob.displayMessageToOwner(mess);
                    mess = CommandTools.makeTranslatedMessage("pokemob.megaevolve.success", "green", old,
                            newEntry.getUnlocalizedName());
                    pokemob.setCombatState(CombatStates.MEGAFORME, true);
                    ICanEvolve.setDelayedMegaEvolve(pokemob, newEntry, mess);
                }
            }
            else
            {
                if (pokemob.getCombatState(CombatStates.MEGAFORME))
                {
                    String old = pokemob.getPokemonDisplayName().getFormattedText();
                    ITextComponent mess = CommandTools.makeTranslatedMessage("pokemob.megaevolve.command.revert",
                            "green", old);
                    pokemob.displayMessageToOwner(mess);
                    newEntry = pokemob.getPokedexEntry().getBaseForme();
                    pokemob.setCombatState(CombatStates.MEGAFORME, false);
                    mess = CommandTools.makeTranslatedMessage("pokemob.megaevolve.revert", "green", old,
                            newEntry.getUnlocalizedName());
                    ICanEvolve.setDelayedMegaEvolve(pokemob, newEntry, mess);
                }
                else
                {
                    player.sendMessage(CommandTools.makeTranslatedMessage("pokemob.megaevolve.failed", "red",
                            pokemob.getPokemonDisplayName()));
                }
            }
        }
    }
}
