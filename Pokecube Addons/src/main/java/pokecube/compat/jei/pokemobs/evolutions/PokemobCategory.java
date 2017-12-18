package pokecube.compat.jei.pokemobs.evolutions;

import javax.annotation.Nonnull;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.util.Translator;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import pokecube.adventures.PokecubeAdv;
import pokecube.compat.jei.JEICompat;
import pokecube.core.database.PokedexEntry;

public class PokemobCategory implements IRecipeCategory<PokemobRecipeWrapper>
{
    @Nonnull
    private final IDrawable background;
    @Nonnull
    private final IDrawable icon;
    @Nonnull
    private final String    localizedName;

    public PokemobCategory(IGuiHelper guiHelper)
    {
        ResourceLocation location = new ResourceLocation(PokecubeAdv.ID, "textures/gui/evorecipe.png");
        background = guiHelper.createDrawable(location, 29, 16, 116, 54);
        localizedName = Translator.translateToLocal("gui.jei.pokemobs");
        icon = guiHelper.createDrawable(JEICompat.TABS, 32, 0, 16, 16);
    }

    @Override
    public String getUid()
    {
        return JEICompat.POKEMOB;
    }

    @Override
    @Nonnull
    public IDrawable getBackground()
    {
        return background;
    }

    @Nonnull
    @Override
    public String getTitle()
    {
        return localizedName;
    }

    @Override
    public IDrawable getIcon()
    {
        return icon;
    }

    @Override
    public void drawExtras(Minecraft minecraft)
    {
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, PokemobRecipeWrapper recipeWrapper, IIngredients ingredients)
    {
        int out = 24;
        IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();
        recipeLayout.getIngredientsGroup(PokedexEntry.class).init(0, false, JEICompat.ingredientRendererOutput, 81, 15,
                out, out, 4, 4);
        int x = 50;
        int y = 0;
        guiItemStacks.init(1, true, x, y);
        x = 14;
        y = 15;
        recipeLayout.getIngredientsGroup(PokedexEntry.class).init(1, true, JEICompat.ingredientRendererOutput, x, y,
                out, out, 4, 4);
        guiItemStacks.set(ingredients);
        recipeLayout.getIngredientsGroup(PokedexEntry.class).set(ingredients);
    }

    @Override
    public String getModName()
    {
        return "Pokecube";
    }

}
