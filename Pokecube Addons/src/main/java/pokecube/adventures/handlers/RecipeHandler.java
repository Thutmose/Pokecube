package pokecube.adventures.handlers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import com.google.common.collect.Lists;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.RecipeSorter;
import net.minecraftforge.oredict.RecipeSorter.Category;
import pokecube.adventures.blocks.cloner.RecipeFossilRevive;
import pokecube.adventures.comands.Config;
import pokecube.adventures.items.bags.RecipeBag;
import pokecube.core.PokecubeItems;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.recipes.IRecipeParser;
import pokecube.core.database.recipes.XMLRecipeHandler;
import pokecube.core.database.recipes.XMLRecipeHandler.XMLRecipe;
import pokecube.core.database.recipes.XMLRecipeHandler.XMLRecipeInput;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.pokemobeggs.ItemPokemobEgg;

public class RecipeHandler
{
    private static final QName ENERGY     = new QName("cost");
    private static final QName PRIORITY   = new QName("priority");
    private static final QName POKEMOB    = new QName("pokemon");
    private static final QName TAME       = new QName("tame");
    private static final QName LEVEL      = new QName("lvl");
    private static final QName REANIMATOR = new QName("reanimator");
    private static final QName REMAIN     = new QName("remain");

    public static boolean      tmRecipe   = true;

    public static class ClonerRecipeParser implements IRecipeParser
    {
        @Override
        public void manageRecipe(XMLRecipe recipe) throws NullPointerException
        {
            List<ItemStack> inputs = Lists.newArrayList();
            for (XMLRecipeInput xml : recipe.inputs)
            {
                inputs.add(XMLRecipeHandler.getStack(xml));
            }
            PokedexEntry entry = Database.getEntry(recipe.values.get(POKEMOB));
            if (entry == null) throw new NullPointerException("No Entry for " + recipe.values.get(POKEMOB));
            ItemStack eggOut = ItemPokemobEgg.getEggStack(entry.getPokedexNb());
            int energy = Integer.parseInt(recipe.values.get(ENERGY));
            boolean failed = false;
            for (Object o : inputs)
                failed = failed || o == null;
            if (failed) { throw new NullPointerException("inputs: " + inputs); }
            int level = Integer.parseInt(recipe.values.get(LEVEL));
            int priority = 0;
            boolean reanimator = false;
            boolean tame = false;
            if (recipe.values.containsKey(PRIORITY)) priority = Integer.parseInt(recipe.values.get(PRIORITY));
            if (recipe.values.containsKey(REANIMATOR)) reanimator = Boolean.parseBoolean(recipe.values.get(REANIMATOR));
            if (recipe.values.containsKey(TAME)) tame = Boolean.parseBoolean(recipe.values.get(TAME));
            RecipeFossilRevive newRecipe = new RecipeFossilRevive(eggOut, inputs, entry, energy);
            newRecipe.level = level;
            newRecipe.setTame(tame);
            newRecipe.reanimator = reanimator;
            newRecipe.priority = priority;
            if (recipe.values.containsKey(REMAIN))
            {
                String[] remain = recipe.values.get(REMAIN).split(",");
                for (String s : remain)
                {
                    newRecipe.remainIndex.add(Integer.parseInt(s));
                }
            }
            RecipeFossilRevive.addRecipe(newRecipe);
        }
    }

    static
    {
        XMLRecipeHandler.recipeParsers.put("cloner", new ClonerRecipeParser());
        XMLRecipeHandler.recipeFiles.add("pokeadvrecipes");
    }

    public static void preInit()
    {
        File temp = new File(Database.CONFIGLOC);
        if (!temp.exists())
        {
            temp.mkdirs();
        }
        String name = "pokeadvrecipes.xml";
        File temp1 = new File(Database.CONFIGLOC + name);
        if (!temp1.exists() || Config.instance.forceRecipes)
        {
            ArrayList<String> rows = Database.getFile("/assets/pokecube_adventures/database/" + name);
            int n = 0;
            try
            {
                File file = new File(Database.CONFIGLOC + name);
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

    }

    private static void addClonerRecipes()
    {
        if (Config.instance.autoAddFossils) for (ItemStack stack : PokecubeItems.fossils.keySet())
        {
            PokedexEntry i = PokecubeItems.fossils.get(stack);
            if (PokecubeMod.registered.get(i.getPokedexNb()))
            {
                RecipeFossilRevive newRecipe = new RecipeFossilRevive(stack, Lists.newArrayList(stack), i,
                        Config.instance.fossilReanimateCost);
                RecipeFossilRevive.addRecipe(newRecipe);
            }
        }
    }

    public static void register()
    {
        RecipeSorter.register("pokecube_adventures:bag", RecipeBag.class, Category.SHAPELESS,
                "after:minecraft:shapeless");
        GameRegistry.addRecipe(new RecipeBag());
        addClonerRecipes();
    }
}
