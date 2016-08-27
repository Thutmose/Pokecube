package pokecube.core.database;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.minecraftforge.fml.common.ProgressManager;
import net.minecraftforge.fml.common.ProgressManager.ProgressBar;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import pokecube.core.database.PokedexEntry.InteractionLogic;
import pokecube.core.database.PokedexEntry.SpawnData;
import pokecube.core.database.PokedexEntry.SpawnData.SpawnEntry;
import pokecube.core.database.PokedexEntryLoader.SpawnRule;
import pokecube.core.database.moves.MoveEntryLoader;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.utils.PokeType;

public class Database
{
    @XmlRootElement(name = "Spawns")
    public static class XMLSpawns
    {
        @XmlElement(name = "Spawn")
        private List<XMLSpawnEntry> pokemon = Lists.newArrayList();
    }

    @XmlRootElement(name = "Spawn")
    public static class XMLSpawnEntry extends SpawnRule
    {
        static final QName STARTER   = new QName("starter");
        @XmlAttribute
        boolean            overwrite = false;
        @XmlAttribute
        String             name;

        public Boolean isStarter()
        {
            if (!values.containsKey(STARTER)) return null;
            return Boolean.parseBoolean(values.get(STARTER));
        }
    }

    /** <br>
     * Index 0 = baseStats<br>
     * Index 1 = moves<br>
     * Index 2 = moveLists<br>
     * Index 3 = exsXp<br>
     * Index 4 = abilities<br>
     * Index 5 = spawndata<br>
    */
    public static enum EnumDatabase
    {
        POKEMON, MOVES
    }

    public static boolean                                  FORCECOPY        = true;
    public static HashMap<Integer, PokedexEntry>           data             = new HashMap<Integer, PokedexEntry>();
    public static HashMap<String, PokedexEntry>            data2            = new HashMap<String, PokedexEntry>();
    public static HashSet<PokedexEntry>                    allFormes        = new HashSet<PokedexEntry>();
    public static HashMap<Integer, PokedexEntry>           baseFormes       = new HashMap<Integer, PokedexEntry>();
    public static HashMap<String, ArrayList<PokedexEntry>> mobReplacements  = new HashMap<String, ArrayList<PokedexEntry>>();

    public static List<PokedexEntry>                       spawnables       = new ArrayList<PokedexEntry>();
    static String                                          DBLOCATION       = "/assets/pokecube/database/";

    public static String                                   CONFIGLOC        = "";

    static HashSet<String>                                 defaultDatabases = Sets.newHashSet("pokemobs.xml");
    static HashSet<String>                                 extraDatabases   = Sets.newHashSet();
    private static HashSet<String>                         spawnDatabases   = Sets.newHashSet();

    /** These are used for config added databasea <br>
     * Index 0 = pokemon<br>
     * Index 1 = moves<br>
    */
    @SuppressWarnings("unchecked")
    private static List<ArrayList<String>>                 configDatabases  = Lists
            .newArrayList(new ArrayList<String>(), new ArrayList<String>());

    public static void addDatabase(String file, EnumDatabase database)
    {
        int index = database.ordinal();
        ArrayList<String> list = configDatabases.get(index);
        if (database == EnumDatabase.POKEMON && !file.endsWith(".xml")) file = file + ".xml";
        else if (!file.endsWith(".json")) file = file + ".json";
        for (String s : list)
        {
            if (s.equals(file)) return;
        }
        list.add(file);
    }

    public static void addEntry(PokedexEntry entry)
    {
        data.put(entry.getPokedexNb(), entry);
    }

    public static void addSpawnData(String file)
    {
        spawnDatabases.add(file);
    }

    public static void checkConfigFiles(FMLPreInitializationEvent evt)
    {
        File file = evt.getSuggestedConfigurationFile();
        String seperator = System.getProperty("file.separator");

        String folder = file.getAbsolutePath();
        String name = file.getName();
        folder = folder.replace(name, "pokecube" + seperator + "database" + seperator + "");

        CONFIGLOC = folder;
        writeDefaultConfig();
        return;
    }

    public static boolean compare(String a, String b)
    {
        boolean ret = false;
        ret = a.toLowerCase(java.util.Locale.ENGLISH).replaceAll("(\\W)", "")
                .equals(b.toLowerCase(java.util.Locale.ENGLISH).replaceAll("(\\W)", ""));
        return ret;
    }

    public static String convertMoveName(String moveNameFromBulbapedia)
    {

        String ret = "";
        String name = moveNameFromBulbapedia.trim().toLowerCase(java.util.Locale.ENGLISH).replaceAll("[^\\w\\s ]", "");
        String[] args = name.split(" ");
        for (int i = 0; i < args.length; i++)
        {
            ret += args[i];
        }
        return ret;
    }

    private static void copyDatabaseFile(String name)
    {
        File temp1 = new File(CONFIGLOC + name);
        if (temp1.exists() && !FORCECOPY)
        {
            System.out.println(" Not Overwriting old database " + name);
            return;
        }
        ArrayList<String> rows = getFile(DBLOCATION + name);
        int n = 0;
        try
        {
            File file = new File(CONFIGLOC + name);
            Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
            for (int i = 0; i < rows.size(); i++)
            {
                out.write(rows.get(i) + "\n");
                n++;
            }
            out.close();
        }
        catch (Exception e)
        {
            System.err.println(name + " " + n);
            e.printStackTrace();
        }
    }

    public static boolean entryExists(int nb)
    {
        return getEntry(nb) != null;
    }

    public static boolean entryExists(String name)
    {
        return getEntry(name) != null;
    }

    public static PokedexEntry getEntry(Integer nb)
    {
        return data.get(nb);
    }

    public static PokedexEntry getEntry(IPokemob mob)
    {
        return data.get(mob.getPokedexNb());
    }

    public static PokedexEntry getEntry(String name)
    {
        PokedexEntry ret = null;
        if (name == null) return null;
        if (name.trim().isEmpty()) return null;
        if (data2.containsKey(name)) return data2.get(name);
        for (PokedexEntry e : allFormes)
        {
            String s = e.getName();
            if (compare(s, name))
            {
                data2.put(name, e);
                data2.put(s, e);
                return e;
            }
        }
        if (name.toLowerCase(java.util.Locale.ENGLISH).contains("mega ")) { return getEntry(
                (name.toLowerCase(java.util.Locale.ENGLISH).replace("mega ", "") + " mega").trim()); }
        return ret;
    }

    public static ArrayList<String> getFile(String file)
    {
        InputStream res = (Database.class).getResourceAsStream(file);

        ArrayList<String> rows = new ArrayList<String>();
        BufferedReader br = null;
        String line = "";
        try
        {

            br = new BufferedReader(new InputStreamReader(res));
            while ((line = br.readLine()) != null)
            {
                rows.add(line);
            }

        }
        catch (FileNotFoundException e)
        {
            System.err.println("Missing a Database file " + file);
        }
        catch (NullPointerException e)
        {
            try
            {
                FileReader temp = new FileReader(new File(file));
                br = new BufferedReader(temp);
                while ((line = br.readLine()) != null)
                {
                    rows.add(line);
                }
            }
            catch (IOException e1)
            {
                e1.printStackTrace();
            }

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (br != null)
            {
                try
                {
                    br.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }

        return rows;
    }

    public static List<String> getLearnableMoves(int nb)
    {
        return entryExists(nb) ? getEntry(nb).getMoves() : null;
    }

    public static List<String> getLevelUpMoves(int nb, int level, int oldLevel)
    {
        return entryExists(nb) ? getEntry(nb).getMovesForLevel(level, oldLevel) : null;
    }

    public static SpawnData getSpawnData(int nb)
    {
        if (data.containsKey(nb)) return data.get(nb).getSpawnData();
        return null;
    }

    public static boolean hasSpawnData(int nb)
    {
        return getEntry(nb) != null && getEntry(nb).getSpawnData() != null;
    }

    public static void init(FMLPreInitializationEvent evt)
    {
        checkConfigFiles(evt);
        for (String s : configDatabases.get(1))
            loadMoves(DBLOCATION + s);

        for (String s : configDatabases.get(EnumDatabase.POKEMON.ordinal()))
        {
            try
            {
                PokedexEntryLoader.makeEntries(new File(DBLOCATION + s), true);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        for (String s : extraDatabases)
        {
            try
            {
                PokedexEntryLoader.makeEntries(new File(s), true);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        System.out.println(
                "Loaded " + data.size() + " by number, and " + allFormes.size() + " by formes from databases.");
    }

    public static void loadMoves(String file)
    {
        MoveEntryLoader.loadMoves(file);
    }

    private static void loadSpawns()
    {
        for (String s : spawnDatabases)
        {
            if (s != null) loadSpawns(s);
        }
    }

    /** This method should only be called for override files, such as the one
     * added by Pokecube Compat
     * 
     * @param file */
    private static void loadSpawns(String file)
    {
        System.out.println(file);
        try
        {
            JAXBContext jaxbContext = JAXBContext.newInstance(XMLSpawns.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            XMLSpawns database = (XMLSpawns) unmarshaller.unmarshal(new FileReader(file));

            for (XMLSpawnEntry xmlEntry : database.pokemon)
            {
                PokedexEntry entry = Database.getEntry(xmlEntry.name);
                if (entry == null) throw new NullPointerException(xmlEntry.name + " not found");
                if (xmlEntry.isStarter() != null) entry.isStarter = xmlEntry.isStarter();
                SpawnData data = entry.getSpawnData();
                if (xmlEntry.overwrite || data == null)
                {
                    data = new SpawnData();
                    entry.setSpawnData(data);
                    System.out.println("Overwriting spawns for " + entry);
                }
                else
                {
                    System.out.println("Editing spawns for " + entry);
                }
                SpawnEntry spawnEntry = new SpawnEntry();
                String val;
                if ((val = xmlEntry.values.get(new QName("min"))) != null)
                {
                    spawnEntry.min = Integer.parseInt(val);
                }
                if ((val = xmlEntry.values.get(new QName("max"))) != null)
                {
                    spawnEntry.max = Integer.parseInt(val);
                }
                if ((val = xmlEntry.values.get(new QName("rate"))) != null)
                {
                    spawnEntry.rate = Float.parseFloat(val);
                }
                SpawnBiomeMatcher matcher = new SpawnBiomeMatcher(xmlEntry);
                data.matchers.put(matcher, spawnEntry);
                if (!Database.spawnables.contains(entry)) Database.spawnables.add(entry);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        // TODO redo this.
    }

    public static void postInit()
    {
        PokedexEntryLoader.postInit();
        loadSpawns();
        ProgressBar bar = ProgressManager.push("Removal Checking", baseFormes.size());
        List<PokedexEntry> toRemove = new ArrayList<PokedexEntry>();
        for (PokedexEntry p : baseFormes.values())
        {
            bar.step(p.getName());
            if (!Pokedex.getInstance().getEntries().contains(p.getPokedexNb()))
            {
                toRemove.add(p);
            }
        }
        ProgressManager.pop(bar);
        bar = ProgressManager.push("Removal", toRemove.size());
        for (PokedexEntry p : toRemove)
        {
            bar.step(p.getName());
            data.remove(p.pokedexNb);
            baseFormes.remove(p.pokedexNb);
            spawnables.remove(p);
        }
        ProgressManager.pop(bar);
        System.err.println("Removed " + toRemove.size() + " Missing Pokemon");

        bar = ProgressManager.push("Base Formes", allFormes.size());
        toRemove.clear();
        for (PokedexEntry e : allFormes)
        {
            bar.step(e.getName());
            PokedexEntry base = baseFormes.get(e.pokedexNb);

            if (base == null)
            {
                toRemove.add(e);
                continue;
            }

            if (base != e)
            {
                base.copyToForm(e);
                if (e.height == -1)
                {
                    e.height = base.height;
                    e.width = base.width;
                    e.length = base.length;
                    e.childNumbers = base.childNumbers;
                    e.species = base.species;
                    e.mobType = base.mobType;
                    e.catchRate = base.catchRate;
                    e.mass = base.mass;
                    e.drops = base.drops;
                    System.err.println("Error with " + e);
                }
                if (e.species == null)
                {
                    e.childNumbers = base.childNumbers;
                    e.species = base.species;
                    System.err.println(e + " Has no Species");
                }
                if (e.type1 == null)
                {
                    e.type1 = base.type1;
                    e.type2 = base.type2;
                }
                if (e.abilities.isEmpty() && base != null) e.abilities.addAll(base.abilities);
            }
            else
            {
                e.setBaseForme(e);
            }
            if (e.mobType == null)
            {
                e.mobType = PokecubeMod.Type.NORMAL;
                System.out.println(e + " Has no Mob Type");
                Thread.dumpStack();
            }
            if (e.type2 == null) e.type2 = PokeType.unknown;
            if (e.interactionLogic.stacks.isEmpty())
            {
                if (e.getBaseForme() != null)
                {
                    if (e.getBaseForme().interactionLogic.stacks.isEmpty())
                    {
                        InteractionLogic.initForEntry(e);
                    }
                    e.interactionLogic.stacks = e.getBaseForme().interactionLogic.stacks;
                }
                else
                {
                    InteractionLogic.initForEntry(e);
                }
            }
            if (!Pokedex.getInstance().getEntries().contains(e.getPokedexNb()))
            {
                if (e.getBaseForme() != null
                        && Pokedex.getInstance().getEntries().contains(e.getBaseForme().getPokedexNb()))
                {
                    continue;
                }
                toRemove.add(e);
            }
        }
        System.out.println(toRemove.size() + " Pokemon Formes Removed");
        allFormes.removeAll(toRemove);
        ProgressManager.pop(bar);

        bar = ProgressManager.push("Relations", data.size());
        for (PokedexEntry p : data.values())
        {
            bar.step(p.getName());
            p.initRelations();
        }
        ProgressManager.pop(bar);
        bar = ProgressManager.push("Prey", data.size());
        for (PokedexEntry p : data.values())
        {
            bar.step(p.getName());
            p.initPrey();
        }
        ProgressManager.pop(bar);
        bar = ProgressManager.push("Children", data.size());
        for (PokedexEntry p : data.values())
        {
            bar.step(p.getName());
            p.getChildNb();
        }
        ProgressManager.pop(bar);

        for (PokedexEntry e : allFormes)
        {
            if (e.stats == null || e.evs == null)
            {
                System.err.println(new NullPointerException(e + " is missing stats or evs " + e.stats + " " + e.evs));
            }
        }
    }

    private static void writeDefaultConfig()
    {
        try
        {
            File temp = new File(CONFIGLOC);
            if (!temp.exists())
            {
                temp.mkdirs();
            }

            copyDatabaseFile("moves.json");

            for (String s : defaultDatabases)
                copyDatabaseFile(s);

            DBLOCATION = CONFIGLOC;

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}