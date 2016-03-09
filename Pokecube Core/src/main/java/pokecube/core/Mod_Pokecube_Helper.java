/**
 *
 */
package pokecube.core;

import static pokecube.core.PokecubeItems.getItem;
import static pokecube.core.PokecubeItems.removeFromHoldables;
import static pokecube.core.interfaces.PokecubeMod.HMs;
import static pokecube.core.PokecubeCore.getWorld;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraftforge.common.ChestGenHooks;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pokecube.core.handlers.ConfigHandler;
import pokecube.core.handlers.ItemHandler;
import pokecube.core.handlers.RecipeHandler;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.ItemTM;
import pokecube.core.moves.TreeRemover;

/** @author Manchou */
public class Mod_Pokecube_Helper
{

    /** Do monsters not spawn. */
    public static boolean       deactivateMonsters   = false;
    /** do Pokemobs spawn */
    public static boolean       pokemonSpawn         = true;
    /** do monster spawns get swapped with shadow pokemobs */
    public static boolean       disableMonsters      = false;
    /** do animals not spawn */
    public static boolean       deactivateAnimals    = false;
    /** are not-real pokemobs allowed. */
    public static boolean       allowFakeMons        = true;
    /** is there a choose first gui on login */
    public static boolean       guiOnLogin           = true;
    /** do meteors fall. */
    public static boolean       meteors              = true;
    /** does defeating a tame pokemob give exp */
    public static boolean       pvpExp               = false;
    /** do wild pokemobs which leave despawnRadius despawn immediately */
    public static boolean       cull                 = false;
    /** Will lithovores eat gravel */
    public static boolean       pokemobsEatGravel    = false;
    /** Is there a warning before a wild pokemob attacks the player. */
    public static boolean       pokemobagresswarning = false;

    public static boolean       POKEMARTSELLER       = true;
    public static boolean       SPAWNBUILDING        = true;
    public static int[]         guiOffset;
    public static boolean       guiDown              = true;
    public static File          configFile;
    public static Configuration config;
    public static boolean       mysterygift          = true;

    public static String        defaultMobs          = "";

    /** This is also the radius which mobs spawn in. Is only despawn radius if
     * cull is true */
    public static int           mobDespawnRadius     = 32;
    /** closest distance to a player the pokemob can spawn. */
    public static int           mobSpawnRadius       = 16;
    /** Distance to player needed to agress the player */
    public static int           mobAggroRadius       = 3;
    /** Approximate umber of ticks before pokemob starts taking hunger damage */
    public static int           pokemobLifeSpan      = 8000;
    /** Capped damage to players by pokemobs */
    public static int           maxPlayerDamage      = 10;
    /** Warning time before a wild pokemob attacks a player */
    public static int           pokemobagressticks   = 100;
    /** Number of threads allowed for AI. */
    public static int           maxAIThreads         = 1;
    /** Do explosions occur and cause damage */
    public static boolean       explosions           = true;
    /** Will nests spawn */
    public static boolean       nests                = false;
    /** number of nests per chunk */
    public static int           nestsPerChunk        = 1;
    /** To be used for nest retrogen. */
    public static boolean       refreshNests         = false;
    /** Minimum level legendaries can spawn at. */
    public static int           minLegendLevel       = 1;

    public static int           GUICHOOSEFIRSTPOKEMOB_ID;
    public static int           GUIDISPLAYPOKECUBEINFO_ID;
    public static int           GUIDISPLAYTELEPORTINFO_ID;
    public static int           GUIPOKECENTER_ID;
    public static int           GUIPOKEDEX_ID;
    public static int           GUIPOKEMOBSPAWNER_ID;
    public static int           GUIPC_ID;
    public static int           GUIPOKEMOB_ID;
    public static int           GUITRADINGTABLE_ID;

    /** Does the healing table have a recipe */
    protected static boolean    tableRecipe          = true;

    protected static String     cave;
    protected static String     surface;
    protected static String     rock;
    protected static String     trees;
    protected static String     plants;
    protected static String     terrains;
    protected static String     industrial;

    /** List of blocks to be considered for the floor of a cave. */
    private static List<Block>  caveBlocks           = new ArrayList<Block>();

    static
    {
        getCaveBlocks().add(Blocks.stone);
        getCaveBlocks().add(Blocks.dirt);
        getCaveBlocks().add(Blocks.gravel);
        getCaveBlocks().add(Blocks.iron_ore);
        getCaveBlocks().add(Blocks.coal_ore);
        getCaveBlocks().add(Blocks.diamond_ore);
        getCaveBlocks().add(Blocks.redstone_ore);
        getCaveBlocks().add(Blocks.gold_ore);
        getCaveBlocks().add(Blocks.gravel);
        getCaveBlocks().add(Blocks.netherrack);
        getCaveBlocks().add(Blocks.nether_brick);
    }

    /** List of blocks to be considered for the surface. */
    private static List<Block> surfaceBlocks = new ArrayList<Block>();

    static
    {
        getSurfaceBlocks().add(Blocks.stone);
        getSurfaceBlocks().add(Blocks.sand);
        getSurfaceBlocks().add(Blocks.dirt);
        getSurfaceBlocks().add(Blocks.gravel);
        getSurfaceBlocks().add(Blocks.netherrack);
        getSurfaceBlocks().add(Blocks.grass);
        getSurfaceBlocks().add(Blocks.leaves);
        getSurfaceBlocks().add(Blocks.hardened_clay);
        getSurfaceBlocks().add(Blocks.stained_hardened_clay);
        getSurfaceBlocks().add(Blocks.mycelium);
    };

    /** List of blocks to be considered to be rocks for the purpose of rocksmash
     * and lithovore eating */
    private static List<Block> rocks = new ArrayList<Block>();

    static
    {
        getRocks().add(Blocks.stone);
        getRocks().add(Blocks.iron_ore);
        getRocks().add(Blocks.coal_ore);
        getRocks().add(Blocks.diamond_ore);
        getRocks().add(Blocks.redstone_ore);
        getRocks().add(Blocks.emerald_ore);
        getRocks().add(Blocks.lapis_ore);
        getRocks().add(Blocks.quartz_ore);
        getRocks().add(Blocks.gold_ore);
        getRocks().add(Blocks.mossy_cobblestone);
        getRocks().add(Blocks.cobblestone);
        getRocks().add(Blocks.netherrack);
    }

    /** List of blocks to be considered to be generic terrain, for dig to reduce
     * drop rates for */
    private static List<Block> terrain = new ArrayList<Block>();

    static
    {
        terrain.add(Blocks.dirt);
        terrain.add(Blocks.grass);
        terrain.add(Blocks.stone);
        terrain.add(Blocks.sand);
        terrain.add(Blocks.gravel);
        terrain.add(Blocks.netherrack);
    }

    public static final String   CATEGORY_ADVANCED = "advanced";
    public static HashSet<Block> allBlocks         = new HashSet<Block>();

    public static void initAllBlocks()
    {
        allBlocks.clear();
        for (int i = 0; i < 4096; i++)
        {
            if (Block.getBlockById(i) != null) allBlocks.add(Block.getBlockById(i));
        }
    }

    public void loadConfig(Configuration config)
    {
        ConfigHandler.loadConfig(this, config);
    }

    @SubscribeEvent
    public void onConfigChangedEvent(ConfigChangedEvent.OnConfigChangedEvent event)
    {
        if (event.modID.equalsIgnoreCase(PokecubeMod.ID))
        {
            loadConfig(Mod_Pokecube_Helper.config);
        }
    }

    public static void postInit()
    {
        for (Block b : allBlocks)
        {
            try
            {
                if (getCaveBlocks().contains(b)) continue;

                if (net.minecraft.block.state.pattern.BlockHelper.forBlock(Blocks.stone).apply(b.getDefaultState()))
                    getCaveBlocks().add(b);
                else if (net.minecraft.block.state.pattern.BlockHelper.forBlock(Blocks.netherrack)
                        .apply(b.getDefaultState()))
                    getCaveBlocks().add(b);
                else if (net.minecraft.block.state.pattern.BlockHelper.forBlock(Blocks.dirt).apply(b.getDefaultState()))
                    getCaveBlocks().add(b);
                else if (net.minecraft.block.state.pattern.BlockHelper.forBlock(Blocks.sand).apply(b.getDefaultState()))
                    getCaveBlocks().add(b);
            }
            catch (Exception e)
            {
            }
        }
        for (Block b : getCaveBlocks())
        {
            if (b.getMaterial() == Material.rock && !getRocks().contains(b)) getRocks().add(b);
            if (!getSurfaceBlocks().contains(b)) getSurfaceBlocks().add(b);
        }

        RecipeHandler.initRecipes();

        addToList(getCaveBlocks(), cave);
        addToList(getSurfaceBlocks(), surface);
        addToList(getRocks(), rock);
        addToList(TreeRemover.woodTypes, trees);
        addToList(TreeRemover.plantTypes, plants);
        addToList(Mod_Pokecube_Helper.terrain, terrains);

        for (int i = 0; i < 4096; i++)
        {
            Block b = Block.getBlockById(i);
            if (b != null)
            {
                try
                {
                    if (b.isWood(getWorld(), new BlockPos(0, 0, 0)) && !TreeRemover.woodTypes.contains(b))
                    {
                        TreeRemover.woodTypes.add(b);
                    }
                }
                catch (Exception e)
                {
                    System.err.println("not wood " + e + " " + b);
                }
            }
        }

        removeFromHoldables("tm");
        initLoots();
    }

    public static void initLoots()
    {
        ItemStack cut = new ItemStack(getItem("tm"));
        ItemTM.addMoveToStack(IMoveConstants.MOVE_CUT, cut);
        HMs.add(cut);
        ItemStack flash = new ItemStack(getItem("tm"));
        ItemTM.addMoveToStack(IMoveConstants.MOVE_FLASH, flash);
        HMs.add(flash);
        ItemStack dig = new ItemStack(getItem("tm"));
        ItemTM.addMoveToStack(IMoveConstants.MOVE_DIG, dig);
        HMs.add(dig);
        ItemStack rockSmash = new ItemStack(getItem("tm"));
        ItemTM.addMoveToStack(IMoveConstants.MOVE_ROCKSMASH, rockSmash);
        HMs.add(rockSmash);

        WeightedRandomChestContent cutContent = new WeightedRandomChestContent(cut, 1, 1, 20);
        ChestGenHooks.addItem(ChestGenHooks.PYRAMID_JUNGLE_CHEST, cutContent);

        WeightedRandomChestContent flashContent = new WeightedRandomChestContent(flash, 1, 1, 20);
        ChestGenHooks.addItem(ChestGenHooks.DUNGEON_CHEST, flashContent);

        WeightedRandomChestContent digContent = new WeightedRandomChestContent(dig, 1, 1, 20);
        ChestGenHooks.addItem(ChestGenHooks.MINESHAFT_CORRIDOR, digContent);

        WeightedRandomChestContent smashContent = new WeightedRandomChestContent(rockSmash, 1, 1, 20);
        ChestGenHooks.addItem(ChestGenHooks.MINESHAFT_CORRIDOR, smashContent);

        // TODO do this for each stone, instead of just one.
        ItemStack stone = new ItemStack(getItem("megastone"));
        WeightedRandomChestContent stoneContent = new WeightedRandomChestContent(stone, 1, 1, 20);
        ChestGenHooks.addItem(ChestGenHooks.VILLAGE_BLACKSMITH, stoneContent);
        ChestGenHooks.addItem(ChestGenHooks.DUNGEON_CHEST, stoneContent);

        ItemStack ring = new ItemStack(getItem("megaring"));
        WeightedRandomChestContent ringContent = new WeightedRandomChestContent(ring, 1, 1, 5);
        ChestGenHooks.addItem(ChestGenHooks.VILLAGE_BLACKSMITH, ringContent);
        ChestGenHooks.addItem(ChestGenHooks.DUNGEON_CHEST, ringContent);
    }

    public void addItems()
    {
        ItemHandler.addItems(this);
    }

    public static void addVillagerTrades()
    {
        // TODO 1.8 villager trades
        // FARMER
        // VillagerRegistry.instance().registerVillageTradeHandler(0, new
        // VillagerRegistry.IVillageTradeHandler() {
        // @Override
        // public void manipulateTradesForVillager(EntityVillager villager,
        // MerchantRecipeList recipeList, Random random) {
        // // int berryId = random.nextInt(17);
        //// ItemStack item = berryItems.get(berryId);//TODO get a new way to
        // make a random berry
        //// if (item!=null){
        //// if (random.nextBoolean()){
        //// recipeList.add(new MerchantRecipe(
        //// new ItemStack(Items.emerald, 7),
        //// new ItemStack(berryItems.get(berryId), 1)));
        //// }
        //// else {
        //// recipeList.add(new MerchantRecipe(
        //// new ItemStack(berryItems.get(berryId), 1),
        //// new ItemStack(Items.emerald, 3)));
        //// }
        //// }
        //// else {
        //// recipeList.add(new MerchantRecipe(
        //// new ItemStack(Items.emerald, 28),
        //// new ItemStack(repelBlock, 1)));
        //// }
        // }
        // });
        // // LIBRARIAN
        // VillagerRegistry.instance().registerVillageTradeHandler(1, new
        // VillagerRegistry.IVillageTradeHandler() {
        // @Override
        // public void manipulateTradesForVillager(EntityVillager villager,
        // MerchantRecipeList recipeList, Random random) {
        // int rand = random.nextInt(2);
        // switch (rand) {
        // case 0:
        // recipeList.add(new MerchantRecipe(
        // new ItemStack(Items.emerald, 15),
        // new ItemStack(pokedex, 1)));
        // break;
        // default: ;
        // break;
        // }
        // }
        // });
        // // PRIEST
        // VillagerRegistry.instance().registerVillageTradeHandler(2, new
        // VillagerRegistry.IVillageTradeHandler() {
        // @Override
        // public void manipulateTradesForVillager(EntityVillager villager,
        // MerchantRecipeList recipeList, Random random) {
        // int rand = random.nextInt(2);
        // switch (rand) {
        // case 0:
        // recipeList.add(new MerchantRecipe(
        // new ItemStack(Items.emerald, 14),
        // new ItemStack(pokecenter, 1)));
        // break;
        // case 1:
        // recipeList.add(new MerchantRecipe(
        // new ItemStack(Items.emerald, 12),
        // new ItemStack(repelBlock, 1)));
        // break;
        // default:
        // break;
        // }
        // }
        // });
        // // SMITH
        // VillagerRegistry.instance().registerVillageTradeHandler(3, new
        // VillagerRegistry.IVillageTradeHandler() {
        // @Override
        // public void manipulateTradesForVillager(EntityVillager villager,
        // MerchantRecipeList recipeList, Random random) {
        // int rand = random.nextInt(13);
        // Item item = null;
        // switch (rand) {
        // case 0: item = dawnstone;
        // break;
        // case 1: item = duskstone;
        // break;
        // case 2: item = everstone;
        // break;
        // case 3: item = firestone;
        // break;
        // case 4: item = kingsrock;
        // break;
        // case 5: item = leafstone;
        // break;
        // case 6: item = moonstone;
        // break;
        // case 7: item = ovalstone;
        // break;
        // case 8: item = shinystone;
        // break;
        // case 9: item = sunstone;
        // break;
        // case 10: item = thunderstone;
        // break;
        // case 11: item = waterstone;
        // break;
        // default: item = everstone;
        // break;
        // }
        // recipeList.add(new MerchantRecipe(
        // new ItemStack(Items.emerald, 5),
        // new ItemStack(item, 1)));
        // }
        // });
    }

    public static void registerStarterTrades()
    {// TODO 1.8 villager trades
     // LIBRARIAN
     // VillagerRegistry.instance().registerVillageTradeHandler(1, new
     // VillagerRegistry.IVillageTradeHandler() {
     // @Override
     // public void manipulateTradesForVillager(EntityVillager villager,
     // MerchantRecipeList recipeList, Random random) {
     // Integer[] starters = core.getStarters();
     // int num = 1;
     // List<Integer> starts = new ArrayList<Integer>();
     // for(Integer i: starters)
     // {
     // if(i>0)
     // {
     // starts.add(i);
     // num++;
     // }
     // }
     // int rand = random.nextInt(num);
     // if(rand<starts.size())
     // {
     // ItemStack eggStarter = new ItemStack(pokemobEgg, 1, new
     // Integer(starts.get(rand) + 7463));
     // recipeList.add(new MerchantRecipe(
     // new ItemStack(Items.emerald, 30),
     // eggStarter));
     // }
     // }
     // });
    }

    private static Block getBlock(String name)
    {
        Block b = null;
        b = Block.getBlockFromName(name.replace("tile.", ""));
        return b;
    }

    private static void addToList(List<Block> list, String toAdd)
    {
        if (toAdd == null || toAdd.equals("")) return;

        String[] conts = toAdd.split(";");
        if (conts == null || conts.length < 1) return;

        Block b = null;
        for (String s : conts)
        {
            b = getBlock(s);
            if (b != null)
            {
                list.add(b);
            }
        }

    }

    public static List<Block> getSurfaceBlocks()
    {
        return surfaceBlocks;
    }

    public static List<Block> getCaveBlocks()
    {
        return caveBlocks;
    }

    public static List<Block> getRocks()
    {
        return rocks;
    }

    public static List<Block> getTerrain()
    {
        return terrain;
    }
}
