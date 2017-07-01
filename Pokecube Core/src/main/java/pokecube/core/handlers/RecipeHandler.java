package pokecube.core.handlers;

import static pokecube.core.PokecubeItems.getEmptyCube;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.registries.GameData;
import pokecube.core.Mod_Pokecube_Helper;
import pokecube.core.database.Database;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.berries.RecipeBrewBerries;
import pokecube.core.items.megastuff.RecipeWearables;
import pokecube.core.items.pokecubes.RecipePokeseals;
import pokecube.core.items.revive.RecipeRevive;

public class RecipeHandler extends Mod_Pokecube_Helper
{
    public static void initRecipes(Object event)
    {
        Database.loadRecipes(event);
        Item snagcube = getEmptyCube(99);
        ResourceLocation group = new ResourceLocation(PokecubeMod.ID, "defaults");
        IRecipe recipe = new ShapedOreRecipe(group, new ItemStack(snagcube, 3),
                new Object[] { "GFG", "CBC", "III", 'F', Items.IRON_INGOT, 'C', Items.GHAST_TEAR, 'G', Items.IRON_INGOT,
                        'B', Blocks.STONE_BUTTON, 'I', Items.IRON_INGOT });
        GameData.register_impl(recipe.setRegistryName(new ResourceLocation(PokecubeMod.ID, "snagcube")));

        // RecipeSorter.register("pokecube:rings", RecipeWearables.class,
        // Category.SHAPELESS, "after:minecraft:shapeless");
        // RecipeSorter.register("pokecube:pokeseals", RecipePokeseals.class,
        // Category.SHAPELESS,
        // "after:minecraft:shapeless");
        // RecipeSorter.register("pokecube:revive", RecipeRevive.class,
        // Category.SHAPELESS, "after:minecraft:shaped");

        OreDictionary.registerOre("logWood", new ItemStack(ItemHandler.log0, 1, OreDictionary.WILDCARD_VALUE));
        OreDictionary.registerOre("logWood", new ItemStack(ItemHandler.log1, 1, OreDictionary.WILDCARD_VALUE));
        OreDictionary.registerOre("plankWood", new ItemStack(ItemHandler.plank0, 1, OreDictionary.WILDCARD_VALUE));
        for (int i = 0; i < 4; i++)
        {
            recipe = new ShapelessRecipes(group.toString(), new ItemStack(ItemHandler.plank0, 4, i),
                    NonNullList.<Ingredient> withSize(1, Ingredient.fromStacks(new ItemStack(ItemHandler.log0, 1, i))));
            GameData.register_impl(recipe.setRegistryName(new ResourceLocation(PokecubeMod.ID, "log0" + i)));
        }
        for (int i = 0; i < 2; i++)
        {
            recipe = new ShapelessRecipes(group.toString(), new ItemStack(ItemHandler.plank0, 4, i + 4),
                    NonNullList.<Ingredient> withSize(1, Ingredient.fromStacks(new ItemStack(ItemHandler.log1, 1, i))));
            GameData.register_impl(recipe.setRegistryName(new ResourceLocation(PokecubeMod.ID, "log0" + (i + 4))));
        }

        GameData.register_impl(
                new RecipeWearables().setRegistryName(new ResourceLocation(PokecubeMod.ID, "wearables")));
        GameData.register_impl(
                new RecipePokeseals().setRegistryName(new ResourceLocation(PokecubeMod.ID, "pokeseals")));
        GameData.register_impl(new RecipeRevive().setRegistryName(new ResourceLocation(PokecubeMod.ID, "revive")));
        BrewingRecipeRegistry.addRecipe(new RecipeBrewBerries());
    }
}
