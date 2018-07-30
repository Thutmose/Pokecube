package pokecube.adventures.handlers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import javax.xml.namespace.QName;

import com.google.common.collect.Lists;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.GameData;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.blocks.cloner.ClonerHelper;
import pokecube.adventures.blocks.cloner.ClonerHelper.DNAPack;
import pokecube.adventures.blocks.cloner.recipe.RecipeFossilRevive;
import pokecube.adventures.blocks.cloner.recipe.RecipeSelector;
import pokecube.adventures.blocks.cloner.recipe.RecipeSelector.SelectorValue;
import pokecube.adventures.commands.Config;
import pokecube.core.PokecubeItems;
import pokecube.core.database.Database;
import pokecube.core.database.Pokedex;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.recipes.IRecipeParser;
import pokecube.core.database.recipes.XMLRecipeHandler;
import pokecube.core.database.recipes.XMLRecipeHandler.XMLRecipe;
import pokecube.core.database.recipes.XMLRecipeHandler.XMLRecipeInput;
import pokecube.core.database.rewards.XMLRewardsHandler;
import pokecube.core.entity.pokemobs.genetics.genes.SpeciesGene;
import pokecube.core.entity.pokemobs.genetics.genes.SpeciesGene.SpeciesInfo;
import pokecube.core.interfaces.PokecubeMod;
import thut.api.entity.genetics.Alleles;
import thut.lib.CompatWrapper;

public class RecipeHandler
{
    private static final QName ENERGY           = new QName("cost");
    private static final QName PRIORITY         = new QName("priority");
    private static final QName POKEMOB          = new QName("pokemon");
    private static final QName TAME             = new QName("tame");
    private static final QName LEVEL            = new QName("lvl");
    private static final QName REMAIN           = new QName("remain");
    private static final QName CHANCE           = new QName("chance");
    private static final QName POKEMOBA         = new QName("pokemonA");
    private static final QName POKEMOBB         = new QName("pokemonB");
    private static final QName POKEMOBE         = new QName("pokemonE");
    private static final QName DNADESTRUCT      = new QName("dna");
    private static final QName SELECTORDESTRUCT = new QName("selector");

    public static boolean      tmRecipe         = true;

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
            int energy = Integer.parseInt(recipe.values.get(ENERGY));
            boolean failed = false;
            for (Object o : inputs)
                failed = failed || o == null;
            if (failed) { throw new NullPointerException("inputs: " + inputs); }
            int level = Integer.parseInt(recipe.values.get(LEVEL));
            int priority = 0;
            boolean tame = false;
            if (recipe.values.containsKey(PRIORITY)) priority = Integer.parseInt(recipe.values.get(PRIORITY));
            if (recipe.values.containsKey(TAME)) tame = Boolean.parseBoolean(recipe.values.get(TAME));
            RecipeFossilRevive newRecipe = new RecipeFossilRevive(inputs, entry, energy);

            newRecipe.level = level;
            newRecipe.setTame(tame);
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

    public static class SplicerRecipeParser implements IRecipeParser
    {
        @Override
        public void manageRecipe(XMLRecipe recipe) throws NullPointerException
        {
            List<ItemStack> inputs = Lists.newArrayList();
            for (XMLRecipeInput xml : recipe.inputs)
            {
                inputs.add(XMLRecipeHandler.getStack(xml));
            }
        }
    }

    public static class ExtractorRecipeParser implements IRecipeParser
    {
        @Override
        public void manageRecipe(XMLRecipe recipe) throws NullPointerException
        {
            List<ItemStack> inputs = Lists.newArrayList();
            for (XMLRecipeInput xml : recipe.inputs)
            {
                inputs.add(XMLRecipeHandler.getStack(xml));
            }
        }
    }

    public static class SelectorRecipeParser implements IRecipeParser
    {
        @Override
        public void manageRecipe(XMLRecipe recipe) throws NullPointerException
        {
            List<ItemStack> inputs = Lists.newArrayList();
            for (XMLRecipeInput xml : recipe.inputs)
            {
                inputs.add(XMLRecipeHandler.getStack(xml));
            }
            if (inputs.size() != 1) throw new NullPointerException("Wrong number of stacks for " + recipe);
            ItemStack stack = inputs.get(0);
            if (!CompatWrapper.isValid(stack)) throw new NullPointerException("Invalid stack for " + recipe);
            float dna = Float.parseFloat(recipe.values.get(DNADESTRUCT));
            float select = Float.parseFloat(recipe.values.get(SELECTORDESTRUCT));
            SelectorValue value = new SelectorValue(select, dna);
            RecipeSelector.addSelector(stack, value);
        }
    }

    public static class DNARecipeParser implements IRecipeParser
    {
        @Override
        public void manageRecipe(XMLRecipe recipe) throws NullPointerException
        {
            List<ItemStack> inputs = Lists.newArrayList();
            for (XMLRecipeInput xml : recipe.inputs)
            {
                inputs.add(XMLRecipeHandler.getStack(xml));
            }
            if (inputs.size() != 1) throw new NullPointerException("Wrong number of stacks for " + recipe);
            ItemStack stack = inputs.get(0);
            if (!CompatWrapper.isValid(stack)) throw new NullPointerException("Invalid stack for " + recipe);
            PokedexEntry entry = Database.getEntry(recipe.values.get(POKEMOB));
            PokedexEntry entryA = Database.getEntry(recipe.values.get(POKEMOBA));
            PokedexEntry entryB = Database.getEntry(recipe.values.get(POKEMOBB));
            PokedexEntry entryE = Database.getEntry(recipe.values.get(POKEMOBE));

            if (entry == null && entryA == null && entryB == null && entryE == null)
                throw new NullPointerException("No Entry for " + recipe.values.get(POKEMOB));

            if (entry == null)
            {
                entry = entryA == null ? entryB == null ? entryE : entryB : entryA;
            }
            if (entryA == null) entryA = entry;
            if (entryB == null) entryB = entry;

            SpeciesGene geneA = new SpeciesGene();
            SpeciesInfo info = geneA.getValue();
            info.entry = entryA;
            SpeciesGene geneB = new SpeciesGene();
            info = geneB.getValue();
            info.entry = entryB;
            Alleles alleles = new Alleles(geneA, geneB);
            if (entryE != null)
            {
                SpeciesGene geneE = new SpeciesGene();
                info = geneE.getValue();
                info.entry = entryE;
            }
            float chance = 1;
            if (recipe.values.containsKey(CHANCE)) chance = Float.parseFloat(recipe.values.get(CHANCE));
            DNAPack pack = new DNAPack(alleles, chance);
            ClonerHelper.registerDNA(pack, stack);
        }
    }

    static
    {
        XMLRecipeHandler.recipeParsers.put("cloner", new ClonerRecipeParser());
        XMLRecipeHandler.recipeParsers.put("splicer", new SplicerRecipeParser());
        XMLRecipeHandler.recipeParsers.put("extractor", new ExtractorRecipeParser());
        XMLRecipeHandler.recipeParsers.put("selector", new SelectorRecipeParser());
        XMLRecipeHandler.recipeParsers.put("dna", new DNARecipeParser());
        XMLRecipeHandler.recipeFiles.add("pokeadvrecipes");
        XMLRewardsHandler.recipeFiles.add("pokeadvrewards");
    }

    public static void preInit()
    {
        String CONFIGLOC = "./config/pokecube/database/";
        File temp = new File(CONFIGLOC);
        if (!temp.exists())
        {
            temp.mkdirs();
        }
        String name = "pokeadvrecipes.json";
        File temp1 = new File(CONFIGLOC + name);
        if (!temp1.exists() || Config.instance.forceRecipes)
        {
            ArrayList<String> rows = Database.getFile("/assets/pokecube_adventures/database/" + name);
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
                PokecubeMod.log(Level.WARNING, name + " " + n, e);
            }
        }
        name = "pokeadvrewards.json";
        temp1 = new File(CONFIGLOC + name);
        if (!temp1.exists() || Config.instance.forceRecipes)
        {
            ArrayList<String> rows = Database.getFile("/assets/pokecube_adventures/database/" + name);
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
                PokecubeMod.log(name + " " + n);
                e.printStackTrace();
            }
        }
    }

    public static void addClonerRecipes()
    {
        if (Config.instance.autoAddFossils) for (ItemStack stack : PokecubeItems.fossils.keySet())
        {
            PokedexEntry i = PokecubeItems.fossils.get(stack);
            if (Pokedex.getInstance().isRegistered(i))
            {
                RecipeFossilRevive newRecipe = new RecipeFossilRevive(Lists.newArrayList(), i,
                        Config.instance.fossilReanimateCost);
                RecipeFossilRevive.addRecipe(newRecipe);
                SpeciesGene geneA = new SpeciesGene();
                SpeciesInfo info = geneA.getValue();
                info.entry = i;
                SpeciesGene geneB = new SpeciesGene();
                info = geneB.getValue();
                info.entry = i;
                Alleles alleles = new Alleles(geneA, geneB);
                DNAPack pack = new DNAPack(alleles, 1);
                ClonerHelper.registerDNA(pack, stack);
            }
        }
    }

    public static void register(Object register)
    {
        RecipeSelector recipe = new RecipeSelector();
        recipe.setRegistryName(new ResourceLocation(PokecubeAdv.ID, "selectormerging"));
        GameData.register_impl(recipe);
    }
}
