package pokecube.compat.jei;

import java.awt.Rectangle;
import java.util.List;

import com.google.common.collect.Lists;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.IItemRegistry;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.gui.IAdvancedGuiHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferRegistry;
import pokecube.adventures.blocks.cloner.RecipeFossilRevive;
import pokecube.adventures.blocks.cloner.ContainerCloner;
import pokecube.adventures.client.gui.GuiCloner;
import pokecube.compat.jei.cloner.ClonerRecipeCategory;
import pokecube.compat.jei.cloner.ClonerRecipeHandler;
import pokecube.core.client.gui.blocks.GuiPC;

@JEIPlugin
public class JEICompat implements IModPlugin
{
    public static final String CLONER = "pokecube_adventures.cloner";

    static boolean             added  = false;

    public static List<Rectangle> getPCModuleAreas(GuiPC gui)
    {
        List<Rectangle> retList = Lists.newArrayList();
        retList.add(new Rectangle(gui.guiLeft, gui.guiTop, gui.xSize + 50, 50));
        return retList;
    }

    IItemRegistry itemRegistry;

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime)
    {
    }

    @Override
    public void register(IModRegistry registry)
    {
        if (added) return;

        added = true;
        IGuiHelper guiHelper = registry.getJeiHelpers().getGuiHelper();
        registry.addRecipeCategories(new ClonerRecipeCategory(guiHelper));
        registry.addRecipeHandlers(new ClonerRecipeHandler());
        registry.addRecipeClickArea(GuiCloner.class, 88, 32, 28, 23, CLONER);

        IRecipeTransferRegistry recipeTransferRegistry = registry.getRecipeTransferRegistry();
        recipeTransferRegistry.addRecipeTransferHandler(ContainerCloner.class, CLONER, 1, 9, 10, 36);
        registry.addRecipes(RecipeFossilRevive.getRecipeList());

        registry.addAdvancedGuiHandlers(new IAdvancedGuiHandler<GuiPC>()
        {
            @Override
            public Class<GuiPC> getGuiContainerClass()
            {
                return GuiPC.class;
            }

            @Override
            public List<Rectangle> getGuiExtraAreas(GuiPC guiContainer)
            {
                return getPCModuleAreas(guiContainer);
            }
        });
    }

}
