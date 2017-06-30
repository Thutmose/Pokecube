package pokecube.core.database.stats;

import java.util.Map;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.util.FakePlayer;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.handlers.PokecubePlayerDataHandler;
import pokecube.core.handlers.playerdata.PokecubePlayerStats;
import pokecube.core.handlers.playerdata.advancements.triggers.Triggers;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.items.pokemobeggs.EntityPokemobEgg;
import pokecube.core.network.packets.PacketDataSync;

/** @author Thutmose */
public class StatsCollector
{
    public static Map<PokedexEntry, Integer> getCaptures(UUID uuid)
    {
        return PokecubePlayerDataHandler.getInstance().getPlayerData(uuid).getData(PokecubePlayerStats.class)
                .getCaptures(uuid);
    }

    public static Map<PokedexEntry, Integer> getKills(UUID uuid)
    {
        return PokecubePlayerDataHandler.getInstance().getPlayerData(uuid).getData(PokecubePlayerStats.class)
                .getKills(uuid);
    }

    public static Map<PokedexEntry, Integer> getHatches(UUID uuid)
    {
        return PokecubePlayerDataHandler.getInstance().getPlayerData(uuid).getData(PokecubePlayerStats.class)
                .getHatches(uuid);
    }

    public static void addCapture(IPokemob captured)
    {
        String owner;
        if (captured.getPokemonOwner() instanceof EntityPlayer && !(captured.getPokemonOwner() instanceof FakePlayer))
        {
            EntityPlayerMP player = (EntityPlayerMP) captured.getPokemonOwner();
            owner = captured.getPokemonOwner().getCachedUniqueIdString();
            PokedexEntry dbe = Database.getEntry(captured);
            PokecubePlayerStats stats = PokecubePlayerDataHandler.getInstance().getPlayerData(owner)
                    .getData(PokecubePlayerStats.class);
            stats.addCapture(captured.getPokemonOwner().getUniqueID(), dbe);
            if (!stats.hasFirst()) stats.setHasFirst(player);
            Triggers.CATCHPOKEMOB.trigger(player, captured);
            PacketDataSync.sendInitPacket(player, stats.getIdentifier());
        }
    }

    public static int getCaptured(PokedexEntry dbe, EntityPlayer player)
    {
        Integer n = PokecubePlayerDataHandler.getInstance().getPlayerData(player).getData(PokecubePlayerStats.class)
                .getCaptures(player.getUniqueID()).get(dbe);
        return n == null ? 0 : n;
    }

    public static int getKilled(PokedexEntry dbe, EntityPlayer player)
    {
        Integer n = PokecubePlayerDataHandler.getInstance().getPlayerData(player).getData(PokecubePlayerStats.class)
                .getKills(player.getUniqueID()).get(dbe);
        return n == null ? 0 : n;
    }

    public static int getHatched(PokedexEntry dbe, EntityPlayer player)
    {
        Integer n = PokecubePlayerDataHandler.getInstance().getPlayerData(player).getData(PokecubePlayerStats.class)
                .getHatches(player.getUniqueID()).get(dbe);
        return n == null ? 0 : n;
    }

    public static void addHatched(EntityPokemobEgg hatched)
    {
        String owner;
        IPokemob mob = null;
        if (hatched.getEggOwner() instanceof EntityPlayer && !(hatched.getEggOwner() instanceof FakePlayer))
        {
            owner = hatched.getEggOwner().getCachedUniqueIdString();
            mob = hatched.getPokemob(true);
            if (mob == null)
            {
                new Exception().printStackTrace();
                return;
            }
            PokedexEntry dbe = Database.getEntry(mob);
            PokecubePlayerStats stats = PokecubePlayerDataHandler.getInstance().getPlayerData(owner)
                    .getData(PokecubePlayerStats.class);
            stats.addHatch(hatched.getEggOwner().getUniqueID(), dbe);
            Triggers.HATCHPOKEMOB.trigger((EntityPlayerMP) hatched.getEggOwner(), mob);
            PacketDataSync.sendInitPacket((EntityPlayerMP) hatched.getEggOwner(), stats.getIdentifier());
        }
    }

    public static void addKill(IPokemob killed, IPokemob killer)
    {
        if (killer == null || killed == null || (killer.getPokemonOwner() instanceof FakePlayer)) return;
        String owner;
        if (killer.getPokemonOwner() instanceof EntityPlayer)
        {
            owner = killer.getPokemonOwner().getCachedUniqueIdString();
            PokedexEntry dbe = Database.getEntry(killed);
            PokecubePlayerStats stats = PokecubePlayerDataHandler.getInstance().getPlayerData(owner)
                    .getData(PokecubePlayerStats.class);
            stats.addKill(killer.getPokemonOwner().getUniqueID(), dbe);
            Triggers.KILLPOKEMOB.trigger((EntityPlayerMP) killer.getPokemonOwner(), killed);
            PacketDataSync.sendInitPacket((EntityPlayerMP) killer.getPokemonOwner(), stats.getIdentifier());
        }
    }

    public StatsCollector()
    {
    }

}
