package pokecube.compat.jei.pokemobs.evolutions;

import java.awt.Rectangle;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Lists;

import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.world.biome.Biome;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.SpawnBiomeMatcher;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.moves.MovesUtils;
import thut.api.terrain.BiomeType;

public class PokemobRecipeWrapper implements IRecipeWrapper
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
    public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY)
    {

    }

    @Override
    public List<String> getTooltipStrings(int mouseX, int mouseY)
    {
        List<String> tooltips = Lists.newArrayList();
        Rectangle arrow = new Rectangle(44, 18, 32, 17);
        if (!arrow.contains(mouseX, mouseY)) return tooltips;
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
        if (recipe.data.duskOnly)
        {
            tooltips.add(I18n.format("gui.jei.pokemob.evo.dusk"));
        }
        if (recipe.data.dawnOnly)
        {
            tooltips.add(I18n.format("gui.jei.pokemob.evo.dawn"));
        }
        if (recipe.data.matcher != null)
        {
            recipe.data.matcher.reset();
            recipe.data.matcher.parse();
            List<String> biomeNames = Lists.newArrayList();
            Iterator<Biome> it = Biome.REGISTRY.iterator();
            for (BiomeType t : recipe.data.matcher.validSubBiomes)
            {
                biomeNames.add(t.readableName);
            }
            while (it.hasNext())
            {
                Biome test = it.next();
                boolean valid = recipe.data.matcher.validBiomes.contains(test);
                if (valid)
                {
                    biomeNames.add(test.getBiomeName());
                }
            }
            for (SpawnBiomeMatcher matcher : recipe.data.matcher.children)
            {
                it = Biome.REGISTRY.iterator();
                for (BiomeType t : matcher.validSubBiomes)
                {
                    biomeNames.add(t.readableName);
                }
                while (it.hasNext())
                {
                    Biome test = it.next();
                    boolean valid = matcher.validBiomes.contains(test);
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
}
