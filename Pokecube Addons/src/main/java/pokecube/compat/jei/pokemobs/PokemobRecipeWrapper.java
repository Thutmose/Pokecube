package pokecube.compat.jei.pokemobs;

import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Lists;

import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.wrapper.ICraftingRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.fluids.FluidStack;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.moves.MovesUtils;

public class PokemobRecipeWrapper implements ICraftingRecipeWrapper
{
    final PokemobRecipe recipe;

    public PokemobRecipeWrapper(PokemobRecipe recipe)
    {
        this.recipe = recipe;
    }

    @Override
    public void getIngredients(IIngredients ingredients)
    {
        List<ItemStack> stackInputs = Lists.newArrayList();
        if (recipe.data.item != null) stackInputs.add(recipe.data.item);
        List<PokedexEntry> mobInput = Lists.newArrayList(recipe.data.preEvolution);
        ingredients.setInputs(ItemStack.class, stackInputs);
        ingredients.setInputs(PokedexEntry.class, mobInput);
        ingredients.setOutput(PokedexEntry.class, recipe.data.evolution);
    }

    @Override
    public List<FluidStack> getFluidInputs()
    {
        return null;
    }

    @Override
    public List<FluidStack> getFluidOutputs()
    {
        return null;
    }

    @Override
    public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY)
    {

    }

    @Override
    public void drawAnimations(Minecraft minecraft, int recipeWidth, int recipeHeight)
    {

    }

    @Override
    public List<String> getTooltipStrings(int mouseX, int mouseY)
    {
        List<String> tooltips = Lists.newArrayList();
        if (recipe.data.level > 0)
        {
            tooltips.add(I18n.format("gui.jei.pokemob.evo.level", recipe.data.level));
        }
        if (recipe.data.traded)
        {
            tooltips.add(I18n.format("gui.jei.pokemob.evo.traded"));
        }
        if (recipe.data.nightOnly)
        {
            tooltips.add(I18n.format("gui.jei.pokemob.evo.night"));
        }
        if (recipe.data.dayOnly)
        {
            tooltips.add(I18n.format("gui.jei.pokemob.evo.day"));
        }
        if (recipe.data.biome != null && !recipe.data.biome.isEmpty())
        {
            String[] biomes = recipe.data.biome.split(",");

            class Checker
            {
                boolean checkPerType(Biome actualBiome, String biome)
                {
                    boolean specific = false;
                    if (biome.startsWith("T")) biome = biome.substring(1);
                    else specific = true;

                    if (specific) { return Database.convertMoveName(biome)
                            .equals(Database.convertMoveName(actualBiome.getBiomeName())); }

                    String[] args = biome.split("\'");
                    List<BiomeDictionary.Type> neededTypes = Lists.newArrayList();
                    List<BiomeDictionary.Type> bannedTypes = Lists.newArrayList();
                    for (String s : args)
                    {
                        String name = s.substring(1);
                        if (s.startsWith("B"))
                        {
                            for (BiomeDictionary.Type t : BiomeDictionary.Type.values())
                            {
                                if (t.toString().equalsIgnoreCase(name))
                                {
                                    bannedTypes.add(t);
                                }
                            }
                        }
                        else if (s.startsWith("W"))
                        {
                            for (BiomeDictionary.Type t : BiomeDictionary.Type.values())
                            {
                                if (t.toString().equalsIgnoreCase(name))
                                {
                                    neededTypes.add(t);
                                }
                            }
                        }
                    }
                    Biome b = actualBiome;
                    boolean correctType = true;
                    boolean bannedType = false;
                    boolean found = false;
                    for (BiomeDictionary.Type t : neededTypes)
                    {
                        if (!found) found = BiomeDictionary.isBiomeOfType(b, t);
                        correctType = correctType && BiomeDictionary.isBiomeOfType(b, t);
                    }
                    for (BiomeDictionary.Type t : bannedTypes)
                    {
                        bannedType = bannedType || BiomeDictionary.isBiomeOfType(b, t);
                    }
                    return correctType && found && !bannedType;
                }
            }

            List<String> biomeNames = Lists.newArrayList();
            Checker check = new Checker();
            for (String biome : biomes)
            {
                Iterator<Biome> it = Biome.REGISTRY.iterator();
                while (it.hasNext())
                {
                    Biome test = it.next();
                    boolean valid = check.checkPerType(test, biome);
                    if (valid)
                    {
                        biomeNames.add(test.getBiomeName());
                    }
                }
            }
            tooltips.add(I18n.format("gui.jei.pokemob.evo.biome", biomeNames));
        }
        if (recipe.data.happy)
        {
            tooltips.add(I18n.format("gui.jei.pokemob.evo.happy"));
        }
        if (recipe.data.move != null && !recipe.data.move.isEmpty())
        {
            tooltips.add(I18n.format("gui.jei.pokemob.evo.move",
                    I18n.format(MovesUtils.getUnlocalizedMove(recipe.data.move))));
        }
        if (recipe.data.rainOnly)
        {
            tooltips.add(I18n.format("gui.jei.pokemob.evo.rain"));
        }
        if (recipe.data.gender != 0)
        {
            String gender = I18n
                    .format("gui.jei.pokemob.gender." + (recipe.data.gender == IPokemob.MALE ? "male" : "female"));
            tooltips.add(I18n.format("gui.jei.pokemob.evo.gender", gender));
        }
        if (recipe.data.randomFactor != 1)
        {
            String var = ((int) (100 * recipe.data.randomFactor)) + "%";
            tooltips.add(I18n.format("gui.jei.pokemob.evo.chance", var));
        }
        return tooltips;
    }

    @Override
    public boolean handleClick(Minecraft minecraft, int mouseX, int mouseY, int mouseButton)
    {
        return false;
    }

    @Override
    public List<?> getInputs()
    {
        return null;
    }

    @Override
    public List<ItemStack> getOutputs()
    {
        return null;
    }

}
