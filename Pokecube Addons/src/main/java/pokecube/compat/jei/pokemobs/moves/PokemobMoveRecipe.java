package pokecube.compat.jei.pokemobs.moves;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import pokecube.core.database.recipes.PokemobMoveRecipeParser.RecipeMove;
import pokecube.core.database.recipes.PokemobMoveRecipeParser.WrappedRecipeMove;
import pokecube.core.database.recipes.PokemobMoveRecipeParser.WrappedSizedIngredient;
import pokecube.core.events.handlers.MoveEventsHandler;
import pokecube.core.interfaces.IMoveAction;
import thut.lib.IDefaultRecipe;

public class PokemobMoveRecipe implements IDefaultRecipe
{

    public static List<PokemobMoveRecipe> getRecipes()
    {
        List<PokemobMoveRecipe> ret = Lists.newArrayList();

        for (String move : MoveEventsHandler.customActions.keySet())
        {
            IMoveAction action = MoveEventsHandler.customActions.get(move);
            Set<RecipeMove> recipes = Sets.newHashSet();
            getRecipes(action, recipes);
            for (RecipeMove recipe : recipes)
            {
                if (recipe != null) ret.add(new PokemobMoveRecipe(recipe));
            }
        }
        return ret;
    }

    private static void getRecipes(IMoveAction action, Set<RecipeMove> toFill)
    {
        if (action instanceof RecipeMove)
        {
            toFill.add((RecipeMove) action);
        }
        else if (action instanceof WrappedRecipeMove)
        {
            try
            {
                IMoveAction parent = ((WrappedRecipeMove) action).parent;
                IMoveAction other = ((WrappedRecipeMove) action).other;
                getRecipes(parent, toFill);
                getRecipes(other, toFill);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

        }
    }

    final IRecipe wrapped;
    final int     cost;
    final String  move;

    public PokemobMoveRecipe(RecipeMove recipe)
    {
        this.wrapped = recipe.recipe;
        this.cost = recipe.hungerCost;
        this.move = recipe.name;
    }

    @Override
    public boolean matches(InventoryCrafting inv, World worldIn)
    {
        return wrapped.matches(inv, worldIn);
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv)
    {
        return wrapped.getCraftingResult(inv);
    }

    @Override
    public ItemStack getRecipeOutput()
    {
        return wrapped.getRecipeOutput();
    }

    @Override
    public NonNullList<Ingredient> getIngredients()
    {
        NonNullList<Ingredient> ret = NonNullList.create();
        for (Ingredient i : wrapped.getIngredients())
        {
            if (i instanceof WrappedSizedIngredient)
            {
                ret.add(((WrappedSizedIngredient) i).wrapped);
            }
            else ret.add(i);
        }
        return ret;
    }

    ResourceLocation registryName;

    @Override
    public IRecipe setRegistryName(ResourceLocation name)
    {
        registryName = name;
        return this;
    }

    @Override
    public ResourceLocation getRegistryName()
    {
        return registryName;
    }

    @Override
    public Class<IRecipe> getRegistryType()
    {
        return IRecipe.class;
    }

    @Override
    public String toString()
    {
        return move + " " + cost + " " + wrapped;
    }
}
