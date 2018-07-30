package pokecube.core.handlers;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.registries.GameData;
import pokecube.core.Mod_Pokecube_Helper;
import pokecube.core.database.Database;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.berries.RecipeBrewBerries;
import pokecube.core.items.pokecubes.RecipePokeseals;
import pokecube.core.items.revive.RecipeRevive;

public class RecipeHandler extends Mod_Pokecube_Helper
{
    public static void initRecipes(Object event)
    {
        Database.loadRecipes(event);
        ResourceLocation group = new ResourceLocation(PokecubeMod.ID, "defaults");
        IRecipe recipe;

        for (String s : ItemGenerator.logs.keySet())
        {
            OreDictionary.registerOre("logWood", ItemGenerator.logs.get(s));
            OreDictionary.registerOre("logWood", ItemGenerator.barks.get(s));
            OreDictionary.registerOre("plankWood", ItemGenerator.planks.get(s));
            recipe = new ShapelessRecipes(group.toString(), new ItemStack(ItemGenerator.planks.get(s), 4), NonNullList
                    .<Ingredient> withSize(1, Ingredient.fromStacks(new ItemStack(ItemGenerator.logs.get(s)))));
            GameData.register_impl(recipe.setRegistryName(new ResourceLocation(PokecubeMod.ID, s + "_0")));
            recipe = new ShapelessRecipes(group.toString(), new ItemStack(ItemGenerator.planks.get(s), 4), NonNullList
                    .<Ingredient> withSize(1, Ingredient.fromStacks(new ItemStack(ItemGenerator.barks.get(s)))));
            GameData.register_impl(recipe.setRegistryName(new ResourceLocation(PokecubeMod.ID, s + "_1")));
        }

        GameData.register_impl(
                new RecipePokeseals().setRegistryName(new ResourceLocation(PokecubeMod.ID, "pokeseals")));
        GameData.register_impl(new RecipeRevive().setRegistryName(new ResourceLocation(PokecubeMod.ID, "revive")));
        BrewingRecipeRegistry.addRecipe(new RecipeBrewBerries());
    }
}
