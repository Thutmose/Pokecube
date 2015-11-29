package pokecube.adventures.handlers;

import java.io.File;
import java.util.ArrayList;

import net.minecraftforge.common.config.Configuration;
import pokecube.adventures.blocks.warppad.TileEntityWarpPad;
import pokecube.core.blocks.pc.InventoryPC;
import pokecube.core.blocks.tradingTable.TileEntityTradingTable;
import pokecube.core.events.handlers.SpawnHandler;
import thut.api.terrain.BiomeType;

public class ConfigHandler
{
    public static File configFile;

    public static boolean trainerSpawn   = true;
    public static boolean trainersInvul  = false;
    public static boolean ONLYPOKECUBES  = true;
    public static boolean ENERGY         = true;
    static String[]       biomes;

    public static ArrayList<String> overrides = new ArrayList<String>();

    static
    {
        overrides.add("thutmose");
    }

    public static void load(Configuration conf)
    {
        conf.load();
        configFile = conf.getConfigFile();
        InventoryPC.PAGECOUNT = conf
                .get(Configuration.CATEGORY_GENERAL, "boxCount", 32, "The number of boxes in the pc").getInt();
        TileEntityWarpPad.MAXRANGE = conf.get(Configuration.CATEGORY_GENERAL, "warpRange", 64,
                "Maximum range of warp pads, set to -1 for no cap").getDouble();
        TeamManager.maxLandCount = conf.get(Configuration.CATEGORY_GENERAL, "landCount", 125,
                "The number of allowed 16x16x16 cubes per player in a team").getInt();
        TileEntityTradingTable.theftEnabled = conf
                .get(Configuration.CATEGORY_GENERAL, "theftEnabled", false,
                        "Can the trading table be used to convert a real player's pokemob in the same way it can convert a dispenser caught pokemob.")
                .getBoolean(false);
        TrainerSpawnHandler.trainerBox = conf.get(Configuration.CATEGORY_GENERAL, "trainerDistance", 128,
                "average distance between pairs of trainers.").getInt();
        trainerSpawn = conf
                .get(Configuration.CATEGORY_GENERAL, "natural trainers", true, "do trainers spawn naturally.")
                .getBoolean(true);
        trainersInvul = conf
                .get(Configuration.CATEGORY_GENERAL, "trainers unvulnerable", false, "are trainers immune to damage.")
                .getBoolean(false);
        ENERGY = conf
                .get(Configuration.CATEGORY_GENERAL, "energy", true, "Do various blocks require RF to run.")
                .getBoolean(true);
        RecipeHandler.tmRecipe = conf.get(Configuration.CATEGORY_GENERAL, "tm's Craftable", true).getBoolean(true);
        if (conf.hasKey(Configuration.CATEGORY_GENERAL, "starteroverrides"))
        {
            String[] defaults = conf
                    .get(Configuration.CATEGORY_GENERAL, "starteroverrides", overrides.toArray(new String[0]))
                    .getStringList();
            for (String s : defaults)
            {
                overrides.add(s);
            }
        }
        String[] defaultValues = { "mound:5-10" };

        biomes = conf.getStringList("biomeLevels", Configuration.CATEGORY_GENERAL, defaultValues,
                "spawn level ranges for pokemon in certain subbiomes");

        parseBiomes();
        conf.save();
    }

    public static void parseBiomes()
    {
        for (String s : biomes)
        {
            String[] args = s.split(":");
            String biome = args[0];
            String[] levels = args[1].split("-");

            try
            {
                SpawnHandler.subBiomeLevels.put(Integer.parseInt(biome),
                        new Integer[] { Integer.parseInt(levels[0]), Integer.parseInt(levels[1]) });
            }
            catch (NumberFormatException e)
            {
                BiomeType b = BiomeType.getBiome(biome);
                SpawnHandler.subBiomeLevels.put(b.getType(),
                        new Integer[] { Integer.parseInt(levels[0]), Integer.parseInt(levels[1]) });
            }

        }
    }

    public static void saveConfig()
    {
        Configuration config = new Configuration(configFile);
        config.load();

        config.get(Configuration.CATEGORY_GENERAL, "natural trainers", trainerSpawn).set(trainerSpawn);

        config.get(Configuration.CATEGORY_GENERAL, "trainers unvulnerable", trainersInvul).set(trainersInvul);

        config.get(Configuration.CATEGORY_GENERAL, "boxCount", InventoryPC.PAGECOUNT).set(InventoryPC.PAGECOUNT);

        config.get(Configuration.CATEGORY_GENERAL, "pokecubesOnly", ONLYPOKECUBES).set(ONLYPOKECUBES);

        config.get(Configuration.CATEGORY_GENERAL, "theftEnabled", TileEntityTradingTable.theftEnabled)
                .set(TileEntityTradingTable.theftEnabled);

        config.save();
    }
}
