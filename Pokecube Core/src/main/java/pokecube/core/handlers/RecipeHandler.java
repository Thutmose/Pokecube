package pokecube.core.handlers;

import static pokecube.core.PokecubeItems.getEmptyCube;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.RecipeSorter;
import net.minecraftforge.oredict.RecipeSorter.Category;
import pokecube.core.Mod_Pokecube_Helper;
import pokecube.core.database.Database;
import pokecube.core.items.berries.RecipeBrewBerries;
import pokecube.core.items.megastuff.RecipeWearables;
import pokecube.core.items.pokecubes.RecipePokeseals;
import pokecube.core.items.revive.RecipeRevive;

public class RecipeHandler extends Mod_Pokecube_Helper
{
    public static void initRecipes()
    {
        Database.loadRecipes();
        Item snagcube = getEmptyCube(99);
        GameRegistry.addRecipe(new ItemStack(snagcube, 3), new Object[] { "GFG", "CBC", "III", 'F', Items.IRON_INGOT,
                'C', Items.GHAST_TEAR, 'G', Items.IRON_INGOT, 'B', Blocks.STONE_BUTTON, 'I', Items.IRON_INGOT });
        RecipeSorter.register("pokecube:rings", RecipeWearables.class, Category.SHAPELESS, "after:minecraft:shapeless");
        RecipeSorter.register("pokecube:pokeseals", RecipePokeseals.class, Category.SHAPELESS,
                "after:minecraft:shapeless");
        RecipeSorter.register("pokecube:revive", RecipeRevive.class, Category.SHAPELESS, "after:minecraft:shaped");
        GameRegistry.addRecipe(new RecipeWearables());
        GameRegistry.addRecipe(new RecipePokeseals());
        GameRegistry.addRecipe(new RecipeRevive());
        BrewingRecipeRegistry.addRecipe(new RecipeBrewBerries());
    }
}
