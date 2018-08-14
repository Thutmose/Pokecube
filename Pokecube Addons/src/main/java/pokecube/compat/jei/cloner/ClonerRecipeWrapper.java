package pokecube.compat.jei.cloner;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import pokecube.adventures.blocks.cloner.recipe.RecipeFossilRevive;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IPokemob;

public class ClonerRecipeWrapper implements IRecipeWrapper
{

    @Nonnull
    private final RecipeFossilRevive recipe;

    public ClonerRecipeWrapper(@Nonnull RecipeFossilRevive recipe)
    {
        this.recipe = recipe;
        for (Object input : this.recipe.recipeItems)
        {
            if (input instanceof ItemStack)
            {
                ItemStack itemStack = (ItemStack) input;
                if (itemStack.getCount() != 1)
                {
                    itemStack.setCount(1);
                }
            }
        }
    }

    @Override
    public void drawInfo(@Nonnull Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY)
    {
    }

    public IPokemob getPokemob()
    {
        return recipe.getPokemob();
    }

    @Nullable
    @Override
    public List<String> getTooltipStrings(int mouseX, int mouseY)
    {
        if (recipe.energyCost > 0) { return Lists.newArrayList("Energy: " + recipe.energyCost); }
        return null;
    }

    @Override
    public void getIngredients(IIngredients ingredients)
    {
        ingredients.setInputs(ItemStack.class, recipe.recipeItems);
        ingredients.setOutput(PokedexEntry.class, recipe.getPokedexEntry());
    }

    @Override
    public boolean handleClick(Minecraft minecraft, int mouseX, int mouseY, int mouseButton)
    {
        return false;
    }
}
