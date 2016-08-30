package pokecube.core.database.stats;

import java.util.Map;

import pokecube.core.database.PokedexEntry;
import pokecube.core.utils.PokeType;

public class EggStats
{
    public static int getNumberUniqueHatchedBy(String playerName)
    {
        int count = 0;
        Map<PokedexEntry, Integer> map = StatsCollector.getHatches(playerName);
        if (map == null) return 0;
        count += map.size();
        return count;
    }

    public static int getTotalNumberHatchedBy(String playerName)
    {
        int count = 0;
        Map<PokedexEntry, Integer> map = StatsCollector.getHatches(playerName);
        if (map == null) return 0;
        for (Integer i : map.values())
        {
            count += i;
        }
        return count;
    }

    public static int getTotalNumberOfPokemobHatchedBy(String playerName, PokedexEntry type)
    {
        int count = 0;
        Map<PokedexEntry, Integer> map = StatsCollector.getHatches(playerName);
        if (map == null) return 0;
        if (map.containsKey(type)) count += map.get(type);
        return count;
    }

    public static int getTotalOfTypeHatchedBy(String player, PokeType type)
    {
        int count = 0;
        for (PokedexEntry dbe : StatsCollector.getHatches(player).keySet())
        {
            if (dbe.isType(type)) count += StatsCollector.getHatches(player).get(dbe);
        }
        return count;
    }
}
