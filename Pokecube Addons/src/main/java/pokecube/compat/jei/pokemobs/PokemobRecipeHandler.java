package pokecube.compat.jei.pokemobs;

import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;
import pokecube.compat.jei.JEICompat;

public class PokemobRecipeHandler implements IRecipeHandler<PokemobRecipe>
{

    @Override
    public Class<PokemobRecipe> getRecipeClass()
    {
        return PokemobRecipe.class;
    }

    @Override
    public String getRecipeCategoryUid(PokemobRecipe recipe)
    {
        return JEICompat.POKEMOB;
    }

    @Override
    public IRecipeWrapper getRecipeWrapper(PokemobRecipe recipe)
    {
        return new PokemobRecipeWrapper(recipe);
    }

    @Override
    public boolean isRecipeValid(PokemobRecipe recipe)
    {
        return true;
    }

}
