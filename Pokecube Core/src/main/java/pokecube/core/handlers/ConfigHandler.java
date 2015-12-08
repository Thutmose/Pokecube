package pokecube.core.handlers;

import static pokecube.core.interfaces.PokecubeMod.hardMode;
import static pokecube.core.interfaces.PokecubeMod.semiHardMode;

import java.util.ArrayList;

import net.minecraft.init.Blocks;
import net.minecraftforge.common.config.Configuration;
import pokecube.core.Mod_Pokecube_Helper;
import pokecube.core.PokecubeItems;
import pokecube.core.database.Database;
import pokecube.core.events.handlers.EventsHandler;
import pokecube.core.events.handlers.SpawnHandler;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.utils.PokecubeSerializer;

public class ConfigHandler extends Mod_Pokecube_Helper
{
    public static void loadConfig(Mod_Pokecube_Helper helper, Configuration config)
    {
        configFile = config.getConfigFile();
        Mod_Pokecube_Helper.config = config;
        deactivateMonsters = config.get(Configuration.CATEGORY_GENERAL, "deactivateMonsters", deactivateMonsters,
                "Whether spawn of hostile Minecraft mobs should be deactivated.").getBoolean(true);
        pokemonSpawn = config.get(Configuration.CATEGORY_GENERAL, "pokemonSpawn", pokemonSpawn,
                "Do pokemon spawn via the pokecube spawning code.").getBoolean(true);
        pvpExp = config
                .get(Configuration.CATEGORY_GENERAL, "pvpExp", pvpExp, "do tame pokemon give experience when defeated.")
                .getBoolean(false);
        disableMonsters = config.get(Configuration.CATEGORY_GENERAL, "disableMonsters", disableMonsters,
                "Whether ALL spawn of hostile Minecraft mobs should be deactivated.").getBoolean(false);
        deactivateAnimals = config.get(Configuration.CATEGORY_GENERAL, "deactivateAnimals", deactivateAnimals,
                "Whether spawn of non-hostile Minecraft mobs should be deactivated.").getBoolean(true);
        allowFakeMons = config
                .get(Configuration.CATEGORY_GENERAL, "fakemonsAllowed", allowFakeMons,
                        "Are addons that add FakeMons allowed to add spawns or starters to the world.")
                .getBoolean(true);
        meteors = config.get(Configuration.CATEGORY_GENERAL, "meteors", meteors, "do meteors fall.").getBoolean(true);

        cull = config.get(Configuration.CATEGORY_GENERAL, "cullDistanced", cull,
                "do pokemon that get too far from a player despawn instantly").getBoolean(false);
        mysterygift = config.get(Configuration.CATEGORY_GENERAL, "mysterygift", mysterygift,
                "allows players to accept special reward pokemon from particular events").getBoolean(true);
        minLegendLevel = config.getInt("minLegendLevel", Configuration.CATEGORY_GENERAL, 1, 1, 100,
                "Minimum Level a legendary can spawn at");
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

        SpawnHandler.MAX_DENSITY = config.get(CATEGORY_ADVANCED, "mobDensity", SpawnHandler.MAX_DENSITY,
                "Spawn density factor, scales the occurance of wild pokemon.").getDouble();

        guiOffset = config.get(CATEGORY_ADVANCED, "guiOffset", new int[] { 0, 0 }, "offset of pokemon moves gui.")
                .getIntList();

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

            SpawnHandler.MAXNUM = config
                    .get(CATEGORY_ADVANCED, "mobNumber", SpawnHandler.MAXNUM,
                            "Number of Pokemobs that can spawn inside the despawn radius, this is multiplied by spawn density")
                    .getInt();

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

            mobDespawnRadius = config
                    .get(CATEGORY_ADVANCED, "despawnRadius", mobDespawnRadius,
                            "If there are no players within this close to the pokemob, it will immediately despawn (does not apply to tamed or angry.")
                    .getInt(96);

            mobSpawnRadius = config.get(CATEGORY_ADVANCED, "spawnRadius", mobSpawnRadius,
                    "mobs will not spawn closer than this to the player.").getInt(10);

            mobAggroRadius = config
                    .get(CATEGORY_ADVANCED, "agroRadius", mobAggroRadius, "mobs might agro a player closer than this.")
                    .getInt(5);

            pokemobLifeSpan = config
                    .get(CATEGORY_ADVANCED, "lifeTime", pokemobLifeSpan,
                            "the minimum number of ticks that a pokemob will live for in the wild, this value is extended by wild pokemon eating.")
                    .getInt(10000);

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

            Mod_Pokecube_Helper.industrial = config.get(CATEGORY_ADVANCED, "Industrial Area Blocks", "",
                    "Blocks here count towards an industrial area.").getString();

            defaultMobs = config
                    .get(CATEGORY_ADVANCED, "Default Mod", "",
                            "if you put something here, Mobs from this mod will be considered as the default mobs.")
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
            if (config.hasKey(CATEGORY_ADVANCED, "cap"))
            {
                SpawnHandler.lvlCap = config.get(CATEGORY_ADVANCED, "cap", true).getBoolean(true);
            }
            if (config.hasKey(CATEGORY_ADVANCED, "reset"))
            {
                PokecubeItems.resetTimeTags = config.get(CATEGORY_ADVANCED, "reset", true).getBoolean(true);
                config.get(CATEGORY_ADVANCED, "reset", true).set(false);

            }

            if (config.hasKey(CATEGORY_ADVANCED, "starteroverrides"))
            {
                String[] defaults = config.get(CATEGORY_ADVANCED, "starteroverrides", Database.defaultStarts)
                        .getStringList();
                ArrayList<String> def = new ArrayList<String>();
                for (String s : defaults)
                {
                    def.add(s);
                }
                for (String s : Database.defaultStarts)
                {
                    def.add(s);
                }
                Database.defaultStarts = def.toArray(new String[0]);
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

    public static void saveConfig()
    {
        Configuration config = new Configuration(configFile);
        config.load();

        config.get(CATEGORY_ADVANCED, "hardMode", hardMode).set(hardMode);
        config.get(CATEGORY_ADVANCED, "semiHMode", semiHardMode).set(semiHardMode);
        config.get(CATEGORY_ADVANCED, "loginGui", guiOnLogin).set(guiOnLogin);
        config.get(CATEGORY_ADVANCED, "advancedOptions", false).set(true);
        config.get(CATEGORY_ADVANCED, "mobNumber", SpawnHandler.MAXNUM).set(SpawnHandler.MAXNUM);
        config.get(CATEGORY_ADVANCED, "despawnRadius", mobDespawnRadius).set(mobDespawnRadius);
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

        config.get(CATEGORY_ADVANCED, "functions", funcs.toArray(new String[0])).set(funcs.toArray(new String[0]));
        config.save();
    }
}
