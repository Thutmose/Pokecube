package pokecube.core.handlers;

import static pokecube.core.interfaces.PokecubeMod.hardMode;
import static pokecube.core.interfaces.PokecubeMod.semiHardMode;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.init.Blocks;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.config.IConfigElement;
import pokecube.core.Mod_Pokecube_Helper;
import pokecube.core.PokecubeItems;
import pokecube.core.database.Database;
import pokecube.core.database.Database.EnumDatabase;
import pokecube.core.events.handlers.EventsHandler;
import pokecube.core.events.handlers.SpawnHandler;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.utils.PokecubeSerializer;

public class ConfigHandler extends Mod_Pokecube_Helper
{
    public static final String CATEGORY_SPAWNING = "mobspawning";
    public static final String CATEGORY_DATABASE = "databases";
    public static String[]     defaultStarts     = {};
    public static boolean      loginmessage      = true;
    public static int          mateMultiplier    = 1;
    public static int          BREEDINGDELAY     = 4000;
    public static int          EGGHATCHTIME      = 10000;

    public static void loadConfig(Mod_Pokecube_Helper helper, Configuration config)
    {
        Property prop;
        configFile = config.getConfigFile();
        Mod_Pokecube_Helper.config = config;
        initDefaultStarts();
        loadSpawnConfigs();
        pvpExp = config
                .get(Configuration.CATEGORY_GENERAL, "pvpExp", pvpExp, "do tame pokemon give experience when defeated.")
                .getBoolean(false);
        allowFakeMons = config
                .get(Configuration.CATEGORY_GENERAL, "fakemonsAllowed", allowFakeMons,
                        "Are addons that add FakeMons allowed to add spawns or starters to the world.")
                .getBoolean(true);
        meteors = config.get(Configuration.CATEGORY_GENERAL, "meteors", meteors, "do meteors fall.").getBoolean(true);

        if (config.hasKey(Configuration.CATEGORY_GENERAL, "loginmessage")
                && config.hasKey(Configuration.CATEGORY_GENERAL, "version"))
        {
            if (config.getString("version", Configuration.CATEGORY_GENERAL, "", "").equals(PokecubeMod.VERSION))
                loginmessage = config.get(Configuration.CATEGORY_GENERAL, "loginmessage", false).getBoolean(false);
        }

        String[] defaultDatabases = { "pokemobs", "moves" };

        (prop = config.get(CATEGORY_DATABASE, "databases", defaultDatabases,
                "Databases for pokemob information Add additional databases by appending , <newfile>."
                        + "  If you remove any of the strings here, things will break.")).set(defaultDatabases);
        String[] configDatabases = prop.getStringList();
        if (configDatabases.length != 2)
        {
            configDatabases = defaultDatabases;
        }

        for (int i = 0; i < EnumDatabase.values().length; i++)
        {
            String[] args = configDatabases[i].split(",");
            for (String s : args)
            {
                Database.addDatabase(s.trim(), EnumDatabase.values()[i]);
            }
        }

        mysterygift = config.get(Configuration.CATEGORY_GENERAL, "mysterygift", mysterygift,
                "allows players to accept special reward pokemon from particular events").getBoolean(true);

        boolean advanced = config
                .get(CATEGORY_ADVANCED, "advancedOptions", false,
                        "set this to true, run the game, and some more advanced config options will be enabled in this file.")
                .getBoolean(false);
        hardMode = config
                .get(CATEGORY_ADVANCED, "hardMode", hardMode,
                        "can tame pokemobs attack players, and do moves have additional effects outside of battle (if true, enables HM like behaviour in certain moves).")
                .getBoolean(false);
        Database.FORCECOPY = config
                .get(CATEGORY_ADVANCED, "forceDatabase", Database.FORCECOPY,
                        "Will Pokecube overwrite the copy of the database with config with the one in the jar, set to false if you plan on changing the database.")
                .getBoolean(true);
        semiHardMode = config
                .get(CATEGORY_ADVANCED, "semiHMode", PokecubeMod.semiHardMode,
                        "do moves have additional effects outside of battle (if true, enables HM like behaviour in certain moves which damage blocks).")
                .getBoolean(false);
        guiOnLogin = config
                .get(CATEGORY_ADVANCED, "loginGui", guiOnLogin, "does the choose first pokemob gui appear on login")
                .getBoolean(false);
        if (hardMode) semiHardMode = true;

        guiOffset = config.get(CATEGORY_ADVANCED, "guiOffset", new int[] { 0, 0 }, "offset of pokemon moves gui.")
                .getIntList();
        guiDown = config.get(CATEGORY_ADVANCED, "guiDown", guiDown, "Are the moves shown below the nametag.").getBoolean();

        maxAIThreads = config
                .get(CATEGORY_ADVANCED, "aiThreads", 2,
                        "How many AI threads are generated, note that it will at most make as many as processors are available, so setting this value above that number will not do anything.")
                .getInt(2);

        pokemobagresswarning = config.get(CATEGORY_ADVANCED, "agresswarning", pokemobagresswarning,
                "is there a warning before a wild pokemob attacks the player.").getBoolean(false);

        pokemobagressticks = (int) config
                .get(CATEGORY_ADVANCED, "agresstime", 100,
                        "number of game ticks between agressing a player and attacking, if warning is enabled")
                .getInt();

        maxPlayerDamage = (int) config.get(CATEGORY_ADVANCED, "maxDamage", -1,
                "maximum damage pokemobs can deal to players, -1 means no cap.").getInt();

        if (advanced)
        {
            int dist = config.get(CATEGORY_ADVANCED, "minMeteorDistance", 3000,
                    "The minimum distance between two meteor impacts").getInt(3000);
            PokecubeSerializer.MeteorDistance = dist * dist;
            //
            pokemobsEatGravel = config.get(CATEGORY_ADVANCED, "pokemobsEatGravel", false,
                    "will rock eating pokemon eat gravel to nothing.").getBoolean(false);

            nests = config.get(CATEGORY_ADVANCED, "nests", false, "will pokemobs spawn from nests instead of randomly.")
                    .getBoolean(false);

            refreshNests = config.get(CATEGORY_ADVANCED, "nestsRefresh", false,
                    "set true to generate nests in an already generated world.").getBoolean(false);

            Mod_Pokecube_Helper.cave = config
                    .get(CATEGORY_ADVANCED, "Cave Spawns",
                            Blocks.stone.getUnlocalizedName() + ";" + Blocks.gravel.getUnlocalizedName(),
                            "additional Block IDs which may be possible floors of caves, cave mobs will only spawn on these blocks, the listed two are examples")
                    .getString();

            Mod_Pokecube_Helper.surface = config
                    .get(CATEGORY_ADVANCED, "Surface Spawns",
                            Blocks.grass.getUnlocalizedName() + ";" + Blocks.dirt.getUnlocalizedName(),
                            "additional Block IDs which may be possible surface blocks, surface mobs will only spawn on these blocks, the listed two are examples")
                    .getString();

            Mod_Pokecube_Helper.rock = config
                    .get(CATEGORY_ADVANCED, "Rock Blocks", Blocks.stone.getUnlocalizedName() + ";",
                            "additional Block IDs which can be smashed using Rock Smash, RockSmash gains fortune based on pokemob level, and can cause mobs to spawn when used, the listed two are examples")
                    .getString();

            Mod_Pokecube_Helper.trees = config
                    .get(CATEGORY_ADVANCED, "Wood Blocks", "",
                            "additional Wood blocks to be looked for while cutting trees, do not add leaves to this list, use same format as for the other lists.")
                    .getString();

            Mod_Pokecube_Helper.plants = config
                    .get(CATEGORY_ADVANCED, "Harvestable Plant Blocks", "",
                            "additional plant blocks to be harvested with cut, use same format as for the other lists.")
                    .getString();

            Mod_Pokecube_Helper.terrains = config
                    .get(CATEGORY_ADVANCED, "Useless Terrain Blocks", "",
                            "any block listed here will have decreased drop rates from dig with high level pokemon, use same format as for the other lists.")
                    .getString();

            defaultMobs = config
                    .get(CATEGORY_ADVANCED, "Default Mod", "",
                            "if you put a modid here, Mobs from this mod will be considered as the default mobs.")
                    .getString();

            if (config.hasKey(CATEGORY_ADVANCED, "stdev"))
            {
                EventsHandler.candyChance = config.get(CATEGORY_ADVANCED, "stdev", 4.5).getDouble(4.5);
            }
            if (config.hasKey(CATEGORY_ADVANCED, "mystlocs"))
            {
                String[] locs = config.getStringList("mystlocs", CATEGORY_ADVANCED, new String[] {}, "");
                for (String loc : locs)
                {
                    PokecubeMod.giftLocations.add(loc);
                }
            }
            if (config.hasKey(CATEGORY_ADVANCED, "juicechance"))
            {
                EventsHandler.juiceChance = config.get(CATEGORY_ADVANCED, "juicechance", 3).getDouble(3);
            }
            if (config.hasKey(CATEGORY_ADVANCED, "reset"))
            {
                PokecubeItems.resetTimeTags = config.get(CATEGORY_ADVANCED, "reset", true).getBoolean(true);
                config.get(CATEGORY_ADVANCED, "reset", true).set(false);
            }

            if (config.hasKey(CATEGORY_ADVANCED, "starteroverrides"))
            {
                String[] defaults = config.get(CATEGORY_ADVANCED, "starteroverrides", defaultStarts).getStringList();
                ArrayList<String> def = new ArrayList<String>();
                for (String s : defaults)
                {
                    def.add(s);
                }
                for (String s : defaultStarts)
                {
                    def.add(s);
                }
                defaultStarts = def.toArray(new String[0]);
            }
            String[] strings = config
                    .get(CATEGORY_ADVANCED, "functions", new String[] { "0:(10^6)*(sin(x*10^-3)^8 + sin(y*10^-3)^8)",
                            "1:10+r/130;r", "2:(10^6)*(sin(x*0.5*10^-3)^8 + sin(y*0.5*10^-3)^8)" },

                            "to recieve legacy lvl functions, replace the first entry with the second, and set the leading 1 to 0")
                    .getStringList();
            SpawnHandler.loadFunctionsFromStrings(strings);

            tableRecipe = config.get(CATEGORY_ADVANCED, "healTables", true, "are healing tables craftable.")
                    .getBoolean(true);

            explosions = config
                    .get(CATEGORY_ADVANCED, "explosions", true, "set false if worried about griefers with Electrodes.")
                    .getBoolean(true);
        }

        SPAWNBUILDING = config
                .get(Configuration.CATEGORY_GENERAL, "spawnbuilding", true, "does a pokecenter spawn at world spawn.")
                .getBoolean(true);
        POKEMARTSELLER = config
                .get(Configuration.CATEGORY_GENERAL, "pokemartseller", true, "Do pokemart sellers spawn in pokemarts.")
                .getBoolean(true);

        config.save();
        // Gui
        GUICHOOSEFIRSTPOKEMOB_ID = 11;
        GUIDISPLAYPOKECUBEINFO_ID = 12;
        GUIPOKECENTER_ID = 13;
        GUIPOKEDEX_ID = 14;
        GUIPOKEMOBSPAWNER_ID = 15;
        GUITRADINGTABLE_ID = 16;
        GUIPC_ID = 17;
        GUIDISPLAYTELEPORTINFO_ID = 18;
        GUIPOKEMOB_ID = 19;
        System.out.println("Pokecube Config Loaded");
    }

    private static void loadSpawnConfigs()
    {
        deactivateMonsters = config.get(CATEGORY_SPAWNING, "deactivateMonsters", deactivateMonsters,
                "Whether spawn of hostile Minecraft mobs should be deactivated.").getBoolean(false);
        deactivateAnimals = config.get(CATEGORY_SPAWNING, "deactivateAnimals", deactivateAnimals,
                "Whether spawn of non-hostile Minecraft mobs should be deactivated.").getBoolean(false);
        disableMonsters = config
                .get(CATEGORY_SPAWNING, "disableMonsters", disableMonsters,
                        "Whether ALL spawn of hostile Minecraft mobs should be deactivated. This may mess up mod compatiblity if enabled")
                .getBoolean(false);

        pokemonSpawn = config.get(CATEGORY_SPAWNING, "pokemonSpawn", pokemonSpawn,
                "Do pokemon spawn via the pokecube spawning code.").getBoolean(true);

        cull = config
                .get(CATEGORY_SPAWNING, "cullDistanced", cull,
                        "do pokemon that get more than despawnDistance from a player despawn instantly")
                .getBoolean(false);

        SpawnHandler.lvlCap = config.getBoolean("cap", CATEGORY_SPAWNING, true,
                "will a level cap be applied, if true, pokemobs will not spawn higher than lvl levelCap");
        SpawnHandler.capLevel = config.getInt("levelCap", CATEGORY_SPAWNING, 50, 1, 100,
                "the maximum level wild pokemobs can spawn at, if cap is enabled");
        minLegendLevel = config.getInt("minLegendLevel", CATEGORY_SPAWNING, 1, 1, 100,
                "Minimum Level a legendary can spawn at");

        pokemobLifeSpan = config
                .get(CATEGORY_SPAWNING, "lifeTime", pokemobLifeSpan,
                        "the minimum number of ticks that a pokemob will live for in the wild, this value is extended by wild pokemon eating.")
                .getInt(10000);

        mateMultiplier = config
                .get(CATEGORY_SPAWNING, "breedingrate", mateMultiplier,
                        "the number of ticks to increment for breeding at a time, increasing this number speeds up mating.")
                .getInt(1);

        BREEDINGDELAY = config
                .get(CATEGORY_SPAWNING, "breedingdelay", BREEDINGDELAY, "Approximate number of ticks between breeding.")
                .getInt(BREEDINGDELAY);
        if (BREEDINGDELAY < 600) BREEDINGDELAY = 1000;

        EGGHATCHTIME = config.getInt("egghatchtime", CATEGORY_SPAWNING, 10000, 1, Integer.MAX_VALUE,
                "twice the Average time to hatch eggs in ticks, actual hatch time is 100 + random.nextInt(egghatchtime)");

        SpawnHandler.MAX_DENSITY = config.get(CATEGORY_SPAWNING, "mobDensity", SpawnHandler.MAX_DENSITY,
                "Spawn density factor, scales the occurance of wild pokemon.").getDouble();

        SpawnHandler.MAXNUM = config
                .get(CATEGORY_SPAWNING, "mobNumber", SpawnHandler.MAXNUM,
                        "Number of Pokemobs that can spawn inside the despawnRadius of a player, this is multiplied by spawn density")
                .getInt();

        mobDespawnRadius = config
                .get(CATEGORY_SPAWNING, "despawnRadius", mobDespawnRadius,
                        "If there are no players within this close to the pokemob, it will immediately despawn if cullDistanced is true (does not apply to tamed or angry)."
                                + "  This is also the maximum distance from a player for a pokemob to spawn.")
                .getInt(32);
        mobSpawnRadius = config.get(CATEGORY_SPAWNING, "spawnRadius", mobSpawnRadius,
                "mobs will not spawn closer than this to the player.").getInt(10);
        mobAggroRadius = config
                .get(CATEGORY_SPAWNING, "agroRadius", mobAggroRadius, "mobs might agro a player closer than this.")
                .getInt(5);
    }

    public static void seenMessage()
    {
        Configuration config = new Configuration(configFile);
        config.load();
        config.get(Configuration.CATEGORY_GENERAL, "loginmessage", false).set(false);
        config.get(Configuration.CATEGORY_GENERAL, "version", PokecubeMod.VERSION).set(PokecubeMod.VERSION);
        config.save();
    }

    public static void saveConfig()
    {
        Configuration config = new Configuration(configFile);
        config.load();

        config.get(CATEGORY_ADVANCED, "hardMode", hardMode).set(hardMode);
        config.get(CATEGORY_ADVANCED, "semiHMode", semiHardMode).set(semiHardMode);
        config.get(CATEGORY_ADVANCED, "loginGui", guiOnLogin).set(guiOnLogin);
        config.get(CATEGORY_ADVANCED, "advancedOptions", false).set(true);

        config.get(CATEGORY_SPAWNING, "mobNumber", SpawnHandler.MAXNUM).set(SpawnHandler.MAXNUM);
        config.get(CATEGORY_SPAWNING, "despawnRadius", mobDespawnRadius).set(mobDespawnRadius);

        if (config.hasKey(CATEGORY_ADVANCED, "explosions") || !explosions)
        {
            config.get(CATEGORY_ADVANCED, "explosions", explosions).set(explosions);
        }
        ArrayList<String> funcs = new ArrayList<String>();
        for (Integer i : SpawnHandler.functions.keySet())
        {
            String s = SpawnHandler.functions.get(i);
            if (i != null && s != null)
            {
                funcs.add(i + ":" + s);
            }
        }

        config.get(CATEGORY_SPAWNING, "functions", funcs.toArray(new String[0])).set(funcs.toArray(new String[0]));
        config.save();
    }

    public static void initDefaultStarts()
    {
        try
        {
            JsonParser parser = new JsonParser();
            URL url = new URL(PokecubeMod.CONTRIBURL);
            URLConnection con = url.openConnection();
            con.setConnectTimeout(1000);
            con.setReadTimeout(1000);
            InputStream in = con.getInputStream();
            JsonElement element = parser.parse(new InputStreamReader(in));
            JsonElement element1 = element.getAsJsonObject().get("contributors");
            JsonArray contribArray = element1.getAsJsonArray();
            List<String> defaults = Lists.newArrayList();
            for (int i = 0; i < contribArray.size(); i++)
            {
                element1 = contribArray.get(i);
                JsonObject obj = element1.getAsJsonObject();
                String name = obj.get("username").getAsString();
                String info = obj.get("info").getAsString();
                defaults.add(name + ":" + info);
            }
            System.out.println(defaults);
            defaultStarts = defaults.toArray(new String[0]);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    public static List<IConfigElement> getConfigElements()
    {
        List<IConfigElement> list = new ArrayList<IConfigElement>();
        for (IConfigElement e : new ConfigElement(config.getCategory(Configuration.CATEGORY_GENERAL))
                .getChildElements())
        {
            if (!e.getName().equals("version")) list.add(e);
        }
        for (IConfigElement e : new ConfigElement(config.getCategory(CATEGORY_ADVANCED)).getChildElements())
        {
            list.add(e);
        }
        for (IConfigElement e : new ConfigElement(config.getCategory(CATEGORY_SPAWNING)).getChildElements())
        {
            list.add(e);
        }
        return list;
    }
}
