package pokecube.adventures.handlers;

import static pokecube.core.PokecubeItems.getStack;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.ShapedOreRecipe;
import pokecube.adventures.items.bags.RecipeBag;
import pokecube.core.PokecubeItems;

public class RecipeHandler
{

    public static boolean tmRecipe = true;

    private static void addLegendarySpawnerRecipes()
    {
        GameRegistry.addRecipe(getStack("registeelspawner"),
                new Object[] { "RSR", "SRS", "RSR", 'S', Blocks.iron_block, 'R', Blocks.redstone_block });

        GameRegistry.addRecipe(getStack("regicespawner"),
                new Object[] { "III", "IGI", "III", 'I', Blocks.packed_ice, 'G', Blocks.gold_block });

        GameRegistry.addRecipe(getStack("regirockspawner"),
                new Object[] { "OSO", "OOO", "OSO", 'O', Blocks.obsidian, 'S', Blocks.stone });

        GameRegistry.addRecipe(getStack("celebispawner"), new Object[] { "MMM", "SRS", "SIS", 'R', Items.redstone, 'S',
                Blocks.log, 'M', Blocks.glass, 'I', Items.iron_ingot });

        GameRegistry.addRecipe(getStack("hoohspawner"), new Object[] { "GGG", "FIF", "GGG", 'F', Items.feather, 'G',
                Blocks.gold_block, 'I', Items.gold_ingot });

        GameRegistry.addRecipe(getStack("kyogrespawner"), new Object[] { "GGG", "FIF", "GGG", 'F', Blocks.ice, 'G',
                Blocks.lapis_block, 'I', PokecubeItems.getStack("waterstone") });

        GameRegistry.addRecipe(getStack("groudonspawner"), new Object[] { "GGG", "FIF", "GGG", 'F', Blocks.coal_block,
                'G', Blocks.redstone_block, 'I', PokecubeItems.getStack("firestone") });
    }

    public static void register()
    {
        GameRegistry.addRecipe(new RecipeBag());
        if (tmRecipe) GameRegistry.addRecipe(getStack("tm"), new Object[] { "SS ", "SOS", "SRS", 'S', Items.iron_ingot,
                'O', Blocks.glass_pane, 'R', Items.redstone });

        // Cloner
        GameRegistry.addRecipe(getStack("cloner"), new Object[] { "III", "SRS", "SMS", 'R', getStack("tradingtable"),
                'S', Blocks.iron_block, 'M', new ItemStack(Items.golden_apple, 1, 1), 'I', Items.iron_ingot });

        // AFA
        GameRegistry.addRecipe(getStack("afa"), new Object[] { "III", "SRS", "SMS", 'R', getStack("tradingtable"), 'S',
                Blocks.iron_block, 'M', Items.redstone, 'I', Items.iron_ingot });

        // Target
        GameRegistry.addRecipe(getStack("pokemobTarget"),
                new Object[] { " R ", "ROR", " E ", 'R', Items.redstone, 'O', Blocks.stone, 'E', Items.emerald });
        // Tainer editor
        GameRegistry.addRecipe(getStack("traderSpawner"),
                new Object[] { " R ", "ROR", " E ", 'R', Items.emerald, 'O', Blocks.stone, 'E', Items.emerald });
        ItemStack pad = getStack("warppad");
        pad.stackSize = 2;
        // Warp Pad
        GameRegistry.addRecipe(pad, new Object[] { "IEI", "EIE", "IEI", 'I', Blocks.iron_block, 'E', Items.ender_eye });

        // Warp Linker
        GameRegistry.addRecipe(getStack("warplinker"),
                new Object[] { " R ", "ROR", " E ", 'R', Items.emerald, 'O', Blocks.stone, 'E', Items.ender_eye });

        // Mega ring
        GameRegistry.addRecipe(getStack("megaring"),
                new Object[] { " S ", "I I", " I ", 'S', getStack("megastone"), 'I', Items.iron_ingot });
        ItemStack output = getStack("pokecubebag");
        GameRegistry.addRecipe(output,
                new Object[] { "CCC", "COC", "CCC", 'C', Blocks.wool, 'O', getStack("pctop").getItem() });

        // Mega Stone
        GameRegistry.addRecipe(getStack("megastone"),
                new Object[] { " D ", "DOD", " D ", 'O', Items.ender_eye, 'D', Items.diamond });

        // RF Siphon
        GameRegistry.addRecipe(new ShapedOreRecipe(PokecubeItems.getBlock("pokesiphon"), new Object[] { "RrR", "rCr",
                "RrR", 'R', Blocks.redstone_block, 'C', PokecubeItems.getBlock("afa"), 'r', Items.redstone }));

        ItemStack shards18 = getStack("emerald_shard");
        ItemStack shards1 = getStack("emerald_shard");
        shards18.stackSize = 18;
        GameRegistry.addShapelessRecipe(shards18, new ItemStack(Items.emerald), new ItemStack(Items.emerald));
        GameRegistry.addShapelessRecipe(new ItemStack(Items.emerald), shards1, shards1, shards1, shards1, shards1,
                shards1, shards1, shards1, shards1);
        addLegendarySpawnerRecipes();

    }
}
