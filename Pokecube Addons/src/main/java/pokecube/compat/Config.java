package pokecube.compat;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import pokecube.compat.blocks.rf.TileEntitySiphon;
import pokecube.core.PokecubeItems;
import pokecube.core.interfaces.PokecubeMod;
import thut.api.terrain.BiomeType;

public class Config {

    public static Map<String, Integer> biomeMap = new HashMap<String, Integer>();
    
	String[] biomes = {
			"meteorsite:"+BiomeType.METEOR.name,
			"smallfortruins:"+BiomeType.RUIN.name,
			"VillageForgeLarge:"+BiomeType.INDUSTRIAL.name,
			"VillageGuardTower:"+BiomeType.VILLAGE.name,
			"VillageInn:"+BiomeType.VILLAGE.name,
			"VillageHouseRich:"+BiomeType.VILLAGE.name,
			"VillageHouseRich1:"+BiomeType.VILLAGE.name,
			"villagewoodmill:"+BiomeType.INDUSTRIAL.name,
			"ClayMound:mound",
			"BigPyramid:"+BiomeType.RUIN.name,
			"DesertFort:"+BiomeType.VILLAGE.name,
			"DesertHut:"+BiomeType.VILLAGE.name,
			"DesertWatchtower:"+BiomeType.RUIN.name,
			"ElvenPond:"+BiomeType.LAKE.name,
			"ForestBeacon:"+BiomeType.RUIN.name,
			"HillAltarHouse:"+BiomeType.RUIN.name,
			"JokerTower:"+BiomeType.RUIN.name,
			"OldWatchtower:"+BiomeType.RUIN.name,
			"PeacefulCrypt:"+BiomeType.RUIN.name,
			"PirateHideout:"+BiomeType.VILLAGE.name,
			"ShrineSmallAir:"+BiomeType.RUIN.name,
			"ShrineSmallEarth:"+BiomeType.RUIN.name,
			"ShrineSmallWater:"+BiomeType.RUIN.name,
			"ShrineSmallFire:"+BiomeType.RUIN.name,
			"SmallAbandonedMine:"+BiomeType.RUIN.name,
			"SmallFortRuins:"+BiomeType.RUIN.name,
			"SmallPyramid:"+BiomeType.RUIN.name,
			"SmallWoodenCottage:"+BiomeType.VILLAGE.name,
			"SmallWoodenCottage1:"+BiomeType.VILLAGE.name,
			"SmallWoodenCottage2:"+BiomeType.VILLAGE.name,
			"TribalJungleHead:"+BiomeType.RUIN.name,
			"TemplePyramid:"+BiomeType.RUIN.name,
			"veldtbath:"+BiomeType.VILLAGE.name,
			"powerplant:"+BiomeType.INDUSTRIAL.name
	};
	
	String[] ranchables = {
			"arceus:nether_star:100000",
			"chinchou:glowstone_dust:500",
			"lanturn:glowstone_dust,2:500",
			"lotad:waterlily:100",
			"tangela:vine:100",
			"bulbasaur:vine:100",
			"octillery:dye:100",
			"camerupt::lava:1000"
	};
	
	public static int[] dimensionBlackList;
	public static String RFLevelFunction = "";
	
	public Config(FMLPreInitializationEvent e) {
		loadConfig(e);
	}

	void loadConfig(FMLPreInitializationEvent e)
	{
		Configuration config = PokecubeMod.core.getPokecubeConfig(e);
		config.load();

 	   String[] defaults = config.get(Configuration.CATEGORY_GENERAL, "subBiomeMapping", 
			   biomes, "mappings of Recurrent Complex structures to sub biomes").getStringList();
		for(String s: defaults)
		{
			if(s!=null && !s.isEmpty())
			{
				String[] args = s.split(":");
				String key = args[0].toLowerCase().replace(".tml", "");
				String subbiome = args[1];				
				biomeMap.put(key, BiomeType.getBiome(subbiome).getType());
			}
		}
 	   ranchables = config.get(Configuration.CATEGORY_GENERAL, "MFRRanchables", 
			   ranchables, "ranchable pokemon via Minefactory Reloaded.\nnote that the double : in camerupt makes it give lava as a fluid, not an item.  \nformat: pokemon:<item>:delay or pokemon:<item>:fluid:delay, fluids are always 1 bucket \n<item> can be: itemname or itemname,number or itemname,number,meta").getStringList();
 	   
 	   dimensionBlackList = config.get(Configuration.CATEGORY_GENERAL, "dimensionBlackList", new int[]{}).getIntList();

       TileEntitySiphon.maxOutput = config.get(Configuration.CATEGORY_GENERAL, "maxrf", 256, "maximum RF/t of an RFSiphon").getInt();
       TileEntitySiphon.function = config.get(Configuration.CATEGORY_GENERAL, "powerfunction", "a*x/10", "Function of power and level to RF output, a = maximum offensive stat, x = level").getString();

 	   config.save();
	}
	
	void postInit()
	{
		processRanchables(ranchables);
	}
	
	void processRanchables(String[] list)
	{
//		for(String s: list)
//		{
//			if(s!=null && !s.isEmpty())
//			{
//				String[] args = s.split(":");
//				String name = args[0];
//				PokedexEntry entry = Database.getEntry(name);
//				if(entry==null)
//					continue;
//				//only item
//				if(args.length==3)
//				{
//					String stack = args[1];
//					int delay = Integer.parseInt(args[2].trim());
//					MFRCompat.ranchables.add(Ranchables.makeRanchable(entry, parseItemStack(stack), null, delay));
//				}
//				else if(args.length==4)//has fluid
//				{
//					String stack = args[1];
//					String fluid = args[2];
//					int delay = Integer.parseInt(args[3].trim());
//					MFRCompat.ranchables.add(Ranchables.makeRanchable(entry, parseItemStack(stack), getFluid(fluid), delay));
//				}
//			}
//		}
	}
	protected FluidStack getFluid(String toParse)
	{
		return FluidRegistry.getFluidStack(toParse, FluidContainerRegistry.BUCKET_VOLUME);
	}
	
	protected ItemStack parseItemStack(String toParse)
	{
		String[] drop = toParse.split(",");
		int count = 1;
		String name = drop[0];
		int meta = 0;
		try {
			if(drop.length>1)
				count = Integer.parseInt(drop[1]);
			if(drop.length>2)
				meta = Integer.parseInt(drop[2]);
		} catch (NumberFormatException e) {
			
		}
		
		Item item = PokecubeItems.getItem(name);
		ItemStack stack = PokecubeItems.getStack(name);
		ItemStack toAdd;
		if(item==null && stack==null)
		{
			return null;
		}
		if(item!=null)
		{
			toAdd = new ItemStack(item, count, meta);
		}
		else
		{
			toAdd = stack;
			toAdd.stackSize = count;
		}
		return toAdd;
	}
	
}
