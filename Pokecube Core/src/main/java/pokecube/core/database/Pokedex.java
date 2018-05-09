/**
 *
 */
package pokecube.core.database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import pokecube.core.interfaces.PokecubeMod;

/** @author Manchou */
public class Pokedex
{
    private static Pokedex instance;

    public static Pokedex getInstance()
    {
        if (instance == null)
        {
            instance = new Pokedex();
        }
        return instance;
    }

    private ArrayList<PokedexEntry>    entries;
    private Map<PokedexEntry, Integer> entryIndecies;
    private HashSet<PokedexEntry>      registeredFormes;

    /**
     *
     */
    private Pokedex()
    {
        entries = Lists.newArrayList();
        entryIndecies = Maps.newHashMap();
        registeredFormes = Sets.newHashSet();
    }

    public List<PokedexEntry> getEntries()
    {
        return entries;
    }

    public Set<PokedexEntry> getRegisteredEntries()
    {
        return registeredFormes;
    }

    public Integer getIndex(PokedexEntry entry)
    {
        Integer ret = entryIndecies.get(entry);
        return ret == null ? 0 : ret;
    }

    public PokedexEntry getEntry(Integer pokedexNb)
    {
        PokedexEntry ret = Database.getEntry(pokedexNb);
        if (ret == null) return ret;
        return ret.getBaseForme() != null ? ret.getBaseForme() : ret;
    }

    public PokedexEntry getFirstEntry()
    {
        if (entries.isEmpty()) return Database.missingno;
        if (entries.get(0) == Database.missingno && entries.size() > 1) return entries.get(1);
        return entries.get(0);
    }

    public PokedexEntry getLastEntry()
    {
        if (entries.isEmpty()) return getFirstEntry();
        return entries.get(entries.size() - 1);
    }

    public PokedexEntry getNext(PokedexEntry pokedexEntry, int i)
    {
        if (!pokedexEntry.base) pokedexEntry = pokedexEntry.getBaseForme();
        Integer index = entryIndecies.get(pokedexEntry);
        if (index == null)
        {
            PokecubeMod.log(Level.WARNING, "Attempt to get a non existant entry: " + pokedexEntry,
                    new NullPointerException());
            return getFirstEntry();
        }
        while (index + i < 0)
        {
            i += entries.size();
        }
        index = (index + i) % entries.size();
        if (entries.get(index) == Database.missingno)
        {
            i = (int) (Math.signum(i));
            while (index + i < 0)
            {
                i += entries.size();
            }
            index = ((index + i) % entries.size());
        }
        return entries.get(index);
    }

    public PokedexEntry getPrevious(PokedexEntry pokedexEntry, int i)
    {
        return getNext(pokedexEntry, -i);
    }

    public void registerPokemon(PokedexEntry entry)
    {
        if (entry == null) { return; }
        if (!entries.contains(entry) && entry.base) entries.add(entry);
        registeredFormes.add(entry);
        resort();
    }

    public boolean isRegistered(PokedexEntry entry)
    {
        return registeredFormes.contains(entry);
    }

    private void resort()
    {
        Collections.sort(entries, Database.COMPARATOR);
        entryIndecies.clear();
        for (int i = 0; i < entries.size(); i++)
        {
            entryIndecies.put(entries.get(i), i);
        }
    }
}
