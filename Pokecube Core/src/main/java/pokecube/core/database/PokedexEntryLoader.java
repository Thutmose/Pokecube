package pokecube.core.database;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import net.minecraftforge.common.BiomeDictionary.Type;
import pokecube.core.database.PokedexEntry.EvolutionData;
import pokecube.core.database.PokedexEntry.InteractionLogic;
import pokecube.core.database.PokedexEntry.SpawnData;
import pokecube.core.database.PokedexEntry.SpawnData.TypeEntry;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.utils.PokeType;
import pokecube.core.utils.Tools;
import thut.api.terrain.BiomeType;

public class PokedexEntryLoader
{
    public static void makeEntries(File file, boolean create)
            throws ParserConfigurationException, SAXException, IOException
    {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

        Document doc = docBuilder.parse(file);
        NodeList entries = doc.getElementsByTagName("Pokemon");

        for (int i = 0; i < entries.getLength(); i++)
        {
            Element pokemonNode = (Element) entries.item(i);
            int number = Integer.parseInt(pokemonNode.getAttribute("number"));
            String name = pokemonNode.getAttribute("name");
            PokedexEntry entry = create ? new PokedexEntry(number, name) : Database.getEntry(name);
            if (entry == null)
            {
                System.err.println(new NullPointerException("No Entry for " + name));
                continue;
            }

            NodeList list = pokemonNode.getElementsByTagName("STATS");
            if (list.getLength() == 1)
            {
                Element stats = (Element) list.item(0);
                try
                {
                    if (create) initStats(entry, stats);
                    else
                    {
                        postIniStats(entry, stats);
                        parseSpawns(entry, stats);
                        parseEvols(entry, stats);
                    }
                }
                catch (Exception e)
                {
                    System.out.println(entry + ": " + e);
                }
            }
            else if (list.getLength() != 0)
            {
                System.err.println(new IllegalArgumentException("Wrong number of STATS nodes for " + entry));
            }

            list = pokemonNode.getElementsByTagName("MOVES");
            if (list.getLength() == 1)
            {
                Element moves = (Element) list.item(0);
                initMoves(entry, moves);
            }
            else if (list.getLength() != 0)
            {
                System.err.println(new IllegalArgumentException("Wrong number of MOVES nodes for " + entry));
            }
            try
            {
                checkBaseForme(entry);
            }
            catch (Exception e)
            {
                System.out.println(entry + ": " + e);
            }
        }
    }

    private static void postIniStats(PokedexEntry entry, Element statsNode)
    {
        NodeList list;
        Element node;

        // Items
        list = statsNode.getElementsByTagName("COMMONDROP");
        if (list.getLength() == 1)
        {
            node = (Element) list.item(0);
            entry.addItems(node.getFirstChild().getNodeValue(), entry.commonDrops);
        }
        list = statsNode.getElementsByTagName("FOODDROP");
        if (list.getLength() == 1)
        {
            node = (Element) list.item(0);
            entry.foodDrop = entry.parseStack(node.getFirstChild().getNodeValue());
        }
        list = statsNode.getElementsByTagName("RAREDROP");
        if (list.getLength() == 1)
        {
            node = (Element) list.item(0);
            entry.addItems(node.getFirstChild().getNodeValue(), entry.rareDrops);
        }
        list = statsNode.getElementsByTagName("HELDITEM");
        if (list.getLength() == 1)
        {
            node = (Element) list.item(0);
            entry.addItems(node.getFirstChild().getNodeValue(), entry.heldItems);
        }
        entry.shouldFly = entry.isType(PokeType.flying);

        // Logics
        list = statsNode.getElementsByTagName("LOGIC");
        if (list.getLength() == 1)
        {
            node = (Element) list.item(0);
            if (node.hasAttribute("shoulder"))
            {
                entry.canSitShoulder = Boolean.parseBoolean(node.getAttribute("shoulder"));
            }
            if (node.hasAttribute("fly"))
            {
                entry.canSitShoulder = Boolean.parseBoolean(node.getAttribute("fly"));
            }
            if (node.hasAttribute("dive"))
            {
                entry.canSitShoulder = Boolean.parseBoolean(node.getAttribute("dive"));
            }
            if (node.hasAttribute("stationary"))
            {
                entry.canSitShoulder = Boolean.parseBoolean(node.getAttribute("stationary"));
            }
            if (node.hasAttribute("dye"))
            {
                entry.hasSpecialTextures[4] = Boolean.parseBoolean(node.getAttribute("dye").split("#")[0]);
                entry.defaultSpecial = Integer.parseInt(node.getAttribute("dye").split("#")[1]);
            }
        }
        list = statsNode.getElementsByTagName("EXPERIENCEMODE");
        if (list.getLength() == 1)
        {
            node = (Element) list.item(0);
            entry.evolutionMode = Tools.getType(node.getFirstChild().getNodeValue());
        }

        list = statsNode.getElementsByTagName("SHADOWREPLACEMENTS");
        if (list.getLength() == 1)
        {
            node = (Element) list.item(0);
            String[] replaces = node.getFirstChild().getNodeValue().split(":");
            for (String s1 : replaces)
            {
                s1 = s1.toLowerCase().trim().replace(" ", "");
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
        list = statsNode.getElementsByTagName("FOODMATERIAL");
        if (list.getLength() == 1)
        {
            node = (Element) list.item(0);
            String[] foods = node.getFirstChild().getNodeValue().split(" ");
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

        list = statsNode.getElementsByTagName("ACTIVETIMES");
        if (list.getLength() == 1)
        {
            node = (Element) list.item(0);
            String[] times = node.getFirstChild().getNodeValue().split(" ");
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
        list = statsNode.getElementsByTagName("INTERACTIONLOGIC");
        if (list.getLength() == 1)
        {
            node = (Element) list.item(0);
            InteractionLogic.initForEntry(entry, node.getFirstChild().getNodeValue());
        }
        else
        {
            InteractionLogic.initForEntry(entry);
        }

        list = statsNode.getElementsByTagName("PARTICLEEFFECTS");
        if (list.getLength() == 1)
        {
            node = (Element) list.item(0);
            entry.particleData = node.getFirstChild().getNodeValue().split(":");
        }

        list = statsNode.getElementsByTagName("HATEDMATERIALRULES");
        if (list.getLength() == 1)
        {
            node = (Element) list.item(0);
            entry.hatedMaterial = node.getFirstChild().getNodeValue().split(":");
        }
    }

    private static void initStats(PokedexEntry entry, Element statsNode)
    {
        int[] stats = new int[6];
        byte[] evs = new byte[6];
        Element node;
        NodeList list = statsNode.getElementsByTagName("BASESTATS");
        boolean stat = false, ev = false;
        if (list.getLength() == 1)
        {
            node = (Element) list.item(0);
            stats[0] = Integer.parseInt(node.getAttribute("hp"));
            stats[1] = Integer.parseInt(node.getAttribute("atk"));
            stats[2] = Integer.parseInt(node.getAttribute("def"));
            stats[3] = Integer.parseInt(node.getAttribute("spatk"));
            stats[4] = Integer.parseInt(node.getAttribute("spdef"));
            stats[5] = Integer.parseInt(node.getAttribute("spd"));
            stat = true;
        }
        list = statsNode.getElementsByTagName("EVYIELD");
        if (list.getLength() == 1)
        {
            node = (Element) list.item(0);
            if (node.hasAttribute("hp")) evs[0] = (byte) Integer.parseInt(node.getAttribute("hp"));
            if (node.hasAttribute("atk")) evs[1] = (byte) Integer.parseInt(node.getAttribute("atk"));
            if (node.hasAttribute("def")) evs[2] = (byte) Integer.parseInt(node.getAttribute("def"));
            if (node.hasAttribute("spatk")) evs[3] = (byte) Integer.parseInt(node.getAttribute("spatk"));
            if (node.hasAttribute("spdef")) evs[4] = (byte) Integer.parseInt(node.getAttribute("spdef"));
            if (node.hasAttribute("spd")) evs[5] = (byte) Integer.parseInt(node.getAttribute("spd"));
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
        list = statsNode.getElementsByTagName("TYPE");
        if (list.getLength() == 1)
        {
            node = (Element) list.item(0);
            if (node.hasAttribute("type1"))
            {
                entry.type1 = PokeType.getType(node.getAttribute("type1"));
            }
            if (node.hasAttribute("type2"))
            {
                entry.type2 = PokeType.getType(node.getAttribute("type2"));
            }
        }
        list = statsNode.getElementsByTagName("SIZES");
        if (list.getLength() == 1)
        {
            node = (Element) list.item(0);
            if (node.hasAttribute("height"))
            {
                entry.height = Float.parseFloat(node.getAttribute("height"));
            }
            if (node.hasAttribute("length"))
            {
                entry.length = Float.parseFloat(node.getAttribute("length"));
                entry.width = Float.parseFloat(node.getAttribute("width"));
            }
            else if (node.hasAttribute("width"))
            {
                entry.length = entry.width = Float.parseFloat(node.getAttribute("width"));
            }
            else
            {
                entry.length = entry.width = entry.height;
            }
        }
        list = statsNode.getElementsByTagName("ABILITY");
        if (list.getLength() == 1)
        {
            node = (Element) list.item(0);
            if (node.hasAttribute("hidden"))
            {
                entry.abilities[2] = node.getAttribute("hidden");
            }
            if (node.hasAttribute("normal"))
            {
                String[] vars = node.getAttribute("normal").split(",");
                for (int i = 0; i < Math.min(entry.abilities.length - 1, vars.length); i++)
                {
                    entry.abilities[i] = vars[i].trim();
                }
            }
        }
        list = statsNode.getElementsByTagName("CAPTURERATE");
        if (list.getLength() == 1)
        {
            node = (Element) list.item(0);
            entry.catchRate = Integer.parseInt(node.getFirstChild().getNodeValue());
        }
        list = statsNode.getElementsByTagName("EXPYIELD");
        if (list.getLength() == 1)
        {
            node = (Element) list.item(0);
            entry.baseXP = Integer.parseInt(node.getFirstChild().getNodeValue());
        }
        list = statsNode.getElementsByTagName("GENDERRATIO");
        if (list.getLength() == 1)
        {
            node = (Element) list.item(0);
            entry.sexeRatio = Integer.parseInt(node.getFirstChild().getNodeValue());
        }
        list = statsNode.getElementsByTagName("RIDDENOFFSET");
        if (list.getLength() == 1)
        {
            node = (Element) list.item(0);
            entry.mountedOffset = Double.parseDouble(node.getFirstChild().getNodeValue());
        }
        list = statsNode.getElementsByTagName("SPECIALEGGSPECIESRULES");
        if (list.getLength() == 1)
        {
            node = (Element) list.item(0);
            String[] matedata = node.getFirstChild().getNodeValue().split(";");
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
        list = statsNode.getElementsByTagName("MOVEMENTTYPE");
        if (list.getLength() == 1)
        {
            node = (Element) list.item(0);
            String[] strings = node.getFirstChild().getNodeValue().trim().split(":");
            entry.mobType = PokecubeMod.Type.getType(strings[0]);
            if (strings.length > 1)
            {
                entry.preferedHeight = Double.parseDouble(strings[1]);
            }
        }
        list = statsNode.getElementsByTagName("BASEFRIENDSHIP");
        if (list.getLength() == 1)
        {
            node = (Element) list.item(0);
            entry.baseHappiness = Integer.parseInt(node.getFirstChild().getNodeValue());
        }
        list = statsNode.getElementsByTagName("MASSKG");
        if (list.getLength() == 1)
        {
            node = (Element) list.item(0);
            entry.mass = Float.parseFloat(node.getFirstChild().getNodeValue());
        }
        list = statsNode.getElementsByTagName("SPECIES");
        if (list.getLength() == 1)
        {
            node = (Element) list.item(0);
            entry.species = node.getFirstChild().getNodeValue().trim().toLowerCase().split(" ");
        }
        list = statsNode.getElementsByTagName("PREY");
        if (list.getLength() == 1)
        {
            node = (Element) list.item(0);
            entry.food = node.getFirstChild().getNodeValue().trim().toLowerCase().split(" ");
        }
    }

    private static void parseSpawns(PokedexEntry entry, Element node)
    {
        NodeList listAll = node.getElementsByTagName("BIOMESALLNEEDED");
        NodeList listAny = node.getElementsByTagName("BIOMESANYACCEPTABLE");
        NodeList listExclude = node.getElementsByTagName("EXCLUDEDBIOMES");
        NodeList listCases = node.getElementsByTagName("SPECIALCASES");
        if (listCases.getLength() == 0) return;

        if (listAll.getLength() == 1 || listAny.getLength() == 1)
        {
            String anyString = "";
            String allString = "";
            String excludeString = "";
            String casesString = ((Element) listCases.item(0)).getFirstChild().getNodeValue();
            if (listAll.getLength() == 1)
            {
                allString = ((Element) listAll.item(0)).getFirstChild().getNodeValue();
            }
            if (listAny.getLength() == 1)
            {
                anyString = ((Element) listAny.item(0)).getFirstChild().getNodeValue();
            }
            if (listExclude.getLength() == 1)
            {
                excludeString = ((Element) listExclude.item(0)).getFirstChild().getNodeValue();
            }
            /** Column 0: Name Column 1: cases
             * (day/night/fossil/starter/legend/water+/water) Column 2 any
             * biomes Column 3 all biomes Column 4 no biomes */
            String cases[] = casesString.split(" ");
            String any[] = null;
            String all[] = null;
            String no[] = null;
            SpawnData spawnData = entry.getSpawnData();
            if (spawnData == null)
            {
                spawnData = new SpawnData();
            }
            for (String s1 : cases)
            {
                if (s1.equalsIgnoreCase("day"))
                {
                    spawnData.types[SpawnData.DAY] = true;
                }
                if (s1.equalsIgnoreCase("night"))
                {
                    spawnData.types[SpawnData.NIGHT] = true;
                }
                if (s1.equalsIgnoreCase("fossil"))
                {
                    spawnData.types[SpawnData.FOSSIL] = true;
                }
                if (s1.equalsIgnoreCase("starter"))
                {
                    spawnData.types[SpawnData.STARTER] = true;
                    PokecubeMod.core.starters.add(entry.pokedexNb);
                    Collections.sort(PokecubeMod.core.starters);
                }
                if (s1.equalsIgnoreCase("water"))
                {
                    spawnData.types[SpawnData.WATER] = true;
                }
                if (s1.equalsIgnoreCase("water+"))
                {
                    spawnData.types[SpawnData.WATERPLUS] = true;
                }
                if (s1.equalsIgnoreCase("legendary"))
                {
                    spawnData.types[SpawnData.LEGENDARY] = true;
                }
            }
            any = anyString.split(";");
            all = allString.split(";");
            no = excludeString.split(" ");
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
                        System.err.println("Error with spawn weights for " + entry + " " + Arrays.toString(vals));
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
                    spawnData.allTypes.add(ent);
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
                            System.err.println("Error with spawn weights for " + entry + " " + Arrays.toString(vals));
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
                        }
                        spawnData.anyTypes.add(ent);
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
                    if (t != null) spawnData.noTypes.add(t);
                }
            }
            if (spawnData.isValid(BiomeType.CAVE.getType())) spawnData.types[SpawnData.CAVE] = true;
            if (spawnData.isValid(BiomeType.VILLAGE.getType())) spawnData.types[SpawnData.VILLAGE] = true;
            if (spawnData.isValid(BiomeType.INDUSTRIAL.getType())) spawnData.types[SpawnData.INDUSTRIAL] = true;

            entry.setSpawnData(spawnData);
            if (!Database.spawnables.contains(entry)) Database.spawnables.add(entry);
        }
    }

    private static void parseEvols(PokedexEntry entry, Element node)
    {
        NodeList listNBs = node.getElementsByTagName("EVOLVESTO");
        NodeList listData = node.getElementsByTagName("EVOLUTIONMODE");
        NodeList listFX = node.getElementsByTagName("EVOLUTIONANIMATION");

        if (listNBs.getLength() != 1 || listData.getLength() != 1) return;

        String numberString = ((Element) listNBs.item(0)).getFirstChild().getNodeValue();
        String dataString = ((Element) listData.item(0)).getFirstChild().getNodeValue();
        String fxString = "";
        if (listFX.getLength() == 1)
        {
            fxString = ((Element) listFX.item(0)).getFirstChild().getNodeValue();
        }

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
                for (int i = 0; i < evols.length; i++)
                {
                    String s1 = evols[i];
                    int evolNum = Integer.parseInt(s1);
                    String s2 = evolFX[i];
                    entry.addEvolution(new EvolutionData(evolNum, evolData[i], s2));
                    if (entry.getPokedexNb() == evolNum)
                    {
                        System.err.println(
                                "Problem in Evolution Data for " + entry + " it is trying to evolve into itself");
                    }
                }
            }
        }
    }

    private static void initMoves(PokedexEntry entry, Element node)
    {
        Map<Integer, ArrayList<String>> lvlUpMoves = new HashMap<Integer, ArrayList<String>>();
        ArrayList<String> allMoves = new ArrayList<String>();

        NodeList listMisc = node.getElementsByTagName("MISC");
        NodeList listLvlUp = node.getElementsByTagName("LVLUP");

        if (listMisc.getLength() == 1 || listLvlUp.getLength() == 1)
        {
            if (listMisc.getLength() == 1)
            {
                Element miscNode = (Element) listMisc.item(0);
                String[] misc = miscNode.getAttribute("moves").split(",");
                for (String s : misc)
                {
                    allMoves.add(Database.convertMoveName(s));
                }
            }
            if (listLvlUp.getLength() == 1)
            {
                Element lvlNode = (Element) listLvlUp.item(0);
                for (int i = 0; i < lvlNode.getAttributes().getLength(); i++)
                {
                    String key = lvlNode.getAttributes().item(i).getNodeName().replace("lvl_", "");
                    String[] values = lvlNode.getAttributes().item(i).getNodeValue().split(",");
                    ArrayList<String> moves;
                    lvlUpMoves.put(Integer.parseInt(key), moves = new ArrayList<String>());
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
    }

    private static void checkBaseForme(PokedexEntry entry)
    {
        if (baseFormes.containsKey(entry.getPokedexNb()))
        {
            baseFormes.get(entry.getPokedexNb()).copyToForm(entry);
        }
        else
        {
            baseFormes.put(entry.getPokedexNb(), entry);
            Database.addEntry(entry);
        }
        Database.allFormes.add(entry);
    }

    private static boolean processWeights(String val, TypeEntry entry)
    {
        float weight = 0;
        int max = 4;
        int min = 2;
        String[] vals = val.split(":");
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

    private static HashMap<Integer, PokedexEntry> baseFormes = new HashMap<>();
}
