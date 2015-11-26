/**
 *
 */
package pokecube.core.database;

import java.util.HashSet;

/**
 * @author Manchou
 *
 */
public class Pokedex
{
    private HashSet<Integer> entries;

    private static Pokedex instance;

    /**
     *
     */
    private Pokedex()
    {
        entries = new HashSet<Integer>();
    }

    public static Pokedex getInstance()
    {
        if (instance == null)
        {
            instance = new Pokedex();
        }

        return instance;
    }

    public void registerPokemon(PokedexEntry entry)
    {
        if (entry == null || entry.pokedexNb == 0)
        {
            return;
        }
        getEntries().add(entry.pokedexNb);
    }

    public PokedexEntry getEntry(int pokedexNb)
    {
        return Database.getEntry(pokedexNb);//map.get(Integer.valueOf(pokedexNb));
    }

    public PokedexEntry getNext(PokedexEntry pokedexEntry, int i)
    {
        int pokedexNb = pokedexEntry!=null?pokedexEntry.pokedexNb:0;
        PokedexEntry returned = null;

        do
        {
            pokedexNb += i;

            if (pokedexNb >= 1500)
            {
                pokedexNb = pokedexNb - i + 1;
            }

            returned = getEntry(pokedexNb);

            if (returned != null)
            {
                return returned;
            }
        }
        while (pokedexNb < 1500);

     //   if(pokedexEntry==null||returned == null) System.err.println("Null Entry "+pokedexEntry+" "+returned);
        return pokedexEntry;
    }

    public PokedexEntry getPrevious(PokedexEntry pokedexEntry, int i)
    {
        int pokedexNb = pokedexEntry.pokedexNb;
        PokedexEntry returned = null;

        do
        {
            pokedexNb -= i;

            if (pokedexNb <= 0)
            {
                pokedexNb = pokedexNb + i - 1;
            }

            returned = getEntry(pokedexNb);

            if (returned != null)
            {
                return returned;
            }
        }
        while (pokedexNb > 0);

        return pokedexEntry;
    }

    public PokedexEntry getFirstEntry()
    {
        int pokedexNb = 0;
        PokedexEntry returned = null;

        do
        {
            pokedexNb += 1;
            returned = getEntry(pokedexNb);

            if (returned != null)
            {
                return returned;
            }
        }
        while (pokedexNb < 1500);

        return null;
    }

	public HashSet<Integer> getEntries()
	{
		return entries;
	}
}
