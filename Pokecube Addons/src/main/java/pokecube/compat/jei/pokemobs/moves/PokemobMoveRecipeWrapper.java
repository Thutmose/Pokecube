package pokecube.compat.jei.pokemobs.moves;

import java.awt.Rectangle;
import java.util.List;

import com.google.common.collect.Lists;

import mezz.jei.api.IJeiHelpers;
import mezz.jei.plugins.vanilla.crafting.ShapelessRecipeWrapper;
import net.minecraft.client.resources.I18n;
import pokecube.core.moves.MovesUtils;

public class PokemobMoveRecipeWrapper extends ShapelessRecipeWrapper<PokemobMoveRecipe>
{
    final PokemobMoveRecipe recipe;

    public PokemobMoveRecipeWrapper(IJeiHelpers helpers, PokemobMoveRecipe recipe)
    {
        super(helpers, recipe);
        this.recipe = recipe;
    }

    @Override
    public List<String> getTooltipStrings(int mouseX, int mouseY)
    {
        Rectangle arrow = new Rectangle(55, 18, 34, 17);
        if (!arrow.contains(mouseX, mouseY)) return super.getTooltipStrings(mouseX, mouseY);
        List<String> tooltips = Lists.newArrayList();
        tooltips.add(I18n.format(MovesUtils.getUnlocalizedMove(recipe.move)));
        return tooltips;
    }
}
