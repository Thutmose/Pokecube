package pokecube.core.database.worldgen;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.logging.Level;

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
import pokecube.core.database.SpawnBiomeMatcher;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.world.gen.WorldGenMultiTemplate;
import pokecube.core.world.gen.WorldGenTemplates;
import pokecube.core.world.gen.WorldGenTemplates.TemplateGen;
import pokecube.core.world.gen.template.PokecubeTemplates;

public class WorldgenHandler
{
    public static File       DEFAULT;
    public static CustomDims dims;

    public static class CustomDims
    {
        public List<CustomDim> dims = Lists.newArrayList();
    }

    public static class CustomDim
    {
        public int    dimid;
        public String world_name;
        public String dim_type;
        public String world_type;
        public String generator_options;

        @Override
        public String toString()
        {
            return dimid + " " + world_name + " " + dim_type + " " + world_type + " " + generator_options;
        }
    }

    public static class Structures
    {
        public List<Structure>      structures      = Lists.newArrayList();
        public List<MultiStructure> multiStructures = Lists.newArrayList();
    }

    public static class MultiStructure
    {
        public String          name;
        float                  chance;
        boolean                syncGround = false;
        public SpawnRule       spawn;
        public List<Structure> structures = Lists.newArrayList();
    }

    public static class Structure
    {
        public String    name;
        float            chance;
        int              offset;
        public String    biomeType;
        public SpawnRule spawn;
        public String    position;
    }

    public static Structures defaults = new Structures();

    static void init()
    {
        defaults.structures.clear();
        Structure ruin_1 = new Structure();
        ruin_1.name = "ruin_1";
        ruin_1.chance = 0.002f;
        ruin_1.offset = -3;
        ruin_1.biomeType = "ruin";
        SpawnRule rule = new SpawnRule();
        rule.values.put(SpawnBiomeMatcher.TYPES, "plains");
        ruin_1.spawn = rule;
        defaults.structures.add(ruin_1);
    }

    public static void loadCustomDims(String dimFile)
    {
        File file = new File(PokecubeTemplates.TEMPLATES, dimFile);
        if (!file.exists())
        {
            PokecubeMod.log("No Custom Dimensions file found: " + file
                    + " If you make one, it will allow specifying custom dimensions and worldgen.");
            return;
        }

        try
        {
            FileInputStream stream = new FileInputStream(file);
            InputStreamReader reader = new InputStreamReader(stream);
            dims = PokedexEntryLoader.gson.fromJson(reader, CustomDims.class);
            PokecubeMod.log("Loaded Dims: "+dims.dims);
        }
        catch (Exception e)
        {
            PokecubeMod.log(Level.WARNING, "Error loading custom Dims from: " + file, e);
        }

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
        Structures database;
        InputStreamReader reader = new InputStreamReader(stream);
        if (json)
        {
            database = PokedexEntryLoader.gson.fromJson(reader, Structures.class);
        }
        else
        {
            throw new IllegalArgumentException("The database for structures Must be a json.");
        }
        defaults.structures.addAll(database.structures);
        defaults.multiStructures.addAll(database.multiStructures);
    }

    public static void processStructures()
    {
        for (Structure struct : defaults.structures)
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
        for (MultiStructure struct : defaults.multiStructures)
        {
            WorldGenMultiTemplate gen = new WorldGenMultiTemplate();
            gen.syncGround = struct.syncGround;
            for (Structure struct2 : struct.structures)
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
                for (Structure struct2 : struct.structures)
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

    public static void reloadWorldgen()
    {
        PokecubeTemplates.clear();
        init(DEFAULT);
        for (String s : PokecubeMod.core.getConfig().extraWorldgenDatabases)
        {
            WorldgenHandler.loadStructures(s);
        }
        WorldgenHandler.processStructures();
        loadCustomDims("custom_dims.json");
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
            String json = gson.toJson(defaults, Structures.class);
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
