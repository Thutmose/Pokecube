package pokecube.adventures.handlers;

import static pokecube.core.PokecubeItems.getStack;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import pokecube.adventures.items.bags.RecipeBag;
import pokecube.core.PokecubeItems;

public class RecipeHandler
{

    public static boolean tmRecipe = true;

    private static void addLegendarySpawnerRecipes()
    {

        GameRegistry.addRecipe(getStack("registeelspawner"),
                new Object[] { "RSR", "SRS", "RSR", 'S', Blocks.IRON_BLOCK, 'R', Blocks.REDSTONE_BLOCK });

        GameRegistry.addRecipe(getStack("regicespawner"),
                new Object[] { "III", "IGI", "III", 'I', Blocks.PACKED_ICE, 'G', Blocks.GOLD_BLOCK });

        GameRegistry.addRecipe(getStack("regirockspawner"),
                new Object[] { "OSO", "OOO", "OSO", 'O', Blocks.OBSIDIAN, 'S', Blocks.STONE });

        GameRegistry.addRecipe(getStack("celebispawner"), new Object[] { "MMM", "SRS", "SIS", 'R', Items.REDSTONE, 'S',
                Blocks.LOG, 'M', Blocks.GLASS, 'I', Items.IRON_INGOT });

        GameRegistry.addRecipe(getStack("hoohspawner"), new Object[] { "GGG", "FIF", "GGG", 'F', Items.FEATHER, 'G',
                Blocks.GOLD_BLOCK, 'I', Items.GOLD_INGOT });

        GameRegistry.addRecipe(getStack("kyogrespawner"), new Object[] { "GGG", "FIF", "GGG", 'F', Blocks.ICE, 'G',
                Blocks.LAPIS_BLOCK, 'I', PokecubeItems.getStack("waterstone") });

        GameRegistry.addRecipe(getStack("groudonspawner"), new Object[] { "GGG", "FIF", "GGG", 'F', Blocks.COAL_BLOCK,
                'G', Blocks.REDSTONE_BLOCK, 'I', PokecubeItems.getStack("firestone") });
    }

    public static void register()
    {
        GameRegistry.addRecipe(new RecipeBag());
        if (tmRecipe) GameRegistry.addRecipe(getStack("tm"), new Object[] { "SS ", "SOS", "SRS", 'S', Items.IRON_INGOT,
                'O', Blocks.GLASS_PANE, 'R', Items.REDSTONE });

        // Cloner
        GameRegistry.addRecipe(getStack("cloner"), new Object[] { "III", "SRS", "SMS", 'R', getStack("tradingtable"),
                'S', Blocks.IRON_BLOCK, 'M', new ItemStack(Items.GOLDEN_APPLE, 1, 1), 'I', Items.IRON_INGOT });

        // AFA
        GameRegistry.addRecipe(getStack("afa"), new Object[] { "III", "SRS", "SMS", 'R', getStack("tradingtable"), 'S',
                Blocks.IRON_BLOCK, 'M', Items.REDSTONE, 'I', Items.IRON_INGOT });

        // Target
        GameRegistry.addRecipe(getStack("pokemobTarget"),
                new Object[] { " R ", "ROR", " E ", 'R', Items.REDSTONE, 'O', Blocks.STONE, 'E', Items.EMERALD });
        // Tainer editor
        GameRegistry.addRecipe(getStack("traderSpawner"),
                new Object[] { " R ", "ROR", " E ", 'R', Items.EMERALD, 'O', Blocks.STONE, 'E', Items.EMERALD });
        ItemStack pad = getStack("warppad");
        pad.stackSize = 2;
        // Warp Pad
        GameRegistry.addRecipe(pad, new Object[] { "IEI", "EIE", "IEI", 'I', Blocks.IRON_BLOCK, 'E', Items.ENDER_EYE });

        // Warp Linker
        GameRegistry.addRecipe(getStack("warplinker"),
                new Object[] { " R ", "ROR", " E ", 'R', Items.EMERALD, 'O', Blocks.STONE, 'E', Items.ENDER_EYE });

        // Mega ring
        GameRegistry.addRecipe(getStack("megaring"),
                new Object[] { " S ", "I I", " I ", 'S', getStack("megastone"), 'I', Items.IRON_INGOT });
        GameRegistry.addRecipe(getStack("pokecubebag"),
                new Object[] { "CCC", "COC", "CCC", 'C', Blocks.WOOL, 'O', getStack("pctop").getItem() });

        // Mega Stone
        GameRegistry.addRecipe(getStack("megastone"),
                new Object[] { " D ", "DOD", " D ", 'O', Items.ENDER_EYE, 'D', Items.DIAMOND });

        ItemStack shards18 = getStack("emerald_shard");
        ItemStack shards1 = getStack("emerald_shard");
        shards18.stackSize = 18;
        GameRegistry.addShapelessRecipe(shards18, new ItemStack(Items.EMERALD), new ItemStack(Items.EMERALD));
        GameRegistry.addShapelessRecipe(new ItemStack(Items.EMERALD), shards1, shards1, shards1, shards1, shards1,
                shards1, shards1, shards1, shards1);
        addLegendarySpawnerRecipes();
        // PRIEST
        // VillagerRegistry.instance().registerVillageTradeHandler(2, new
        // VillagerRegistry.IVillageTradeHandler() {
        // @Override
        // public void manipulateTradesForVillager(EntityVillager villager,
        // MerchantRecipeList recipeList, Random random) {
        // int rand = random.nextInt(1);
        // switch (rand) {
        // case 0:
        // recipeList.add(new MerchantRecipe(
        // new ItemStack(Items.emerald, 20),
        // getStack("exp_share")));
        // break;
        // default:
        // break;
        // }
        // }
        // });

    }
}
