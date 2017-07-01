package pokecube.compat.jei.ingredients;

import java.awt.Color;
import java.util.List;

import com.google.common.collect.Lists;

import mezz.jei.api.ingredients.IIngredientHelper;
import pokecube.core.database.PokedexEntry;
import pokecube.core.utils.PokeType;

public class PokedexEntryIngredientHelper implements IIngredientHelper<PokedexEntry>
{
    @Override
    public List<PokedexEntry> expandSubtypes(List<PokedexEntry> ingredients)
    {
        return ingredients;
    }

    @Override
    public PokedexEntry getMatch(Iterable<PokedexEntry> ingredients, final PokedexEntry ingredientToMatch)
    {
        for (PokedexEntry e : ingredients)
        {
            if (e == ingredientToMatch) { return e; }
        }
        return null;
    }

    @Override
    public String getDisplayName(PokedexEntry ingredient)
    {
        return ingredient.getName();
    }

    @Override
    public String getUniqueId(PokedexEntry ingredient)
    {
        return ingredient.getName();
    }

    @Override
    public String getWildcardId(PokedexEntry ingredient)
    {
        return "pokemob";
    }

    @Override
    public String getModId(PokedexEntry ingredient)
    {
        return "pokecube";
    }

    @Override
    public Iterable<Color> getColors(PokedexEntry ingredient)
    {
        List<Color> colours = Lists.newArrayList();
        if (ingredient.getType1() != PokeType.unknown) colours.add(new Color(ingredient.getType1().colour));
        if (ingredient.getType2() != PokeType.unknown) colours.add(new Color(ingredient.getType2().colour));
        return colours;
    }

    @Override
    public String getErrorInfo(PokedexEntry ingredient)
    {
        return ingredient.getName();
    }

    @Override
    public String getResourceId(PokedexEntry ingredient)
    {
        return ingredient.getModId() + ":" + ingredient.getName();
    }

    @Override
    public PokedexEntry copyIngredient(PokedexEntry ingredient)
    {
        return ingredient;
    }

}
