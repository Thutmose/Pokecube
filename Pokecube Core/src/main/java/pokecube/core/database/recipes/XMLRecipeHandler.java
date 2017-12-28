package pokecube.core.database.recipes;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import net.minecraftforge.registries.GameData;
import pokecube.core.database.PokedexEntryLoader.Drop;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.utils.Tools;
import thut.lib.CompatWrapper;

public class XMLRecipeHandler
{
    private static int                       num           = 0;
    public static Set<String>                recipeFiles   = Sets.newHashSet();
    public static Map<String, IRecipeParser> recipeParsers = Maps.newHashMap();

    private static final QName               OREDICT       = new QName("oreDict");

    public static class DefaultParser implements IRecipeParser
    {
        @Override
        public void manageRecipe(XMLRecipe recipe) throws NullPointerException
        {
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
            IRecipe toAdd = null;
            ResourceLocation group = new ResourceLocation(PokecubeMod.ID, "loaded");
            if (recipe.shapeless) try
            {
                toAdd = new ShapelessOreRecipe(group, IngredientHandler.shapelessInputs(inputs.toArray()), output);
            }
            catch (Exception e)
            {
                e.printStackTrace();
                System.err.println("Error with Recipe for " + output);
                if (output != null && output.hasTagCompound()) System.err.println(output.getTagCompound());
            }
            else toAdd = new ShapedOreRecipe(group, output, IngredientHandler.parseShaped(inputs.toArray()));
            GameData.register_impl(toAdd.setRegistryName(new ResourceLocation("pokecube", "autoloaded" + (num++))));
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
        }
        catch (NullPointerException e)
        {
            PokecubeMod.log(Level.WARNING, "Error with a recipe, Error for: " + recipe, e);
        }
    }
}
