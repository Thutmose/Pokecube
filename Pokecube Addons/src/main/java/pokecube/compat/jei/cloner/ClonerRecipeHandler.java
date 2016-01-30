package pokecube.compat.jei.cloner;

import javax.annotation.Nonnull;

import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.ItemStack;
import pokecube.compat.jei.JEICompat;

public class ClonerRecipeHandler implements IRecipeHandler<ClonerRecipe> {

    @Override
    @Nonnull
    public Class<ClonerRecipe> getRecipeClass() {
        return ClonerRecipe.class;
    }

    @Nonnull
    @Override
    public String getRecipeCategoryUid() {
        return JEICompat.CLONER;
    }

    @Override
    @Nonnull
    public IRecipeWrapper getRecipeWrapper(@Nonnull ClonerRecipe recipe) {
        return new ClonerRecipeWrapper(recipe);
    }

    @Override
    public boolean isRecipeValid(@Nonnull ClonerRecipe recipe) {
        if (recipe.getRecipeOutput() == null) {
            return false;
        }
        int inputCount = 0;
        for (Object input : recipe.recipeItems) {
            if (input instanceof ItemStack) {
                inputCount++;
            } else {
                return false;
            }
        }
        return inputCount > 0;
    }
}
