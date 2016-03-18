package pokecube.core.database.stats;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentString;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.moves.teleport.Move_Teleport;
import pokecube.core.utils.PokeType;

public class SpecialCaseRegister 
{
	public static int countSpawnableTypes(PokeType type)
	{
		int ret = 0;
		for(PokedexEntry e: Database.spawnables)
		{
			if(type==null||e.isType(type))
				ret++;
		}
		return ret;
	}
	
	public static ISpecialCaptureCondition getCaptureCondition(String name)
	{
		if(Database.getEntry(name)!=null&&ISpecialCaptureCondition.captureMap.containsKey(Database.getEntry(name).getPokedexNb()))
		{
			return ISpecialCaptureCondition.captureMap.get(Database.getEntry(name).getPokedexNb());
		}
			
		return null;
	}
	
	public static ISpecialSpawnCondition getSpawnCondition(String name)
	{
		if(Database.getEntry(name)!=null&&ISpecialSpawnCondition.spawnMap.containsKey(Database.getEntry(name).getPokedexNb()))
		{
			return ISpecialSpawnCondition.spawnMap.get(Database.getEntry(name).getPokedexNb());
		}
			
		return null;
	}
	
	public static void register()
	{

		ISpecialCaptureCondition mewCondition = new ISpecialCaptureCondition() {
			
			@Override
			public boolean canCapture(Entity trainer) {
				return false;
			}

			@Override
			public boolean canCapture(Entity trainer, IPokemob pokemon) {
	    		int caught = CaptureStats.getNumberUniqueCaughtBy(trainer.getUniqueID().toString());
	    		
	    		if(caught < Database.spawnables.size() - 1)
	    		{
	    			if(trainer instanceof EntityPlayer)
	    				((EntityPlayer)trainer).addChatMessage(new TextComponentString("You do not have enough badges to control Mew!"));
	    			Move_Teleport.teleportRandomly((EntityLivingBase) pokemon);
		    		return false;
	    		}
	    		
				return true;
			}
		};
		
		ISpecialCaptureCondition.captureMap.put(151, mewCondition);
	}
	
	public static void register(String name, ISpecialCaptureCondition condition)
	{
		if(Database.entryExists(name))
		{
			ISpecialCaptureCondition.captureMap.put(Database.getEntry(name).getPokedexNb(), condition);
		}
	}
	
	public static void register(String name, ISpecialSpawnCondition condition)
	{
		if(Database.entryExists(name))
			ISpecialSpawnCondition.spawnMap.put(Database.getEntry(name).getPokedexNb(), condition);
		
	}
}
