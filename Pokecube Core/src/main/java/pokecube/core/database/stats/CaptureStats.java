package pokecube.core.database.stats;

import static pokecube.core.database.stats.StatsCollector.playerCaptures;

import java.util.HashMap;

import pokecube.core.database.PokedexEntry;
import pokecube.core.utils.PokeType;

public class CaptureStats {
	
	public static int getNumberUniqueCaught()
	{
		int count = 0;

		for(HashMap<PokedexEntry, Integer> map: playerCaptures.values())
		{
			count += map.size();
		}
		return count;
	}
	public static int getNumberUniqueCaughtBy(String playerName)
	{
		int count = 0;
		HashMap<PokedexEntry, Integer> map = playerCaptures.get(playerName);
		if(map==null) return 0;
		count += map.size();
		return count;
	}
	public static int getTotalNumberCaught()
	{
		int count = 0;
		for(HashMap<PokedexEntry, Integer> map: playerCaptures.values())
		{
			for(Integer i: map.values())
			{
				count += i;
			}
		}
		return count;
	}
	public static int getTotalNumberCaughtBy(String playerName)
	{
		int count = 0;
		HashMap<PokedexEntry, Integer> map = playerCaptures.get(playerName);
		if(map==null) return 0;
		for(Integer i: map.values())
		{
			count += i;
		}
		return count;
	}
	public static int getTotalNumberOfPokemobCaughtBy(String playerName, PokedexEntry type)
	{
		int count = 0;
		HashMap<PokedexEntry, Integer> map = playerCaptures.get(playerName);
		if(map==null) return 0;
		if(map.containsKey(type))
		{
			count += map.get(type);
		}
		return count;
	}
	public static int getTotalOfPokemobCaught(PokedexEntry type)
	{
		int count = 0;
		for(HashMap<PokedexEntry, Integer> map: playerCaptures.values())
		{
			if(map.containsKey(type))
			{
				count += map.get(type);
			}
		}
		return count;
	}
	public static int getTotalOfTypeCaught(PokeType type)
	{
		int count = 0;
		for(HashMap<PokedexEntry, Integer> map: playerCaptures.values())
		{
			for(PokedexEntry dbe: map.keySet())
			{
				if(dbe.isType(type))
					count+=map.get(dbe);
			}
		}
		return count;
	}
	public static int getTotalOfTypeCaughtBy(String player, PokeType type)
	{
		int count = 0;
		if(playerCaptures.containsKey(player))
		for(PokedexEntry dbe: playerCaptures.get(player).keySet())
		{
			if(dbe.isType(type))
				count+=playerCaptures.get(player).get(dbe);
		}
		return count;
	}
	public static int getTotalUniqueOfTypeCaughtBy(String player, PokeType type)
	{
		int count = 0;
		if(playerCaptures.containsKey(player))
		for(PokedexEntry dbe: playerCaptures.get(player).keySet())
		{
			if(dbe.isType(type))
				count++;
		}
		return count;
	}
}
