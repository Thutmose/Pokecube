package pokecube.compat.jei.cloner;

import javax.annotation.Nonnull;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.ICraftingGridHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.wrapper.ICraftingRecipeWrapper;
import mezz.jei.util.Log;
import mezz.jei.util.Translator;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import pokecube.compat.jei.JEICompat;

public class ClonerRecipeCategory implements IRecipeCategory<ClonerRecipeWrapper>
{

    private static final int craftOutputSlot = 0;
    private static final int craftInputSlot1 = 1;

    @Nonnull
    private final IDrawable           background;
    @Nonnull
    private final String              localizedName;
    @Nonnull
    private final ICraftingGridHelper craftingGridHelper;

    public ClonerRecipeCategory(IGuiHelper guiHelper)
    {
        ResourceLocation location = new ResourceLocation("minecraft", "textures/gui/container/crafting_table.png");
        background = guiHelper.createDrawable(location, 29, 16, 116, 54);
        localizedName = Translator.translateToLocal("tile.cloner.name");
        craftingGridHelper = guiHelper.createCraftingGridHelper(craftInputSlot1, craftOutputSlot);
    }

    @Override
    public void drawAnimations(Minecraft minecraft)
    {

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
        return JEICompat.CLONER;
    }

    @Override
    public void setRecipe(@Nonnull IRecipeLayout recipeLayout, @Nonnull ClonerRecipeWrapper recipeWrapper)
    {
        IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();
        
        guiItemStacks.init(craftOutputSlot, false, 94, 18);

        for (int y = 0; y < 3; ++y)
        {
            for (int x = 0; x < 3; ++x)
            {
                int index = craftInputSlot1 + x + (y * 3);
                guiItemStacks.init(index, true, x * 18, y * 18);
            }
        }

        if(recipeWrapper instanceof ClonerRecipeWrapper)
        {
            ClonerRecipeWrapper clonerwrapper = recipeWrapper;
            if(clonerwrapper.isVanilla())
            {
                ICraftingRecipeWrapper wrapper = recipeWrapper;
                craftingGridHelper.setInput(guiItemStacks, wrapper.getInputs());
                craftingGridHelper.setOutput(guiItemStacks, wrapper.getOutputs());
            }
            else
            {
                ICraftingRecipeWrapper wrapper = recipeWrapper;
                craftingGridHelper.setInput(guiItemStacks, wrapper.getInputs());
                
            }
        }
        else
        {
            Log.error("RecipeWrapper is not a known crafting wrapper type: {}", recipeWrapper);
        }
    }

}
