package pokecube.core.database;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
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
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.ProgressManager;
import net.minecraftforge.fml.common.ProgressManager.ProgressBar;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.IForgeRegistryEntry;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
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
import pokecube.core.events.onload.InitDatabase;
import pokecube.core.handlers.playerdata.PokecubePlayerStats;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.utils.PokeType;

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
     * Index 0 = pokemobs<br>
     * Index 1 = moves<br>
    */
    public static enum EnumDatabase
    {
        POKEMON, MOVES
    }

    public static boolean                                   FORCECOPY        = true;
    public static boolean                                   FORCECOPYRECIPES = true;
    public static boolean                                   FORCECOPYREWARDS = true;
    public static List<ItemStack>                           starterPack      = Lists.newArrayList();
    public static Int2ObjectOpenHashMap<PokedexEntry>       data             = new Int2ObjectOpenHashMap<>();
    public static HashMap<String, PokedexEntry>             data2            = new HashMap<String, PokedexEntry>();
    public static HashSet<PokedexEntry>                     allFormes        = new HashSet<PokedexEntry>();
    public static HashMap<Integer, PokedexEntry>            baseFormes       = new HashMap<Integer, PokedexEntry>();
    public static HashMap<String, ArrayList<PokedexEntry>>  mobReplacements  = new HashMap<String, ArrayList<PokedexEntry>>();
    public static Int2ObjectOpenHashMap<List<PokedexEntry>> formLists        = new Int2ObjectOpenHashMap<>();

    public static List<PokedexEntry>                        spawnables       = new ArrayList<PokedexEntry>();
    public static final String                              DBLOCATION       = "/assets/pokecube/database/";

    public static final String                              CONFIGLOC        = "." + File.separator + "config"
            + File.separator + "pokecube" + File.separator + "database" + File.separator;

    static HashSet<String>                                  defaultDatabases = Sets.newHashSet();
    private static HashSet<String>                          spawnDatabases   = Sets.newHashSet();
    private static Set<String>                              dropDatabases    = Sets.newHashSet();
    private static Set<String>                              heldDatabases    = Sets.newHashSet();

    public static final PokedexEntry                        missingno        = new PokedexEntry(0, "missingno");

    public static final Comparator<PokedexEntry>            COMPARATOR       = new Comparator<PokedexEntry>()
                                                                             {
                                                                                 @Override
                                                                                 public int compare(PokedexEntry o1,
                                                                                         PokedexEntry o2)
                                                                                 {
                                                                                     int diff = o1.getPokedexNb()
                                                                                             - o2.getPokedexNb();
                                                                                     if (diff == 0)
                                                                                     {
                                                                                         if (o1.base && !o2.base)
                                                                                             diff = -1;
                                                                                         else if (o2.base && !o1.base)
                                                                                             diff = 1;
                                                                                     }
                                                                                     return diff;
                                                                                 }
                                                                             };

    // Init some stuff for the missignno entry.
    static
    {
        missingno.type1 = PokeType.unknown;
        missingno.type2 = PokeType.unknown;
        missingno.evs = new byte[6];
        missingno.stats = new int[6];
        missingno.stats[0] = 33;
        missingno.stats[1] = 136;
        missingno.stats[2] = 0;
        missingno.stats[3] = 6;
        missingno.stats[4] = 6;
        missingno.stats[5] = 29;
        missingno.addMoves(Lists.newArrayList(), Maps.newHashMap());
        missingno.addMove("skyattack");
    }

    /** These are used for config added databasea <br>
     * Index 0 = pokemon<br>
     * Index 1 = moves<br>
    */
    @SuppressWarnings("unchecked")
    public static List<ArrayList<String>> configDatabases = Lists.newArrayList(new ArrayList<String>(),
            new ArrayList<String>());

    public static void addDatabase(String file, EnumDatabase database)
    {
        int index = database.ordinal();
        ArrayList<String> list = configDatabases.get(index);
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

    public static List<PokedexEntry> getFormes(PokedexEntry variant)
    {
        return getFormes(variant.getPokedexNb());
    }

    public static List<PokedexEntry> getFormes(int number)
    {
        return formLists.get(number);
    }

    public static String convertMoveName(String moveNameFromBulbapedia)
    {
        String ret = trim(moveNameFromBulbapedia);
        return ret;
    }

    static void copyDatabaseFile(String name)
    {
        File temp1 = new File(CONFIGLOC + name);
        if (temp1.exists() && !FORCECOPY)
        {
            PokecubeMod.log("Not Overwriting old database: " + temp1);
            return;
        }
        ArrayList<String> rows = getFile(DBLOCATION + name);
        int n = 0;
        try
        {
            File file = new File(CONFIGLOC + name);
            file.getParentFile().mkdirs();
            PokecubeMod.log("Copying Database File: " + file);
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

    static String trim(String name)
    {
        // English locale to prevent issues with turkish letters.
        name = name.toLowerCase(Locale.ENGLISH);
        // Replace all non word chars.
        name = name.replaceAll("([\\W])", "");
        return name;
    }

    public static PokedexEntry getEntry(String name)
    {
        PokedexEntry ret = null;
        if (name == null) return null;
        if (name.trim().isEmpty()) return null;
        if (data2.containsKey(name)) return data2.get(name);
        String newName = trim(name);
        for (PokedexEntry e : allFormes)
        {
            String s = e.getTrimmedName();
            if (s.equals(newName))
            {
                data2.put(name, e);
                data2.put(newName, e);
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
            PokecubeMod.log(Level.SEVERE, "Missing a Database file " + file, e);
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
            catch (Exception e1)
            {
                PokecubeMod.log(Level.SEVERE, "Error with " + file, e1);
            }

        }
        catch (Exception e)
        {
            PokecubeMod.log(Level.SEVERE, "Error with " + file, e);
        }
        finally
        {
            if (br != null)
            {
                try
                {
                    br.close();
                }
                catch (Exception e)
                {
                    PokecubeMod.log(Level.SEVERE, "Error with " + file, e);
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
        for (String s : configDatabases.get(EnumDatabase.MOVES.ordinal()))
        {
            try
            {
                File moves = new File(CONFIGLOC + s);
                File anims = new File(Database.CONFIGLOC + "animations.json");
                JsonMoves.loadMoves(moves);
                JsonMoves.merge(anims, moves);
                MovesParser.load(moves);
            }
            catch (Exception e1)
            {
                PokecubeMod.log(Level.SEVERE, "Error with " + CONFIGLOC + s + " " + e1);
            }
        }
        for (String s : configDatabases.get(EnumDatabase.POKEMON.ordinal()))
        {
            try
            {
                PokedexEntryLoader.initDatabase(new File(CONFIGLOC + "pokemobs" + File.separator + s));
            }
            catch (Exception e)
            {
                PokecubeMod.log(Level.SEVERE, "Error with " + CONFIGLOC + "pokemobs" + File.separator + s + " " + e);
            }
        }
        MinecraftForge.EVENT_BUS.post(new InitDatabase.Load());

        XMLWorldgenHandler.reloadWorldgen();

        try
        {
            PokedexEntryLoader.makeEntries(true);
        }
        catch (Exception e)
        {
            PokecubeMod.log(Level.SEVERE, "Error with databases " + e);
            allFormes.add(missingno);
            // TODO autoregister missingno here?
            // throw new RuntimeException("Database loading failed, this is very
            // bad.");
        }
        PokedexEntryLoader.writeCompoundDatabase();
        initFormLists();
        PokecubeMod.log("Loaded " + data.size() + " by number, and " + allFormes.size() + " by formes from databases.");
    }

    public static void initSounds(Object registry)
    {
        for (PokedexEntry e : allFormes)
        {
            if (e.getModId() == null) continue;
            if (e.sound == null)
            {
                if (e.base) e.setSound("mobs." + e.getTrimmedName());
                else e.setSound("mobs." + e.getBaseForme().getTrimmedName());
            }
            e.event = new SoundEvent(e.sound);
            if (SoundEvent.REGISTRY.containsKey(e.sound)) continue;
            ReflectionHelper.setPrivateValue(IForgeRegistryEntry.Impl.class, e.event, e.sound, "registryName");
            GameRegistry.register(e.event);
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

    private static void initFormLists()
    {
        ProgressBar bar = ProgressManager.push("Form Processing", baseFormes.size());
        for (Map.Entry<Integer, PokedexEntry> vars : baseFormes.entrySet())
        {
            PokedexEntry entry = vars.getValue();
            bar.step(entry.getName());
            List<PokedexEntry> formes = Lists.newArrayList();
            Set<PokedexEntry> set = Sets.newHashSet();
            set.addAll(entry.forms.values());
            set.add(entry);
            /** Collect all the different forms we can for this mob. */
            for (PokedexEntry e : allFormes)
                if (e.getPokedexNb() == entry.getPokedexNb()) set.add(e);
            PokedexEntry female = entry.getForGender(IPokemob.FEMALE);
            PokedexEntry male = entry.getForGender(IPokemob.MALE);
            set.add(male);
            set.add(female);
            formes.addAll(set);
            formes.sort(COMPARATOR);

            /** First init the formes, to copy the stuff over from the current
             * base forme if needed. */
            if (formes.size() > 1) initFormes(formes, entry);
            /** If the forme has both male and female entries, replace the base
             * forme with the male forme. */
            if (male != female && male != entry && female != entry)
            {
                male.base = true;
                male.male = male;
                female.male = male;
                male.female = female;
                data.put(male.getPokedexNb(), male);
                data2.put(entry.getTrimmedName(), male);
                vars.setValue(male);
                // Set all the subformes base to this new one.
                for (PokedexEntry e : formes)
                {
                    // Set the forme.
                    e.setBaseForme(male);
                    // Initialize some things.
                    e.getBaseForme();
                }
                entry = male;
            }
            formLists.put(entry.getPokedexNb(), formes);
        }
        ProgressManager.pop(bar);
    }

    private static void initFormes(List<PokedexEntry> formes, PokedexEntry base)
    {
        base.copyToGenderFormes();
        PokecubeMod.log("Processing " + base + " " + formes);
        for (PokedexEntry e : formes)
        {
            e.forms.clear();
            for (PokedexEntry e1 : formes)
            {
                if (e1 != e) e.forms.put(e1.getTrimmedName(), e1);
            }
            if (base != e)
            {
                e.setBaseForme(base);
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
                    PokecubeMod.log("Error with " + e);
                }
                if (e.species == null)
                {
                    e.childNumbers = base.childNumbers;
                    e.species = base.species;
                    PokecubeMod.log(e + " Has no Species");
                }
                if (e.type1 == null)
                {
                    e.type1 = base.type1;
                    e.type2 = base.type2;
                    if (PokecubeMod.debug) PokecubeMod.log("Copied Types from " + base + " to " + e);
                }
                boolean noAbilities;
                if (noAbilities = e.abilities.isEmpty()) e.abilities.addAll(base.abilities);
                if (noAbilities && e.abilitiesHidden.isEmpty()) e.abilitiesHidden.addAll(base.abilitiesHidden);
            }
            if (e.mobType == null)
            {
                e.mobType = PokecubeMod.Type.NORMAL;
                PokecubeMod.log(e + " Has no Mob Type");
            }
            if (e.type2 == null) e.type2 = PokeType.unknown;
            if (e.interactionLogic.actions.isEmpty())
            {
                if (e.getBaseForme() != null)
                {
                    if (e.getBaseForme().interactionLogic.actions.isEmpty())
                    {
                        InteractionLogic.initForEntry(e);
                    }
                    e.interactionLogic.actions = e.getBaseForme().interactionLogic.actions;
                }
                else
                {
                    InteractionLogic.initForEntry(e);
                }
            }
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
            PokecubeMod.log(Level.SEVERE, "Error with " + file, e);
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
        PokecubeMod.log("Post Init of Database.");
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
        int cleaned = 0;
        for (PokedexEntry p : allFormes)
        {
            bar.step(p.getName());
            if (!Pokedex.getInstance().getRegisteredEntries().contains(p))
            {
                if (p.base && p != getEntry(p.getPokedexNb()))
                {
                    cleaned++;
                }
                else removedNums.add(p.getPokedexNb());
                toRemove.add(p);
                removed.add(p);
            }
        }
        Collections.sort(toRemove, COMPARATOR);
        PokecubeMod.log("Removing " + toRemove);
        ProgressManager.pop(bar);
        bar = ProgressManager.push("Removal", toRemove.size());
        for (PokedexEntry p : toRemove)
        {
            bar.step(p.getName());
            if (p == getEntry(p.pokedexNb))
            {
                data.remove(p.pokedexNb);
                baseFormes.remove(p.pokedexNb);
                formLists.remove(p.pokedexNb);
            }
            else if (formLists.containsKey(p.pokedexNb))
            {
                formLists.get(p.pokedexNb).remove(p);
            }
            spawnables.remove(p);
        }

        for (PokedexEntry e : allFormes)
        {
            List<EvolutionData> invalidEvos = Lists.newArrayList();
            for (EvolutionData d : e.evolutions)
            {
                if (!Pokedex.getInstance().getRegisteredEntries().contains(d.evolution))
                {
                    invalidEvos.add(d);
                }
            }
            e.evolutions.removeAll(invalidEvos);
        }

        allFormes.removeAll(toRemove);
        ProgressManager.pop(bar);
        PokecubeMod.log("Removed " + removedNums.size() + " Missing Pokemon and " + (toRemove.size() - cleaned)
                + " missing Formes");

        toRemove.clear();
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
                PokecubeMod.log(Level.WARNING, name + " " + n, e);
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
}