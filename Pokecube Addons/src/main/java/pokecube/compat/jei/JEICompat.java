package pokecube.compat.jei;

import java.awt.Rectangle;
import java.util.List;

import com.google.common.collect.Lists;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.IItemRegistry;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.ISubtypeRegistry;
import mezz.jei.api.ISubtypeRegistry.ISubtypeInterpreter;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.gui.IAdvancedGuiHandler;
import mezz.jei.api.ingredients.IModIngredientRegistration;
import mezz.jei.api.recipe.transfer.IRecipeTransferRegistry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import pokecube.adventures.blocks.cloner.ContainerCloner;
import pokecube.adventures.blocks.cloner.RecipeFossilRevive;
import pokecube.adventures.client.gui.GuiCloner;
import pokecube.compat.jei.cloner.ClonerRecipeCategory;
import pokecube.compat.jei.cloner.ClonerRecipeHandler;
import pokecube.core.PokecubeItems;
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

    @Override
    public void registerItemSubtypes(ISubtypeRegistry subtypeRegistry)
    {
        // TODO Auto-generated method stub
        Item item = PokecubeItems.megastone;
        subtypeRegistry.registerNbtInterpreter(item, new ISubtypeInterpreter()
        {
            @Override
            public String getSubtypeInfo(ItemStack itemStack)
            {
                if (itemStack.hasTagCompound()) return itemStack.getTagCompound().getString("pokemon");
                return null;
            }
        });
        item = PokecubeItems.held;
        subtypeRegistry.registerNbtInterpreter(item, new ISubtypeInterpreter()
        {
            @Override
            public String getSubtypeInfo(ItemStack itemStack)
            {
                if (itemStack.hasTagCompound()) return itemStack.getTagCompound().getString("type");
                return null;
            }
        });
        item = PokecubeItems.fossil;
        subtypeRegistry.registerNbtInterpreter(item, new ISubtypeInterpreter()
        {
            @Override
            public String getSubtypeInfo(ItemStack itemStack)
            {
                if (itemStack.hasTagCompound()) return itemStack.getTagCompound().getString("pokemon");
                return null;
            }
        });
    }

    @Override
    public void registerIngredients(IModIngredientRegistration registry)
    {
        // TODO Auto-generated method stub

    }

}
