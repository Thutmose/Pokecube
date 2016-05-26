package pokecube.core.handlers;

import static pokecube.core.PokecubeItems.dawnstone;
import static pokecube.core.PokecubeItems.duskstone;
import static pokecube.core.PokecubeItems.everstone;
import static pokecube.core.PokecubeItems.firestone;
import static pokecube.core.PokecubeItems.getEmptyCube;
import static pokecube.core.PokecubeItems.getStack;
import static pokecube.core.PokecubeItems.leafstone;
import static pokecube.core.PokecubeItems.luckyEgg;
import static pokecube.core.PokecubeItems.pokecenter;
import static pokecube.core.PokecubeItems.pokedex;
import static pokecube.core.PokecubeItems.repelBlock;
import static pokecube.core.PokecubeItems.shinystone;
import static pokecube.core.PokecubeItems.thunderstone;
import static pokecube.core.PokecubeItems.waterstone;

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

        GameRegistry.addShapelessRecipe(new ItemStack(Items.PUMPKIN_PIE),
                new Object[] { Blocks.PUMPKIN, Items.SUGAR, luckyEgg });
        GameRegistry.addRecipe(new ItemStack(Items.CAKE, 1), new Object[] { "AAA", "BEB", "CCC", 'A', Items.MILK_BUCKET,
                'B', Items.SUGAR, 'C', Items.WHEAT, 'E', luckyEgg });

        if (PokecubeMod.core.getConfig().tableRecipe) GameRegistry.addRecipe(new ItemStack(pokecenter), new Object[] { "III", "SRS", "SMS", 'R',
                Items.REDSTONE, 'S', Blocks.STONE, 'M', Items.MILK_BUCKET, 'I', Items.IRON_INGOT });
        if (PokecubeMod.core.getConfig().tableRecipe) GameRegistry.addRecipe(new ItemStack(pokecenter), new Object[] { "III", "SRS", "SES", 'R',
                Items.REDSTONE, 'S', Blocks.STONE, 'E', luckyEgg, 'I', Items.IRON_INGOT });
        GameRegistry.addRecipe(new ItemStack(pokedex),
                new Object[] { "PRR", "RBR", "RRR", 'P', pokecube, 'R', Items.REDSTONE, 'B', Items.BOOK });

        GameRegistry.addRecipe(new ItemStack(Items.SADDLE),
                new Object[] { " L ", "L L", "I I", 'L', Items.LEATHER, 'I', Items.IRON_INGOT });

        ItemStack charcoal = new ItemStack(Items.COAL, 1, 1);
        ItemStack red = new ItemStack(Items.DYE, 1, 1);
        ItemStack green = new ItemStack(Items.DYE, 1, 2);
        ItemStack lapis = new ItemStack(Items.DYE, 1, 4);
        ItemStack sand = new ItemStack(Blocks.SAND, 1, 1);

        GameRegistry.addRecipe(new ItemStack(pokecube, 6), new Object[] { "RRR", "CBC", "III", 'R', Items.REDSTONE, 'C',
                Items.COAL, 'B', Blocks.STONE_BUTTON, 'I', Items.IRON_INGOT });
        GameRegistry.addRecipe(new ItemStack(pokecube, 6), new Object[] { "RRR", "CBC", "III", 'R', Items.REDSTONE, 'C',
                charcoal, 'B', Blocks.STONE_BUTTON, 'I', Items.IRON_INGOT });
        GameRegistry.addRecipe(new ItemStack(greatcube, 4), new Object[] { "RAR", "CBC", "III", 'R', Items.REDSTONE,
                'C', Items.COAL, 'A', lapis, 'B', Blocks.STONE_BUTTON, 'I', Items.IRON_INGOT });
        GameRegistry.addRecipe(new ItemStack(greatcube, 4), new Object[] { "RAR", "CBC", "III", 'R', Items.REDSTONE,
                'C', charcoal, 'A', lapis, 'B', Blocks.STONE_BUTTON, 'I', Items.IRON_INGOT });
        GameRegistry.addRecipe(new ItemStack(ultracube, 3), new Object[] { "GFG", "CBC", "III", 'F', Items.FLINT, 'C',
                Items.COAL, 'G', Items.GOLD_INGOT, 'B', Blocks.STONE_BUTTON, 'I', Items.IRON_INGOT });
        GameRegistry.addRecipe(new ItemStack(ultracube, 3), new Object[] { "GFG", "CBC", "III", 'F', Items.FLINT, 'C',
                charcoal, 'G', Items.GOLD_INGOT, 'B', Blocks.STONE_BUTTON, 'I', Items.IRON_INGOT });
        GameRegistry.addRecipe(new ItemStack(snagcube, 3), new Object[] { "GFG", "CBC", "III", 'F', Items.IRON_INGOT,
                'C', Items.GHAST_TEAR, 'G', Items.IRON_INGOT, 'B', Blocks.STONE_BUTTON, 'I', Items.IRON_INGOT });
        GameRegistry.addRecipe(new ItemStack(duskcube, 1),
                new Object[] { "CDC", "DPD", "CDC", 'C', Items.COAL, 'D', green, 'P', premiercube });
        GameRegistry.addRecipe(new ItemStack(quickcube, 1),
                new Object[] { "GLG", "LPL", "GLG", 'G', Items.GOLD_INGOT, 'L', lapis, 'P', premiercube });
        GameRegistry.addRecipe(new ItemStack(timercube, 1),
                new Object[] { "CRC", "CPC", "QQQ", 'C', Items.COAL, 'R', sand, 'P', premiercube, 'Q', Items.QUARTZ });
        GameRegistry.addRecipe(new ItemStack(netcube, 1),
                new Object[] { "SSS", "SPS", "SSS", 'S', Items.STRING, 'P', premiercube });
        GameRegistry.addRecipe(new ItemStack(nestcube, 1),
                new Object[] { "TGT", "GPG", "TTT", 'T', Blocks.TALLGRASS, 'P', premiercube, 'G', Items.GOLD_INGOT });
        GameRegistry.addRecipe(new ItemStack(divecube, 1),
                new Object[] { "BBB", "LPL", "BBB", 'B', Items.WATER_BUCKET, 'P', premiercube, 'L', lapis });
        GameRegistry.addRecipe(new ItemStack(nestcube, 1),
                new Object[] { "TGT", "GPG", "TTT", 'T', Blocks.TALLGRASS, 'P', premiercube, 'G', Items.GOLD_INGOT });
        GameRegistry.addRecipe(new ItemStack(repeatcube, 1), new Object[] { "RRR", "CPC", "III", 'R', Items.REDSTONE,
                'P', premiercube, 'C', Items.COAL, 'I', Items.IRON_INGOT });
        GameRegistry.addRecipe(new ItemStack(premiercube, 2),
                new Object[] { "III", "CBC", "III", 'B', Blocks.STONE_BUTTON, 'C', Items.COAL, 'I', Items.IRON_INGOT });
        GameRegistry.addRecipe(new ItemStack(pokeseal, 2), new Object[] { "GGG", "RBR", "LLL", 'G', Blocks.GLASS, 'R',
                red, 'B', Blocks.STONE_BUTTON, 'L', lapis });

        GameRegistry.addRecipe(new ItemStack(waterstone),
                new Object[] { "SSS", "SWS", "SSS", 'S', Blocks.STONE, 'W', Items.WATER_BUCKET });
        GameRegistry.addRecipe(new ItemStack(firestone),
                new Object[] { "SSS", "SWS", "SSS", 'S', Blocks.STONE, 'W', Blocks.TORCH });
        GameRegistry.addRecipe(new ItemStack(leafstone),
                new Object[] { "SSS", "SWS", "SSS", 'S', Blocks.STONE, 'W', Blocks.SAPLING });
        GameRegistry.addRecipe(new ItemStack(thunderstone),
                new Object[] { "SSS", "SWS", "SSS", 'S', Blocks.STONE, 'W', Items.REDSTONE });
        GameRegistry.addRecipe(new ItemStack(everstone),
                new Object[] { "SSS", "SOS", "SSS", 'S', Blocks.STONE, 'O', Blocks.OBSIDIAN });
        GameRegistry.addRecipe(new ItemStack(duskstone),
                new Object[] { "SSS", "STS", "SSS", 'S', Blocks.SOUL_SAND, 'T', Blocks.TORCH });
        GameRegistry.addRecipe(new ItemStack(dawnstone),
                new Object[] { "QQQ", "QTQ", "QQQ", 'Q', new ItemStack(Items.DYE, 1, 4), 'T', Blocks.TORCH });
        GameRegistry.addRecipe(new ItemStack(shinystone),
                new Object[] { "QQQ", "QGQ", "QQQ", 'Q', Items.QUARTZ, 'G', Items.GLOWSTONE_DUST });

        GameRegistry.addRecipe(new ItemStack(repelBlock), new Object[] { "JR", "RJ", 'J',
                BerryManager.getBerryItem("jaboca"), 'R', BerryManager.getBerryItem("rowap"), });

        for (Integer i : PokecubeItems.pokecubes.keySet())
        {
            GameRegistry.addShapelessRecipe(new ItemStack(PokecubeItems.getFilledCube(i), 1, 1),
                    PokecubeItems.getStack("revive"), new ItemStack(PokecubeItems.getFilledCube(i), 1, 32767));
        }
//        GameRegistry.addRecipe(new RecipePokeseals());
//        GameRegistry.addRecipe(new RecipeRevive());

        RecipeSorter.register("pokecube:pokeseals", RecipePokeseals.class, Category.SHAPELESS,
                "after:minecraft:shaped");
        RecipeSorter.register("pokecube:revive", RecipeRevive.class, Category.SHAPELESS, "after:minecraft:shaped");
        

        // TOP
        GameRegistry.addRecipe(getStack("pctop"), new Object[] { "MMM", "SRS", "SIS", 'R', Items.REDSTONE, 'S',
                Blocks.IRON_BLOCK, 'M', Blocks.GLASS, 'I', Items.IRON_INGOT });
        // BASE
        GameRegistry.addRecipe(getStack("pcbase"), new Object[] { "III", "SRS", "SSS", 'R', Items.REDSTONE, 'S',
                Blocks.IRON_BLOCK, 'I', Items.IRON_INGOT });

        // Trading Table
        GameRegistry.addRecipe(getStack("tradingtable"), new Object[] { "III", "SRS", "SMS", 'R', Items.REDSTONE, 'S',
                Blocks.STONE, 'M', Items.EMERALD, 'I', Items.IRON_INGOT });

        BrewingRecipeRegistry.addRecipe(new RecipeBrewBerries());
    }
}
