package pokecube.core.database;

import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.ProgressManager;
import net.minecraftforge.fml.common.ProgressManager.ProgressBar;
import pokecube.core.PokecubeItems;
import pokecube.core.database.PokedexEntry.EvolutionData;
import pokecube.core.database.PokedexEntry.InteractionLogic;
import pokecube.core.database.PokedexEntry.MegaRule;
import pokecube.core.database.PokedexEntry.SpawnData;
import pokecube.core.database.PokedexEntry.SpawnData.SpawnEntry;
import pokecube.core.database.PokedexEntryLoader.StatsNode.Stats;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.utils.PokeType;
import pokecube.core.utils.Tools;

public class PokedexEntryLoader
{
    public static class MegaEvoRule implements MegaRule
    {
        final ItemStack    stack;
        final String       moveName;
        final PokedexEntry baseForme;

        public MegaEvoRule(ItemStack stack, String moveName, PokedexEntry baseForme)
        {
            this.stack = stack;
            this.moveName = moveName;
            this.baseForme = baseForme;
        }

        @Override
        public boolean shouldMegaEvolve(IPokemob mobIn)
        {
            boolean rightStack = true;
            boolean hasMove = true;
            boolean rule = false;
            if (stack != null)
            {
                rightStack = Tools.isSameStack(stack, ((EntityLivingBase) mobIn).getHeldItemMainhand());
                rule = true;
            }
            if (moveName != null && !moveName.isEmpty())
            {
                hasMove = Tools.hasMove(moveName, mobIn);
                rule = true;
            }
            return rule && hasMove && rightStack;
        }
    }

    @XmlRootElement(name = "BODY")
    public static class BodyNode
    {
        @XmlElement(name = "PART")
        List<BodyPart> parts = Lists.newArrayList();
    }

    @XmlRootElement(name = "PART")
    public static class BodyPart
    {
        @XmlAttribute(name = "name")
        String name;
        @XmlAttribute(name = "offset")
        String offset;
        @XmlAttribute(name = "dimensions")
        String dimensions;
    }

    @XmlRootElement(name = "Drop")
    public static class Drop
    {
        @XmlAnyAttribute
        Map<QName, String> values = Maps.newHashMap();
        @XmlElement(name = "tag")
        String             tag;
    }

    @XmlRootElement(name = "Interact")
    public static class Interact
    {
        @XmlElement(name = "Key")
        Key    key;
        @XmlElement(name = "Action")
        Action action;
    }

    @XmlRootElement(name = "Key")
    public static class Key
    {
        @XmlAnyAttribute
        Map<QName, String> values = Maps.newHashMap();
        @XmlElement(name = "tag")
        String             tag;
    }

    @XmlRootElement(name = "Action")
    public static class Action
    {
        @XmlAnyAttribute
        Map<QName, String> values = Maps.newHashMap();
        @XmlElement(name = "tag")
        String             tag;
        @XmlElement(name = "Drop")
        List<Drop>         drops  = Lists.newArrayList();
    }

    @XmlRootElement(name = "Spawn")
    public static class SpawnRule
    {
        @XmlAnyAttribute
        Map<QName, String> values = Maps.newHashMap();
    }

    @XmlRootElement(name = "MOVES")
    public static class Moves
    {
        @XmlRootElement(name = "LVLUP")
        public static class LvlUp
        {
            @XmlAnyAttribute
            Map<QName, String> values;
        }

        @XmlRootElement(name = "MISC")
        public static class Misc
        {
            @XmlAttribute(name = "moves")
            String moves;

            @Override
            public String toString()
            {
                return moves;
            }
        }

        @XmlElement(name = "LVLUP")
        LvlUp lvlupMoves;

        @XmlElement(name = "MISC")
        Misc  misc;
    }

    @XmlRootElement(name = "STATS")
    public static class StatsNode
    {
        public static class Stats
        {
            @XmlAnyAttribute
            Map<QName, String> values;
        }

        @XmlAttribute
        public String   spawns;
        // Evolution stuff
        @XmlElement(name = "EVOLUTIONMODE")
        String          evoModes;
        @XmlElement(name = "EVOLUTIONANIMATION")
        String          evolAnims;

        @XmlElement(name = "EVOLVESTO")
        String          evoTo;
        // Species and food
        @XmlElement(name = "SPECIES")
        String          species;
        @XmlElement(name = "PREY")
        String          prey;
        @XmlElement(name = "FOODMATERIAL")
        String          foodMat;

        @XmlElement(name = "SPECIALEGGSPECIESRULES")
        String          specialEggRules;
        // Drops and items
        @XmlElement(name = "FOODDROP")
        String          foodDrop;
        @XmlElement(name = "COMMONDROP")
        String          commonDrop;
        @XmlElement(name = "RAREDROP")
        String          rareDrop;

        @XmlElement(name = "Drop")
        List<Drop>      drops          = Lists.newArrayList();
        @XmlElement(name = "Held")
        List<Drop>      held           = Lists.newArrayList();

        @XmlElement(name = "HELDITEM")
        String          heldItems;
        // Spawn Rules
        @XmlElement(name = "Spawn")
        List<SpawnRule> spawnRules     = Lists.newArrayList();
        // STATS
        @XmlElement(name = "BASESTATS")
        Stats           stats;
        @XmlElement(name = "EVYIELD")
        Stats           evs;
        @XmlElement(name = "SIZES")
        Stats           sizes;
        @XmlElement(name = "TYPE")
        Stats           types;
        @XmlElement(name = "ABILITY")
        Stats           abilities;
        @XmlElement(name = "MASSKG")
        float           mass           = -1;
        @XmlElement(name = "CAPTURERATE")
        int             captureRate    = -1;
        @XmlElement(name = "EXPYIELD")
        int             baseExp        = -1;
        @XmlElement(name = "BASEFRIENDSHIP")
        int             baseFriendship = -1;
        @XmlElement(name = "EXPERIENCEMODE")
        String          expMode;

        @XmlElement(name = "GENDERRATIO")
        int             genderRatio    = -1;
        // MISC
        @XmlElement(name = "LOGIC")
        Stats           logics;
        @XmlElement(name = "FORMEITEMS")
        Stats           formeItems;
        @XmlElement(name = "MEGARULES")
        Stats           megaRules;
        @XmlElement(name = "MOVEMENTTYPE")
        String          movementType;
        @XmlElement(name = "Interact")
        List<Interact>  interactions   = Lists.newArrayList();
        @XmlElement(name = "SHADOWREPLACEMENTS")
        String          shadowReplacements;
        @XmlElement(name = "HATEDMATERIALRULES")
        String          hatedMaterials;

        @XmlElement(name = "ACTIVETIMES")
        String          activeTimes;
    }

    @XmlRootElement(name = "Document")
    public static class XMLDatabase
    {
        @XmlElement(name = "Pokemon")
        private List<XMLPokedexEntry> pokemon = Lists.newArrayList();
    }

    @XmlRootElement(name = "Pokemon")
    public static class XMLPokedexEntry
    {
        @XmlAttribute
        public String  name;
        @XmlAttribute
        public int     number;
        @XmlAttribute
        public String  special;
        @XmlAttribute
        public boolean base    = false;
        @XmlAttribute
        public boolean breed   = true;
        @XmlAttribute
        public boolean starter = false;
        @XmlAttribute
        public boolean legend  = false;
        @XmlElement(name = "STATS")
        StatsNode      stats;
        @XmlElement(name = "MOVES")
        Moves          moves;
        @XmlElement(name = "BODY")
        BodyNode       body;

        @Override
        public String toString()
        {
            return name + " " + number + " " + stats + " " + moves;
        }

        void mergeMissingFrom(XMLPokedexEntry other)
        {
            if (moves == null && other.moves != null)
            {
                moves = other.moves;
            }
            else if (other.moves != null)
            {
                if (moves.lvlupMoves == null)
                {
                    moves.lvlupMoves = other.moves.lvlupMoves;
                }
                if (moves.misc == null)
                {
                    moves.misc = other.moves.misc;
                }
            }
            if (body == null && other.body != null)
            {
                body = other.body;
            }
            if (stats == null && other.stats != null)
            {
                stats = other.stats;
            }
            else if (other.stats != null)
            {
                // Copy everything which is missing
                for (Field f : StatsNode.class.getDeclaredFields())
                {
                    try
                    {
                        Object ours = f.get(stats);
                        Object theirs = f.get(other.stats);
                        boolean isNumber = !(ours instanceof String || ours instanceof Stats);
                        if (isNumber)
                        {
                            if (ours instanceof Float)
                            {
                                isNumber = (float) ours == -1;
                            }
                            else if (ours instanceof Integer)
                            {
                                isNumber = (int) ours == -1;
                            }
                        }
                        if (ours == null)
                        {
                            f.set(stats, theirs);
                        }
                        else if (isNumber)
                        {
                            f.set(stats, theirs);
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    static XMLDatabase          database;

    static Set<XMLPokedexEntry> entries = Sets.newHashSet();

    public static void addOverrideEntry(XMLPokedexEntry entry, boolean overwrite)
    {
        for (XMLPokedexEntry e : entries)
        {
            if (e.name.equals(entry.name))
            {
                if (overwrite)
                {
                    entries.remove(e);
                    entries.add(entry);
                    entry.mergeMissingFrom(e);
                    return;
                }
                else
                {
                    return;
                }
            }
        }
        entries.add(entry);
    }

    private static void initMoves(PokedexEntry entry, Moves xmlMoves)
    {
        Map<Integer, ArrayList<String>> lvlUpMoves = new HashMap<Integer, ArrayList<String>>();
        ArrayList<String> allMoves = new ArrayList<String>();
        if (xmlMoves.misc != null)
        {
            String[] misc = xmlMoves.misc.moves.split(",");
            for (String s : misc)
            {
                allMoves.add(Database.convertMoveName(s));
            }
        }
        if (xmlMoves.lvlupMoves != null)
        {
            for (QName key : xmlMoves.lvlupMoves.values.keySet())
            {
                String keyName = key.toString();
                String[] values = xmlMoves.lvlupMoves.values.get(key).split(",");
                ArrayList<String> moves;
                lvlUpMoves.put(Integer.parseInt(keyName.replace("lvl_", "")), moves = new ArrayList<String>());
                moves:
                for (String s : values)
                {
                    s = Database.convertMoveName(s);
                    moves.add(s);
                    for (String s1 : allMoves)
                    {
                        if (s1.equalsIgnoreCase(s)) continue moves;
                    }
                    allMoves.add(Database.convertMoveName(s));
                }
            }
        }

        if (allMoves.isEmpty())
        {
            allMoves = null;
        }
        if (lvlUpMoves.isEmpty())
        {
            lvlUpMoves = null;
        }
        entry.addMoves(allMoves, lvlUpMoves);

    }

    private static void initStats(PokedexEntry entry, StatsNode xmlStats)
    {
        int[] stats = new int[6];
        byte[] evs = new byte[6];
        boolean stat = false, ev = false;
        if (xmlStats.stats != null)
        {
            Map<QName, String> values = xmlStats.stats.values;
            for (QName key : values.keySet())
            {
                String keyString = key.toString();
                String value = values.get(key);
                if (keyString.equals("hp")) stats[0] = Integer.parseInt(value);
                if (keyString.equals("atk")) stats[1] = Integer.parseInt(value);
                if (keyString.equals("def")) stats[2] = Integer.parseInt(value);
                if (keyString.equals("spatk")) stats[3] = Integer.parseInt(value);
                if (keyString.equals("spdef")) stats[4] = Integer.parseInt(value);
                if (keyString.equals("spd")) stats[5] = Integer.parseInt(value);
            }
            stat = true;
        }
        if (xmlStats.evs != null)
        {
            Map<QName, String> values = xmlStats.evs.values;
            for (QName key : values.keySet())
            {
                String keyString = key.toString();
                String value = values.get(key);
                if (keyString.equals("hp")) evs[0] = (byte) Integer.parseInt(value);
                if (keyString.equals("atk")) evs[1] = (byte) Integer.parseInt(value);
                if (keyString.equals("def")) evs[2] = (byte) Integer.parseInt(value);
                if (keyString.equals("spatk")) evs[3] = (byte) Integer.parseInt(value);
                if (keyString.equals("spdef")) evs[4] = (byte) Integer.parseInt(value);
                if (keyString.equals("spd")) evs[5] = (byte) Integer.parseInt(value);
            }
            ev = true;
        }
        if (stat)
        {
            entry.stats = stats;
        }
        if (ev)
        {
            entry.evs = evs;
        }
        if (xmlStats.types != null)
        {
            Map<QName, String> values = xmlStats.types.values;
            for (QName key : values.keySet())
            {
                String keyString = key.toString();
                String value = values.get(key);
                if (keyString.equals("type1"))
                {
                    entry.type1 = PokeType.getType(value);
                }
                if (keyString.equals("type2"))
                {
                    entry.type2 = PokeType.getType(value);
                }
            }
        }
        if (xmlStats.sizes != null)
        {
            Map<QName, String> values = xmlStats.sizes.values;
            entry.length = -1;
            entry.width = -1;
            for (QName key : values.keySet())
            {
                String keyString = key.toString();
                String value = values.get(key);
                if (keyString.equals("height"))
                {
                    entry.height = Float.parseFloat(value);
                }
                if (keyString.equals("length"))
                {
                    entry.length = Float.parseFloat(value);
                }
                if (keyString.equals("width"))
                {
                    entry.width = Float.parseFloat(value);
                }
            }
            if (entry.width == -1)
            {
                entry.width = entry.height;
            }
            if (entry.length == -1)
            {
                entry.length = entry.width;
            }
        }
        if (xmlStats.abilities != null)
        {
            Map<QName, String> values = xmlStats.abilities.values;
            for (QName key : values.keySet())
            {
                String keyString = key.toString();
                String value = values.get(key);
                if (keyString.equals("hidden"))
                {
                    String[] vars = value.split(",");
                    for (int i = 0; i < vars.length; i++)
                    {
                        entry.abilitiesHidden.add(vars[i].trim());
                    }
                }
                if (keyString.equals("normal"))
                {
                    String[] vars = value.split(",");
                    for (int i = 0; i < vars.length; i++)
                    {
                        entry.abilities.add(vars[i].trim());
                    }
                    if (entry.abilities.size() == 1)
                    {
                        entry.abilities.add(entry.abilities.get(0));
                    }
                }
            }
        }
        entry.catchRate = xmlStats.captureRate;
        entry.baseXP = xmlStats.baseExp;
        entry.baseHappiness = xmlStats.baseFriendship;
        entry.sexeRatio = xmlStats.genderRatio;
        entry.mass = xmlStats.mass;

        if (xmlStats.specialEggRules != null)
        {
            String[] matedata = xmlStats.specialEggRules.split(";");
            for (String s1 : matedata)
            {
                String[] args = s1.split(":");
                int fatherNb = Integer.parseInt(args[0]);
                String[] args1 = args[1].split("`");
                int[] childNbs = new int[args1.length];
                for (int i = 0; i < args1.length; i++)
                {
                    childNbs[i] = Integer.parseInt(args1[i]);
                }
                entry.childNumbers.put(fatherNb, childNbs);
            }
        }
        if (xmlStats.movementType != null)
        {
            String[] strings = xmlStats.movementType.trim().split(":");
            entry.mobType = PokecubeMod.Type.getType(strings[0]);
            if (strings.length > 1)
            {
                entry.preferedHeight = Double.parseDouble(strings[1]);
            }
        }
        if (xmlStats.species != null)
        {
            entry.species = xmlStats.species.trim().split(" ");
        }
        if (xmlStats.prey != null)
        {
            entry.food = xmlStats.prey.trim().split(" ");
        }
    }

    public static XMLDatabase loadDatabase(File file) throws Exception
    {
        JAXBContext jaxbContext = JAXBContext.newInstance(XMLDatabase.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        XMLDatabase database = (XMLDatabase) unmarshaller.unmarshal(new FileReader(file));
        return database;
    }

    public static void makeEntries(File file, boolean create) throws Exception
    {
        ProgressBar bar = ProgressManager.push("Loading Database", 1);
        if (database == null)
        {
            database = loadDatabase(file);
            entries.addAll(database.pokemon);
        }
        else if (create)
        {
            XMLDatabase toAdd = loadDatabase(file);
            if (toAdd != null)
            {
                for (XMLPokedexEntry e : toAdd.pokemon)
                {
                    addOverrideEntry(e, true);
                }
            }
            else throw new NullPointerException(file + " Contains no database");
        }
        bar.step("Done");
        ProgressManager.pop(bar);

        bar = ProgressManager.push("Loading Pokemon", entries.size());
        for (XMLPokedexEntry xmlEntry : entries)
        {
            String name = xmlEntry.name;
            bar.step(name);
            int number = xmlEntry.number;
            if (create)
            {
                PokedexEntry entry = new PokedexEntry(number, name);
                if (xmlEntry.base)
                {
                    entry.base = xmlEntry.base;
                    Database.baseFormes.put(number, entry);
                    Database.addEntry(entry);
                }
            }
            updateEntry(xmlEntry, create);
        }
        ProgressManager.pop(bar);
        if (create)
        {
            bar = ProgressManager.push("Loading Evolutions", entries.size());
            for (XMLPokedexEntry xmlEntry : entries)
            {
                bar.step(xmlEntry.name);
                preCheckEvolutions(xmlEntry);
            }
            ProgressManager.pop(bar);
        }
    }

    private static void parseEvols(PokedexEntry entry, StatsNode xmlStats, boolean error)
    {
        String numberString = xmlStats.evoTo;
        String dataString = xmlStats.evoModes;
        String fxString = xmlStats.evolAnims;
        if (numberString == null || dataString == null) return;
        if (fxString == null) fxString = "";
        String evolutionNbs = numberString;
        if (evolutionNbs != null && !evolutionNbs.isEmpty())
        {
            String[] evols = numberString.split(" ");
            String[] evolData = dataString.split(" ");
            String[] evolFX = fxString.split(" ");
            if (evols.length != evolData.length)
            {
                System.out.println("Error with evolution data for " + entry);
                new Exception().printStackTrace();
            }
            else
            {
                entry.evolutions.clear();
                for (int i = 0; i < evols.length; i++)
                {
                    String s1 = evols[i];
                    String s2 = evolFX[i % evolFX.length];
                    PokedexEntry evol;
                    try
                    {
                        int num = Integer.parseInt(s1);
                        evol = Database.getEntry(num);
                    }
                    catch (NumberFormatException e)
                    {
                        evol = Database.getEntry(s1);
                    }
                    if (evol != null)
                    {
                        EvolutionData data = null;
                        for (EvolutionData d : entry.evolutions)
                        {
                            if (d.evolution == evol)
                            {
                                data = d;
                                break;
                            }
                        }
                        if (data == null)
                        {
                            data = new EvolutionData(evol, evolData[i], s2);
                            entry.addEvolution(data);
                        }
                        else
                        {
                            data.data = evolData[i];
                            data.FX = s2;
                        }
                    }
                    else if (error) System.out.println("No evolution " + s1 + " for " + entry);
                }
            }
        }
    }

    private static void parseSpawns(PokedexEntry entry, StatsNode xmlStats)
    {
        if (xmlStats.spawnRules.isEmpty()) return;
        boolean overwrite = xmlStats.spawns == null ? false : Boolean.parseBoolean(xmlStats.spawns);
        SpawnData spawnData = entry.getSpawnData();
        if (spawnData == null || overwrite)
        {
            spawnData = new SpawnData();
        }
        SpawnEntry spawnEntry;
        for (SpawnRule rule : xmlStats.spawnRules)
        {
            spawnEntry = new SpawnEntry();
            String val;
            if ((val = rule.values.get(new QName("min"))) != null)
            {
                spawnEntry.min = Integer.parseInt(val);
            }
            if ((val = rule.values.get(new QName("max"))) != null)
            {
                spawnEntry.max = Integer.parseInt(val);
            }
            if ((val = rule.values.get(new QName("rate"))) != null)
            {
                spawnEntry.rate = Float.parseFloat(val);
            }
            SpawnBiomeMatcher matcher = new SpawnBiomeMatcher(rule);
            spawnData.matchers.put(matcher, spawnEntry);
        }
        entry.setSpawnData(spawnData);
        if (!Database.spawnables.contains(entry)) Database.spawnables.add(entry);

    }

    private static void parseSpecial(String special, PokedexEntry entry)
    {
        if (special.equals("shadow"))
        {
            entry.isShadowForme = true;
            if (entry.getBaseForme() != null) entry.getBaseForme().shadowForme = entry;
        }
    }

    private static void postIniStats(PokedexEntry entry, StatsNode xmlStats)
    {

        // Items
        if (!xmlStats.drops.isEmpty())
        {
            for (Drop d : xmlStats.drops)
            {
                Map<QName, String> values = d.values;
                if (d.tag != null)
                {
                    QName name = new QName("tag");
                    values.put(name, d.tag);
                }
                ItemStack stack = Tools.getStack(d.values);
                if (stack != null)
                {
                    float chance = 1;
                    for (QName key : values.keySet())
                    {
                        if (key.toString().equals("r"))
                        {
                            chance = Float.parseFloat(values.get(key));
                            break;
                        }
                    }
                    entry.drops.put(stack, chance);
                }
            }
        }
        if (!xmlStats.held.isEmpty())
        {
            for (Drop d : xmlStats.held)
            {
                Map<QName, String> values = d.values;
                if (d.tag != null)
                {
                    QName name = new QName("tag");
                    values.put(name, d.tag);
                }
                ItemStack stack = Tools.getStack(d.values);
                if (stack != null)
                {
                    float chance = 1;
                    for (QName key : values.keySet())
                    {
                        if (key.toString().equals("r"))
                        {
                            chance = Float.parseFloat(values.get(key));
                            break;
                        }
                    }
                    entry.held.put(stack, chance);
                }
            }
        }
        // Logics
        if (xmlStats.logics != null)
        {
            entry.shouldFly = entry.isType(PokeType.flying);
            Map<QName, String> values = xmlStats.logics.values;
            for (QName key : values.keySet())
            {
                String keyString = key.toString();
                String value = values.get(key);
                if (keyString.equals("shoulder")) entry.canSitShoulder = Boolean.parseBoolean(value);
                if (keyString.equals("fly")) entry.shouldFly = Boolean.parseBoolean(value);
                if (keyString.equals("dive")) entry.shouldDive = Boolean.parseBoolean(value);
                if (keyString.equals("surf")) entry.shouldSurf = Boolean.parseBoolean(value);
                if (keyString.equals("stationary")) entry.isStationary = Boolean.parseBoolean(value);
                if (keyString.equals("dye"))
                {
                    entry.dyeable = Boolean.parseBoolean(value.split("#")[0]);
                    entry.defaultSpecial = Integer.parseInt(value.split("#")[1]);
                }
            }
        }
        if (xmlStats.expMode != null) entry.evolutionMode = Tools.getType(xmlStats.expMode);
        if (xmlStats.shadowReplacements != null)
        {
            String[] replaces = xmlStats.shadowReplacements.split(":");
            for (String s1 : replaces)
            {
                s1 = s1.toLowerCase(java.util.Locale.ENGLISH).trim().replace(" ", "");
                if (s1.isEmpty()) continue;

                if (Database.mobReplacements.containsKey(s1))
                {
                    Database.mobReplacements.get(s1).add(entry);
                }
                else
                {
                    Database.mobReplacements.put(s1, new ArrayList<PokedexEntry>());
                    Database.mobReplacements.get(s1).add(entry);
                }
            }
        }
        if (xmlStats.foodMat != null)
        {
            String[] foods = xmlStats.foodMat.split(" ");
            entry.foods = new boolean[] { false, false, false, false, false, true, false };
            for (String s1 : foods)
            {
                if (s1.equalsIgnoreCase("light"))
                {
                    entry.activeTimes.add(PokedexEntry.day);
                    entry.foods[0] = true;
                }
                else if (s1.equalsIgnoreCase("rock"))
                {
                    entry.foods[1] = true;
                }
                else if (s1.equalsIgnoreCase("electricity"))
                {
                    entry.foods[2] = true;
                }
                else if (s1.equalsIgnoreCase("grass"))
                {
                    entry.foods[3] = true;
                }
                else if (s1.equalsIgnoreCase("water"))
                {
                    entry.foods[6] = true;
                }
                else if (s1.equalsIgnoreCase("none"))
                {
                    entry.foods[4] = true;
                }
            }
        }
        if (entry.isType(PokeType.ghost)) entry.foods[4] = true;

        if (xmlStats.activeTimes != null)
        {
            String[] times = xmlStats.activeTimes.split(" ");
            entry.activeTimes.clear();
            for (String s1 : times)
            {
                if (s1.equalsIgnoreCase("day"))
                {
                    entry.activeTimes.add(PokedexEntry.day);
                }
                else if (s1.equalsIgnoreCase("night"))
                {
                    entry.activeTimes.add(PokedexEntry.night);
                }
                else if (s1.equalsIgnoreCase("dusk"))
                {
                    entry.activeTimes.add(PokedexEntry.dusk);
                }
                else if (s1.equalsIgnoreCase("dawn"))
                {
                    entry.activeTimes.add(PokedexEntry.dawn);
                }
            }
        }
        if (!xmlStats.interactions.isEmpty()) InteractionLogic.initForEntry(entry, xmlStats.interactions);

        if (xmlStats.hatedMaterials != null)
        {
            entry.hatedMaterial = xmlStats.hatedMaterials.split(":");
        }

        if (xmlStats.formeItems != null)
        {
            Map<QName, String> values = xmlStats.formeItems.values;
            entry.formeItems.clear();
            for (QName key : values.keySet())
            {
                String keyString = key.toString();
                String value = values.get(key);
                if (keyString.equals("forme"))
                {
                    String[] args = value.split(",");
                    for (String s : args)
                    {
                        String forme = "";
                        String item = "";
                        String[] args2 = s.split(":");
                        for (String s1 : args2)
                        {
                            String arg1 = s1.trim().substring(0, 1);
                            String arg2 = s1.trim().substring(1);
                            if (arg1.equals("N"))
                            {
                                forme = arg2;
                            }
                            else if (arg1.equals("I"))
                            {
                                item = arg2.replace("`", ":");
                            }
                        }

                        PokedexEntry formeEntry = Database.getEntry(forme);
                        if (!forme.isEmpty() && formeEntry != null)
                        {
                            ItemStack stack = PokecubeItems.getStack(item, false);
                            PokecubeItems.addToHoldables(item);
                            entry.formeItems.put(stack, formeEntry);
                        }
                    }
                }
            }
        }
        if (xmlStats.megaRules != null)
        {
            entry.megaRules.clear();
            Map<QName, String> values = xmlStats.megaRules.values;
            for (QName key : values.keySet())
            {
                String keyString = key.toString();
                String value = values.get(key);
                if (keyString.equals("forme"))
                {
                    String[] args = value.split(",");
                    for (String s : args)
                    {
                        String forme = "";
                        String item = "";
                        String move = "";
                        String[] args2 = s.split(":");
                        for (String s1 : args2)
                        {
                            String arg1 = s1.trim().substring(0, 1);
                            String arg2 = s1.trim().substring(1);
                            if (arg1.equals("N"))
                            {
                                forme = arg2;
                            }
                            else if (arg1.equals("I"))
                            {
                                item = arg2;
                            }
                            else if (arg1.equals("M"))
                            {
                                move = arg2;
                            }
                        }
                        if (forme.contains("___"))
                        {
                            forme = forme.replace("___", entry.getName() + " Mega");
                        }
                        if (item.equals("___"))
                        {
                            item = forme.replace(" ", "").toLowerCase(java.util.Locale.ENGLISH);
                        }

                        PokedexEntry formeEntry = Database.getEntry(forme);
                        if (!forme.isEmpty() && formeEntry != null)
                        {
                            ItemStack stack = item.isEmpty() ? null : PokecubeItems.getStack(item, false);
                            String moveName = move;
                            if (move.isEmpty() && stack == null) continue;
                            MegaRule rule = new MegaEvoRule(stack, moveName, entry);
                            entry.megaRules.put(formeEntry, rule);
                        }
                    }
                }
            }
        }
    }

    public static void postInit()
    {
        ProgressBar bar = ProgressManager.push("Databases",
                Database.defaultDatabases.size() + Database.extraDatabases.size());
        for (String s : Database.defaultDatabases)
        {
            bar.step(s);
            try
            {
                PokedexEntryLoader.makeEntries(new File(Database.DBLOCATION + s), false);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        for (String s : Database.extraDatabases)
        {
            bar.step(s);
            try
            {
                PokedexEntryLoader.makeEntries(new File(s), false);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        PokemobBodies.initBodies();
    }

    public static void preCheckEvolutions(XMLPokedexEntry xmlEntry)
    {
        PokedexEntry entry = Database.getEntry(xmlEntry.name);
        StatsNode stats = xmlEntry.stats;
        if (stats != null) try
        {
            parseEvols(entry, stats, false);
        }
        catch (Exception e)
        {
            System.out.println(xmlEntry + ", " + entry + ": " + e + " ");
        }

    }

    public static void updateEntry(XMLPokedexEntry xmlEntry, boolean init)
    {
        String name = xmlEntry.name;
        PokedexEntry entry = Database.getEntry(name);
        StatsNode stats = xmlEntry.stats;
        Moves moves = xmlEntry.moves;
        if (stats != null) try
        {
            // if (init)
            initStats(entry, stats);
            // else
            if (!init)
            {
                entry.breeds = xmlEntry.breed;
                entry.isStarter = xmlEntry.starter;
                entry.legendary = xmlEntry.legend;
                if (entry.isStarter && !PokecubeMod.core.starters.contains(entry.pokedexNb))
                {
                    PokecubeMod.core.starters.add(entry.pokedexNb);
                    Collections.sort(PokecubeMod.core.starters);
                }
                else if (!entry.isStarter)
                {
                    for (int i = 0; i < PokecubeMod.core.starters.size(); i++)
                        if (PokecubeMod.core.starters.get(i) == entry.pokedexNb) PokecubeMod.core.starters.remove(i);
                }
                postIniStats(entry, stats);
                parseSpawns(entry, stats);
                parseEvols(entry, stats, true);
                if (xmlEntry.special != null)
                {
                    parseSpecial(xmlEntry.special, entry);
                }
            }
        }
        catch (Exception e)
        {
            System.out.println(xmlEntry + ", " + entry + ": " + e + " " + init);
        }
        if (moves != null) initMoves(entry, moves);
    }
}
