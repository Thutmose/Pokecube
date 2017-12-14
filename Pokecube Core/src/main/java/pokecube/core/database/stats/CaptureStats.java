package pokecube.core.database.stats;

import java.util.Map;
import java.util.UUID;

import pokecube.core.database.PokedexEntry;
import pokecube.core.utils.PokeType;

public class CaptureStats
{

    public static int getNumberUniqueCaughtBy(UUID playerID)
    {
        int count = 0;
        Map<PokedexEntry, Integer> map = StatsCollector.getCaptures(playerID);
        if (map == null) return 0;
        count += map.size();
        return count;
    }

    public static int getTotalNumberCaughtBy(UUID playerID)
    {
        int count = 0;
        Map<PokedexEntry, Integer> map = StatsCollector.getCaptures(playerID);
        if (map == null) return 0;
        for (Integer i : map.values())
        {
            count += i;
        }
        return count;
    }

    public static int getTotalNumberOfPokemobCaughtBy(UUID playerID, PokedexEntry type)
    {
        int count = 0;
        Map<PokedexEntry, Integer> map = StatsCollector.getCaptures(playerID);
        if (map == null) return 0;
        if (map.containsKey(type))
        {
            count += map.get(type);
        }
        return count;
    }

    public static int getTotalOfTypeCaughtBy(UUID player, PokeType type)
    {
        int count = 0;
        for (PokedexEntry dbe : StatsCollector.getCaptures(player).keySet())
        {
            if (dbe.isType(type)) count += StatsCollector.getCaptures(player).get(dbe);
        }
        return count;
    }

    public static int getUniqueOfTypeCaughtBy(UUID player, PokeType type)
    {
        int count = 0;
        for (PokedexEntry dbe : StatsCollector.getCaptures(player).keySet())
        {
            if (dbe.isType(type)) count++;
        }
        return count;
    }
}
