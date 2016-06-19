package pokecube.compat.jei;

import java.awt.Rectangle;
import java.util.List;

import com.google.common.collect.Lists;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.IItemRegistry;
import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.IRecipeRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.gui.IAdvancedGuiHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferRegistry;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import pokecube.adventures.blocks.cloner.ContainerCloner;
import pokecube.adventures.client.gui.GuiCloner;
import pokecube.compat.jei.cloner.ClonerRecipe;
import pokecube.compat.jei.cloner.ClonerRecipeCategory;
import pokecube.compat.jei.cloner.ClonerRecipeHandler;
import pokecube.core.PokecubeItems;
import pokecube.core.client.gui.blocks.GuiPC;
import pokecube.core.database.Database;

@JEIPlugin
public class JEICompat implements IModPlugin
{
    public static final String CLONER = "pokecube_adventures.cloner";

    static boolean added = false;
    public static List<Rectangle> getPCModuleAreas(GuiPC gui)
    {
        List<Rectangle> retList = Lists.newArrayList();
        retList.add(new Rectangle(gui.guiLeft, gui.guiTop, gui.xSize + 50, 50));
        return retList;
    }

    IItemRegistry itemRegistry;

    IJeiHelpers   jeiHelpers;

    @Override
    public void onItemRegistryAvailable(IItemRegistry itemRegistry)
    {
        this.itemRegistry = itemRegistry;
    }

    @Override
    public void onJeiHelpersAvailable(IJeiHelpers jeiHelpers)
    {
        this.jeiHelpers = jeiHelpers;
    }

    @Override
    public void onRecipeRegistryAvailable(IRecipeRegistry recipeRegistry)
    {
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime)
    {
    }
    
    @Override
    public void register(IModRegistry registry)
    {
        if (added) return;
        added = true;
        IGuiHelper guiHelper = jeiHelpers.getGuiHelper();
        registry.addRecipeCategories(new ClonerRecipeCategory(guiHelper));
        registry.addRecipeHandlers(new ClonerRecipeHandler());
        registry.addRecipeClickArea(GuiCloner.class, 88, 32, 28, 23, CLONER);

        IRecipeTransferRegistry recipeTransferRegistry = registry.getRecipeTransferRegistry();
        recipeTransferRegistry.addRecipeTransferHandler(ContainerCloner.class, CLONER, 1, 9, 10, 36);

        List<IRecipe> recipes = CraftingManager.getInstance().getRecipeList();
        List<IRecipe> newRecipes = Lists.newArrayList();
        for (IRecipe recipe : recipes)
        {
            ClonerRecipe newRecipe = null;
            if (recipe instanceof ShapelessRecipes)
                newRecipe = new ClonerRecipe(recipe.getRecipeOutput(), ((ShapelessRecipes) recipe).recipeItems, true);
            if (recipe instanceof ShapedRecipes) newRecipe = new ClonerRecipe(recipe.getRecipeOutput(),
                    Lists.newArrayList(((ShapedRecipes) recipe).recipeItems), true);
            if (newRecipe != null) newRecipes.add(newRecipe);
        }
        for (ItemStack stack : PokecubeItems.fossils.keySet())
        {
            Integer i = PokecubeItems.fossils.get(stack);
            if (Database.entryExists(i))
            {
                ClonerRecipe newRecipe = new ClonerRecipe(stack, Lists.newArrayList(stack), i, 20000);
                newRecipes.add(newRecipe);
            }
        }
        ItemStack egg = PokecubeItems.getStack("pokemobEgg");
        ItemStack mewhair = PokecubeItems.getStack("mewHair");
        ItemStack ironBlock = new ItemStack(Blocks.iron_block);
        ItemStack redstoneBlock = new ItemStack(Blocks.redstone_block);
        ItemStack diamondBlock = new ItemStack(Blocks.diamond_block);
        ItemStack dome = PokecubeItems.getStack("kabuto");
        ItemStack potion = new ItemStack(Items.potionitem, 1, Short.MAX_VALUE);

        ClonerRecipe newRecipe = new ClonerRecipe(egg, Lists.newArrayList(mewhair, egg, potion), 132, 10000);
        newRecipes.add(newRecipe);
        potion = new ItemStack(Items.potionitem, 1, 8225);
        newRecipe = new ClonerRecipe(egg, Lists.newArrayList(ironBlock, redstoneBlock, diamondBlock, dome, potion), 649,
                30000);
        newRecipes.add(newRecipe);

        registry.addRecipes(newRecipes);

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
