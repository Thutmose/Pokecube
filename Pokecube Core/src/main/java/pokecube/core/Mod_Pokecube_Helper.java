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
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraft.util.math.BlockPos;
import pokecube.core.handlers.ItemHandler;
import pokecube.core.handlers.RecipeHandler;
import pokecube.core.interfaces.IMoveNames;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.ItemTM;
import pokecube.core.moves.TreeRemover;

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

        getTerrain().add(Blocks.dirt);
        getTerrain().add(Blocks.grass);
        getTerrain().add(Blocks.stone);
        getTerrain().add(Blocks.sand);
        getTerrain().add(Blocks.gravel);
        getTerrain().add(Blocks.netherrack);
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
