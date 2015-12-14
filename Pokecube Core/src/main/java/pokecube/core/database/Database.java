package pokecube.core.database;

import static pokecube.core.utils.PokeType.getType;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.core.database.PokedexEntry.EvolutionData;
import pokecube.core.database.PokedexEntry.InteractionLogic;
import pokecube.core.database.PokedexEntry.SpawnData;
import pokecube.core.database.PokedexEntry.SpawnData.TypeEntry;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.utils.PokeType;
import pokecube.core.utils.Tools;
import thut.api.terrain.BiomeType;

public class Database implements IMoveConstants
{

    public static boolean                                  FORCECOPY       = true;
    public static HashMap<Integer, PokedexEntry>           data            = new HashMap<Integer, PokedexEntry>();
    public static HashMap<String, PokedexEntry>            data2           = new HashMap<String, PokedexEntry>();
    public static HashSet<PokedexEntry>                    allFormes       = new HashSet<PokedexEntry>();
    public static HashMap<String, ArrayList<PokedexEntry>> mobReplacements = new HashMap<String, ArrayList<PokedexEntry>>();
    public static List<PokedexEntry>                       spawnables      = new ArrayList<PokedexEntry>();

    private static String DBLOCATION = "/assets/pokecube/database/";
    public static String  CONFIGLOC  = "";

    private static List<String> externalDatabases = new ArrayList<String>();
    private static List<String> externalSpawnData = new ArrayList<String>();

    private static List<String> externalStatDatabases = new ArrayList<String>();
    private static List<String> externalEVXPDatabases = new ArrayList<String>();
    public static String[]      defaultStarts         = new String[] {
            "Thutmose:mew;Sshiny`Micebeam:shuckle;Sshiny`Mrollout`Macupressure`Mrest`Mgyroball:null",
            "Legnak:beldum;R0:null", "Oracion:mew:Sshiny:null", "Manchou:mew:Sshiny:null", "cflame13:mew:Sshiny:null",
            "Kaividian:magikarp;Sshiny`Mhyperbeam:null" };

    public static void init(FMLPreInitializationEvent evt)
    {

        checkConfigFiles(evt);

        initPokemobs(DBLOCATION + "baseStats.csv");
        for (String s : externalStatDatabases)
        {
            initPokemobs(s);
        }

        System.out.println("Loading Moves Databases");
        loadMoves(DBLOCATION + "moves.csv");
        load(DBLOCATION + "moveLists.csv");
        for (String s : externalDatabases)
        {
            load(s);
        }
    }

    public static void postInit()
    {
        loadStats(DBLOCATION + "baseStats.csv");
        loadEVXP(DBLOCATION + "evsXp.csv");
        loadAbilities(DBLOCATION + "abilities.csv");

        for (String s : externalEVXPDatabases)
        {
            if (s != null) loadEVXP(s);
        }
        for (String s : externalStatDatabases)
        {
            loadStats(s);
        }
        loadSpawns();
        List<PokedexEntry> toRemove = new ArrayList<PokedexEntry>();
        for (PokedexEntry p : data.values())
        {
            if (!Pokedex.getInstance().getEntries().contains(p.getPokedexNb()))
            {
                toRemove.add(p);
            }
        }
        for (PokedexEntry p : toRemove)
        {
            data.remove(p.pokedexNb);
            // System.out.println(p);
            spawnables.remove(p);
        }
        System.err.println("Removed " + toRemove.size() + " Missing Pokedex Entries");

        for (PokedexEntry p : data.values())
        {
            p.initRelations();
        }

        for (PokedexEntry p : data.values())
        {
            p.initPrey();
        }

        for (PokedexEntry p : data.values())
        {
            p.getChildNb();
        }

        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) for (PokedexEntry p : Database.data.values())
        {
            try
            {
                String args = p.getTexture((byte) 0).substring(0, p.getTexture((byte) 0).length() - 4);
                args = args + "Sh.png";
                ResourceLocation tex = new ResourceLocation(p.getModId(), args);
                Minecraft.getMinecraft().getResourceManager().getResource(tex);

                p.hasSpecialTextures[3] = true;
                // System.out.println(p+" Has "+tex);

            }
            catch (Exception e)
            {
                // System.out.println(p+" Has No Texture");
            }
            try
            {
                String args = p.getTexture((byte) 0).substring(0, p.getTexture((byte) 0).length() - 4);
                args = args + "Ra.png";
                ResourceLocation tex = new ResourceLocation(p.getModId(), args);
                Minecraft.getMinecraft().getResourceManager().getResource(tex);

                p.hasSpecialTextures[0] = true;
                // System.out.println(p+" Has "+tex);

            }
            catch (Exception e)
            {
                // System.out.println(p+" Has No Texture");
            }

        }
        // Reloads all the sizes for any formes which were missed on the first
        // pass due to lack of pokedex entries
        updateSizes();
        toRemove.clear();
        for (PokedexEntry e : allFormes)
        {
            if (e.height == -1 && e.baseForme != null)
            {
                e.height = e.baseForme.height;
                e.width = e.baseForme.width;
                e.length = e.baseForme.length;
                e.childNumbers = e.baseForme.childNumbers;
                e.species = e.baseForme.species;
                e.setModId(e.baseForme.getModId());
                e.mobType = e.baseForme.mobType;
                e.catchRate = e.baseForme.catchRate;
                e.mass = e.baseForme.mass;
                e.foodDrop = e.baseForme.foodDrop;
                e.commonDrops = e.baseForme.commonDrops;
                e.rareDrops = e.baseForme.rareDrops;
            }
            if (e.species == null && e.baseForme != null)
            {
                e.childNumbers = e.baseForme.childNumbers;
                e.species = e.baseForme.species;
            }
            if (!Pokedex.getInstance().getEntries().contains(e.getPokedexNb()))
            {
                if (e.baseForme != null && Pokedex.getInstance().getEntries().contains(e.baseForme.getPokedexNb()))
                {
                    continue;
                }
                toRemove.add(e);
            }
        }

        allFormes.removeAll(toRemove);

    }

    public static void addDatabase(String file)
    {
        externalDatabases.add(file);
    }

    public static void addSpawnData(String file)
    {
        externalSpawnData.add(file);
    }

    public static void addStatDatabase(String file)
    {
        externalStatDatabases.add(file);
    }

    public static void addEVXPSpawnData(String file)
    {
        externalEVXPDatabases.add(file);
    }

    public static void addEntry(PokedexEntry entry)
    {
        data.put(entry.getPokedexNb(), entry);
    }

    public static PokedexEntry getEntry(int nb)
    {
        return data.get(nb);
    }

    public static PokedexEntry getEntry(IPokemob mob)
    {
        return data.get(mob.getPokedexNb());
    }

    public static SpawnData getSpawnData(int nb)
    {
        if (data.containsKey(nb)) return data.get(nb).getSpawnData();
        return null;
    }

    public static PokedexEntry getEntry(String name)
    {
        PokedexEntry ret = null;
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
        return ret;
    }

    public static boolean compare(String a, String b)
    {
        boolean ret = false;

        ret = a.toLowerCase().replaceAll("(\\W)", "").equals(b.toLowerCase().replaceAll("(\\W)", ""));

        return ret;
    }

    public static List<String> getLevelUpMoves(int nb, int level, int oldLevel)
    {
        return entryExists(nb) ? getEntry(nb).getMovesForLevel(level, oldLevel) : null;
    }

    public static List<String> getLearnableMoves(int nb)
    {
        return entryExists(nb) ? getEntry(nb).getMoves() : null;
    }

    public static boolean entryExists(int nb)
    {
        return getEntry(nb) != null;
    }

    public static boolean entryExists(String name)
    {
        return getEntry(name) != null;
    }

    public static boolean hasSpawnData(int nb)
    {
        return getEntry(nb) != null && getEntry(nb).getSpawnData() != null;
    }

    private static void loadSpawns()
    {
        loadSpawns(DBLOCATION + "spawndata.csv");

        if (PokecubeMod.debug) System.out.println("loaded Standard Spawns ");
        for (String s : externalSpawnData)
        {
            if (s != null) if (PokecubeMod.debug) System.out.println("loading from " + s);
            if (s != null) loadSpawns(s);
        }
    }

    public static void updateSizes()
    {
        loadStats(DBLOCATION + "baseStats.csv", true, false);
    }

    private static void loadStats(String file)
    {
        loadStats(file, false, false);
    }

    private static void initPokemobs(String file)
    {
        loadStats(file, false, true);
    }

    private static void loadStats(String file, boolean justSize, boolean justMaking)
    {
        ArrayList<ArrayList<String>> rows = getRows(file);
        for (ArrayList<String> s : rows)
        {
            int num = 0;

            try
            {
                num = Integer.parseInt(s.get(0));
            }
            catch (NumberFormatException e2)
            {
                continue;
            }

            PokedexEntry e = getEntry(num);
            if (justMaking)
            {
                if (e == null)
                {
                    e = new PokedexEntry(num, s.get(1).trim());
                    addEntry(e);
                }
                continue;
            }

            e = getEntry(num);

            if (e != null)
            {
                int catchRate = 3;
                String first = "";
                String second = "";
                String name = e.name;
                String forme = s.get(1).trim();

                if (s.size() == 2)
                {
                    PokedexEntry copy = Database.getEntry(forme);
                    if (copy == null)
                    {
                        copy = new PokedexEntry(e.pokedexNb, forme);
                    }
                    e.copyToForm(copy);
                    e.addForm(copy);
                    continue;
                }

                if (!justSize)
                {

                    if (forme.contains(" "))
                    {
                        catchRate = e.catchRate;
                    }
                    else if (s.size() > 9) try
                    {
                        catchRate = Integer.parseInt(s.get(8));
                    }
                    catch (NumberFormatException e1)
                    {
                        System.err.println("Missing Catch Rate for " + forme);
                    }
                    if (s.size() > 9) first = s.get(9);
                    if (s.size() > 10) second = s.get(10);

                    if (forme.equals("")) System.err.println("Missing name in Stats file for a form of " + name);
                    else name = forme.trim();

                    if (getType(first) == getType(second)) System.err.println("Error in Types for form " + name + " of "
                            + e.name + ", Listed as '" + first + "' and '" + second + "'");

                    e.addStats(name, Integer.parseInt(s.get(2)), Integer.parseInt(s.get(3)), Integer.parseInt(s.get(4)),
                            Integer.parseInt(s.get(5)), Integer.parseInt(s.get(6)), Integer.parseInt(s.get(7)),
                            catchRate, getType(first), getType(second));

                    String species = s.get(11);
                    e.species = species.toLowerCase().trim().split(":")[0].split(" ");
                    if (species.toLowerCase().trim().split(":").length > 1)
                    {
                        e.food = species.toLowerCase().trim().split(":")[1].split(" ");
                    }

                    String evolutionNbs = s.get(12);
                    if (evolutionNbs != null && !evolutionNbs.isEmpty())
                    {
                        String[] evols = s.get(12).split(" ");
                        String[] evolData = s.get(13).split(" ");
                        String[] evolFX = s.get(14).split(" ");
                        if (evols.length != evolData.length)
                        {
                            System.out.println("Error with evolution data for " + name);
                            new Exception().printStackTrace();
                        }
                        else
                        {
                            for (int i = 0; i < evols.length; i++)
                            {
                                String s1 = evols[i];
                                int evolNum = Integer.parseInt(s1);
                                String s2 = evolFX[i];
                                e.addEvolution(new EvolutionData(evolNum, evolData[i], s2));
                                if (num == evolNum)
                                {
                                    System.err.println("Problem in Evolution Data for " + name);
                                    // Integer.parseInt("time to crash");
                                }
                            }
                        }
                    }
                }

                if (forme.contains(" "))
                {
                    e = e.getForm(forme);
                    if (e == null) continue;
                }

                String[] args = s.get(15).split(":");
                e.width = Float.parseFloat(args[0]);
                if (args.length > 1)
                {
                    e.length = Float.parseFloat(args[1]);
                }
                else
                {
                    e.length = e.width;
                }
                e.height = Float.parseFloat(s.get(16));

                if (s.size() > 22 && !s.get(22).isEmpty())
                {
                    String[] matedata = s.get(22).split(";");
                    for (String s1 : matedata)
                    {
                        args = s1.split(":");
                        int fatherNb = Integer.parseInt(args[0]);
                        String[] args1 = args[1].split("`");
                        int[] childNbs = new int[args1.length];
                        for (int i = 0; i < args1.length; i++)
                        {
                            childNbs[i] = Integer.parseInt(args1[i]);
                        }
                        e.childNumbers.put(fatherNb, childNbs);
                    }
                }
                if (s.size() > 18)
                {
                    String[] strings = s.get(18).trim().split(":");
                    e.mobType = PokecubeMod.Type.getType(strings[0]);
                    if (strings.length > 1)
                    {
                        e.preferedHeight = Double.parseDouble(strings[1]);
                    }
                }
                if (s.size() > 19)
                {
                    String[] texturedetails = s.get(19).split(":");
                    String[][] details = new String[2][];
                    String males = texturedetails[0];
                    String females = texturedetails.length == 2 ? texturedetails[1] : null;
                    details[0] = males.split("`");
                    details[1] = females != null ? females.split("`") : null;
                    e.textureDetails = details;
                }
                if (s.size() > 20 && !s.get(20).isEmpty())
                {
                    try
                    {
                        e.mountedOffset = Double.parseDouble(s.get(20));
                    }
                    catch (NumberFormatException e1)
                    {
                        System.out.println("derp");
                    }
                }
                try
                {
                    e.mass = Double.parseDouble(s.get(21));
                }
                catch (Exception e1)
                {
                }
            }
        }

    }

    private static void loadEVXP(String s2)
    {
        ArrayList<ArrayList<String>> rows = getRows(s2);

        for (ArrayList<String> s : rows)
        {
            int num = 0;

            try
            {
                num = Integer.parseInt(s.get(0));
            }
            catch (NumberFormatException e2)
            {
                continue;
            }
            PokedexEntry e = getEntry(num);
            if (e != null)
            {
                int xpGain = 142;
                int evolutionMode = -1;
                int happiness = 70;
                int sexRatio = e.sexeRatio;
                String name = e.getName();
                try
                {
                    xpGain = Integer.parseInt(s.get(2));
                }
                catch (NumberFormatException e1)
                {
                    if (PokecubeMod.debug)
                        System.err.println("Missing XP gain for " + e + ", setting to a default of 142");
                    // e1.printStackTrace();
                }
                if (s.get(1).equals("")) System.err.println("Missing name in EV file for a form of " + name);
                else name = s.get(1).trim();

                try
                {
                    evolutionMode = Tools.getType(s.get(9));
                }
                catch (Exception e1)
                {

                }
                try
                {
                    sexRatio = Integer.parseInt(s.get(10));
                }
                catch (Exception e1)
                {

                }
                try
                {
                    happiness = Integer.parseInt(s.get(11));
                }
                catch (Exception e1)
                {

                }

                e.addItems(s.get(12), e.commonDrops);
                e.addItems(s.get(13), e.rareDrops);
                e.foodDrop = e.parseStack(s.get(14));
                e.addItems(s.get(15), e.heldItems);
                e.shouldFly = e.isType(flying);

                if (s.size() > 16 && !s.get(16).isEmpty())
                {
                    e.particleData = s.get(16).split(":");
                }
                if (s.size() > 17 && !s.get(17).isEmpty())
                {
                    e.hatedMaterial = s.get(17).split(":");
                }
                if (s.size() > 18)
                {
                    String[] args = s.get(18).split(":");
                    e.canSitShoulder = Boolean.parseBoolean(args[0]);
                    if (args.length > 1 && !args[1].isEmpty()) e.shouldFly = Boolean.parseBoolean(args[1]);
                    if (args.length > 2 && !args[2].isEmpty()) e.shouldDive = Boolean.parseBoolean(args[2]);
                    if (args.length > 3 && !args[3].isEmpty())
                    {
                        e.hasSpecialTextures[4] = Boolean.parseBoolean(args[3].split("#")[0]);
                        e.defaultSpecial = Integer.parseInt(args[3].split("#")[1]);
                    }
                }
                if (s.size() > 19)
                {
                    String[] replaces = s.get(19).split(":");
                    for (String s1 : replaces)
                    {
                        s1 = s1.toLowerCase().trim().replace(" ", "");
                        if (s.isEmpty()) continue;

                        if (mobReplacements.containsKey(s1))
                        {
                            mobReplacements.get(s1).add(e);
                        }
                        else
                        {
                            mobReplacements.put(s1, new ArrayList<PokedexEntry>());
                            mobReplacements.get(s1).add(e);
                        }
                    }
                }
                if (s.size() > 20)
                {
                    String[] foods = s.get(20).split(" ");
                    for (String s1 : foods)
                    {
                        if (s1.equalsIgnoreCase("light"))
                        {
                            e.activeTimes.add(PokedexEntry.day);
                            e.foods[0] = true;
                        }
                        else if (s1.equalsIgnoreCase("rock"))
                        {
                            e.foods[1] = true;
                        }
                        else if (s1.equalsIgnoreCase("electricity"))
                        {
                            e.foods[2] = true;
                        }
                        else if (s1.equalsIgnoreCase("grass"))
                        {
                            e.foods[3] = true;
                        }
                        else if (s1.equalsIgnoreCase("water"))
                        {
                            e.foods[6] = true;
                        }
                        else if (s1.equalsIgnoreCase("none"))
                        {
                            e.foods[4] = true;
                        }
                    }
                }

                if (e.isType(ghost)) e.foods[4] = true;

                if (s.size() > 21)
                {
                    String[] times = s.get(21).split(" ");
                    for (String s1 : times)
                    {
                        if (s1.equalsIgnoreCase("day"))
                        {
                            e.activeTimes.add(PokedexEntry.day);
                        }
                        else if (s1.equalsIgnoreCase("night"))
                        {
                            e.activeTimes.add(PokedexEntry.night);
                        }
                        else if (s1.equalsIgnoreCase("dusk"))
                        {
                            e.activeTimes.add(PokedexEntry.dusk);
                        }
                        else if (s1.equalsIgnoreCase("dawn"))
                        {
                            e.activeTimes.add(PokedexEntry.dawn);
                        }
                    }
                }
                if (s.size() > 22 && !s.get(22).trim().isEmpty())
                {
                    InteractionLogic.initForEntry(e, s.get(22));
                }
                else
                {
                    InteractionLogic.initForEntry(e);
                }

                if (evolutionMode == -1)
                {
                    System.err.println("Missing XP type for " + name + ", setting default as Medium Slow ");
                    evolutionMode = 2;
                }
                if (sexRatio == -1)
                {
                    System.err.println("Missing Sex Ratio for " + name + ", setting default as 50/50");
                    sexRatio = 127;
                }

                e.addEVXP(name, Integer.parseInt(s.get(3)), Integer.parseInt(s.get(4)), Integer.parseInt(s.get(5)),
                        Integer.parseInt(s.get(6)), Integer.parseInt(s.get(7)), Integer.parseInt(s.get(8)), xpGain,
                        evolutionMode, sexRatio);
                e.baseHappiness = happiness;
            }
        }
    }

    private static void loadSpawns(String file)
    {
        ArrayList<ArrayList<String>> rows = getRows(file);

        for (ArrayList<String> s : rows)
        {
            if (s == null || s.size() < 2) continue;

            String name = s.get(0);

            if (!entryExists(name))
            {
                if (PokecubeMod.debug)
                    System.err.println("'" + name + "' Does not exist in the Database, or is spelt incorrectly");
                continue;
            }
            PokedexEntry dbe = getEntry(name);

            /** Column 0: Name Column 1: cases
             * (day/night/fossil/starter/legend/water+/water) Column 2 any
             * biomes Column 3 all biomes Column 4 no biomes */
            String cases[] = s.get(1).trim().split(" ");
            String any[] = null;
            String all[] = null;
            String no[] = null;
            SpawnData entry = dbe.getSpawnData();
            if (entry == null)
            {
                entry = new SpawnData();
            }
            else
            {
                if (s.size() > 5)
                {
                    String s1 = s.get(5);
                    try
                    {
                        boolean b = Boolean.parseBoolean(s1);
                        if (b)
                        {
                            System.out.println("Replacing Spawns for " + dbe);
                            entry = new SpawnData();
                        }
                        else
                        {
                            System.out.println("Reloading Spawns for " + dbe);
                        }
                    }
                    catch (Exception e)
                    {
                        System.out.println("Reloading Spawns for " + dbe);
                    }
                }
                else
                {
                    System.out.println("Reloading Spawns for " + dbe);
                }
            }
            for (String s1 : cases)
            {
                if (s1.equalsIgnoreCase("day"))
                {
                    entry.types[SpawnData.DAY] = true;
                }
                if (s1.equalsIgnoreCase("night"))
                {
                    entry.types[SpawnData.NIGHT] = true;
                }
                if (s1.equalsIgnoreCase("fossil"))
                {
                    entry.types[SpawnData.FOSSIL] = true;
                }
                if (s1.equalsIgnoreCase("starter"))
                {
                    entry.types[SpawnData.STARTER] = true;
                    PokecubeMod.core.starters.add(dbe.pokedexNb);
                    Collections.sort(PokecubeMod.core.starters);
                }
                if (s1.equalsIgnoreCase("water"))
                {
                    entry.types[SpawnData.WATER] = true;
                }
                if (s1.equalsIgnoreCase("water+"))
                {
                    entry.types[SpawnData.WATERPLUS] = true;
                }
                if (s1.equalsIgnoreCase("legendary"))
                {
                    entry.types[SpawnData.LEGENDARY] = true;
                }
            }
            if (s.size() < 3) continue;

            any = s.get(2).split(";");
            if (s.size() > 3)
            {
                all = s.get(3).split(";");
            }
            if (s.size() > 4)
            {
                no = s.get(4).trim().split(" ");
            }

            if (all != null)
            {
                for (String al : all)
                {

                    String[] vals = al.trim().split(" ");
                    if (vals.length <= 1)
                    {
                        continue;
                    }

                    TypeEntry ent = new TypeEntry();
                    if (!processWeights(vals[vals.length - 1], ent))
                    {
                        System.err.println("Error with spawn weights for " + dbe + " " + Arrays.toString(vals));
                        continue;
                    }

                    for (int i = 0; i < vals.length - 1; i++)
                    {
                        if (vals[i] == null || vals[i].isEmpty())
                        {
                            continue;
                        }
                        Type t = null;
                        try
                        {
                            t = Type.valueOf(vals[i].trim().toUpperCase());
                        }
                        catch (Exception e)
                        {

                        }
                        if (t != null) ent.biomes.add(t);
                        else
                        {
                            BiomeType t1 = BiomeType.getBiome(vals[i]);
                            if (t1 != null)
                            {
                                ent.biome2.add(t1);
                            }
                            else
                            {
                                new Exception().printStackTrace();
                            }
                        }
                    }
                    entry.allTypes.add(ent);
                }
            }

            if (any != null)
            {
                for (String an : any)
                {

                    String[] vals = an.trim().split(" ");

                    if (vals.length <= 1) continue;

                    for (int i = 0; i < vals.length - 1; i++)
                    {
                        Type t = null;
                        if (vals[i] == null || vals[i].isEmpty())
                        {
                            continue;
                        }

                        TypeEntry ent = new TypeEntry();
                        if (!processWeights(vals[vals.length - 1], ent))
                        {
                            System.err.println("Error with spawn weights for " + dbe + " " + Arrays.toString(vals));
                            continue;
                        }
                        try
                        {
                            t = Type.valueOf(vals[i].trim().toUpperCase());
                        }
                        catch (Exception e)
                        {

                        }
                        if (t != null) ent.biomes.add(t);
                        else
                        {
                            String biome = vals[i];
                            try
                            {
                                Double.parseDouble(biome);
                                biome = null;
                            }
                            catch (Exception e)
                            {
                                biome = "none";
                            }
                            if (biome != null)
                            {
                                BiomeType t1 = BiomeType.getBiome(vals[i]);
                                ent.biome2.add(t1);
                            }
                            else
                            {
                                // System.out.println("Error with spawndata for
                                // "+name);
                            }
                        }
                        entry.anyTypes.add(ent);
                    }
                }
            }

            if (no != null)
            {
                for (String s1 : no)
                {
                    Type t = null;
                    try
                    {
                        t = Type.valueOf(s1.trim().toUpperCase());
                    }
                    catch (Exception e)
                    {

                    }
                    if (t != null) entry.noTypes.add(t);
                }
            }
            if (entry.isValid(BiomeType.CAVE.getType())) entry.types[SpawnData.CAVE] = true;
            if (entry.isValid(BiomeType.VILLAGE.getType())) entry.types[SpawnData.VILLAGE] = true;
            if (entry.isValid(BiomeType.INDUSTRIAL.getType())) entry.types[SpawnData.INDUSTRIAL] = true;

            dbe.setSpawnData(entry);
            if (!spawnables.contains(dbe)) spawnables.add(dbe);

        }
    }

    private static boolean processWeights(String val, TypeEntry entry)
    {
        float weight = 0;
        int max = 4;
        int min = 2;
        String[] vals = val.split(":");
        // System.out.println(val+" "+Arrays.toString(vals));
        try
        {
            weight = Float.parseFloat(vals[0]);
        }
        catch (Exception e)
        {
            return false;
        }
        try
        {
            max = Integer.parseInt(vals[1]);
        }
        catch (Exception e)
        {

        }
        try
        {
            min = Integer.parseInt(vals[2]);
        }
        catch (Exception e)
        {

        }
        if (entry != null)
        {
            entry.weight = weight;
            entry.groupMax = max;
            entry.groupMin = min;
        }

        return entry != null;
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

            byte amount = 0;
            int[] amounts = new int[] { 0, 0, 0, 0, 0, 0, 0, 0 };
            String[] statEffects = s.get(10).trim().toLowerCase().split(";");
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
            int[] old = modifiers.clone();
            modifiers[1] = (byte) Math.max(-6, Math.min(6, modifiers[1] + amounts[1] * (effect & 1)));
            modifiers[2] = (byte) Math.max(-6, Math.min(6, modifiers[2] + amounts[2] * ((effect & 2) / 2)));
            modifiers[3] = (byte) Math.max(-6, Math.min(6, modifiers[3] + amounts[3] * ((effect & 4) / 4)));
            modifiers[4] = (byte) Math.max(-6, Math.min(6, modifiers[4] + amounts[4] * ((effect & 8) / 8)));
            modifiers[5] = (byte) Math.max(-6, Math.min(6, modifiers[5] + amounts[5] * ((effect & 16) / 16)));
            modifiers[6] = (byte) Math.max(-6, Math.min(6, modifiers[6] + amounts[6] * ((effect & 32) / 32)));
            modifiers[7] = (byte) Math.max(-6, Math.min(6, modifiers[7] + amounts[7] * ((effect & 64) / 64)));
            for (int i = 0; i < old.length; i++)
            {
                if (old[i] != modifiers[i])
                {
                    break;
                }
            }
            if ((move.attackCategory & CATEGORY_SELF) > 0 || (move.attackCategory & CATEGORY_SELF_EFFECT) > 0)
            {
                move.attackerStatModification = modifiers.clone();
                move.attackerStatModProb = chance;
            }
            else
            {
                move.attackedStatModification = modifiers.clone();
                move.attackedStatModProb = chance;
            }

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

    private static ArrayList<ArrayList<String>> getRows(String file)
    {
        InputStream res = (net.minecraft.util.StringTranslate.class).getResourceAsStream(file);

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

    private static void load(String file)
    {
        ArrayList<ArrayList<String>> rows = getRows(file);

        for (int i = 0; i < rows.size() - 1; i += 2)
        {
            ArrayList<String> row = rows.get(i);
            ArrayList<String> row2 = rows.get(i + 1);
            if (row.size() < 2) continue;
            if (row2.size() < 2) continue;

            String[] levels = row.get(1).split(":");
            String[] moves = row2.get(1).split(":");
            int nb = Integer.parseInt(row2.get(0).trim());
            PokedexEntry dbe = getEntry(nb);

            if (levels.length > moves.length)
            {
                System.err.println("Error in moves for " + dbe);
                continue;
            }

            // if (dbe == null)
            {
                Map<Integer, ArrayList<String>> lvlUpMoves = new HashMap<Integer, ArrayList<String>>();
                ArrayList<String> allMoves = new ArrayList<String>();
                int n = 0;
                for (n = 0; n < levels.length; n++)
                {
                    int level = 0;
                    try
                    {
                        level = Integer.parseInt(levels[n]);
                    }
                    catch (NumberFormatException e1)
                    {
                    }
                    ArrayList<String> movesForLevel = lvlUpMoves.get(level);
                    if (movesForLevel == null)
                    {
                        movesForLevel = new ArrayList<String>();
                        lvlUpMoves.put(level, movesForLevel);
                    }
                    movesForLevel.add(convertMoveName(moves[n].trim()));
                    allMoves.add(convertMoveName(moves[n].trim()));
                }
                for (n = levels.length; n < moves.length; n++)
                {
                    if (!moves[n].trim().isEmpty() && !allMoves.contains(convertMoveName(moves[n].trim())))
                        allMoves.add(convertMoveName(moves[n].trim()));
                }
                dbe.addMoves(allMoves, lvlUpMoves);
            }
        }
    }

    private static void loadAbilities(String file)
    {
        ArrayList<ArrayList<String>> rows = getRows(file);

        for (ArrayList<String> s : rows)
        {
            int num = Integer.parseInt(s.get(0).trim());
            PokedexEntry e = getEntry(num);
            if (e == null)
            {
                System.err.println("missing entry for " + num);
                continue;
            }

            String name = s.get(1);
            // TODO abilities for megas here
            if (name.contains("Mega"))
            {

            }
            else
            {
                String a1 = s.get(2).trim();

                if (!a1.isEmpty())
                {
                    e.abilities[0] = a1;
                }

                if (s.size() > 3)
                {
                    String a2 = s.get(3).trim();
                    if (!a2.isEmpty())
                    {
                        e.abilities[1] = a2;
                    }
                }
                if (s.size() > 4)
                {
                    String a3 = s.get(4).trim();
                    if (!a3.isEmpty())
                    {
                        e.abilities[2] = a3;
                    }
                }
            }

        }
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

    private static PrintWriter out;
    private static FileWriter  fwriter;

    private static void writeDefaultConfig()
    {
        try
        {
            File temp = new File(CONFIGLOC);
            if (!temp.exists())
            {
                temp.mkdirs();
            }
            copyDatabaseFile("baseStats.csv");
            copyDatabaseFile("evsXp.csv");
            copyDatabaseFile("abilities.csv");
            copyDatabaseFile("moves.csv");
            copyDatabaseFile("moveLists.csv");
            copyDatabaseFile("spawndata.csv");

            DBLOCATION = CONFIGLOC;

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
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
        try
        {
            fwriter = new FileWriter(CONFIGLOC + name);
            out = new PrintWriter(fwriter);
            for (int i = 0; i < rows.size(); i++)
                out.println(rows.get(i));
            out.close();
            fwriter.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private static ArrayList<String> getFile(String file)
    {
        InputStream res = (net.minecraft.util.StringTranslate.class).getResourceAsStream(file);

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
}