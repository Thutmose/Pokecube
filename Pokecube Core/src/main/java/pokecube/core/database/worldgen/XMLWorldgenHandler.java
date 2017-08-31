package pokecube.core.database.worldgen;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import net.minecraft.util.math.BlockPos;
import pokecube.core.database.PokedexEntryLoader;
import pokecube.core.database.PokedexEntryLoader.SpawnRule;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.database.SpawnBiomeMatcher;
import pokecube.core.world.gen.WorldGenMultiTemplate;
import pokecube.core.world.gen.WorldGenTemplates;
import pokecube.core.world.gen.WorldGenTemplates.TemplateGen;
import pokecube.core.world.gen.template.PokecubeTemplates;

public class XMLWorldgenHandler
{
    public static File DEFAULT;

    @XmlRootElement(name = "Structures")
    public static class XMLStructures
    {
        @XmlElement(name = "Structure")
        public List<XMLStructure>      structures      = Lists.newArrayList();
        @XmlElement(name = "MultiStructure")
        public List<XMLMultiStructure> multiStructures = Lists.newArrayList();
    }

    @XmlRootElement(name = "MultiStructure")
    public static class XMLMultiStructure
    {
        @XmlAttribute
        public String             name;
        @XmlAttribute
        float                     chance;
        @XmlAttribute
        boolean                   syncGround = false;
        @XmlElement
        public SpawnRule          spawn;
        @XmlElement(name = "Structure")
        public List<XMLStructure> structures = Lists.newArrayList();
    }

    @XmlRootElement(name = "Structure")
    public static class XMLStructure
    {
        @XmlAttribute
        public String    name;
        @XmlAttribute
        float            chance;
        @XmlAttribute
        int              offset;
        @XmlAttribute
        public String    biomeType;
        @XmlElement
        public SpawnRule spawn;
        @XmlAttribute
        public String    position;
    }

    public static XMLStructures defaults = new XMLStructures();

    static void init()
    {
        defaults.structures.clear();
        XMLStructure ruin_1 = new XMLStructure();
        ruin_1.name = "ruin_1";
        ruin_1.chance = 0.002f;
        ruin_1.offset = -3;
        ruin_1.biomeType = "ruin";
        SpawnRule rule = new SpawnRule();
        rule.values.put(SpawnBiomeMatcher.TYPES, "plains");
        ruin_1.spawn = rule;
        defaults.structures.add(ruin_1);
    }

    public static void loadStructures(String configFile)
    {
        boolean json = configFile.endsWith("json");
        File file = new File(PokecubeTemplates.TEMPLATES, configFile);
        try
        {
            FileInputStream stream = new FileInputStream(file);
            try
            {
                loadStructures(stream, json);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            stream.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    public static void loadStructures(InputStream stream, boolean json) throws Exception
    {
        XMLStructures database;
        InputStreamReader reader = new InputStreamReader(stream);
        if (json)
        {
            database = PokedexEntryLoader.gson.fromJson(reader, XMLStructures.class);
        }
        else
        {
            JAXBContext jaxbContext = JAXBContext.newInstance(XMLStructures.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            database = (XMLStructures) unmarshaller.unmarshal(reader);
        }
        defaults.structures.addAll(database.structures);
        defaults.multiStructures.addAll(database.multiStructures);
    }

    public static void processStructures()
    {
        for (XMLStructure struct : defaults.structures)
        {
            try
            {
                WorldGenTemplates.TemplateGen template = new TemplateGen(struct.name,
                        new SpawnBiomeMatcher(struct.spawn), struct.chance, struct.offset);
                WorldGenTemplates.templates.add(template);
                WorldGenTemplates.namedTemplates.put(struct.name, new TemplateGen(struct.name,
                        new SpawnBiomeMatcher(struct.spawn), struct.chance, struct.offset));
            }
            catch (Exception e)
            {
                System.out.println(struct.name + " " + struct.spawn + " " + struct.chance + " " + struct.offset);
                e.printStackTrace();
            }
        }
        for (XMLMultiStructure struct : defaults.multiStructures)
        {
            WorldGenMultiTemplate gen = new WorldGenMultiTemplate();
            gen.syncGround = struct.syncGround;
            for (XMLStructure struct2 : struct.structures)
            {
                try
                {
                    WorldGenTemplates.TemplateGen subGen = new TemplateGen(struct2.name,
                            new SpawnBiomeMatcher(struct2.spawn), struct2.chance, struct2.offset);
                    WorldGenMultiTemplate.Template template = new WorldGenMultiTemplate.Template();
                    template.template = subGen;
                    String[] args = struct2.position.split(",");
                    BlockPos pos = new BlockPos(Integer.parseInt(args[0]), Integer.parseInt(args[1]),
                            Integer.parseInt(args[2]));
                    template.offset = pos;
                }
                catch (Exception e)
                {
                    System.out
                            .println(struct2.name + " " + struct2.spawn + " " + struct2.chance + " " + struct2.offset);
                    e.printStackTrace();
                }
            }
            if (!gen.subTemplates.isEmpty())
            {
                WorldGenTemplates.templates.add(gen);
                gen = new WorldGenMultiTemplate();
                gen.chance = struct.chance;
                gen.syncGround = struct.syncGround;
                for (XMLStructure struct2 : struct.structures)
                {
                    try
                    {
                        WorldGenTemplates.TemplateGen subGen = new TemplateGen(struct2.name,
                                new SpawnBiomeMatcher(struct2.spawn), struct2.chance, struct2.offset);
                        WorldGenMultiTemplate.Template template = new WorldGenMultiTemplate.Template();
                        template.template = subGen;
                        String[] args = struct2.position.split(",");
                        BlockPos pos = new BlockPos(Integer.parseInt(args[0]), Integer.parseInt(args[1]),
                                Integer.parseInt(args[2]));
                        template.offset = pos;
                    }
                    catch (Exception e)
                    {
                        System.out.println(
                                struct2.name + " " + struct2.spawn + " " + struct2.chance + " " + struct2.offset);
                        e.printStackTrace();
                    }
                }
                WorldGenTemplates.namedTemplates.put(struct.name, gen);
            }
        }
    }

    // Old do not call this.
    @Deprecated
    public static void loadDefaults(File file)
    {

    }

    public static void reloadWorldgen()
    {
        PokecubeTemplates.clear();
        init(DEFAULT);
        for (String s : PokecubeMod.core.getConfig().extraWorldgenDatabases)
        {
            XMLWorldgenHandler.loadStructures(s);
        }
        XMLWorldgenHandler.processStructures();
    }

    public static void init(File file)
    {
        init();
        if (!file.exists())
        {
            Gson gson = new GsonBuilder().registerTypeAdapter(QName.class, new TypeAdapter<QName>()
            {
                @Override
                public void write(JsonWriter out, QName value) throws IOException
                {
                    out.value(value.toString());
                }

                @Override
                public QName read(JsonReader in) throws IOException
                {
                    return new QName(in.nextString());
                }
            }).setPrettyPrinting().create();
            String json = gson.toJson(defaults, XMLStructures.class);
            try
            {
                FileWriter writer = new FileWriter(file);
                writer.append(json);
                writer.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            defaults.structures.clear();
            defaults.multiStructures.clear();
            FileInputStream stream;
            try
            {
                stream = new FileInputStream(file);
                try
                {
                    loadStructures(stream, true);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                stream.close();
            }
            catch (FileNotFoundException e)
            {
                e.printStackTrace();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
}
