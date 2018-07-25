package pokecube.core.database.recipes;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;

import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntryLoader.Drop;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.utils.Tools;
import thut.lib.CompatWrapper;

public class XMLRecipeHandler
{
    // Replace calls to GameRegistry.addShapeless/ShapedRecipe with these
    // methods, which will dump it to a json in your dir of choice
    // Also works with OD, replace GameRegistry.addRecipe(new
    // ShapedOreRecipe/ShapelessOreRecipe with the same calls

    private static final Gson        GSON          = new GsonBuilder().setPrettyPrinting().create();
    private static File              RECIPE_DIR    = null;
    private static final Set<String> USED_OD_NAMES = new TreeSet<>();

    private static void setupDir()
    {
        if (RECIPE_DIR == null)
        {
            RECIPE_DIR = new File(Database.CONFIGLOC + "recipes");
        }

        if (!RECIPE_DIR.exists())
        {
            RECIPE_DIR.mkdir();
        }
    }

    private static void addShapedRecipe(String group, ItemStack result, Object... components)
    {
        setupDir();

        // GameRegistry.addShapedRecipe(result, components);

        Map<String, Object> json = new LinkedHashMap<>();

        List<String> pattern = new ArrayList<>();
        int i = 0;
        while (i < components.length && components[i] instanceof String)
        {
            pattern.add(((String) components[i]).toUpperCase());
            i++;
        }
        boolean isOreDict = false;
        Map<String, Map<String, Object>> key = new HashMap<>();
        Character curKey = null;
        for (; i < components.length; i++)
        {
            Object o = components[i];
            if (o instanceof Character)
            {
                if (curKey != null) throw new IllegalArgumentException("Provided two char keys in a row");
                curKey = (Character) o;
            }
            else
            {
                if (curKey == null) throw new IllegalArgumentException("Providing object without a char key");
                if (o instanceof String) isOreDict = true;
                key.put(Character.toString(Character.toUpperCase(curKey)), serializeItem(o));
                curKey = null;
            }
        }
        json.put("type", isOreDict ? "forge:ore_shaped" : "minecraft:crafting_shaped");
        json.put("group", group);
        json.put("pattern", pattern);
        json.put("key", key);
        json.put("result", serializeItem(result));

        // names the json the same name as the output's registry name
        // repeatedly adds _alt if a file already exists
        // janky I know but it works
        String suffix = result.getItem().getHasSubtypes() ? "_" + result.getItemDamage() : "";
        File f = new File(RECIPE_DIR, result.getItem().getRegistryName().getResourcePath() + suffix + ".json");

        while (f.exists())
        {
            suffix += "_alt";
            f = new File(RECIPE_DIR, result.getItem().getRegistryName().getResourcePath() + suffix + ".json");
        }

        try (FileWriter w = new FileWriter(f))
        {
            GSON.toJson(json, w);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private static void addShapelessRecipe(String group, ItemStack result, Object... components)
    {
        setupDir();

        // addShapelessRecipe(result, components);

        Map<String, Object> json = new LinkedHashMap<>();

        boolean isOreDict = false;
        List<Map<String, Object>> ingredients = new ArrayList<>();
        for (Object o : components)
        {
            if (o instanceof String) isOreDict = true;
            ingredients.add(serializeItem(o));
        }
        json.put("type", isOreDict ? "forge:ore_shapeless" : "minecraft:crafting_shapeless");
        json.put("group", group);
        json.put("ingredients", ingredients);
        json.put("result", serializeItem(result));

        // names the json the same name as the output's registry name
        // repeatedly adds _alt if a file already exists
        // janky I know but it works
        String suffix = result.getItem().getHasSubtypes() ? "_" + result.getItemDamage() : "";
        File f = new File(RECIPE_DIR, result.getItem().getRegistryName().getResourcePath() + suffix + ".json");

        while (f.exists())
        {
            suffix += "_alt";
            f = new File(RECIPE_DIR, result.getItem().getRegistryName().getResourcePath() + suffix + ".json");
        }

        try (FileWriter w = new FileWriter(f))
        {
            GSON.toJson(json, w);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private static Map<String, Object> serializeItem(Object thing)
    {
        if (thing instanceof Item) { return serializeItem(new ItemStack((Item) thing)); }
        if (thing instanceof Block) { return serializeItem(new ItemStack((Block) thing)); }
        if (thing instanceof ItemStack)
        {
            ItemStack stack = (ItemStack) thing;
            Map<String, Object> ret = new LinkedHashMap<>();
            if (stack.hasTagCompound()) ret.put("type", "minecraft:item_nbt");
            ret.put("item", stack.getItem().getRegistryName().toString());
            if (stack.getItem().getHasSubtypes() || stack.getItemDamage() != 0)
            {
                ret.put("data", stack.getItemDamage());
            }
            if (stack.hasTagCompound())
            {
                ret.put("nbt", stack.getTagCompound().toString());
            }
            if (stack.getCount() > 1)
            {
                ret.put("count", stack.getCount());
            }

            return ret;
        }
        if (thing instanceof String)
        {
            Map<String, Object> ret = new HashMap<>();
            USED_OD_NAMES.add((String) thing);
            ret.put("item", "#" + ((String) thing).toUpperCase(Locale.ROOT));
            return ret;
        }

        throw new IllegalArgumentException("Not a block, item, stack, or od name");
    }

    private static void generateConstants()
    {
        List<Map<String, Object>> json = new ArrayList<>();
        for (String s : USED_OD_NAMES)
        {
            Map<String, Object> entry = new HashMap<>();
            entry.put("name", s.toUpperCase(Locale.ROOT));
            entry.put("ingredient", ImmutableMap.of("type", "forge:ore_dict", "ore", s));
            json.add(entry);
        }

        try (FileWriter w = new FileWriter(new File(RECIPE_DIR, "_constants.json")))
        {
            GSON.toJson(json, w);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private static int                       num           = 0;
    public static Set<String>                recipeFiles   = Sets.newHashSet();
    public static Map<String, IRecipeParser> recipeParsers = Maps.newHashMap();

    private static final QName               OREDICT       = new QName("oreDict");

    public static class DefaultParser implements IRecipeParser
    {
        @Override
        public void manageRecipe(XMLRecipe recipe) throws NullPointerException
        {
            if (!PokecubeMod.debug) return;
            ItemStack output = getStack(recipe.output);
            List<Object> inputs = Lists.newArrayList();
            if (recipe.shapeless) for (XMLRecipeInput xml : recipe.inputs)
            {
                if (xml.values.containsKey(OREDICT)) inputs.add(xml.values.get(OREDICT));
                else inputs.add(getStack(xml));
            }
            else
            {
                String[] map = recipe.map.split(",");
                for (String s : map)
                    inputs.add(s);
                for (XMLRecipeInput xml : recipe.inputs)
                {
                    Character ch = xml.key.charAt(0);
                    inputs.add(ch);
                    if (xml.values.containsKey(OREDICT)) inputs.add(xml.values.get(OREDICT));
                    else inputs.add(getStack(xml));
                }
            }
            boolean failed = !CompatWrapper.isValid(output);
            for (Object o : inputs)
                failed = failed || o == null;
            if (failed) { throw new NullPointerException("output: " + output + " inputs: " + inputs); }
            String group = PokecubeMod.ID;
            if (recipe.shapeless) try
            {
                addShapelessRecipe(group, output, inputs.toArray());
            }
            catch (Exception e)
            {
                e.printStackTrace();
                System.err.println("Error with Recipe for " + output);
                if (output != null && output.hasTagCompound()) System.err.println(output.getTagCompound());
            }
            else addShapedRecipe(group, output, inputs.toArray());
            generateConstants();

        }

        @Override
        public String serialize(XMLRecipe recipe)
        {

            return IRecipeParser.super.serialize(recipe);
        }

    }

    static
    {
        recipeParsers.put("default", new DefaultParser());
        recipeParsers.put("move_effect", new PokemobMoveRecipeParser());
    }

    @XmlRootElement(name = "Recipes")
    public static class XMLRecipes
    {
        @XmlElement(name = "Recipe")
        public List<XMLRecipe> recipes = Lists.newArrayList();
    }

    @XmlRootElement(name = "Recipe")
    public static class XMLRecipe
    {
        @XmlAttribute
        public boolean              shapeless = false;
        @XmlAttribute
        String                      handler   = "default";
        @XmlAttribute
        public String               map       = "";
        @XmlElement(name = "Output")
        public XMLRecipeOutput      output;
        @XmlElement(name = "Input")
        public List<XMLRecipeInput> inputs    = Lists.newArrayList();
        @XmlAnyAttribute
        public Map<QName, String>   values    = Maps.newHashMap();

        @Override
        public String toString()
        {
            return "output: " + output + " inputs: " + inputs + " shapeless: " + shapeless + " map: " + map;
        }
    }

    @XmlRootElement(name = "Output")
    public static class XMLRecipeOutput extends Drop
    {
        @Override
        public String toString()
        {
            return "values: " + values + " tag: " + tag;
        }
    }

    @XmlRootElement(name = "Input")
    public static class XMLRecipeInput extends Drop
    {
        @XmlAttribute
        public String key = "";

        @Override
        public String toString()
        {
            return "values: " + values + " tag: " + tag + " key: " + key;
        }
    }

    public static ItemStack getStack(Drop drop)
    {
        Map<QName, String> values = drop.values;
        if (drop.tag != null)
        {
            QName name = new QName("tag");
            values.put(name, drop.tag);
        }
        return Tools.getStack(drop.values);
    }

    public static void addRecipe(XMLRecipe recipe)
    {
        IRecipeParser parser = recipeParsers.get(recipe.handler);
        try
        {
            parser.manageRecipe(recipe);
            if (PokecubeMod.debug) try
            {
                File dir = new File(Database.CONFIGLOC + "recipes");
                dir.mkdirs();
                File outputFile = new File(dir, File.separator + parser.fileName("autoloaded" + (num++) + ".json"));
                String json = parser.serialize(recipe);
                FileWriter writer = new FileWriter(outputFile);
                writer.append(json);
                writer.close();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

        }
        catch (NullPointerException e)
        {
            PokecubeMod.log(Level.WARNING, "Error with a recipe, Error for: " + recipe, e);
        }
    }
}
