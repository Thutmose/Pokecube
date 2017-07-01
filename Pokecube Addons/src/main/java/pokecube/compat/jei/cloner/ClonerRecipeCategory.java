package pokecube.compat.jei.cloner;

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

public class ClonerRecipeCategory implements IRecipeCategory<ClonerRecipeWrapper>
{

    private static final int craftOutputSlot = 0;
    private static final int craftInputSlot1 = 1;

    @Nonnull
    private final IDrawable  background;
    @Nonnull
    private final IDrawable  icon;
    @Nonnull
    private final String     localizedName;

    public ClonerRecipeCategory(IGuiHelper guiHelper)
    {
        ResourceLocation location = new ResourceLocation(PokecubeAdv.ID, "textures/gui/clonergui.png");
        background = guiHelper.createDrawable(location, 29, 16, 116, 54);
        localizedName = Translator.translateToLocal("tile.cloner.reanimator.name");
        icon = guiHelper.createDrawable(JEICompat.TABS, 16, 0, 16, 16);
    }

    @Override
    public void drawExtras(Minecraft minecraft)
    {

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
    public String getUid()
    {
        return JEICompat.REANIMATOR;
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, ClonerRecipeWrapper recipeWrapper, IIngredients ingredients)
    {
        IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();
        recipeLayout.getIngredientsGroup(PokedexEntry.class).init(craftOutputSlot, false,
                JEICompat.ingredientRendererInput, 94, 18, 16, 16, 0, 0);
        for (int y = 0; y < 3; ++y)
        {
            for (int x = 0; x < 3; ++x)
            {
                int index = craftInputSlot1 + x + (y * 3);
                int dy = x == 1 ? 0 : 9;
                guiItemStacks.init(index, true, x * 18 + 2, y * 18 + dy);
            }
        }
        guiItemStacks.set(ingredients);
        recipeLayout.getIngredientsGroup(PokedexEntry.class).set(ingredients);
    }

    @Override
    public IDrawable getIcon()
    {
        return icon;
    }

    @Override
    public String getModName()
    {
        return "Pokecube";
    }

}
