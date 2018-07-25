package pokecube.core.database.recipes;

import pokecube.core.database.PokedexEntryLoader;
import pokecube.core.database.recipes.XMLRecipeHandler.XMLRecipe;

public interface IRecipeParser
{
    void manageRecipe(XMLRecipe recipe) throws NullPointerException;

    default String fileName(String default_)
    {
        return default_;
    }

    default String serialize(XMLRecipe recipe)
    {
        return PokedexEntryLoader.gson.toJson(recipe);
    }

    default XMLRecipe deserialize(String recipe)
    {
        return PokedexEntryLoader.gson.fromJson(recipe, XMLRecipe.class);
    }
}
