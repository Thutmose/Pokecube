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
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.utils.PokeType;

public class Database implements IMoveConstants
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
        else if (!file.endsWith(".csv")) file = file + ".csv";
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
        ret = a.toLowerCase().replaceAll("(\\W)", "").equals(b.toLowerCase().replaceAll("(\\W)", ""));
        return ret;
    }

    public static String convertMoveName(String moveNameFromBulbapedia)
    {

        String ret = "";
        String name = moveNameFromBulbapedia.trim().toLowerCase().replaceAll("[^\\w\\s ]", "");
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
        if (name.toLowerCase()
                .contains("mega ")) { return getEntry((name.toLowerCase().replace("mega ", "") + " mega").trim()); }
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

    /** Index 1 = attacked modifiers, index 2 = attacked modifier chance.<br>
     * Index 3 = attacker modifiers, index 4 = attacker modifier chance.<br>
     * 
     * @param input
     * @return */
    private static int[][] getModifiers(String input, boolean selfMove)
    {
        int[][] ret = new int[4][];

        byte effect = 0;
        byte amount = 0;
        int chance = 100;
        int[] amounts = new int[] { 0, 0, 0, 0, 0, 0, 0, 0 };

        String[] stats = input.split("`");

        String[] statEffects = stats[0].split(";");
        if (statEffects.length == 3)
        {
            String stat = statEffects[0];
            effect = stat.equals("atk") ? ATTACK
                    : stat.equals("def") ? DEFENSE
                            : stat.equals("spd") ? VIT
                                    : stat.equals("spatk") ? SPATACK
                                            : stat.equals("spdef") ? SPDEFENSE
                                                    : stat.equals("acc") ? ACCURACY
                                                            : stat.equals("eva") ? EVASION : STATUS_NON;
            if (stat.equalsIgnoreCase("all"))
            {
                effect = ATTACK + DEFENSE + VIT + SPATACK + SPDEFENSE + ACCURACY + EVASION;
            }

            amount = Byte.parseByte(statEffects[1]);
            chance = Integer.parseInt(statEffects[2]);
        }
        else
        {
            effect = 0;
            chance = 100;
            for (int i = 0; i < statEffects.length; i++)
            {
                String stat = statEffects[i];
                byte effec = stat.equals("atk") ? ATTACK
                        : stat.equals("def") ? DEFENSE
                                : stat.equals("spd") ? VIT
                                        : stat.equals("spatk") ? SPATACK
                                                : stat.equals("spdef") ? SPDEFENSE
                                                        : stat.equals("acc") ? ACCURACY
                                                                : stat.equals("eva") ? EVASION : STATUS_NON;
                effect += effec;
                int j = (int) Math.round(Math.log(effec) / Math.log(2)) + 1;

                amounts[j] = Byte.parseByte(statEffects[i + 1]);

                i++;
            }

        }
        if (amount != 0)
        {
            for (int i = 0; i < amounts.length; i++)
            {
                amounts[i] = amount;
            }
        }

        int[] modifiers = new int[] { 0, 0, 0, 0, 0, 0, 0, 0 };
        modifiers[1] = (byte) Math.max(-6, Math.min(6, modifiers[1] + amounts[1] * (effect & 1)));
        modifiers[2] = (byte) Math.max(-6, Math.min(6, modifiers[2] + amounts[2] * ((effect & 2) / 2)));
        modifiers[3] = (byte) Math.max(-6, Math.min(6, modifiers[3] + amounts[3] * ((effect & 4) / 4)));
        modifiers[4] = (byte) Math.max(-6, Math.min(6, modifiers[4] + amounts[4] * ((effect & 8) / 8)));
        modifiers[5] = (byte) Math.max(-6, Math.min(6, modifiers[5] + amounts[5] * ((effect & 16) / 16)));
        modifiers[6] = (byte) Math.max(-6, Math.min(6, modifiers[6] + amounts[6] * ((effect & 32) / 32)));
        modifiers[7] = (byte) Math.max(-6, Math.min(6, modifiers[7] + amounts[7] * ((effect & 64) / 64)));

        if (selfMove)
        {
            ret[0] = new int[] { 0, 0, 0, 0, 0, 0, 0, 0 };
            ret[1] = new int[] { 0 };
            ret[2] = modifiers.clone();
            ret[3] = new int[] { chance };
            return ret;
        }
        else if (stats.length == 1)
        {
            ret[2] = new int[] { 0, 0, 0, 0, 0, 0, 0, 0 };
            ret[3] = new int[] { 0 };
            ret[0] = modifiers.clone();
            ret[1] = new int[] { chance };
            return ret;
        }
        ret[0] = modifiers.clone();
        ret[1] = new int[] { chance };

        effect = 0;
        chance = 100;

        statEffects = stats[1].split(";");
        if (statEffects.length == 3)
        {
            String stat = statEffects[0];
            effect = stat.equals("atk") ? ATTACK
                    : stat.equals("def") ? DEFENSE
                            : stat.equals("spd") ? VIT
                                    : stat.equals("spatk") ? SPATACK
                                            : stat.equals("spdef") ? SPDEFENSE
                                                    : stat.equals("acc") ? ACCURACY
                                                            : stat.equals("eva") ? EVASION : STATUS_NON;
            if (stat.equalsIgnoreCase("all"))
            {
                effect = ATTACK + DEFENSE + VIT + SPATACK + SPDEFENSE + ACCURACY + EVASION;
            }

            amount = Byte.parseByte(statEffects[1]);
            chance = Integer.parseInt(statEffects[2]);
        }
        else
        {
            effect = 0;
            chance = 100;
            for (int i = 0; i < statEffects.length; i++)
            {
                String stat = statEffects[i];
                byte effec = stat.equals("atk") ? ATTACK
                        : stat.equals("def") ? DEFENSE
                                : stat.equals("spd") ? VIT
                                        : stat.equals("spatk") ? SPATACK
                                                : stat.equals("spdef") ? SPDEFENSE
                                                        : stat.equals("acc") ? ACCURACY
                                                                : stat.equals("eva") ? EVASION : STATUS_NON;
                effect += effec;
                int j = (int) Math.round(Math.log(effec) / Math.log(2)) + 1;

                amounts[j] = Byte.parseByte(statEffects[i + 1]);

                i++;
            }

        }
        if (amount != 0)
        {
            for (int i = 0; i < amounts.length; i++)
            {
                amounts[i] = amount;
            }
        }

        modifiers[1] = (byte) Math.max(-6, Math.min(6, modifiers[1] + amounts[1] * (effect & 1)));
        modifiers[2] = (byte) Math.max(-6, Math.min(6, modifiers[2] + amounts[2] * ((effect & 2) / 2)));
        modifiers[3] = (byte) Math.max(-6, Math.min(6, modifiers[3] + amounts[3] * ((effect & 4) / 4)));
        modifiers[4] = (byte) Math.max(-6, Math.min(6, modifiers[4] + amounts[4] * ((effect & 8) / 8)));
        modifiers[5] = (byte) Math.max(-6, Math.min(6, modifiers[5] + amounts[5] * ((effect & 16) / 16)));
        modifiers[6] = (byte) Math.max(-6, Math.min(6, modifiers[6] + amounts[6] * ((effect & 32) / 32)));
        modifiers[7] = (byte) Math.max(-6, Math.min(6, modifiers[7] + amounts[7] * ((effect & 64) / 64)));

        ret[2] = modifiers.clone();
        ret[3] = new int[] { chance };

        return ret;
    }

    private static ArrayList<ArrayList<String>> getRows(String file)
    {
        InputStream res = (Database.class).getResourceAsStream(file);

        ArrayList<ArrayList<String>> rows = new ArrayList<ArrayList<String>>();
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";

        try
        {

            br = new BufferedReader(new InputStreamReader(res));
            int n = 0;
            while ((line = br.readLine()) != null)
            {

                String[] row = line.split(cvsSplitBy);
                rows.add(new ArrayList<String>());
                for (int i = 0; i < row.length; i++)
                {
                    rows.get(n).add(row[i]);
                }
                n++;
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
                int n = 0;
                while ((line = br.readLine()) != null)
                {

                    String[] row = line.split(cvsSplitBy);
                    rows.add(new ArrayList<String>());
                    for (int i = 0; i < row.length; i++)
                    {
                        rows.get(n).add(row[i]);
                    }
                    n++;
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
        ArrayList<ArrayList<String>> rows = getRows(file);

        for (ArrayList<String> s : rows)
        {
            if (s == null || s.size() < 7 || s.get(0).equalsIgnoreCase("number")) continue;
            int index = Integer.parseInt(s.get(0).trim());
            String name = convertMoveName(s.get(1));

            MoveEntry move = new MoveEntry(name, index);

            move.type = PokeType.getType(s.get(2));

            String cat = s.get(3).trim().toLowerCase();
            if (cat.contains("spec") || cat.contains("status")) move.category = SPECIAL;
            else move.category = PHYSICAL;
            if (cat.contains("distance"))
            {
                move.attackCategory = CATEGORY_DISTANCE;
            }
            if (cat.contains("contact"))
            {
                move.attackCategory = CATEGORY_CONTACT;
            }
            if (cat.equals("self"))
            {
                move.attackCategory = CATEGORY_SELF;
            }
            else if (cat.contains("self"))
            {
                move.attackCategory += CATEGORY_SELF_EFFECT;
            }

            if (move.attackCategory == 0)
            {
                if (move.category == SPECIAL)
                {
                    move.attackCategory = CATEGORY_DISTANCE;
                }
                else
                {
                    move.attackCategory = CATEGORY_CONTACT;
                }
            }

            if (move.category == -1)
            {
                move.category = (move.attackCategory & CATEGORY_DISTANCE) > 0 ? SPECIAL : PHYSICAL;
            }

            move.pp = Integer.parseInt(s.get(5));

            try
            {
                move.power = Integer.parseInt(s.get(6));
            }
            catch (NumberFormatException e)
            {
                if (s.get(6).equalsIgnoreCase("level"))
                {
                    move.power = MoveEntry.LEVEL;
                }
                else
                {
                    move.power = MoveEntry.NODAMAGE;
                }
            }

            try
            {
                move.accuracy = (int) Float.parseFloat(s.get(7).replace("%", ""));
            }
            catch (NumberFormatException e)
            {
                move.accuracy = -1;
            }

            String[] statusEffects = s.get(9).trim().toLowerCase().split(";");
            int chance = 0;
            byte effect = 0;
            if (statusEffects.length == 2)
            {
                String status = statusEffects[0];
                chance = Integer.parseInt(statusEffects[1]);
                effect = status.equals("par") ? STATUS_PAR
                        : status.equals("brn") ? STATUS_BRN
                                : status.equals("frz") ? STATUS_FRZ
                                        : status.equals("slp") ? STATUS_SLP
                                                : status.equals("psn") ? STATUS_PSN
                                                        : status.equals("psn2") ? STATUS_PSN2 : STATUS_NON;
            }
            else
            {
                // TODO finish this for tri-attack
            }
            move.statusChange = effect;
            move.statusChance = chance;

            int[][] statmods = getModifiers(s.get(10).trim().toLowerCase(),
                    (move.attackCategory & CATEGORY_SELF) > 0 || (move.attackCategory & CATEGORY_SELF_EFFECT) > 0);

            move.attackerStatModification = statmods[2];
            move.attackerStatModProb = statmods[3][0];
            move.attackedStatModification = statmods[0];
            move.attackedStatModProb = statmods[1][0];

            String[] changes = s.get(11).split(";");
            if (!changes[0].equals("none"))
            {
                move.change = changes[0].equalsIgnoreCase("flinch") ? CHANGE_FLINCH : CHANGE_CONFUSED;
                move.chanceChance = Integer.parseInt(changes[1].trim());
            }
            changes = s.get(12).split(";");
            move.damageHealRatio = Integer.parseInt(changes[0].trim().replace("%", ""));
            move.selfHealRatio = Integer.parseInt(changes[1].trim().replace("%", ""));

            changes = s.get(13).split(";");
            move.multiTarget = Boolean.parseBoolean(changes[0].trim());
            move.notIntercepable = Boolean.parseBoolean(changes[1].trim());

            move.protect = Boolean.parseBoolean(s.get(14).trim());
            move.magiccoat = Boolean.parseBoolean(s.get(15).trim());
            move.snatch = Boolean.parseBoolean(s.get(16).trim());
            move.kingsrock = Boolean.parseBoolean(s.get(17).trim());

            move.crit = Integer.parseInt(s.get(18).trim());

            changes = s.get(19).split(";");
            if (changes.length > 1)
            {
                String amt = changes[0].replace("%", "");
                String cond = changes[1];
                move.selfDamage = Float.parseFloat(amt);
                move.selfDamageType = cond.contains("miss") ? MoveEntry.MISS
                        : cond.contains("hp") ? MoveEntry.RELATIVEHP : MoveEntry.DAMAGEDEALT;
            }
            String anim = s.get(20);
            move.animDefault = anim;
        }
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

            copyDatabaseFile("moves.csv");

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