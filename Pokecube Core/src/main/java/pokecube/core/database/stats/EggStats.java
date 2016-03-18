package pokecube.core.database.stats;

import static pokecube.core.database.stats.StatsCollector.eggsHatched;

import java.util.HashMap;

import pokecube.core.database.PokedexEntry;
import pokecube.core.utils.PokeType;

public class EggStats {
	public static int getNumberUniqueHatched()
	{
		int count = 0;

		for(HashMap<PokedexEntry, Integer> map: eggsHatched.values())
		{
			count += map.size();
		}
		return count;
	}
	public static int getNumberUniqueHatchedBy(String playerName)
	{
		int count = 0;
		HashMap<PokedexEntry, Integer> map = eggsHatched.get(playerName);
		if(map==null) return 0;
		count += map.size();
		return count;
	}
	public static int getTotalNumberHatched()
	{
		int count = 0;
		for(HashMap<PokedexEntry, Integer> map: eggsHatched.values())
		{
			for(Integer i: map.values())
			{
				count += i;
			}
		}
		return count;
	}
	public static int getTotalNumberHatchedBy(String playerName)
	{
		int count = 0;
		HashMap<PokedexEntry, Integer> map = eggsHatched.get(playerName);
		if(map==null) return 0;
		for(Integer i: map.values())
		{
			count += i;
		}
		return count;
	}
	public static int getTotalNumberOfPokemobHatchedBy(String playerName, PokedexEntry type)
	{
		int count = 0;
		HashMap<PokedexEntry, Integer> map = eggsHatched.get(playerName);
		if(map==null) return 0;
		if(map.containsKey(type))
			count += map.get(type);
		return count;
	}
	public static int getTotalOfPokemobHatched(PokedexEntry type)
	{
		int count = 0;
		for(HashMap<PokedexEntry, Integer> map: eggsHatched.values())
		{
			if(map.containsKey(type))
			{
				count += map.get(type);
			}
		}
		return count;
	}
	public static int getTotalOfTypeHatched(PokeType type)
	{
		int count = 0;
		for(HashMap<PokedexEntry, Integer> map: eggsHatched.values())
		{
			for(PokedexEntry dbe: map.keySet())
			{
				if(dbe.isType(type))
					count+=map.get(dbe);
			}
		}
		return count;
	}
	public static int getTotalOfTypeHatchedBy(String player, PokeType type)
	{
		int count = 0;
		if(eggsHatched.containsKey(player))
		for(PokedexEntry dbe: eggsHatched.get(player).keySet())
		{
			if(dbe.isType(type))
				count+=eggsHatched.get(player).get(dbe);
		}
		return count;
	}
}
