package pokecube.core.interfaces;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.mojang.authlib.GameProfile;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.Achievement;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.AchievementPage;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.core.ByteClassLoader;
import pokecube.core.CreativeTabPokecube;
import pokecube.core.CreativeTabPokecubeBerries;
import pokecube.core.CreativeTabPokecubeBlocks;
import pokecube.core.CreativeTabPokecubes;

public abstract class PokecubeMod 
{
    public final static String ID = "pokecube";
    public final static String VERSION = "@VERSION@";
    
    public final static String UPDATEURL = "https://raw.githubusercontent.com/Thutmose/Pokecube/master/Pokecube%20Core/versions.json";
	
    public static final int MAX_DAMAGE = 0x7FFF;
    public static final int FULL_HEALTH = MAX_DAMAGE-1;

    private static HashMap<Integer, FakePlayer> fakePlayers = new HashMap<Integer, FakePlayer>();
	/**
	 * If you are a developer, you can set this flag to true.
	 * Set to false before a build. 
	 */
	public final static boolean debug = false;
	
    public static PokecubeMod core;
    
    public static SimpleNetworkWrapper packetPipeline;
    
    //Manchou mobs are default mobs
    public static String defaultMod = "pokecube_origin";
    
    public static boolean hardMode = false;
    public static boolean semiHardMode = false;
    public static double MAX_DENSITY = 1;
    
    public static Map<String, String> gifts = new HashMap<String, String>();
    public static List<String> giftLocations = new ArrayList<String>();

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static Map<Integer, Class> pokedexmap = new HashMap();
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static Map<Integer, Class> genericMobClasses = new HashMap();
    public static BitSet registered = new BitSet();
    
    public abstract Configuration getPokecubeConfig(FMLPreInitializationEvent evt);
    
    /**
     * Creates a new instance of an entity in the world for 
     * the pokemob specified by its pokedex number.
     * 
     * @param pokedexNb the pokedex number
     * @param world the {@link World} where to spawn
     * @return the {@link Entity} instance or null if a problem occurred
     */
    public abstract Entity createEntityByPokedexNb(int pokedexNb, World world);
    
    public abstract Integer[] getStarters();
    
    public abstract void registerPokemon(boolean createEgg, Object mod, String name);
    /**
     * Registers a Pokemob into the Pokedex.
     * Have a look to the file called <code>"HelpEntityJava.png"</code> provided with the SDK.
     *
     * @param createEgg whether an egg should be created for this species (is a base non legendary pokemob)
     * @param mod the instance of your mod
     * @param pokedexnb the pokedex number
     * 
     */
    public abstract void registerPokemon(boolean createEgg, Object mod, int pokedexNb);
    
    @SuppressWarnings("rawtypes")
    public abstract void registerPokemonByClass(Class clazz, boolean createEgg, Object mod, int pokedexNb);
    
    public static CommonProxy getProxy()
    {
    	return CommonProxy.getClientInstance();
    }
    
    public static CreativeTabs creativeTabPokecube = new CreativeTabPokecube(CreativeTabs.creativeTabArray.length, "Pokecube");
    public static CreativeTabs creativeTabPokecubes = new CreativeTabPokecubes(CreativeTabs.creativeTabArray.length, "Pokecubes");
    public static CreativeTabs creativeTabPokecubeBerries = new CreativeTabPokecubeBerries(CreativeTabs.creativeTabArray.length, "Berries");
    public static CreativeTabs creativeTabPokecubeBlocks = new CreativeTabPokecubeBlocks(CreativeTabs.creativeTabArray.length, "Pokecube Blocks");
    
    @SuppressWarnings("rawtypes")
    public static HashMap pokemobEggs = new HashMap();
    
    //Contains TMs for cut, flash, etc.  These are not infinite uses, as they can be copied by teaching to a pokemob, then
    //placing it in the PC.
    public static ArrayList<ItemStack> HMs = new ArrayList<ItemStack>();
    // Achievements
    public static AchievementPage achievementPagePokecube;
    public static Achievement get1stPokemob;
    public static HashMap<Integer, Achievement> pokemobAchievements;
    public ByteClassLoader loader;
    public ArrayList<Integer> starters = new ArrayList<Integer>();

    public static FakePlayer getFakePlayer()
    {
    	return getFakePlayer(0);
    }
    
    public static FakePlayer getFakePlayer(int dim)
    {
    	if(fakePlayers.get(dim)==null)
    	{
    		WorldServer world;
    		
    		if(FMLCommonHandler.instance().getEffectiveSide()==Side.CLIENT)
    		{
    			if(FMLClientHandler.instance().getServer()!=null)
    				world = FMLClientHandler.instance().getServer().worldServerForDimension(dim);
    			else
    				world = null;
    		}
    		else
    		{
    			world = FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(dim);
    		}
    		
    		System.out.println(world);
    		FakePlayer fakeplayer = FakePlayerFactory.get(world, new GameProfile(new UUID(1234, 4321), "[Pokecube]DispenserPlayer"));
    		fakePlayers.put(dim, fakeplayer);
    	}
    	return fakePlayers.get(dim);
    }
    
    public static FakePlayer getFakePlayer(World world)
    {
    	return getFakePlayer(world.provider.getDimensionId());
    }
    
    public static enum Type
    {
    	FLYING,
    	FLOATING,
    	WATER,
    	NORMAL;
    	
    	public static Type getType(String type)
    	{
    		for(Type t:values())
    		{
    			if(t.toString().equalsIgnoreCase(type))
    				return t;
    		}
    		return NORMAL;
    	}
    }

	public String getTranslatedPokenameFromPokedexNumber(int pokedexNb) {
		return null;
	}
	
    /**
     * Returns the class of the {@link EntityLiving} for the given pokedexNb.
     * If no Pokemob has been registered for this pokedex number, it returns <code>null</code>.
     * 
     * @param pokedexNb the pokedex number
     * @return the {@link Class} of the pokemob
     */
    @SuppressWarnings("rawtypes")
    public abstract Class getEntityClassFromPokedexNumber(int pokedexNb);
}
