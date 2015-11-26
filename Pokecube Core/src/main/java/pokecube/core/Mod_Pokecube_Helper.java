/**
 *
 */
package pokecube.core;

import static pokecube.core.PokecubeItems.getItem;
import static pokecube.core.PokecubeItems.removeFromHoldables;
import static pokecube.core.interfaces.PokecubeMod.HMs;
import static pokecube.core.interfaces.PokecubeMod.hardMode;
import static pokecube.core.interfaces.PokecubeMod.semiHardMode;
import static pokecube.core.mod_Pokecube.getWorld;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
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
import pokecube.core.events.handlers.SpawnHandler;
import pokecube.core.handlers.ConfigHandler;
import pokecube.core.handlers.ItemHandler;
import pokecube.core.handlers.RecipeHandler;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.items.ItemTM;
import pokecube.core.moves.TreeRemover;

/** @author Manchou */
public class Mod_Pokecube_Helper
{

    public static boolean deactivateMonsters   = true;
    public static boolean pokemonSpawn         = true;
    public static boolean disableMonsters      = false;
    public static boolean deactivateAnimals    = true;
    public static boolean allowFakeMons        = true;
    public static boolean guiOnLogin           = true;
    public static boolean meteors              = true;
    public static boolean pvpExp               = false;
    public static boolean cull                 = false;
    public static boolean pokemobsEatGravel    = false;
    public static boolean pokemobagresswarning = false;

    public static boolean       POKEMARTSELLER = true;
    public static boolean       SPAWNBUILDING  = true;
    public static int[]         guiOffset;
    public static File          configFile;
    public static Configuration config;
    public static boolean       mysterygift    = true;

    public static String defaultMobs = "";

    // public static int maxMobSpawn = 5;
    public static int     mobDespawnRadius   = 32;
    public static int     mobSpawnRadius     = 16;
    public static int     mobAggroRadius     = 3;
    public static int     pokemobLifeSpan    = 8000;
    public static int     maxPlayerDamage    = 10;
    public static int     pokemobagressticks = 100;
    public static int     maxAIThreads       = Runtime.getRuntime().availableProcessors();
    public static boolean explosions         = true;
    public static boolean nests              = false;                                     // TODO
                                                                                          // config
                                                                                          // for
                                                                                          // this
    public static int     nestsPerChunk      = 1;
    public static boolean refreshNests       = false;
    public static int     minLegendLevel     = 1;

    public static int GUICHOOSEFIRSTPOKEMOB_ID;
    public static int GUIDISPLAYPOKECUBEINFO_ID;
    public static int GUIDISPLAYTELEPORTINFO_ID;
    public static int GUIPOKECENTER_ID;
    public static int GUIPOKEDEX_ID;
    public static int GUIPOKEMOBSPAWNER_ID;
    public static int GUIPC_ID;
    public static int GUIPOKEMOB_ID;
    public static int GUITRADINGTABLE_ID;

    protected static boolean tableRecipe = true;

    protected static String cave;
    protected static String surface;
    protected static String rock;
    protected static String trees;
    protected static String plants;
    protected static String terrains;
    protected static String industrial;

    private static List<Block> caveBlocks = new ArrayList<Block>();

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
        getCaveBlocks().add(Blocks.mossy_cobblestone);
        getCaveBlocks().add(Blocks.cobblestone);
        getCaveBlocks().add(Blocks.gravel);
        getCaveBlocks().add(Blocks.netherrack);
        getCaveBlocks().add(Blocks.nether_brick);
    }

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

    public static final String  CATEGORY_ADVANCED = "advanced";
    public static HashSet<Block> allBlocks = new HashSet<Block>();

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

    public static void postInit()
    {
        for (Block b : allBlocks)
        {
            try
            {
                // if(!getCaveBlocks().contains(b)&&b.isReplaceableOreGen(null,
                // new BlockPos(0,0,0),
                // net.minecraft.block.state.pattern.BlockHelper.forBlock(Blocks.stone))
                // ||b.isReplaceableOreGen(null, new BlockPos(0,0,0),
                // net.minecraft.block.state.pattern.BlockHelper.forBlock(Blocks.netherrack)))
                getCaveBlocks().add(b);
            }
            catch (Exception e)
            {
                // e.printStackTrace();//TODO figure out how to get this
                // automatically finding worldgen blocks again
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
                    System.err.println("not wood");
                }
            }
        }

        removeFromHoldables("tm");
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
    }

    public void addItems()
    {
        ItemHandler.addItems(this);
    }


    private static PrintWriter out;
    private static FileWriter  fwriter;

    protected static void generateCubeJsons(String cube)
    {

        boolean makejson = false;
        if (!makejson) return;

        String seperator = System.getProperty("file.separator");
        File temp = new File("mods" + seperator + "pokecube" + seperator + "assets" + seperator + "pokecube" + seperator
                + "models" + seperator + "block");
        if (!temp.exists())
        {
            temp.mkdirs();
        }
        temp = new File("mods" + seperator + "pokecube" + seperator + "assets" + seperator + "pokecube" + seperator
                + "models" + seperator + "item");
        if (!temp.exists())
        {
            temp.mkdirs();
        }

        temp = new File("mods" + seperator + "pokecube" + seperator + "assets" + seperator + "pokecube" + seperator
                + "models" + seperator + "block" + seperator + cube + ".json");
        try
        {
            fwriter = new FileWriter(temp);
            out = new PrintWriter(fwriter);
            out.println("{");
            out.println("    \"parent\": \"block/orientable\",");
            out.println("    \"textures\": {");
            out.println("        \"top\": \"pokecube:items/" + cube + "top\",");
            out.println("        \"bottom\": \"pokecube:items/" + cube + "bottom\",");
            out.println("        \"front\": \"pokecube:items/" + cube + "front\",");
            out.println("        \"side\": \"pokecube:items/" + cube + "side\"");
            out.println("    }");
            out.println("}");
            out.close();
            fwriter.close();

            temp = new File("mods" + seperator + "pokecube" + seperator + "assets" + seperator + "pokecube" + seperator
                    + "models" + seperator + "item" + seperator + cube + ".json");

            fwriter = new FileWriter(temp);
            out = new PrintWriter(fwriter);
            out.println("{");
            out.println("    \"parent\": \"pokecube:block/" + cube + "\",");
            out.println("    \"display\": {");
            out.println("    \"thirdperson\": {");
            out.println("        \"rotation\": [ 10, -45, 170 ],");
            out.println("        \"translation\": [ 0, 1.5, -2.75 ],");
            out.println("        \"scale\": [ 0.375, 0.375, 0.375 ]");
            out.println("		}");
            out.println("    }");
            out.println("}");
            out.close();
            fwriter.close();

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static void generateBlockJsons(String identifier, String texturelocation, String modid, boolean blockstate,
            String parent)
    {
        String seperator = System.getProperty("file.separator");
        File temp = new File("mods" + seperator + "pokecube" + seperator + "assets" + seperator + modid + seperator
                + "models" + seperator + "block");
        if (!temp.exists())
        {
            temp.mkdirs();
        }
        temp = new File("mods" + seperator + "pokecube" + seperator + "assets" + seperator + modid + seperator
                + "models" + seperator + "block" + seperator + identifier + ".json");
        try
        {

            if (parent.equalsIgnoreCase("crop"))
            {
                for (int i = 0; i < 8; i++)
                {
                    temp = new File("mods" + seperator + "pokecube" + seperator + "assets" + seperator + modid
                            + seperator + "models" + seperator + "block" + seperator + identifier + "_" + i + ".json");
                    String num = "_" + i;
                    num = "";
                    fwriter = new FileWriter(temp);
                    out = new PrintWriter(fwriter);
                    out.println("{");
                    out.println("    \"parent\": \"block/" + parent + "\",");
                    out.println("    \"textures\": {");
                    out.println("        \"crop\": \"" + modid + ":" + texturelocation + "/" + identifier + num + "\"");
                    out.println("    }");
                    out.println("}");
                    out.close();
                    fwriter.close();
                }
            }
            else if (parent.equalsIgnoreCase("cross"))
            {
                temp = new File("mods" + seperator + "pokecube" + seperator + "assets" + seperator + modid + seperator
                        + "models" + seperator + "block" + seperator + identifier + ".json");

                fwriter = new FileWriter(temp);
                out = new PrintWriter(fwriter);
                out.println("{");
                out.println("    \"parent\": \"block/" + parent + "\",");
                out.println("    \"textures\": {");
                out.println("        \"cross\": \"" + modid + ":" + texturelocation + "/" + identifier + "\"");
                out.println("    }");
                out.println("}");
                out.close();
                fwriter.close();
            }
            else if (parent.equalsIgnoreCase("log"))
            {
                temp = new File("mods" + seperator + "pokecube" + seperator + "assets" + seperator + modid + seperator
                        + "models" + seperator + "block" + seperator + identifier + ".json");
                fwriter = new FileWriter(temp);
                out = new PrintWriter(fwriter);
                out.println("{");
                out.println("    \"parent\": \"block/cube_column\",");
                out.println("    \"textures\": {");
                out.println("        \"end\": \"" + modid + ":" + texturelocation + "/" + identifier + "_top\",");
                out.println("        \"side\": \"" + modid + ":" + texturelocation + "/" + identifier + "\"");
                out.println("    }");
                out.println("}");
                out.close();
                fwriter.close();
                temp = new File("mods" + seperator + "pokecube" + seperator + "assets" + seperator + modid + seperator
                        + "models" + seperator + "block" + seperator + identifier + "_side.json");
                fwriter = new FileWriter(temp);
                out = new PrintWriter(fwriter);
                out.println("{");
                out.println("    \"parent\": \"block/column_side\",");
                out.println("    \"textures\": {");
                out.println("        \"end\": \"" + modid + ":" + texturelocation + "/" + identifier + "_top\",");
                out.println("        \"side\": \"" + modid + ":" + texturelocation + "/" + identifier + "\"");
                out.println("    }");
                out.println("}");
                out.close();
                fwriter.close();
                temp = new File("mods" + seperator + "pokecube" + seperator + "assets" + seperator + modid + seperator
                        + "models" + seperator + "block" + seperator + identifier + "_bark.json");
                fwriter = new FileWriter(temp);
                out = new PrintWriter(fwriter);
                out.println("{");
                out.println("    \"parent\": \"block/cube_all\",");
                out.println("    \"textures\": {");
                out.println("        \"all\": \"" + modid + ":" + texturelocation + "/" + identifier + "\"");
                out.println("    }");
                out.println("}");
                out.close();
                fwriter.close();
            }
            else
            {
                fwriter = new FileWriter(temp);
                out = new PrintWriter(fwriter);
                out.println("{");
                out.println("    \"parent\": \"block/" + parent + "\",");
                out.println("    \"textures\": {");

                if (parent.equalsIgnoreCase("orientable"))
                {
                    out.println("        \"top\": \"" + modid + ":" + texturelocation + "/" + identifier + "top\",");
                    out.println(
                            "        \"bottom\": \"" + modid + ":" + texturelocation + "/" + identifier + "bottom\",");
                    out.println(
                            "        \"front\": \"" + modid + ":" + texturelocation + "/" + identifier + "front\",");
                    out.println("        \"side\": \"" + modid + ":" + texturelocation + "/" + identifier + "side\"");
                }
                else if (parent.equalsIgnoreCase("cube_all") || parent.equalsIgnoreCase("leaves"))
                {
                    out.println("        \"all\": \"" + modid + ":" + texturelocation + "/" + identifier + "\"");
                }
                out.println("    }");
                out.println("}");
                out.close();
                fwriter.close();
            }

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        if (blockstate)
        {
            temp = new File("mods" + seperator + "pokecube" + seperator + "assets" + seperator + modid + seperator
                    + "blockstates");
            if (!temp.exists())
            {
                temp.mkdirs();
            }
            temp = new File("mods" + seperator + "pokecube" + seperator + "assets" + seperator + modid + seperator
                    + "blockstates" + seperator + identifier + ".json");
            try
            {
                fwriter = new FileWriter(temp);
                out = new PrintWriter(fwriter);
                out.println("{");
                out.println("   \"variants\": {");

                if (parent.equalsIgnoreCase("crop"))
                {
                    out.println("       \"age=0\": { \"model\": \"" + modid + ":" + identifier + "_0\" },");
                    out.println("       \"age=1\": { \"model\": \"" + modid + ":" + identifier + "_1\" },");
                    out.println("       \"age=2\": { \"model\": \"" + modid + ":" + identifier + "_2\" },");
                    out.println("       \"age=3\": { \"model\": \"" + modid + ":" + identifier + "_3\" },");
                    out.println("       \"age=4\": { \"model\": \"" + modid + ":" + identifier + "_4\" },");
                    out.println("       \"age=5\": { \"model\": \"" + modid + ":" + identifier + "_5\" },");
                    out.println("       \"age=6\": { \"model\": \"" + modid + ":" + identifier + "_6\" },");
                    out.println("       \"age=7\": { \"model\": \"" + modid + ":" + identifier + "_7\" }");
                }
                else if (parent.equalsIgnoreCase("log"))
                {
                    out.println("       \"axis=y\": { \"model\": \"" + modid + ":" + identifier + "\" },");
                    out.println("       \"axis=z\": { \"model\": \"" + modid + ":" + identifier + "_side\" },");
                    out.println(
                            "       \"axis=x\": { \"model\": \"" + modid + ":" + identifier + "_side\", \"y\": 90  },");
                    out.println("       \"axis=none\": { \"model\": \"" + modid + ":" + identifier + "_bark\" }");
                }
                else if (parent.equalsIgnoreCase("cross"))
                {
                    out.println("   \"normal\": { \"model\": \"" + modid + ":" + identifier + "\" }");
                }
                else
                {
                    out.println("   \"normal\": { \"model\": \"" + modid + ":" + identifier + "\" }");
                }
                out.println("    }");
                out.println("}");
                out.close();
                fwriter.close();

            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

        }

        generateItemJsons(identifier, "block", modid, true);
    }

    public static void generateItemJsons(String identifier, String parent, String modid, boolean blockModel)
    {
        String seperator = System.getProperty("file.separator");
        File temp = new File("mods" + seperator + "pokecube" + seperator + "assets" + seperator + modid + seperator
                + "models" + seperator + "item");
        if (!temp.exists())
        {
            temp.mkdirs();
        }
        try
        {
            temp = new File("mods" + seperator + "pokecube" + seperator + "assets" + seperator + modid + seperator
                    + "models" + seperator + "item" + seperator + identifier + ".json");

            fwriter = new FileWriter(temp);
            out = new PrintWriter(fwriter);
            out.println("{");

            if (blockModel)
            {
                out.println("    \"parent\": \"" + modid + ":" + parent + "/" + identifier + "\",");
            }
            else
            {
                out.println("    \"parent\": \"builtin/generated\",");
                out.println("    \"textures\": {");
                out.println("    	\"layer0\": \"" + modid + ":" + parent + "/" + identifier + "\"");
                out.println("    },");
            }
            String comma = blockModel ? "" : ",";
            out.println("    \"display\": {");
            out.println("    \"thirdperson\": {");
            out.println("        \"rotation\": [ 10, -45, 170 ],");
            out.println("        \"translation\": [ 0, 1.5, -2.75 ],");
            out.println("        \"scale\": [ 0.375, 0.375, 0.375 ]");
            out.println("		}" + comma);
            if (!blockModel)
            {
                out.println("    \"firstperson\": {");
                out.println("        \"rotation\": [0, -135, 25 ],");
                out.println("        \"translation\": [0, 4, 2 ],");
                out.println("        \"scale\": [1.7, 1.7, 1.7]");
                out.println("       }");
            }
            out.println("    }");
            out.println("}");
            out.close();
            fwriter.close();

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
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

    public static void saveConfig()
    {
        Configuration config = new Configuration(configFile);
        config.load();

        config.get(CATEGORY_ADVANCED, "hardMode", hardMode).set(hardMode);
        config.get(CATEGORY_ADVANCED, "semiHMode", semiHardMode).set(semiHardMode);
        config.get(CATEGORY_ADVANCED, "loginGui", guiOnLogin).set(guiOnLogin);
        if (config.hasKey(CATEGORY_ADVANCED, "explosions") || !explosions)
        {
            config.get(CATEGORY_ADVANCED, "advancedOptions", false).set(true);
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
