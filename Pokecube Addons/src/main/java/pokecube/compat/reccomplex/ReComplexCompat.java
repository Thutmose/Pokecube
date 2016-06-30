package pokecube.compat.reccomplex;

import net.minecraftforge.common.MinecraftForge;

public class ReComplexCompat {
	
	
	public static void register()
	{
		ReComplexCompat compat = new ReComplexCompat();
		MinecraftForge.EVENT_BUS.register(compat);
	}
//	
//	@SubscribeEvent
//	public void gen(StructureGenerationEventLite evt)
//	{
//		System.out.println(evt.structureName);
//		if(evt instanceof StructureGenerationEventLite.Suggest)
//			return;
//		
//		if(evt instanceof StructureGenerationEventLite.Post)
//		{
//			Vector3 pos = Vector3.getNewVector().set(evt.coordinates);
//			AxisAlignedBB box = pos.addTo(evt.size[0]/2,evt.size[1]/2,evt.size[2]/2).getAABB().expand(evt.size[0]/2,evt.size[1]/2,evt.size[2]/2);
//			List entities = evt.world.getEntitiesWithinAABB(Entity.class, box);
//			if(entities!=null && !entities.isEmpty())
//			{
//				for(Object o: entities)
//				{
//					System.out.println(o);
//					if(o instanceof EntityLiving)
//					{
//						EntityLiving v = (EntityLiving) o;
//						
//						pos.set(v);
//						ChunkCoordinates loc = null;
//						boolean hasGuardAI = false;
//						TimePeriod time = new TimePeriod(0.00, 0.5);
//						for(Object o2: v.tasks.taskEntries)
//						{
//							EntityAITaskEntry taskEntry = (EntityAITaskEntry)o2;
//							if( taskEntry.action instanceof GuardAI )
//							{
//								v.tasks.removeTask(taskEntry.action);
//								loc = ((GuardAI)taskEntry.action).pos;
//								time = ((GuardAI)taskEntry.action).guardPeriod;
//								hasGuardAI = true;
//								break;
//							}
//						}
//						if(hasGuardAI)
//						{
//							System.out.println(loc+" "+pos);
//							GuardAI ai;
//							v.tasks.addTask(2,ai = new GuardAI(v, new ChunkCoordinates(pos.intX(), pos.intY(), pos.intZ()),
//									1.0f, 48.0f, time, false));
//							GuardAIProperties props = new GuardAIProperties();
//							props.init(v, v.getEntityWorld());
//							NBTTagCompound nbt = new NBTTagCompound();
//							v.writeToNBT(nbt);
//							props.saveNBTData(nbt);
//							v.readFromNBT(nbt);
//						}
//						
//					}
//					if(o instanceof EntityTrainer)
//					{
//						EntityTrainer trainer = (EntityTrainer) o;
//						if(trainer.getShouldRandomize())
//						{
//							randomizeTrainerTeam(trainer);
//						}
//					}
//				}
//			}
//			Matrix3.freeAABB(box);
//			
//			pos.freeVectorFromPool();
//			return;
//		}
//		
//		if(!biomeMap.containsKey(evt.structureName.toLowerCase()))
//		{
//			return;
//		}
//		
//		int biome = biomeMap.get(evt.structureName.toLowerCase());
//		Vector3 pos = Vector3.getNewVector().set(evt.coordinates);
//		System.out.println("Setting "+evt.structureName+" as biome type "+BiomeDatabase.getReadableNameFromType(biome));
//		for(int i = 0; i<evt.size[0]; i++)
//		{
//			for(int j = 0; j< evt.size[1]; j++)
//			{
//				for(int k = 0; k<evt.size[2]; k++)
//				{
//					pos.set(evt.coordinates);
//					TerrainManager.getInstance().getTerrian(evt.world, pos.addTo(i, j, k)).setBiome(pos, biome);
//				}
//			}
//		}
//		pos.freeVectorFromPool();
//	}
//	
//	public static void randomizeTrainerTeam(EntityTrainer trainer)
//	{
////		if(trainer instanceof EntityLeader)
////		{
////			
////		}
////		else
//		{
//			Vector3 loc = Vector3.getNewVector().set(trainer);
//			int maxXp = SpawnHandler.getSpawnXp( trainer.getEntityWorld(), loc, Database.getEntry(1));
//			trainer.initTrainer(trainer.getType(), maxXp);
//			System.out.println("Randomized "+trainer.name);
//			loc.freeVectorFromPool();
//		}
//	}
//	
}
