package pokecube.core.database.stats;

import java.util.HashMap;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.items.pokemobeggs.EntityPokemobEgg;

/**
 * 
 * @author Thutmose
 *
 */
public class StatsCollector 
{
	public static HashMap<String, HashMap<PokedexEntry, Integer>> playerCaptures = new HashMap<String, HashMap<PokedexEntry,Integer>>();
	public static HashMap<String, HashMap<PokedexEntry, Integer>> playerKills = new HashMap<String, HashMap<PokedexEntry,Integer>>();
	public static HashMap<String, HashMap<PokedexEntry, Integer>> eggsHatched = new HashMap<String, HashMap<PokedexEntry,Integer>>();
	
	public static void addCapture(IPokemob captured)
	{
		String owner;
        if(captured.getPokemonOwner() instanceof EntityPlayer )
            owner = captured.getPokemonOwner().getUniqueID().toString();
        else
            owner = new UUID(1234, 4321).toString();
		HashMap<PokedexEntry, Integer> map = playerCaptures.get(owner);
		PokedexEntry dbe = Database.getEntry(captured);
		int current = 1;
		
		assert dbe!=null;
		
		if(map==null)
		{
			map = new HashMap<PokedexEntry, Integer>();
			playerCaptures.put(owner, map);
		}
		else if(map.containsKey(dbe))
		{
			current += map.get(dbe);
		}
		playerCaptures.get(owner).put(dbe, current);
	}
	
	public static void addHatched(EntityPokemobEgg hatched)
	{
		String owner;
		if(hatched.getEggOwner() instanceof EntityPlayer )
			owner = hatched.getEggOwner().getUniqueID().toString();
		else
			owner = new UUID(1234, 4321).toString();
		
		assert hatched.getPokemob()!=null;
			
		HashMap<PokedexEntry, Integer> map = eggsHatched.get(owner);
		if(hatched.getPokemob()==null)
		{
		    new Exception().printStackTrace();
		    return;
		}
		
		PokedexEntry dbe = Database.getEntry(hatched.getPokemob());
		int current = 1;
		
		assert dbe!=null;
		
		if(map==null)
		{
			map = new HashMap<PokedexEntry, Integer>();
			eggsHatched.put(owner, map);
		}
		else if(map.containsKey(dbe))
		{
			current += map.get(dbe);
		}
		eggsHatched.get(owner).put(dbe, current);
	}

	public static void addKill(IPokemob killed, IPokemob killer)
	{
		
		if(killer==null||killed==null) return;

        String owner;
        if(killer.getPokemonOwner() instanceof EntityPlayer )
            owner = killer.getPokemonOwner().getUniqueID().toString();
        else
            owner = new UUID(1234, 4321).toString();
		HashMap<PokedexEntry, Integer> map = playerKills.get(owner);
		PokedexEntry dbe = Database.getEntry(killed);
		int current = 1;
		
		assert dbe!=null;
		
		if(map==null)
		{
			map = new HashMap<PokedexEntry, Integer>();
			playerKills.put(owner, map);
		}
		else if(map.containsKey(dbe))
		{
			current += map.get(dbe);
		}

		playerKills.get(owner).put(dbe, current);
	}
	
	public static void readFromNBT(NBTTagCompound nbt)
	{
		
		NBTBase temp = nbt.getTag("kills");
		if(temp instanceof NBTTagList)
		{
			NBTTagList list = (NBTTagList) temp;
			for(int i = 0; i<list.tagCount(); i++)
			{
				NBTTagCompound kills = list.getCompoundTagAt(i);
				String s = kills.getString("username");
				for(PokedexEntry dbe : Database.data.values())
				{
					int count = kills.getInteger(dbe.getName());
					if(count!=0)
					{
						setKills(dbe, s, count);
					}
				}
			}
		}
		
		temp = nbt.getTag("captures");
		if(temp instanceof NBTTagList)
		{
			NBTTagList list = (NBTTagList) temp;
			for(int i = 0; i<list.tagCount(); i++)
			{
				NBTTagCompound captures = list.getCompoundTagAt(i);
				String s = captures.getString("username");
				for(PokedexEntry dbe : Database.data.values())
				{
					int count = captures.getInteger(dbe.getName());
					if(count!=0)
					{
						setCaptures(dbe, s, count);
					}
				}
			}
		}
		
		temp = nbt.getTag("hatches");
		if(temp instanceof NBTTagList)
		{
			NBTTagList list = (NBTTagList) temp;
			for(int i = 0; i<list.tagCount(); i++)
			{
				NBTTagCompound captures = list.getCompoundTagAt(i);
				String s = captures.getString("username");
				for(PokedexEntry dbe : Database.data.values())
				{
					int count = captures.getInteger(dbe.getName());
					if(count!=0)
					{
						setHatches(dbe, s, count);
					}
				}
			}
		}
	}
	
	
	public static void setCaptures(PokedexEntry dbe, String owner, int count)
	{
		if(owner.equals(""))
			owner = new UUID(1234, 4321).toString();
		HashMap<PokedexEntry, Integer> map = playerCaptures.get(owner);
		if(map==null)
		{
			map = new HashMap<PokedexEntry, Integer>();
			playerCaptures.put(owner, map);
		}
		map.put(dbe, count);
	}
	
	public static void setHatches(PokedexEntry dbe, String owner, int count)
	{
		if(owner.equals(""))
			owner = new UUID(1234, 4321).toString();
		HashMap<PokedexEntry, Integer> map = eggsHatched.get(owner);
		if(map==null)
		{
			map = new HashMap<PokedexEntry, Integer>();
			eggsHatched.put(owner, map);
		}
		map.put(dbe, count);
	}
	
	public static void setKills(PokedexEntry dbe, String owner, int count)
	{
		if(owner.equals(""))
			owner = new UUID(1234, 4321).toString();
		HashMap<PokedexEntry, Integer> map = playerKills.get(owner);
		if(map==null)
		{
			map = new HashMap<PokedexEntry, Integer>();
			playerKills.put(owner, map);
		}
		map.put(dbe, count);
	}
	
	public static void writeToNBT(NBTTagCompound nbt)
	{
		NBTTagList playerKillTag = new NBTTagList();
		NBTTagList playerCaptureTag = new NBTTagList();
		NBTTagList playerEggTag = new NBTTagList();
		
		for(String s: playerKills.keySet())
		{
			NBTTagCompound tag = new NBTTagCompound();
			tag.setString("username", s);
			for(PokedexEntry dbe: playerKills.get(s).keySet())
			{
				tag.setInteger(dbe.getName(), playerKills.get(s).get(dbe));
			}
			playerKillTag.appendTag(tag);
		}
		
		nbt.setTag("kills", playerKillTag);
		
		for(String s: playerCaptures.keySet())
		{
			NBTTagCompound tag = new NBTTagCompound();
			tag.setString("username", s);
			for(PokedexEntry dbe: playerCaptures.get(s).keySet())
			{
				tag.setInteger(dbe.getName(), playerCaptures.get(s).get(dbe));
			}
			playerCaptureTag.appendTag(tag);
		}
		
		nbt.setTag("captures", playerCaptureTag);
		
		for(String s: eggsHatched.keySet())
		{
			NBTTagCompound tag = new NBTTagCompound();
			tag.setString("username", s);
			for(PokedexEntry dbe: eggsHatched.get(s).keySet())
			{
				tag.setInteger(dbe.getName(), eggsHatched.get(s).get(dbe));
			}
			playerEggTag.appendTag(tag);
		}
		
		nbt.setTag("hatches", playerEggTag);
		
	}
	
	public StatsCollector(){}
	
}
