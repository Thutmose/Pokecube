package pokecube.compat.jei.cloner;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapelessRecipes;
import pokecube.core.database.Database;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;

public class ClonerRecipe extends ShapelessRecipes
{
    public final boolean vanilla;
    public final int     pokedexNb;
    public final int     energyCost;
    private IPokemob     pokemob;

    public ClonerRecipe(ItemStack output, List<ItemStack> inputList, boolean vanilla)
    {
        super(output, inputList);
        this.vanilla = vanilla;
        this.pokedexNb = 0;
        this.energyCost = 0;
    }

    public ClonerRecipe(ItemStack output, List<ItemStack> inputList, int number, int cost)
    {
        super(output, inputList);
        this.vanilla = false;
        this.pokedexNb = number;
        this.energyCost = cost;
    }

    public IPokemob getPokemob()
    {
        if (pokemob == null && pokedexNb > 0 && Database.entryExists(pokedexNb))
        {
            pokemob = (IPokemob) PokecubeMod.core.createEntityByPokedexNb(pokedexNb, null);
        }
        return pokemob;
    }
}
