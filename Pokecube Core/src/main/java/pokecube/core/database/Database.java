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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.ProgressManager;
import net.minecraftforge.fml.common.ProgressManager.ProgressBar;
import pokecube.core.database.PokedexEntry.EvolutionData;
import pokecube.core.database.PokedexEntry.InteractionLogic;
import pokecube.core.database.PokedexEntry.SpawnData;
import pokecube.core.database.PokedexEntryLoader.Drop;
import pokecube.core.database.PokedexEntryLoader.SpawnRule;
import pokecube.core.database.moves.MoveEntryLoader;
import pokecube.core.database.moves.MovesParser;
import pokecube.core.database.moves.json.JsonMoves;
import pokecube.core.database.recipes.XMLRecipeHandler;
import pokecube.core.database.recipes.XMLRecipeHandler.XMLRecipe;
import pokecube.core.database.recipes.XMLRecipeHandler.XMLRecipes;
import pokecube.core.database.rewards.XMLRewardsHandler;
import pokecube.core.database.rewards.XMLRewardsHandler.XMLReward;
import pokecube.core.database.rewards.XMLRewardsHandler.XMLRewards;
import pokecube.core.database.worldgen.XMLWorldgenHandler;
import pokecube.core.handlers.playerdata.PokecubePlayerStats;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.utils.PokeType;
import pokecube.core.world.gen.template.PokecubeTemplates;

public class Database
{
    @XmlRootElement(name = "Items")
    public static class XMLStarterItems
    {
        @XmlElement(name = "Item")
        private List<Drop> drops = Lists.newArrayList();
    }

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

    @XmlRootElement(name = "Drops")
    public static class XMLDrops
    {
        @XmlElement(name = "Drop")
        private List<XMLDropEntry> pokemon = Lists.newArrayList();
    }

    @XmlRootElement(name = "Drop")
    public static class XMLDropEntry extends Drop
    {
        @XmlAttribute
        boolean overwrite = false;
        @XmlAttribute
        String  name;
    }

    @XmlRootElement(name = "Helds")
    public static class XMLHelds
    {
        @XmlElement(name = "Held")
        private List<XMLHeldEntry> pokemon = Lists.newArrayList();
    }

    @XmlRootElement(name = "Held")
    public static class XMLHeldEntry extends Drop
    {
        @XmlAttribute
        boolean overwrite = false;
        @XmlAttribute
        String  name;
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
    public static boolean                                  FORCECOPYRECIPES = true;
    public static boolean                                  FORCECOPYREWARDS = true;
    public static List<ItemStack>                          starterPack      = Lists.newArrayList();
    public static Int2ObjectOpenHashMap<PokedexEntry>      data             = new Int2ObjectOpenHashMap<>();
    public static HashMap<String, PokedexEntry>            data2            = new HashMap<String, PokedexEntry>();
    public static HashSet<PokedexEntry>                    allFormes        = new HashSet<PokedexEntry>();
    public static HashMap<Integer, PokedexEntry>           baseFormes       = new HashMap<Integer, PokedexEntry>();
    public static HashMap<String, ArrayList<PokedexEntry>> mobReplacements  = new HashMap<String, ArrayList<PokedexEntry>>();

    public static List<PokedexEntry>                       spawnables       = new ArrayList<PokedexEntry>();
    public static String                                   DBLOCATION       = "/assets/pokecube/database/";

    public static String                                   CONFIGLOC        = "";

    static HashSet<String>                                 defaultDatabases = Sets.newHashSet("pokemobs.xml");
    static HashSet<String>                                 extraDatabases   = Sets.newHashSet();
    private static HashSet<String>                         spawnDatabases   = Sets.newHashSet();
    private static Set<String>                             dropDatabases    = Sets.newHashSet();
    private static Set<String>                             heldDatabases    = Sets.newHashSet();

    public static final PokedexEntry                       missingno        = new PokedexEntry(0, "missingno");

    /** These are used for config added databasea <br>
     * Index 0 = pokemon<br>
     * Index 1 = moves<br>
    */
    public static List<ArrayList<String>>                  configDatabases  = Lists
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

    public static void addDropData(String file)
    {
        dropDatabases.add(file);
    }

    public static void addHeldData(String file)
    {
        heldDatabases.add(file);
    }

    public static void checkConfigFiles()
    {
        File file = new File("./config/pokecube.cfg");
        String seperator = System.getProperty("file.separator");
        String folder = file.getAbsolutePath();
        String name = file.getName();
        CONFIGLOC = folder.replace(name, "pokecube" + seperator + "database" + seperator + "");
        PokecubeTemplates.TEMPLATES = folder.replace(name, "pokecube" + seperator + "structures" + seperator + "");
        PokecubeTemplates.initFiles();
        XMLWorldgenHandler.loadDefaults(new File(PokecubeTemplates.TEMPLATES, "worldgen.xml"));
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

    public static PokedexEntry getEntry(int nb)
    {
        return data.get(nb);
    }

    public static PokedexEntry getEntry(IPokemob mob)
    {
        return mob.getPokedexEntry();
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

    public static List<String> getLevelUpMoves(PokedexEntry entry, int level, int oldLevel)
    {
        return entry != null ? entry.getMovesForLevel(level, oldLevel) : null;
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

    public static void init()
    {
        checkConfigFiles();
        for (String s : configDatabases.get(1))
        {
            try
            {
                File moves = new File(DBLOCATION + s);
                File anims = new File(Database.DBLOCATION + "animations.json");
                JsonMoves.loadMoves(moves);
                JsonMoves.merge(anims, moves);
                MovesParser.load(moves);
            }
            catch (IOException e1)
            {
                e1.printStackTrace();
            }
        }

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

    public static void initSounds(Object registry)
    {
        for (PokedexEntry e : allFormes)
        {
            if (e.getModId() == null) continue;
            if (e.sound == null) e.setSound("mobs." + e.getBaseName());
            e.event = new SoundEvent(e.sound);
            if (SoundEvent.REGISTRY.containsKey(e.sound)) continue;
//            ReflectionHelper.setPrivateValue(IForgeRegistryEntry.Impl.class, e.event, e.sound, "registryName");
//            GameRegistry.register(e.event);
        }
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

    private static void loadDrops()
    {
        for (String s : dropDatabases)
        {
            if (s != null) loadDrops(s);
        }
    }

    private static void loadHeld()
    {
        for (String s : heldDatabases)
        {
            if (s != null) loadHeld(s);
        }
    }

    /** This method should only be called for override files, such as the one
     * added by Pokecube Compat
     * 
     * @param file */
    private static void loadSpawns(String file)
    {
        try
        {
            JAXBContext jaxbContext = JAXBContext.newInstance(XMLSpawns.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            FileReader reader = new FileReader(file);
            XMLSpawns database = (XMLSpawns) unmarshaller.unmarshal(reader);
            reader.close();
            for (XMLSpawnEntry xmlEntry : database.pokemon)
            {
                PokedexEntry entry = Database.getEntry(xmlEntry.name);
                if (entry == null)
                {
                    new NullPointerException(xmlEntry.name + " not found").printStackTrace();
                    continue;
                }
                if (entry.isGenderForme) continue;
                if (xmlEntry.isStarter() != null) entry.isStarter = xmlEntry.isStarter();
                SpawnData data = entry.getSpawnData();
                if (xmlEntry.overwrite || data == null)
                {
                    data = new SpawnData(entry);
                    entry.setSpawnData(data);
                    System.out.println("Overwriting spawns for " + entry);
                }
                else
                {
                    System.out.println("Editing spawns for " + entry);
                }
                PokedexEntryLoader.handleAddSpawn(data, xmlEntry);
                if (data.matchers.isEmpty())
                {
                    Database.spawnables.remove(entry);
                }
                else if (!Database.spawnables.contains(entry)) Database.spawnables.add(entry);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /** This method should only be called for override files, such as the one
     * added by Pokecube Compat
     * 
     * @param file */
    private static void loadDrops(String file)
    {
        try
        {
            JAXBContext jaxbContext = JAXBContext.newInstance(XMLDrops.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            FileReader reader = new FileReader(file);
            XMLDrops database = (XMLDrops) unmarshaller.unmarshal(reader);
            reader.close();
            for (XMLDropEntry xmlEntry : database.pokemon)
            {
                PokedexEntry entry = Database.getEntry(xmlEntry.name);
                if (entry == null)
                {
                    new NullPointerException(xmlEntry.name + " not found").printStackTrace();
                    continue;
                }
                if (entry.isGenderForme) continue;
                System.out.println(entry.drops);
                if (xmlEntry.overwrite)
                {
                    entry.drops.clear();
                }
                PokedexEntryLoader.handleAddDrop(entry, xmlEntry);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /** This method should only be called for override files, such as the one
     * added by Pokecube Compat
     * 
     * @param file */
    private static void loadHeld(String file)
    {
        try
        {
            JAXBContext jaxbContext = JAXBContext.newInstance(XMLHelds.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            FileReader reader = new FileReader(file);
            XMLHelds database = (XMLHelds) unmarshaller.unmarshal(reader);
            reader.close();
            for (XMLHeldEntry xmlEntry : database.pokemon)
            {
                PokedexEntry entry = Database.getEntry(xmlEntry.name);
                if (entry == null)
                {
                    new NullPointerException(xmlEntry.name + " not found").printStackTrace();
                    continue;
                }
                if (entry.isGenderForme) continue;
                if (xmlEntry.overwrite)
                {
                    entry.held.clear();
                }
                PokedexEntryLoader.handleAddHeld(entry, xmlEntry);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void postInit()
    {
        PokedexEntryLoader.postInit();
        loadSpawns();
        loadDrops();
        loadHeld();
        loadStarterPack();
        ProgressBar bar = ProgressManager.push("Removal Checking", allFormes.size());
        List<PokedexEntry> toRemove = new ArrayList<PokedexEntry>();
        Set<Integer> removedNums = Sets.newHashSet();
        List<PokedexEntry> removed = Lists.newArrayList();
        for (PokedexEntry e : Pokedex.getInstance().getRegisteredEntries())
        {
            if (e.base) addEntry(e);
        }
        for (PokedexEntry p : allFormes)
        {
            bar.step(p.getName());
            if (!Pokedex.getInstance().getRegisteredEntries().contains(p))
            {
                if (p.base) removedNums.add(p.getPokedexNb());
                toRemove.add(p);
                removed.add(p);
            }
        }
        ProgressManager.pop(bar);
        bar = ProgressManager.push("Removal", toRemove.size());
        for (PokedexEntry p : toRemove)
        {
            bar.step(p.getName());
            if (p.base)
            {
                data.remove(p.pokedexNb);
                baseFormes.remove(p.pokedexNb);
            }
            spawnables.remove(p);
        }

        allFormes.removeAll(toRemove);
        ProgressManager.pop(bar);
        System.err.println("Removed " + removedNums.size() + " Missing Pokemon");

        bar = ProgressManager.push("Base Formes", allFormes.size());
        toRemove.clear();
        List<PokedexEntry> sortedEntries = Lists.newArrayList();
        sortedEntries.addAll(Database.allFormes);
        Comparator<PokedexEntry> sorter = new Comparator<PokedexEntry>()
        {
            @Override
            public int compare(PokedexEntry o1, PokedexEntry o2)
            {
                int diff = o1.getPokedexNb() - o2.getPokedexNb();
                if (diff == 0)
                {
                    if (o1.base && !o2.base) diff = -1;
                    else if (o2.base && !o1.base) diff = 1;
                }
                return diff;
            }
        };
        Collections.sort(sortedEntries, sorter);

        for (PokedexEntry e : sortedEntries)
        {
            bar.step(e.getName());
            PokedexEntry base = baseFormes.get(e.pokedexNb);

            if (base == null)
            {
                toRemove.add(e);
                continue;
            }
            if (base == e)
            {
                base.copyToGenderFormes();
            }

            if (base != e)
            {
                base.copyToForm(e);
                if (e.height <= 0)
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
                if (e.abilities.isEmpty()) e.abilities.addAll(base.abilities);
                if (e.abilitiesHidden.isEmpty()) e.abilitiesHidden.addAll(base.abilitiesHidden);
            }
            else
            {
                e.setBaseForme(e);
            }
            if (e.mobType == null)
            {
                e.mobType = PokecubeMod.Type.NORMAL;
                PokecubeMod.log(e + " Has no Mob Type");
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
            List<EvolutionData> invalidEvos = Lists.newArrayList();
            for (EvolutionData d : e.evolutions)
            {
                if (!Pokedex.getInstance().getRegisteredEntries().contains(d.evolution))
                {
                    invalidEvos.add(d);
                }
            }
            e.evolutions.removeAll(invalidEvos);

            if (!Pokedex.getInstance().getRegisteredEntries().contains(e))
            {
                toRemove.add(e);
                removed.add(e);
                spawnables.remove(e);
            }
        }
        System.out.println(toRemove.size() + " Pokemon Formes Removed");
        allFormes.removeAll(toRemove);
        ProgressManager.pop(bar);
        Collections.sort(removed, sorter);
        bar = ProgressManager.push("Relations", allFormes.size());
        for (PokedexEntry p : allFormes)
        {
            bar.step(p.getName());
            p.initRelations();
        }
        ProgressManager.pop(bar);
        bar = ProgressManager.push("Prey", allFormes.size());
        for (PokedexEntry p : allFormes)
        {
            bar.step(p.getName());
            p.initPrey();
        }
        ProgressManager.pop(bar);
        bar = ProgressManager.push("Children", allFormes.size());
        for (PokedexEntry p : allFormes)
        {
            bar.step(p.getName());
            p.getChild();
        }
        ProgressManager.pop(bar);

        bar = ProgressManager.push("Achivements", allFormes.size());
        for (PokedexEntry e : allFormes)
        {
            bar.step(e.getName());
            PokecubePlayerStats.registerAchievements(e);
        }
        ProgressManager.pop(bar);
    }

    private static void loadStarterPack()
    {
        File temp = new File(CONFIGLOC);
        if (!temp.exists())
        {
            temp.mkdirs();
        }
        String name = "pack.xml";
        File temp1 = new File(CONFIGLOC + name);
        if (!temp1.exists())
        {
            ArrayList<String> rows = getFile("/assets/pokecube/database/" + name);
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
        try
        {
            JAXBContext jaxbContext = JAXBContext.newInstance(XMLStarterItems.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            FileReader reader = new FileReader(temp1);
            XMLStarterItems database = (XMLStarterItems) unmarshaller.unmarshal(reader);
            reader.close();
            for (Drop drop : database.drops)
            {
                ItemStack stack = PokedexEntryLoader.getStackFromDrop(drop);
                if (stack != null) starterPack.add(stack);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void loadRecipes(Object event)
    {
        File temp = new File(CONFIGLOC);
        if (!temp.exists())
        {
            temp.mkdirs();
        }
        for (String name : XMLRecipeHandler.recipeFiles)
        {
            name = name + ".xml";
            File temp1 = new File(CONFIGLOC + name);
            if (!temp1.exists() || (name.equals("recipes.xml") && FORCECOPYRECIPES))
            {
                ArrayList<String> rows = getFile("/assets/pokecube/database/" + name);
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
            try
            {
                JAXBContext jaxbContext = JAXBContext.newInstance(XMLRecipes.class);
                Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                FileReader reader = new FileReader(temp1);
                XMLRecipes database = (XMLRecipes) unmarshaller.unmarshal(reader);
                reader.close();
                for (XMLRecipe drop : database.recipes)
                {
                    XMLRecipeHandler.addRecipe(drop);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public static void loadRewards()
    {
        File temp = new File(CONFIGLOC);
        if (!temp.exists())
        {
            temp.mkdirs();
        }
        for (String name : XMLRewardsHandler.recipeFiles)
        {
            name = name + ".xml";
            File temp1 = new File(CONFIGLOC + name);
            if (!temp1.exists() || (name.equals("rewards.xml") && FORCECOPYREWARDS))
            {
                ArrayList<String> rows = getFile("/assets/pokecube/database/" + name);
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
            try
            {
                JAXBContext jaxbContext = JAXBContext.newInstance(XMLRewards.class);
                Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                FileReader reader = new FileReader(temp1);
                XMLRewards database = (XMLRewards) unmarshaller.unmarshal(reader);
                reader.close();
                for (XMLReward drop : database.recipes)
                {
                    XMLRewardsHandler.addReward(drop);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
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
            copyDatabaseFile("animations.json");
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