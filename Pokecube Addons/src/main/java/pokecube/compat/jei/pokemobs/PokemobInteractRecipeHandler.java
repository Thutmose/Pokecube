package pokecube.compat.jei.pokemobs;

import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.IRecipeWrapperFactory;

public class PokemobInteractRecipeHandler implements IRecipeWrapperFactory<PokemobInteractRecipe>
{
    @Override
    public IRecipeWrapper getRecipeWrapper(PokemobInteractRecipe recipe)
    {
        return new PokemobInteractRecipeWrapper(recipe);
    }
}
