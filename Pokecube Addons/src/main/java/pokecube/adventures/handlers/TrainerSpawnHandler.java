package pokecube.adventures.handlers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.nfunk.jep.JEP;

import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityEvent.EntityConstructing;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.Type;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.adventures.entity.trainers.EntityTrainer;
import pokecube.adventures.entity.trainers.TypeTrainer;
import pokecube.core.ai.properties.GuardAIProperties;
import pokecube.core.database.Database;
import pokecube.core.events.handlers.SpawnHandler;
import pokecube.core.utils.ChunkCoordinate;
import thut.api.maths.Vector3;
import thut.api.terrain.BiomeDatabase;
import thut.api.terrain.TerrainManager;

public class TrainerSpawnHandler 
{
	public static int trainerBox = 64;
	
	private static TrainerSpawnHandler instance;
	Vector3 v = Vector3.getNewVectorFromPool(), v1 = Vector3.getNewVectorFromPool(), v2 = Vector3.getNewVectorFromPool();
	
	public TrainerSpawnHandler()
	{
		FMLCommonHandler.instance().bus().register(this);
		MinecraftForge.EVENT_BUS.register(this);
		instance = this;
	}
	
	public static TrainerSpawnHandler getInstance()
	{
		return instance;
	}
	public static HashSet trainers = new HashSet();

	public static boolean addTrainerCoord(Entity e)
	{
		int x = (int) e.posX;
		int y = (int) e.posY;
		int z = (int) e.posZ;
		int dim = e.dimension;
		return addTrainerCoord(x, y, z, dim);
	}
	
	public static boolean addTrainerCoord(int x, int y, int z, int dim) {
		ChunkCoordinate coord = new ChunkCoordinate(x, y, z, dim);
		if(trainers.contains(coord))
			return false;
		
		return trainers.add(coord);
	}

	public static boolean removeTrainerCoord(int x, int y, int z,
			int dim) {
		ChunkCoordinate coord = new ChunkCoordinate(x, y, z, dim);
		return trainers.remove(coord);
	}
	
	public static int countTrainersInArea(World world, int chunkPosX,
			int chunkPosY, int chunkPosZ) {
		int tolerance = trainerBox;

		int ret = 0;
		for (Object o : trainers) {
			ChunkCoordinate coord = (ChunkCoordinate) o;
			if (	   chunkPosX >= coord.getX() - tolerance
					&& chunkPosZ >= coord.getZ() - tolerance
					&& chunkPosY >= coord.getY() - tolerance
					&& chunkPosY <= coord.getY() + tolerance
					&& chunkPosX <= coord.getX() + tolerance
					&& chunkPosZ <= coord.getZ() + tolerance
					&& world.provider.getDimensionId() == coord.dim) {
				ret++;
			}
		}
		return ret;
	}
	
	@SubscribeEvent
	public void onEntityConstructing(EntityConstructing event)
	{
		if( event.entity instanceof EntityVillager )
		{
			EntityVillager villager = (EntityVillager)event.entity;
			if(null == GuardAIProperties.get(villager) )
			{
				GuardAIProperties.register(villager);
			}
		}
	}
	
	@SubscribeEvent
	public void tickEvent(WorldTickEvent evt)
	{
		if(ConfigHandler.trainerSpawn && evt.phase == Phase.END && evt.type != Type.CLIENT && evt.side != Side.CLIENT && Math.random()>0.999)
		{
			long time = System.nanoTime();
			tick(evt.world);
			double dt = (System.nanoTime() - time)/1000000D;
			if(dt>50)
				System.err.println(FMLCommonHandler.instance().getEffectiveSide()+"Trainer Spawn Tick took "+dt+"ms");
		}
	}
	
	JEP parser = new JEP();
	public void tick(World w)
	{
		if(w.isRemote)
		{
			return;
		}
		ArrayList<Object> players = new ArrayList<Object>();
		players.addAll(w.playerEntities);
		Collections.shuffle(players);
		if(players.size()<1) return;
	//	for(Object o: w.playerEntities)
		{
			EntityPlayer p = (EntityPlayer) players.get(0);
			Vector3 v = SpawnHandler.getRandomSpawningPointNearEntity(w, p, trainerBox);
			if(v==null) return;
			if(v.y<0)
				v.y = v.getMaxY(w);
			Vector3 temp = v.getNextSurfacePoint2(w, v, v1.set(0, -1, 0), 4);
			v = temp!=null?temp.offset(EnumFacing.UP):v;
			
			if(!SpawnHandler.checkNoSpawnerInArea(w, v.intX(), v.intY(), v.intZ()))
				return;
			int count = countTrainersInArea(w, v.intX(), v.intY(), v.intZ());
		//	System.out.println(count+" "+trainers);
			if(count<2)
			{


				TypeTrainer ttype;
				Material m = v.getBlockMaterial(w);
				String biome = BiomeDatabase.getNameFromType(TerrainManager.getInstance().getTerrian(w, v).getBiome(v));
				
				List<TypeTrainer> trainers = TypeTrainer.biomes.get(biome);

				if(trainers==null||trainers.size()==0)
					return;

				Collections.shuffle(trainers);
				ttype = trainers.get(0);

				int counts = 0;
				
				if(m!=ttype.material)
				{
					for(TypeTrainer b: trainers)
					{
						if(b.material == m )
						{
							ttype = b;
							if((m==b.material))
								break;
						}
					}
				}

				if(m!=ttype.material)
				{
					return;
				}

				int maxXp = SpawnHandler.getSpawnXp( w, v, Database.getEntry(1));
		    	long time = System.nanoTime();
				EntityTrainer t = new EntityTrainer(w, ttype, maxXp);

		        
		    	double dt = (System.nanoTime() - time)/1000000D;
		    	if(dt>20)
		    		System.err.println(FMLCommonHandler.instance().getEffectiveSide()+" Trainer "+ttype.name+" "+dt+"ms ");
				v.offset(EnumFacing.UP).moveEntity(t);
				if(t.countPokemon()>0 && SpawnHandler.checkNoSpawnerInArea(w, (int)t.posX, (int)t.posY, (int)t.posZ))
				{
					addTrainerCoord(t);
					w.spawnEntityInWorld(t);
					
				}
				else
					t.setDead();
			}
		}
	}
}
