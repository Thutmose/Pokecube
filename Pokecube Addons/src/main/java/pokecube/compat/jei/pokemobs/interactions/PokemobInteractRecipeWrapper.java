package pokecube.compat.jei.pokemobs.interactions;

import java.awt.Rectangle;
import java.util.List;

import com.google.common.collect.Lists;

import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import pokecube.core.database.PokedexEntry;

public class PokemobInteractRecipeWrapper implements IRecipeWrapper
{
    final PokemobInteractRecipe recipe;

    public PokemobInteractRecipeWrapper(PokemobInteractRecipe recipe)
    {
        this.recipe = recipe;
    }

    @Override
    public void getIngredients(IIngredients ingredients)
    {
        List<ItemStack> stackInputs = Lists.newArrayList();
        stackInputs.add(recipe.key);
        List<PokedexEntry> mobInput = Lists.newArrayList(recipe.entry);
        ingredients.setInputs(ItemStack.class, stackInputs);
        ingredients.setInputs(PokedexEntry.class, mobInput);
        if (recipe.outputStack != null) ingredients.setOutput(ItemStack.class, recipe.outputStack);
        if (recipe.outputForme != null) ingredients.setOutput(PokedexEntry.class, recipe.outputForme);

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
        if (!recipe.logic.female)
        {
            String gender = I18n.format("gui.jei.pokemob.gender." + "female");
            tooltips.add(I18n.format("gui.jei.pokemob.nogender", gender));
        }
        if (!recipe.logic.male)
        {
            String gender = I18n.format("gui.jei.pokemob.gender." + "male");
            tooltips.add(I18n.format("gui.jei.pokemob.nogender", gender));
        }
        return tooltips;
    }

    @Override
    public boolean handleClick(Minecraft minecraft, int mouseX, int mouseY, int mouseButton)
    {
        return false;
    }
}
