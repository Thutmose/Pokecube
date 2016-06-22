package pokecube.core.handlers;

import static pokecube.core.PokecubeItems.getEmptyCube;
import static pokecube.core.PokecubeItems.getStack;
import static pokecube.core.PokecubeItems.luckyEgg;
import static pokecube.core.PokecubeItems.pokecenter;
import static pokecube.core.PokecubeItems.pokedex;
import static pokecube.core.PokecubeItems.repelBlock;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.RecipeSorter;
import net.minecraftforge.oredict.RecipeSorter.Category;
import pokecube.core.Mod_Pokecube_Helper;
import pokecube.core.PokecubeItems;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.berries.BerryManager;
import pokecube.core.items.berries.RecipeBrewBerries;
import pokecube.core.items.pokecubes.RecipePokeseals;
import pokecube.core.items.revive.RecipeRevive;

public class RecipeHandler extends Mod_Pokecube_Helper
{
    public static void initRecipes()
    {
        Item pokecube = getEmptyCube(0);
        Item greatcube = getEmptyCube(1);
        Item ultracube = getEmptyCube(2);
        Item pokeseal = getEmptyCube(-2);
        Item duskcube = getEmptyCube(5);
        Item quickcube = getEmptyCube(6);
        Item timercube = getEmptyCube(7);
        Item netcube = getEmptyCube(8);
        Item nestcube = getEmptyCube(9);
        Item divecube = getEmptyCube(10);
        Item repeatcube = getEmptyCube(11);
        Item premiercube = getEmptyCube(12);
        Item snagcube = getEmptyCube(99);

        GameRegistry.addShapelessRecipe(new ItemStack(Items.pumpkin_pie),
                new Object[] { Blocks.pumpkin, Items.sugar, luckyEgg });
        GameRegistry.addRecipe(new ItemStack(Items.cake, 1), new Object[] { "AAA", "BEB", "CCC", 'A', Items.milk_bucket,
                'B', Items.sugar, 'C', Items.wheat, 'E', luckyEgg });

        if (PokecubeMod.core.getConfig().tableRecipe)
            GameRegistry.addRecipe(new ItemStack(pokecenter), new Object[] { "III", "SRS", "SMS", 'R', Items.redstone,
                    'S', Blocks.stone, 'M', Items.milk_bucket, 'I', Items.iron_ingot });
        if (PokecubeMod.core.getConfig().tableRecipe) GameRegistry.addRecipe(new ItemStack(pokecenter), new Object[] {
                "III", "SRS", "SES", 'R', Items.redstone, 'S', Blocks.stone, 'E', luckyEgg, 'I', Items.iron_ingot });
        GameRegistry.addRecipe(new ItemStack(pokedex),
                new Object[] { "PRR", "RBR", "RRR", 'P', pokecube, 'R', Items.redstone, 'B', Items.book });

        GameRegistry.addRecipe(new ItemStack(Items.saddle),
                new Object[] { " L ", "L L", "I I", 'L', Items.leather, 'I', Items.iron_ingot });

        ItemStack charcoal = new ItemStack(Items.coal, 1, 1);
        ItemStack red = new ItemStack(Items.dye, 1, 1);
        ItemStack green = new ItemStack(Items.dye, 1, 2);
        ItemStack lapis = new ItemStack(Items.dye, 1, 4);
        ItemStack sand = new ItemStack(Blocks.sand, 1, 1);

        GameRegistry.addRecipe(new ItemStack(pokecube, 6), new Object[] { "RRR", "CBC", "III", 'R', Items.redstone, 'C',
                Items.coal, 'B', Blocks.stone_button, 'I', Items.iron_ingot });
        GameRegistry.addRecipe(new ItemStack(pokecube, 6), new Object[] { "RRR", "CBC", "III", 'R', Items.redstone, 'C',
                charcoal, 'B', Blocks.stone_button, 'I', Items.iron_ingot });
        GameRegistry.addRecipe(new ItemStack(greatcube, 4), new Object[] { "RAR", "CBC", "III", 'R', Items.redstone,
                'C', Items.coal, 'A', lapis, 'B', Blocks.stone_button, 'I', Items.iron_ingot });
        GameRegistry.addRecipe(new ItemStack(greatcube, 4), new Object[] { "RAR", "CBC", "III", 'R', Items.redstone,
                'C', charcoal, 'A', lapis, 'B', Blocks.stone_button, 'I', Items.iron_ingot });
        GameRegistry.addRecipe(new ItemStack(ultracube, 3), new Object[] { "GFG", "CBC", "III", 'F', Items.flint, 'C',
                Items.coal, 'G', Items.gold_ingot, 'B', Blocks.stone_button, 'I', Items.iron_ingot });
        GameRegistry.addRecipe(new ItemStack(ultracube, 3), new Object[] { "GFG", "CBC", "III", 'F', Items.flint, 'C',
                charcoal, 'G', Items.gold_ingot, 'B', Blocks.stone_button, 'I', Items.iron_ingot });
        GameRegistry.addRecipe(new ItemStack(snagcube, 3), new Object[] { "GFG", "CBC", "III", 'F', Items.iron_ingot,
                'C', Items.ghast_tear, 'G', Items.iron_ingot, 'B', Blocks.stone_button, 'I', Items.iron_ingot });
        GameRegistry.addRecipe(new ItemStack(duskcube, 1),
                new Object[] { "CDC", "DPD", "CDC", 'C', Items.coal, 'D', green, 'P', premiercube });
        GameRegistry.addRecipe(new ItemStack(quickcube, 1),
                new Object[] { "GLG", "LPL", "GLG", 'G', Items.gold_ingot, 'L', lapis, 'P', premiercube });
        GameRegistry.addRecipe(new ItemStack(timercube, 1),
                new Object[] { "CRC", "CPC", "QQQ", 'C', Items.coal, 'R', sand, 'P', premiercube, 'Q', Items.quartz });
        GameRegistry.addRecipe(new ItemStack(netcube, 1),
                new Object[] { "SSS", "SPS", "SSS", 'S', Items.string, 'P', premiercube });
        GameRegistry.addRecipe(new ItemStack(nestcube, 1),
                new Object[] { "TGT", "GPG", "TTT", 'T', Blocks.tallgrass, 'P', premiercube, 'G', Items.gold_ingot });
        GameRegistry.addRecipe(new ItemStack(divecube, 1),
                new Object[] { "BBB", "LPL", "BBB", 'B', Items.water_bucket, 'P', premiercube, 'L', lapis });
        GameRegistry.addRecipe(new ItemStack(nestcube, 1),
                new Object[] { "TGT", "GPG", "TTT", 'T', Blocks.tallgrass, 'P', premiercube, 'G', Items.gold_ingot });
        GameRegistry.addRecipe(new ItemStack(repeatcube, 1), new Object[] { "RRR", "CPC", "III", 'R', Items.redstone,
                'P', premiercube, 'C', Items.coal, 'I', Items.iron_ingot });
        GameRegistry.addRecipe(new ItemStack(premiercube, 2),
                new Object[] { "III", "CBC", "III", 'B', Blocks.stone_button, 'C', Items.coal, 'I', Items.iron_ingot });
        GameRegistry.addRecipe(new ItemStack(pokeseal, 2), new Object[] { "GGG", "RBR", "LLL", 'G', Blocks.glass, 'R',
                red, 'B', Blocks.stone_button, 'L', lapis });

        GameRegistry.addRecipe(PokecubeItems.getStack("waterstone"),
                new Object[] { "SSS", "SWS", "SSS", 'S', Blocks.stone, 'W', Items.water_bucket });
        GameRegistry.addRecipe(PokecubeItems.getStack("firestone"),
                new Object[] { "SSS", "SWS", "SSS", 'S', Blocks.stone, 'W', Blocks.torch });
        GameRegistry.addRecipe(PokecubeItems.getStack("leafstone"),
                new Object[] { "SSS", "SWS", "SSS", 'S', Blocks.stone, 'W', Blocks.sapling });
        GameRegistry.addRecipe(PokecubeItems.getStack("thunderstone"),
                new Object[] { "SSS", "SWS", "SSS", 'S', Blocks.stone, 'W', Items.redstone });
        GameRegistry.addRecipe(PokecubeItems.getStack("everstone"),
                new Object[] { "SSS", "SOS", "SSS", 'S', Blocks.stone, 'O', Blocks.obsidian });
        GameRegistry.addRecipe(PokecubeItems.getStack("duskstone"),
                new Object[] { "SSS", "STS", "SSS", 'S', Blocks.soul_sand, 'T', Blocks.torch });
        GameRegistry.addRecipe(PokecubeItems.getStack("dawnstone"),
                new Object[] { "QQQ", "QTQ", "QQQ", 'Q', new ItemStack(Items.dye, 1, 4), 'T', Blocks.torch });
        GameRegistry.addRecipe(PokecubeItems.getStack("shinystone"),
                new Object[] { "QQQ", "QGQ", "QQQ", 'Q', Items.quartz, 'G', Items.glowstone_dust });

        GameRegistry.addRecipe(new ItemStack(repelBlock), new Object[] { "JR", "RJ", 'J',
                BerryManager.getBerryItem("jaboca"), 'R', BerryManager.getBerryItem("rowap"), });

        GameRegistry.addRecipe(new RecipePokeseals());
        GameRegistry.addRecipe(new RecipeRevive());

        RecipeSorter.register("pokecube:pokeseals", RecipePokeseals.class, Category.SHAPELESS,
                "after:minecraft:shaped");
        RecipeSorter.register("pokecube:revive", RecipeRevive.class, Category.SHAPELESS, "after:minecraft:shaped");

        // TOP
        GameRegistry.addRecipe(getStack("pctop"), new Object[] { "MMM", "SRS", "SIS", 'R', Items.redstone, 'S',
                Blocks.iron_block, 'M', Blocks.glass, 'I', Items.iron_ingot });
        // BASE
        GameRegistry.addRecipe(getStack("pcbase"), new Object[] { "III", "SRS", "SSS", 'R', Items.redstone, 'S',
                Blocks.iron_block, 'I', Items.iron_ingot });

        // Trading Table
        GameRegistry.addRecipe(getStack("tradingtable"), new Object[] { "III", "SRS", "SMS", 'R', Items.redstone, 'S',
                Blocks.stone, 'M', Items.emerald, 'I', Items.iron_ingot });

        BrewingRecipeRegistry.addRecipe(new RecipeBrewBerries());
    }
}
