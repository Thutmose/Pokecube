package pokecube.adventures.comands;

import static pokecube.core.handlers.Config.misc;
import static pokecube.core.handlers.Config.spawning;
import static pokecube.core.handlers.Config.world;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import pokecube.adventures.blocks.warppad.TileEntityWarpPad;
import pokecube.adventures.entity.helper.EntityHasTrades.CubeTrade;
import pokecube.adventures.entity.trainers.EntityTrainer;
import pokecube.adventures.handlers.RecipeHandler;
import pokecube.adventures.handlers.TeamManager;
import pokecube.adventures.handlers.TrainerSpawnHandler;
import pokecube.adventures.utils.DBLoader;
import pokecube.core.PokecubeItems;
import pokecube.core.blocks.tradingTable.TileEntityTradingTable;
import pokecube.core.events.handlers.SpawnHandler;
import thut.api.terrain.BiomeType;
import thut.core.common.config.ConfigBase;
import thut.core.common.config.Configure;

public class Config extends ConfigBase
{

    public static final String         machines            = "machine";
    public static final String         trainers            = "trainers";
    public static final String         teams               = "teams";
    public static final String         database            = "database";

    public static Config               instance;

    public static Map<String, Integer> biomeMap            = new HashMap<String, Integer>();

    @Configure(category = world)
    String[]                           structureBiomes     = {
            // @formatter:off
            "meteorsite:" + BiomeType.METEOR.name, "smallfortruins:" + BiomeType.RUIN.name,
            "VillageForgeLarge:" + BiomeType.INDUSTRIAL.name, "VillageGuardTower:" + BiomeType.VILLAGE.name,
            "VillageInn:" + BiomeType.VILLAGE.name, "VillageHouseRich:" + BiomeType.VILLAGE.name,
            "VillageHouseRich1:" + BiomeType.VILLAGE.name, "villagewoodmill:" + BiomeType.INDUSTRIAL.name,
            "ClayMound:mound", "BigPyramid:" + BiomeType.RUIN.name, "DesertFort:" + BiomeType.VILLAGE.name,
            "DesertHut:" + BiomeType.VILLAGE.name, "DesertWatchtower:" + BiomeType.RUIN.name,
            "ElvenPond:" + BiomeType.LAKE.name, "ForestBeacon:" + BiomeType.RUIN.name,
            "HillAltarHouse:" + BiomeType.RUIN.name, "JokerTower:" + BiomeType.RUIN.name,
            "OldWatchtower:" + BiomeType.RUIN.name, "PeacefulCrypt:" + BiomeType.RUIN.name,
            "PirateHideout:" + BiomeType.VILLAGE.name, "ShrineSmallAir:" + BiomeType.RUIN.name,
            "ShrineSmallEarth:" + BiomeType.RUIN.name, "ShrineSmallWater:" + BiomeType.RUIN.name,
            "ShrineSmallFire:" + BiomeType.RUIN.name, "SmallAbandonedMine:" + BiomeType.RUIN.name,
            "SmallFortRuins:" + BiomeType.RUIN.name, "SmallPyramid:" + BiomeType.RUIN.name,
            "SmallWoodenCottage:" + BiomeType.VILLAGE.name, "SmallWoodenCottage1:" + BiomeType.VILLAGE.name,
            "SmallWoodenCottage2:" + BiomeType.VILLAGE.name, "TribalJungleHead:" + BiomeType.RUIN.name,
            "TemplePyramid:" + BiomeType.RUIN.name, "veldtbath:" + BiomeType.VILLAGE.name,
            "powerplant:" + BiomeType.INDUSTRIAL.name
            // @formatter:on
    };
    @Configure(category = world)
    public boolean                     exp_shareLoot       = true;
    @Configure(category = world)
    public boolean                     HMLoot              = true;

    @Configure(category = machines)
    public int                         maxOutput           = 256;
    @Configure(category = machines)
    public String                      powerFunction       = "a*x/10";
    @Configure(category = machines)
    public boolean                     warpPadEnergy       = true;
    @Configure(category = machines)
    public boolean                     theft               = false;
    @Configure(category = machines)
    public int                         warpPadRange        = 64;
    @Configure(category = machines)
    String[]                           ranchables          = {
            // @formatter:off
            "arceus:nether_star:100000", "chinchou:glowstone_dust:500", "lanturn:glowstone_dust,2:500",
            "lotad:waterlily:100", "tangela:vine:100", "bulbasaur:vine:100", "octillery:dye:100", "camerupt::lava:1000"
            // @formatter:on
    };
    @Configure(category = spawning)
    String[]                           biomeLevels         = { "mound:5-10" };
    @Configure(category = spawning)
    public int[]                       dimensionBlackList  = {};

    @Configure(category = trainers)
    public boolean                     trainerSpawn        = true;
    @Configure(category = trainers)
    public boolean                     trainersInvul       = false;
    @Configure(category = trainers)
    public boolean                     trainersTradeMobs   = true;
    @Configure(category = trainers)
    public boolean                     trainersTradeItems  = true;
    @Configure(category = trainers)
    public int                         trainerBox          = 128;
    @Configure(category = trainers)
    public int                         trainerCooldown     = 10000;
    @Configure(category = trainers)
    public int                         trainerSendOutDelay = 50;
    @Configure(category = trainers)
    public int                         trainerBattleDelay  = 100;
    @Configure(category = trainers)
    public int                         megaCost            = 16;
    @Configure(category = trainers)
    public int                         orbCost             = 32;
    @Configure(category = trainers)
    public int                         shinyCost           = 128;
    @Configure(category = trainers)
    public int                         tmCost              = 16;
    @Configure(category = trainers)
    public int                         badgeCost           = 64;
    @Configure(category = trainers)
    public int                         vitaminCost         = 8;
    @Configure(category = trainers)
    String[]                           cubeCosts           = { "0:16:4-8:1", "1:8:2-4:1", "2:2:1-2:1", "3:1:1:64" };

    @Configure(category = teams)
    private int                        teamLandPerPlayer   = 125;
    @Configure(category = teams)
    private boolean                    denyExplosions      = false;

    @Configure(category = misc)
    protected boolean                  tmRecipe            = true;

    @Configure(category = database)
    protected boolean                  forceDatabase       = true;

    public Config()
    {
        super(null);
    }

    public Config(File file)
    {
        super(file, new Config());
        instance = this;
        MinecraftForge.EVENT_BUS.register(this);
        populateSettings();
        applySettings();
        save();
    }

    @Override
    public void applySettings()
    {
        String[] defaults = structureBiomes;
        for (String s : defaults)
        {
            if (s != null && !s.isEmpty())
            {
                String[] args = s.split(":");
                String key = args[0].toLowerCase().replace(".tml", "");
                String subbiome = args[1];
                biomeMap.put(key, BiomeType.getBiome(subbiome).getType());
            }
        }
        TileEntityWarpPad.MAXRANGE = warpPadRange;
        TeamManager.maxLandCount = teamLandPerPlayer;
        TeamManager.denyBlasts = denyExplosions;
        TileEntityTradingTable.theftEnabled = theft;
        TrainerSpawnHandler.trainerBox = trainerBox;
        RecipeHandler.tmRecipe = tmRecipe;
        DBLoader.FORCECOPY = forceDatabase;
        EntityTrainer.cubeList.clear();
        for (String s : cubeCosts)
        {
            String[] args = s.split(":");
            int id = Integer.parseInt(args[0]);
            int num = Integer.parseInt(args[1]);
            String[] nums = args[2].split("-");
            int cost = Integer.parseInt(args[3]);
            if (cost == -1) continue;
            int min, max;
            min = Integer.parseInt(nums[0]);
            if (nums.length > 1) max = Integer.parseInt(nums[1]);
            else max = min;
            CubeTrade trade = new CubeTrade(id, min, max, cost);
            for (int i = 0; i < num; i++)
                EntityTrainer.cubeList.add(trade);
        }
    }

    protected FluidStack getFluid(String toParse)
    {
        return FluidRegistry.getFluidStack(toParse, Fluid.BUCKET_VOLUME);
    }

    private void parseBiomes()
    {
        for (String s : biomeLevels)
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

    protected ItemStack parseItemStack(String toParse)
    {
        String[] drop = toParse.split(",");
        int count = 1;
        String name = drop[0];
        int meta = 0;
        try
        {
            if (drop.length > 1) count = Integer.parseInt(drop[1]);
            if (drop.length > 2) meta = Integer.parseInt(drop[2]);
        }
        catch (NumberFormatException e)
        {

        }

        Item item = PokecubeItems.getItem(name);
        ItemStack stack = PokecubeItems.getStack(name);
        ItemStack toAdd;
        if (item == null && stack == null) { return null; }
        if (item != null)
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

    public void postInit()
    {
        processRanchables(ranchables);
        parseBiomes();
    }

    void processRanchables(String[] list)
    {
        // for(String s: list)
        // {
        // if(s!=null && !s.isEmpty())
        // {
        // String[] args = s.split(":");
        // String name = args[0];
        // PokedexEntry entry = Database.getEntry(name);
        // if(entry==null)
        // continue;
        // //only item
        // if(args.length==3)
        // {
        // String stack = args[1];
        // int delay = Integer.parseInt(args[2].trim());
        // MFRCompat.ranchables.add(Ranchables.makeRanchable(entry,
        // parseItemStack(stack), null, delay));
        // }
        // else if(args.length==4)//has fluid
        // {
        // String stack = args[1];
        // String fluid = args[2];
        // int delay = Integer.parseInt(args[3].trim());
        // MFRCompat.ranchables.add(Ranchables.makeRanchable(entry,
        // parseItemStack(stack), getFluid(fluid), delay));
        // }
        // }
        // }
    }

}
