/**
 *
 */
package pokecube.core;

import static pokecube.core.PokecubeCore.getWorld;
import static pokecube.core.PokecubeItems.getItem;
import static pokecube.core.PokecubeItems.removeFromHoldables;
import static pokecube.core.handlers.Config.GUICHOOSEFIRSTPOKEMOB_ID;
import static pokecube.core.handlers.Config.GUIDISPLAYPOKECUBEINFO_ID;
import static pokecube.core.handlers.Config.GUIDISPLAYTELEPORTINFO_ID;
import static pokecube.core.handlers.Config.GUIPC_ID;
import static pokecube.core.handlers.Config.GUIPOKECENTER_ID;
import static pokecube.core.handlers.Config.GUIPOKEDEX_ID;
import static pokecube.core.handlers.Config.GUIPOKEMOBSPAWNER_ID;
import static pokecube.core.handlers.Config.GUIPOKEMOB_ID;
import static pokecube.core.handlers.Config.GUITRADINGTABLE_ID;
import static pokecube.core.interfaces.PokecubeMod.HMs;

import java.util.HashSet;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.pattern.BlockMatcher;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import pokecube.core.handlers.ItemHandler;
import pokecube.core.handlers.RecipeHandler;
import pokecube.core.interfaces.IMoveNames;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.ItemTM;
import pokecube.core.moves.TreeRemover;
import thut.api.maths.ExplosionCustom;

/** @author Manchou */
public class Mod_Pokecube_Helper
{

    public static final String   CATEGORY_ADVANCED = "advanced";

    public static HashSet<Block> allBlocks         = new HashSet<Block>();

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

    private static Block getBlock(String name)
    {
        Block b = null;
        b = Block.getBlockFromName(name.replace("tile.", ""));
        return b;
    }

    public static List<Block> getCaveBlocks()
    {
        return PokecubeMod.core.getConfig().getCaveBlocks();
    }

    public static List<Block> getRocks()
    {
        return PokecubeMod.core.getConfig().getRocks();
    }

    public static List<Block> getSurfaceBlocks()
    {
        return PokecubeMod.core.getConfig().getSurfaceBlocks();
    }

    public static List<Block> getTerrain()
    {
        return PokecubeMod.core.getConfig().getTerrain();
    }

    static void initLists()
    {
        getCaveBlocks().add(Blocks.STONE);
        getCaveBlocks().add(Blocks.DIRT);
        getCaveBlocks().add(Blocks.GRAVEL);
        getCaveBlocks().add(Blocks.IRON_ORE);
        getCaveBlocks().add(Blocks.COAL_ORE);
        getCaveBlocks().add(Blocks.DIAMOND_ORE);
        getCaveBlocks().add(Blocks.REDSTONE_ORE);
        getCaveBlocks().add(Blocks.GOLD_ORE);
        getCaveBlocks().add(Blocks.GRAVEL);
        getCaveBlocks().add(Blocks.NETHERRACK);
        getCaveBlocks().add(Blocks.NETHER_BRICK);

        getRocks().add(Blocks.STONE);
        getRocks().add(Blocks.IRON_ORE);
        getRocks().add(Blocks.COAL_ORE);
        getRocks().add(Blocks.DIAMOND_ORE);
        getRocks().add(Blocks.REDSTONE_ORE);
        getRocks().add(Blocks.EMERALD_ORE);
        getRocks().add(Blocks.LAPIS_ORE);
        getRocks().add(Blocks.QUARTZ_ORE);
        getRocks().add(Blocks.GOLD_ORE);
        getRocks().add(Blocks.MOSSY_COBBLESTONE);
        getRocks().add(Blocks.COBBLESTONE);
        getRocks().add(Blocks.NETHERRACK);

        getSurfaceBlocks().add(Blocks.STONE);
        getSurfaceBlocks().add(Blocks.SAND);
        getSurfaceBlocks().add(Blocks.DIRT);
        getSurfaceBlocks().add(Blocks.GRAVEL);
        getSurfaceBlocks().add(Blocks.NETHERRACK);
        getSurfaceBlocks().add(Blocks.GRASS);
        getSurfaceBlocks().add(Blocks.LEAVES);
        getSurfaceBlocks().add(Blocks.HARDENED_CLAY);
        getSurfaceBlocks().add(Blocks.STAINED_HARDENED_CLAY);
        getSurfaceBlocks().add(Blocks.MYCELIUM);

        if(ExplosionCustom.dust!=null) getSurfaceBlocks().add(ExplosionCustom.dust);
        if(ExplosionCustom.melt!=null) getSurfaceBlocks().add(ExplosionCustom.melt);
        if(ExplosionCustom.solidmelt!=null) getSurfaceBlocks().add(ExplosionCustom.solidmelt);

        getTerrain().add(Blocks.DIRT);
        getTerrain().add(Blocks.GRASS);
        getTerrain().add(Blocks.STONE);
        getTerrain().add(Blocks.SAND);
        getTerrain().add(Blocks.GRAVEL);
        getTerrain().add(Blocks.NETHERRACK);
    }

    public static void initLoots()
    {
        ItemStack cut = new ItemStack(getItem("tm"));
        ItemTM.addMoveToStack(IMoveNames.MOVE_CUT, cut);
        HMs.add(cut);
        ItemStack flash = new ItemStack(getItem("tm"));
        ItemTM.addMoveToStack(IMoveNames.MOVE_FLASH, flash);
        HMs.add(flash);
        ItemStack dig = new ItemStack(getItem("tm"));
        ItemTM.addMoveToStack(IMoveNames.MOVE_DIG, dig);
        HMs.add(dig);
        ItemStack rockSmash = new ItemStack(getItem("tm"));
        ItemTM.addMoveToStack(IMoveNames.MOVE_ROCKSMASH, rockSmash);
        HMs.add(rockSmash);
        // TODO loot tables
        // WeightedRandomChestContent cutContent = new
        // WeightedRandomChestContent(cut, 1, 1, 20);
        // ChestGenHooks.addItem(ChestGenHooks.PYRAMID_JUNGLE_CHEST,
        // cutContent);
        //
        // WeightedRandomChestContent flashContent = new
        // WeightedRandomChestContent(flash, 1, 1, 20);
        // ChestGenHooks.addItem(ChestGenHooks.DUNGEON_CHEST, flashContent);
        //
        // WeightedRandomChestContent digContent = new
        // WeightedRandomChestContent(dig, 1, 1, 20);
        // ChestGenHooks.addItem(ChestGenHooks.MINESHAFT_CORRIDOR, digContent);
        //
        // WeightedRandomChestContent smashContent = new
        // WeightedRandomChestContent(rockSmash, 1, 1, 20);
        // ChestGenHooks.addItem(ChestGenHooks.MINESHAFT_CORRIDOR,
        // smashContent);
        //
        // // TODO do this for each stone, instead of just one.
        // ItemStack stone = new ItemStack(getItem("megastone"));
        // WeightedRandomChestContent stoneContent = new
        // WeightedRandomChestContent(stone, 1, 1, 20);
        // ChestGenHooks.addItem(ChestGenHooks.VILLAGE_BLACKSMITH,
        // stoneContent);
        // ChestGenHooks.addItem(ChestGenHooks.DUNGEON_CHEST, stoneContent);
        //
        // ItemStack ring = new ItemStack(getItem("megaring"));
        // WeightedRandomChestContent ringContent = new
        // WeightedRandomChestContent(ring, 1, 1, 5);
        // ChestGenHooks.addItem(ChestGenHooks.VILLAGE_BLACKSMITH, ringContent);
        // ChestGenHooks.addItem(ChestGenHooks.DUNGEON_CHEST, ringContent);
    }

    public void addItems()
    {
        ItemHandler.addItems(this);
    }

    public void addVillagerTrades()
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

    public void initAllBlocks()
    {
        allBlocks.clear();
        initLists();
        for (int i = 0; i < 4096; i++)
        {
            if (Block.getBlockById(i) != null) allBlocks.add(Block.getBlockById(i));
        }
    }

    public void postInit()
    {
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

        for (Block b : allBlocks)
        {
            try
            {
                if (getCaveBlocks().contains(b)) continue;

                if (BlockMatcher.forBlock(Blocks.STONE).apply(b.getDefaultState())) getCaveBlocks().add(b);
                else if (BlockMatcher.forBlock(Blocks.NETHERRACK).apply(b.getDefaultState())) getCaveBlocks().add(b);
                else if (BlockMatcher.forBlock(Blocks.DIRT).apply(b.getDefaultState())) getCaveBlocks().add(b);
                else if (BlockMatcher.forBlock(Blocks.SAND).apply(b.getDefaultState())) getCaveBlocks().add(b);
            }
            catch (Exception e)
            {
            }
        }
        for (Block b : getCaveBlocks())
        {
            if (b.getDefaultState().getMaterial() == Material.ROCK && !getRocks().contains(b)) getRocks().add(b);
            if (!getSurfaceBlocks().contains(b)) getSurfaceBlocks().add(b);
        }

        RecipeHandler.initRecipes();

        addToList(PokecubeMod.core.getConfig().getCaveBlocks(), PokecubeMod.core.getConfig().cave);
        addToList(PokecubeMod.core.getConfig().getSurfaceBlocks(), PokecubeMod.core.getConfig().surface);
        addToList(PokecubeMod.core.getConfig().getRocks(), PokecubeMod.core.getConfig().rock);
        addToList(TreeRemover.woodTypes, PokecubeMod.core.getConfig().trees);
        addToList(TreeRemover.plantTypes, PokecubeMod.core.getConfig().plants);
        addToList(PokecubeMod.core.getConfig().getTerrain(), PokecubeMod.core.getConfig().terrains);

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
}
