package pokecube.core.database.stats;

import java.util.Map;
import java.util.UUID;

import pokecube.core.database.PokedexEntry;
import pokecube.core.utils.PokeType;

public class KillStats
{

    public static int getNumberUniqueKilledBy(UUID playerName)
    {
        int count = 0;
        Map<PokedexEntry, Integer> map = StatsCollector.getKills(playerName);
        if (map == null) return 0;
        count += map.size();
        return count;
    }

    public static int getTotalNumberKilledBy(UUID playerName)
    {
        int count = 0;
        Map<PokedexEntry, Integer> map = StatsCollector.getKills(playerName);
        if (map == null) return 0;
        for (Integer i : map.values())
        {
            count += i;
        }
        return count;
    }

    public static int getTotalNumberOfPokemobKilledBy(UUID playerName, PokedexEntry type)
    {
        int count = 0;
        Map<PokedexEntry, Integer> map = StatsCollector.getKills(playerName);
        if (map == null) return 0;
        if (map.containsKey(type)) count += map.get(type);
        return count;
    }

    public static int getTotalOfTypeKilledBy(UUID player, PokeType type)
    {
        int count = 0;
        for (PokedexEntry dbe : StatsCollector.getKills(player).keySet())
        {
            if (dbe.isType(type)) count += StatsCollector.getKills(player).get(dbe);
        }
        return count;
    }

    public static int getTotalUniqueOfTypeKilledBy(UUID player, PokeType type)
    {
        int count = 0;
        for (PokedexEntry dbe : StatsCollector.getKills(player).keySet())
        {
            if (dbe.isType(type)) count++;
        }
        return count;
    }
}
