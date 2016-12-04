package pokecube.adventures.blocks.cloner.recipe;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import thut.lib.CompatWrapper;

public class RecipeSelector implements IRecipe
{
    private static List<RecipeSelector> recipeList = Lists.newArrayList();

    public static List<RecipeSelector> getRecipeList()
    {
        return Lists.newArrayList(recipeList);
    }

    public static void addRecipe(RecipeSelector toAdd)
    {
        recipeList.add(toAdd);
    }

    ItemStack output = CompatWrapper.nullStack;

    @Override
    public boolean matches(InventoryCrafting inv, World worldIn)
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getRecipeSize()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public ItemStack getRecipeOutput()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ItemStack[] getRemainingItems(InventoryCrafting inv)
    {
        // TODO Auto-generated method stub
        return null;
    }

}
