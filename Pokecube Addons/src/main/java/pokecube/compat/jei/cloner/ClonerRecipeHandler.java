package pokecube.compat.jei.cloner;

import javax.annotation.Nonnull;

import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.IRecipeWrapperFactory;
import pokecube.adventures.blocks.cloner.recipe.RecipeFossilRevive;

public class ClonerRecipeHandler implements IRecipeWrapperFactory<RecipeFossilRevive>
{
    @Override
    @Nonnull
    public IRecipeWrapper getRecipeWrapper(@Nonnull RecipeFossilRevive recipe)
    {
        return new ClonerRecipeWrapper(recipe);
    }
}
