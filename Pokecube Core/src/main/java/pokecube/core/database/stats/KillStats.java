package pokecube.core.database.stats;

import static pokecube.core.database.stats.StatsCollector.playerKills;

import java.util.HashMap;

import pokecube.core.database.PokedexEntry;
import pokecube.core.utils.PokeType;

public class KillStats {

	public static int getNumberUniqueKilled()
	{
		int count = 0;

		for(HashMap<PokedexEntry, Integer> map: playerKills.values())
		{
			count += map.size();
		}
		return count;
	}
	public static int getNumberUniqueKilledBy(String playerName)
	{
		int count = 0;
		HashMap<PokedexEntry, Integer> map = playerKills.get(playerName);
		if(map==null) return 0;
		count += map.size();
		return count;
	}
	public static int getTotalNumberKilled()
	{
		int count = 0;
		for(HashMap<PokedexEntry, Integer> map: playerKills.values())
		{
			for(Integer i: map.values())
			{
				count += i;
			}
		}
		return count;
	}
	public static int getTotalNumberKilledBy(String playerName)
	{
		int count = 0;
		HashMap<PokedexEntry, Integer> map = playerKills.get(playerName);
		if(map==null) return 0;
		for(Integer i: map.values())
		{
			count += i;
		}
		return count;
	}
	public static int getTotalNumberOfPokemobKilledBy(String playerName, PokedexEntry type)
	{
		int count = 0;
		HashMap<PokedexEntry, Integer> map = playerKills.get(playerName);
		if(map==null) return 0;
		if(map.containsKey(type))
			count += map.get(type);
		return count;
	}
	public static int getTotalOfPokemobKilled(PokedexEntry type)
	{
		int count = 0;
		for(HashMap<PokedexEntry, Integer> map: playerKills.values())
		{
			if(map.containsKey(type))
			{
				count += map.get(type);
			}
		}
		return count;
	}
	public static int getTotalOfTypeKilled(PokeType type)
	{
		int count = 0;
		for(HashMap<PokedexEntry, Integer> map: playerKills.values())
		{
			for(PokedexEntry dbe: map.keySet())
			{
				if(dbe.isType(type))
					count+=map.get(dbe);
			}
		}
		return count;
	}
	public static int getTotalOfTypeKilledBy(String player, PokeType type)
	{
		int count = 0;
		if(playerKills.containsKey(player))
		for(PokedexEntry dbe: playerKills.get(player).keySet())
		{
			if(dbe.isType(type))
				count+=playerKills.get(player).get(dbe);
		}
		return count;
	}
	public static int getTotalUniqueOfTypeKilledBy(String player, PokeType type)
	{
		int count = 0;
		if(playerKills.containsKey(player))
		for(PokedexEntry dbe: playerKills.get(player).keySet())
		{
			if(dbe.isType(type))
				count++;
		}
		return count;
	}
}
