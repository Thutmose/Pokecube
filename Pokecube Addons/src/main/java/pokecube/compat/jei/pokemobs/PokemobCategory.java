package pokecube.compat.jei.pokemobs;

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
        ResourceLocation location = new ResourceLocation("minecraft", "textures/gui/container/crafting_table.png");// TODO
                                                                                                                   // make
                                                                                                                   // better
                                                                                                                   // for
                                                                                                                   // this.
        background = guiHelper.createDrawable(location, 29, 16, 116, 54);
        localizedName = Translator.translateToLocal("gui.jei.pokemobs");
        location = new ResourceLocation("pokecube_adventures", "textures/gui/mewhair.png");
        icon = guiHelper.createDrawable(location, 0, 0, 16, 16);
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
        // TODO Auto-generated method stub

    }

    @Override
    public void drawAnimations(Minecraft minecraft)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, PokemobRecipeWrapper recipeWrapper)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, PokemobRecipeWrapper recipeWrapper, IIngredients ingredients)
    {
        // TODO Auto-generated method stub
        IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();
        recipeLayout.getIngredientsGroup(PokedexEntry.class).init(0, false, JEICompat.ingredientRenderer, 94, 18, 16,
                16, 0, 0);
        for (int y = 0; y < 2; ++y)
        {
            for (int x = 0; x < 3; ++x)
            {
                int index = 1 + x + (y * 3);
                guiItemStacks.init(index, true, x * 18, y * 18);
            }
        }
        for (int y = 2; y < 3; ++y)
        {
            for (int x = 0; x < 3; ++x)
            {
                int index = 1 + x + (y * 3);
                recipeLayout.getIngredientsGroup(PokedexEntry.class).init(index, true, JEICompat.ingredientRenderer,
                        x * 18, y * 18, 16, 16, 0, 0);
            }
        }
        guiItemStacks.set(ingredients);
        recipeLayout.getIngredientsGroup(PokedexEntry.class).set(ingredients);
        recipeLayout.getIngredientsGroup(PokedexEntry.class).set(ingredients);
    }

}
