package pokecube.compat.jei.cloner;

import javax.annotation.Nonnull;

import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.ItemStack;
import pokecube.adventures.blocks.cloner.recipe.RecipeFossilRevive;
import pokecube.compat.jei.JEICompat;

public class ClonerRecipeHandler implements IRecipeHandler<RecipeFossilRevive>
{

    @Nonnull
    @Override
    public String getRecipeCategoryUid()
    {
        return JEICompat.REANIMATOR;
    }

    @Override
    public String getRecipeCategoryUid(RecipeFossilRevive recipe)
    {
        return JEICompat.REANIMATOR;
    }

    @Override
    @Nonnull
    public Class<RecipeFossilRevive> getRecipeClass()
    {
        return RecipeFossilRevive.class;
    }

    @Override
    @Nonnull
    public IRecipeWrapper getRecipeWrapper(@Nonnull RecipeFossilRevive recipe)
    {
        return new ClonerRecipeWrapper(recipe);
    }

    @Override
    public boolean isRecipeValid(@Nonnull RecipeFossilRevive recipe)
    {
        if (recipe.getRecipeOutput() == null) { return false; }
        int inputCount = 0;
        for (Object input : recipe.recipeItems)
        {
            if (input instanceof ItemStack)
            {
                inputCount++;
            }
            else
            {
                return false;
            }
        }
        return inputCount > 0;
    }
}
