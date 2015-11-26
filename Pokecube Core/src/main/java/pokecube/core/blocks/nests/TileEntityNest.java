package pokecube.core.blocks.nests;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.EnumDifficulty;
import net.minecraftforge.common.MinecraftForge;
import pokecube.core.Mod_Pokecube_Helper;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.PokedexEntry.SpawnData;
import pokecube.core.events.EggEvent;
import pokecube.core.events.handlers.SpawnHandler;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.EntityPokemobEgg;
import pokecube.core.items.ItemPokemobEgg;
import thut.api.maths.Matrix3;
import thut.api.maths.Vector3;
import thut.api.terrain.TerrainManager;
import thut.api.terrain.TerrainSegment;

public class TileEntityNest extends TileEntity  implements ITickable{

	int pokedexNb = 0;

	HashSet<IPokemob> residents = new HashSet();
	int time = 0;
	@Override
	public void update() {
		time++;
        int power = worldObj.getRedstonePower(getPos(), EnumFacing.DOWN);//.getBlockPowerInput(xCoord, yCoord, zCoord);
		
		if (worldObj.isRemote || (worldObj.getDifficulty() == EnumDifficulty.PEACEFUL && power==0))
			return;
		
		
		if(worldObj.getClosestPlayer(getPos().getX(),getPos().getY(),getPos().getZ(), Mod_Pokecube_Helper.mobDespawnRadius)==null)
			return;
		
		if (pokedexNb == 0 && time >=200)
		{
			time = 0;
			init();
		}
		if (pokedexNb == 0)
			return;
		int num = 3;
		PokedexEntry entry = Database.getEntry(pokedexNb);
		
		SpawnData data = entry.getSpawnData();
		if(data!=null)
		{
			Vector3 here = Vector3.getNewVectorFromPool().set(this);

			TerrainSegment t = TerrainManager.getInstance().getTerrian(worldObj, here);
			int b = t.getBiome(here);
			int min = data.getMin(b); 
			num = min + worldObj.rand.nextInt(data.getMax(b) - min + 1);
			here.freeVectorFromPool();
		}
		//System.out.println("tick");
		if (residents.size() < num && time > 200+worldObj.rand.nextInt(2000)) {
			time = 0;
			Vector3 here = Vector3.getNewVectorFromPool().set(this);
			AxisAlignedBB aabb = here.getAABB().expand(16, 16, 16);
			List<IPokemob> list = worldObj
					.getEntitiesWithinAABB(PokecubeMod.core.getEntityClassFromPokedexNumber(pokedexNb), aabb);
			//System.out.println("checking to make egg");
			ItemStack eggItem = ItemPokemobEgg.getEggStack(pokedexNb);

			NBTTagCompound nbt = new NBTTagCompound();
			nbt.setIntArray("nestLocation", new int[] { getPos().getX(), getPos().getY(), getPos().getZ() });
			eggItem.setTagCompound(nbt);
			Random rand = new Random();
			EntityPokemobEgg egg = new EntityPokemobEgg(worldObj, getPos().getX() + rand.nextGaussian(), getPos().getY() + 1,
					getPos().getZ() + rand.nextGaussian(), eggItem, null);
			EggEvent.Lay event = new EggEvent.Lay(egg);
			MinecraftForge.EVENT_BUS.post(event);
			if (!event.isCanceled()) {
				worldObj.spawnEntityInWorld(egg);
			}
			Matrix3.freeAABB(aabb);
			here.freeVectorFromPool();
		}
	}

	public void addResident(IPokemob resident) {
		residents.add(resident);
	}

	public void removeResident(IPokemob resident) {
		residents.remove(resident);
	}

	public void init() {
		Vector3 here = Vector3.getNewVectorFromPool().set(this);

		TerrainSegment t = TerrainManager.getInstance().getTerrian(worldObj, here);
		t.refresh(worldObj);
		t.checkIndustrial(worldObj);
		int b = t.getBiome(here);
	//	System.out.println("init");
		if (SpawnHandler.spawns.containsKey(b)) {
			ArrayList<PokedexEntry> entries = SpawnHandler.spawns.get(b);
			if (entries.isEmpty()) {
				SpawnHandler.spawns.remove(b);
			}
			Collections.shuffle(entries);
			int index = 0;
			while (pokedexNb == 0 && index < 2*entries.size()) {
				PokedexEntry dbe = entries.get((index++) % entries.size());
				float weight = dbe.getSpawnData().getWeight(b);
				if (Math.random() > weight)
					continue;
				if(!(!SpawnHandler.canSpawn(t, dbe.getSpawnData(), here.set(this).offsetBy(EnumFacing.UP).offsetBy(EnumFacing.UP), worldObj))) continue;
				if (!SpawnHandler.isPointValidForSpawn(worldObj, here, dbe)) continue;
				
				pokedexNb = dbe.getPokedexNb();
			}
			//System.out.println("Set Spawn Pokemon to " + Database.getEntry(pokedexNb));
		}
//		if(pokedexNb==0)
//			worldObj.setBlock(xCoord, yCoord, zCoord, worldObj.getBlock(xCoord, yCoord - 1, zCoord));

		here.freeVectorFromPool();
	}

	/**
	 * Reads a tile entity from NBT.
	 */
	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		pokedexNb = nbt.getInteger("pokedexNb");
		time = nbt.getInteger("time");
	}

	/**
	 * Writes a tile entity to NBT.
	 */
	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setInteger("pokedexNb", pokedexNb);
		nbt.setInteger("time", time);
	}
	
    @Override
    public void validate() {
    	super.validate();
    	
    	addForbiddenSpawningCoord();
    }
    
    @Override
    public void invalidate() {
    	super.invalidate();
		pokedexNb = 0;
    	removeForbiddenSpawningCoord();
    }
	
    public boolean addForbiddenSpawningCoord(){
    	return SpawnHandler.addForbiddenSpawningCoord(getPos(), worldObj.provider.getDimensionId(), 10);
    }
    
    public boolean removeForbiddenSpawningCoord(){
    	return SpawnHandler.removeForbiddenSpawningCoord(getPos(), worldObj.provider.getDimensionId());
    }
}
