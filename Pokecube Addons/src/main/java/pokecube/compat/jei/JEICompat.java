package pokecube.compat.jei;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.IItemRegistry;
import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.IRecipeRegistry;
import mezz.jei.api.JEIPlugin;
import pokecube.adventures.client.gui.GuiCloner;
import pokecube.compat.jei.cloner.ClonerRecipeCategory;

@JEIPlugin
public class JEICompat implements IModPlugin
{
    public static final String CLONER = "pokecube_adventures.cloner";
    private IItemRegistry      itemRegistry;
    private IJeiHelpers        jeiHelpers;

    @Override
    public void onJeiHelpersAvailable(IJeiHelpers jeiHelpers)
    {
        this.jeiHelpers = jeiHelpers;
    }

    @Override
    public void onItemRegistryAvailable(IItemRegistry itemRegistry)
    {
        this.itemRegistry = itemRegistry;
    }

    @Override
    public void register(IModRegistry registry)
    {
        IGuiHelper guiHelper = jeiHelpers.getGuiHelper();
        Thread.dumpStack();
        registry.addRecipeCategories(new ClonerRecipeCategory(guiHelper));
        registry.addRecipeClickArea(GuiCloner.class, 88, 32, 28, 23, CLONER);
    }

    @Override
    public void onRecipeRegistryAvailable(IRecipeRegistry recipeRegistry)
    {
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime)
    {
    }

}
