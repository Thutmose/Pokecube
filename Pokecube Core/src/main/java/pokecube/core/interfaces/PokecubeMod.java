package pokecube.core.interfaces;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList.EntityEggInfo;
import net.minecraft.entity.EntityLiving;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.core.ByteClassLoader;
import pokecube.core.CreativeTabPokecube;
import pokecube.core.CreativeTabPokecubeBerries;
import pokecube.core.CreativeTabPokecubeBlocks;
import pokecube.core.CreativeTabPokecubes;
import pokecube.core.database.PokedexEntry;
import pokecube.core.handlers.Config;
import pokecube.core.network.NetworkWrapper;
import thut.api.maths.Vector3;

public abstract class PokecubeMod
{
    public static enum Type
    {
        FLYING, FLOATING, WATER, NORMAL;

        public static Type getType(String type)
        {
            for (Type t : values())
            {
                if (t.toString().equalsIgnoreCase(type)) return t;
            }
            return NORMAL;
        }
    }

    public final static String                    ID                         = "pokecube";
    public final static String                    VERSION                    = "@VERSION";
    public final static String                    MCVERSIONS                 = "@MCVERSION";
    public final static String                    MINVERSION                 = "@MINVERSION";
    public final static String                    MINFORGEVERSION            = "@FORGEVERSION";

    public final static String                    DEPSTRING                  = ";required-after:thutcore@@THUTCORE";
    private final static String                   GIST                       = "https://gist.githubusercontent.com/Thutmose/4d7320c36696cd39b336/raw/";
    public final static String                    UPDATEURL                  = GIST + "core.json";
    public final static String                    CONTRIBURL                 = GIST + "contributors.json";

    public final static String                    GIFTURL                    = GIST + "gift";
    public static final int                       MAX_DAMAGE                 = 0x7FFF;

    public static final int                       FULL_HEALTH                = MAX_DAMAGE - 1;
    private static HashMap<Integer, FakePlayer>   fakePlayers                = new HashMap<Integer, FakePlayer>();

    /** If you are a developer, you can set this flag to true. Set to false
     * before a build. */
    public final static boolean                   debug                      = false;

    public static PokecubeMod                     core;

    public static NetworkWrapper                  packetPipeline;

    // Manchou mobs are default mobs
    public static String                          defaultMod                 = "pokecube_ml";
    public static boolean                         pokemobsDamageOwner        = false;
    public static boolean                         pokemobsDamagePlayers      = true;
    public static boolean                         pokemobsDamageBlocks       = false;

    public static double                          MAX_DENSITY                = 1;
    public static Map<String, String>             gifts                      = Maps.newHashMap();
    public static List<String>                    giftLocations              = Lists.newArrayList();
    public static Map<PokedexEntry, Class<?>>     pokedexmap                 = Maps.newHashMap();
    public static Map<PokedexEntry, Class<?>>     genericMobClasses          = Maps.newHashMap();

    public static BitSet                          registered                 = new BitSet();

    public static CreativeTabs                    creativeTabPokecube        = new CreativeTabPokecube(
            CreativeTabs.CREATIVE_TAB_ARRAY.length, "Pokecube");

    public static CreativeTabs                    creativeTabPokecubes       = new CreativeTabPokecubes(
            CreativeTabs.CREATIVE_TAB_ARRAY.length, "Pokecubes");

    public static CreativeTabs                    creativeTabPokecubeBerries = new CreativeTabPokecubeBerries(
            CreativeTabs.CREATIVE_TAB_ARRAY.length, "Berries");

    public static CreativeTabs                    creativeTabPokecubeBlocks  = new CreativeTabPokecubeBlocks(
            CreativeTabs.CREATIVE_TAB_ARRAY.length, "Pokecube Blocks");

    public static HashMap<Integer, EntityEggInfo> pokemobEggs                = Maps.newHashMap();

    public static final UUID                      fakeUUID                   = new UUID(1234, 4321);
    public static Logger                          logger                     = Logger.getLogger("Pokecube");
    protected static FileHandler                  logHandler                 = null;

    public static FakePlayer getFakePlayer()
    {
        return getFakePlayer(0);
    }

    public static FakePlayer getFakePlayer(int dim)
    {
        if (fakePlayers.get(dim) == null)
        {
            WorldServer world;

            if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
            {
                if (FMLClientHandler.instance().getServer() != null)
                    world = FMLClientHandler.instance().getServer().getWorld(dim);
                else world = null;
            }
            else
            {
                world = FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(dim);
            }

            FakePlayer fakeplayer = FakePlayerFactory.get(world,
                    new GameProfile(fakeUUID, "[Pokecube]DispenserPlayer"));
            fakePlayers.put(dim, fakeplayer);
        }
        return fakePlayers.get(dim);
    }

    public static FakePlayer getFakePlayer(World world)
    {
        FakePlayer player = getFakePlayer(world.provider.getDimension());
        player.setWorld(world);
        return player;
    }

    public static boolean isDeobfuscated()
    {
        Object deObf = Launch.blackboard.get("fml.deobfuscatedEnvironment");
        return Boolean.valueOf(String.valueOf(deObf)).booleanValue();
    }

    public ByteClassLoader    loader;
    public ArrayList<Integer> starters = new ArrayList<Integer>();

    /** Creates a new instance of an entity in the world for the pokemob
     * specified by its pokedex entry.
     * 
     * @param entry
     *            the pokedexentry
     * @param world
     *            the {@link World} where to spawn
     * @return the {@link Entity} instance or null if a problem occurred */
    public abstract Entity createPokemob(PokedexEntry entry, World world);

    public abstract Config getConfig();

    /** Returns the class of the {@link EntityLiving} for the given pokedexNb.
     * If no Pokemob has been registered for this pokedex number, it returns
     * <code>null</code>.
     * 
     * @param pokedexNb
     *            the pokedex number
     * @return the {@link Class} of the pokemob */
    @SuppressWarnings("rawtypes")
    public abstract Class getEntityClassFromPokedexNumber(int pokedexNb);

    public abstract IEntityProvider getEntityProvider();

    public abstract Configuration getPokecubeConfig(FMLPreInitializationEvent evt);

    public abstract Integer[] getStarters();

    public String getTranslatedPokenameFromPokedexNumber(int pokedexNb)
    {
        return null;
    }

    /** Registers a Pokemob into the Pokedex. Have a look to the file called
     * <code>"HelpEntityJava.png"</code> provided with the SDK.
     *
     * @param createEgg
     *            whether an egg should be created for this species (is a base
     *            non legendary pokemob)
     * @param mod
     *            the instance of your mod
     * @param entry
     *            the pokedex entry */
    public abstract void registerPokemon(boolean createEgg, Object mod, PokedexEntry entry);

    @SuppressWarnings("rawtypes")
    public abstract void registerPokemonByClass(Class clazz, boolean createEgg, Object mod, PokedexEntry entry);

    public abstract void setEntityProvider(IEntityProvider provider);

    public abstract void spawnParticle(World world, String par1Str, Vector3 location, Vector3 velocity, int... args);

    public static void log(String toLog)
    {
        logger.log(Level.INFO, toLog);
    }
}
