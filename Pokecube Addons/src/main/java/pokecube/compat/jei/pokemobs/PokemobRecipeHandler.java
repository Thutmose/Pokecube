package pokecube.compat.jei.pokemobs;

import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.IRecipeWrapperFactory;

public class PokemobRecipeHandler implements IRecipeWrapperFactory<PokemobRecipe>
{
    @Override
    public IRecipeWrapper getRecipeWrapper(PokemobRecipe recipe)
    {
        return new PokemobRecipeWrapper(recipe);
    }
}
