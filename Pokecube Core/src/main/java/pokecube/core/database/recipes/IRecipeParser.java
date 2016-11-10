package pokecube.core.database.recipes;

import pokecube.core.database.recipes.XMLRecipeHandler.XMLRecipe;

public interface IRecipeParser
{
    void manageRecipe(XMLRecipe recipe) throws NullPointerException;
}
