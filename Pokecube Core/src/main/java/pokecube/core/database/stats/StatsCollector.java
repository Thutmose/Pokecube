package pokecube.core.database.stats;

import java.util.Map;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.util.FakePlayer;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.handlers.PokecubePlayerDataHandler;
import pokecube.core.handlers.playerdata.PokecubePlayerStats;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.items.pokemobeggs.EntityPokemobEgg;

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
            owner = captured.getPokemonOwner().getCachedUniqueIdString();
            PokedexEntry dbe = Database.getEntry(captured);
            PokecubePlayerDataHandler.getInstance().getPlayerData(owner).getData(PokecubePlayerStats.class)
                    .addCapture(captured.getPokemonOwner().getUniqueID(), dbe);
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
            PokecubePlayerDataHandler.getInstance().getPlayerData(owner).getData(PokecubePlayerStats.class)
                    .addHatch(hatched.getEggOwner().getUniqueID(), dbe);
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
            PokecubePlayerDataHandler.getInstance().getPlayerData(owner).getData(PokecubePlayerStats.class)
                    .addKill(killer.getPokemonOwner().getUniqueID(), dbe);
        }
    }

    public StatsCollector()
    {
    }

}
