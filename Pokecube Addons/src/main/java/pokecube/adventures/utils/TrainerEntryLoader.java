package pokecube.adventures.utils;

import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;

import com.google.common.collect.Lists;

import net.minecraft.item.ItemStack;
import pokecube.adventures.entity.trainers.TypeTrainer;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.PokedexEntryLoader.SpawnRule;
import pokecube.core.database.SpawnBiomeMatcher;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.utils.PokeType;
import pokecube.core.utils.Tools;

public class TrainerEntryLoader
{
    static XMLDatabase database;

    @XmlRootElement(name = "TYPETRAINERSET")
    public static class XMLDatabase
    {
        @XmlElement(name = "TYPETRAINER")
        private List<TrainerEntry> trainers = Lists.newArrayList();
    }

    @XmlRootElement(name = "TYPETRAINER")
    public static class TrainerEntry
    {
        @XmlAttribute
        String          tradeTemplate = "default";
        @XmlElement(name = "TYPE")
        String          type;
        @XmlElement(name = "POKEMON")
        String          pokemon;
        @XmlElement(name = "Spawn")
        List<SpawnRule> spawns        = Lists.newArrayList();
        @XmlElement(name = "GENDER")
        String          gender;
        @XmlElement(name = "BAG")
        Bag             bag;
        @XmlElement(name = "BELT")
        boolean         belt          = true;
        @XmlElement(name = "HELD")
        Held            held;
        @XmlElement(name = "REWARD")
        Held            reward;

        @Override
        public String toString()
        {
            return type + " " + spawns;
        }
    }

    @XmlRootElement(name = "BAG")
    public static class Bag
    {
        @XmlAnyAttribute
        Map<QName, String> values;
        @XmlElement(name = "tag")
        String             tag;
    }

    @XmlRootElement(name = "HELD")
    public static class Held
    {
        @XmlAnyAttribute
        Map<QName, String> values;
        @XmlElement(name = "tag")
        String             tag;
    }

    private static XMLDatabase loadDatabase(File file) throws Exception
    {
        JAXBContext jaxbContext = JAXBContext.newInstance(XMLDatabase.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        FileReader reader = new FileReader(file);
        XMLDatabase database = (XMLDatabase) unmarshaller.unmarshal(reader);
        reader.close();
        return database;
    }

    public static void makeEntries(File file)
    {
        if (database == null)
        {
            try
            {
                database = loadDatabase(file);
            }
            catch (Exception e)
            {
                PokecubeMod.log(Level.WARNING, file + "", e);
                throw new RuntimeException();
            }
        }
        else
        {
            try
            {
                XMLDatabase newDatabase = loadDatabase(file);
                for (TrainerEntry entry : newDatabase.trainers)
                {
                    for (TrainerEntry old : database.trainers)
                    {
                        if (old.type.equals(entry.type))
                        {
                            database.trainers.remove(old);
                            break;
                        }
                    }
                    database.trainers.add(entry);
                }
            }
            catch (Exception e)
            {
                PokecubeMod.log(Level.WARNING, file + "", e);
                throw new RuntimeException();
            }
        }
        for (TrainerEntry entry : database.trainers)
        {
            String name = entry.type;
            TypeTrainer type = TypeTrainer.typeMap.get(name);
            if (type == null) type = new TypeTrainer(name);
            type.matchers.clear();
            type.pokemon.clear();
            byte male = 1;
            byte female = 2;
            type.tradeTemplate = entry.tradeTemplate;
            type.hasBag = entry.bag != null;
            if (type.hasBag)
            {
                if (entry.bag.tag != null) entry.bag.values.put(new QName("tag"), entry.bag.tag);
                ItemStack bag = Tools.getStack(entry.bag.values);
                type.bag = bag;
            }
            if (entry.spawns != null) for (SpawnRule rule : entry.spawns)
            {
                Float weight;
                try
                {
                    weight = Float.parseFloat(rule.values.get(new QName("rate")));
                }
                catch (Exception e)
                {
                    PokecubeMod.log(Level.WARNING,
                            "Error with weight for " + type.name + " " + rule.values + " " + entry.spawns, e);
                    continue;
                }
                SpawnBiomeMatcher matcher = new SpawnBiomeMatcher(rule);
                type.matchers.put(matcher, weight);
            }

            type.hasBelt = entry.belt;
            if (entry.gender != null) type.genders = (byte) (entry.gender.equalsIgnoreCase("male") ? male
                    : entry.gender.equalsIgnoreCase("female") ? female : male + female);
            String[] pokeList = entry.pokemon == null ? new String[] {} : entry.pokemon.split(",");
            if (entry.held != null)
            {
                if (entry.held.tag != null) entry.held.values.put(new QName("tag"), entry.held.tag);
                ItemStack held = Tools.getStack(entry.held.values);
                type.held = held;
            }
            if (pokeList.length == 0) continue;
            if (!pokeList[0].startsWith("-"))
            {
                for (String s : pokeList)
                {
                    PokedexEntry e = Database.getEntry(s);
                    if (e != null && !type.pokemon.contains(e))
                    {
                        type.pokemon.add(e);
                    }
                    else if (e == null)
                    {
                        // System.err.println("Error in reading of "+s);
                    }
                }
            }
            else
            {
                String[] types = pokeList[0].replace("-", "").split(":");
                if (types[0].equalsIgnoreCase("all"))
                {
                    for (PokedexEntry s : Database.spawnables)
                    {
                        if (!s.legendary && s.getPokedexNb() != 151)
                        {
                            type.pokemon.add(s);
                        }
                    }
                }
                else
                {
                    for (int i = 0; i < types.length; i++)
                    {
                        PokeType pokeType = PokeType.getType(types[i]);
                        if (pokeType != PokeType.unknown)
                        {
                            for (PokedexEntry s : Database.spawnables)
                            {
                                if (s.isType(pokeType) && !s.legendary && s.getPokedexNb() != 151)
                                {
                                    type.pokemon.add(s);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
