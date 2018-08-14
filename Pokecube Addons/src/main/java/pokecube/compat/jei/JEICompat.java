package pokecube.compat.jei;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.ISubtypeRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IModIngredientRegistration;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import mezz.jei.api.recipe.transfer.IRecipeTransferRegistry;
import mezz.jei.config.IngredientBlacklistType;
import mezz.jei.ingredients.IngredientBlacklistInternal;
import mezz.jei.ingredients.IngredientFilter;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.blocks.cloner.container.ContainerCloner;
import pokecube.adventures.blocks.cloner.recipe.RecipeFossilRevive;
import pokecube.adventures.client.gui.cloner.GuiCloner;
import pokecube.compat.jei.cloner.ClonerRecipeCategory;
import pokecube.compat.jei.cloner.ClonerRecipeWrapper;
import pokecube.compat.jei.ingredients.PokedexEntryIngredientHelper;
import pokecube.compat.jei.ingredients.PokedexEntryIngredientRenderer;
import pokecube.compat.jei.pokemobs.evolutions.PokemobCategory;
import pokecube.compat.jei.pokemobs.evolutions.PokemobRecipe;
import pokecube.compat.jei.pokemobs.evolutions.PokemobRecipeWrapper;
import pokecube.compat.jei.pokemobs.interactions.PokemobInteractCategory;
import pokecube.compat.jei.pokemobs.interactions.PokemobInteractRecipe;
import pokecube.compat.jei.pokemobs.interactions.PokemobInteractRecipeWrapper;
import pokecube.compat.jei.pokemobs.moves.PokemobMoveCategory;
import pokecube.compat.jei.pokemobs.moves.PokemobMoveRecipe;
import pokecube.compat.jei.pokemobs.moves.PokemobMoveRecipeWrapper;
import pokecube.core.PokecubeItems;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.PokedexEntry.EvolutionData;
import pokecube.core.database.PokedexEntry.InteractionLogic;
import pokecube.core.database.PokedexEntry.InteractionLogic.Interaction;

@JEIPlugin
public class JEICompat implements IModPlugin
{
    public static final String                            REANIMATOR               = "pokecube_adventures.reanimator";
    public static final String                            POKEMOB                  = "pokecube_adventures.pokemobs";
    public static final String                            POKEMOBINTERACT          = "pokecube_adventures.pokemobs.interact";
    public static final String                            POKEMOBMOVES             = "pokecube_adventures.pokemobs.moves";

    public static final ResourceLocation                  TABS                     = new ResourceLocation(
            PokecubeAdv.ID, "textures/gui/jeitabs.png");

    public static final IIngredientHelper<PokedexEntry>   ingredientHelper         = new PokedexEntryIngredientHelper();
    public static final IIngredientRenderer<PokedexEntry> ingredientRendererInput  = new PokedexEntryIngredientRenderer();
    public static final IIngredientRenderer<PokedexEntry> ingredientRendererOutput = new PokedexEntryIngredientRenderer();

    public static boolean                                 autoHideJEI              = true;

    IModRegistry                                          registry;

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime)
    {
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry)
    {
        IGuiHelper guiHelper = registry.getJeiHelpers().getGuiHelper();
        registry.addRecipeCategories(new ClonerRecipeCategory(guiHelper));
        registry.addRecipeCategories(new PokemobCategory(guiHelper));
        registry.addRecipeCategories(new PokemobInteractCategory(guiHelper));
        registry.addRecipeCategories(new PokemobMoveCategory(guiHelper));
    }

    @Override
    public void register(IModRegistry registry)
    {
        System.out.println("JEI INIT RECIPES");
        this.registry = registry;
        registry.handleRecipes(RecipeFossilRevive.class, recipe -> new ClonerRecipeWrapper(recipe),
                JEICompat.REANIMATOR);
        registry.handleRecipes(PokemobRecipe.class, recipe -> new PokemobRecipeWrapper(recipe), JEICompat.POKEMOB);
        registry.handleRecipes(PokemobInteractRecipe.class, recipe -> new PokemobInteractRecipeWrapper(recipe),
                JEICompat.POKEMOBINTERACT);
        registry.handleRecipes(PokemobMoveRecipe.class,
                recipe -> new PokemobMoveRecipeWrapper(registry.getJeiHelpers(), recipe), JEICompat.POKEMOBMOVES);
        registry.addRecipeClickArea(GuiCloner.class, 88, 32, 28, 23, REANIMATOR);

        List<PokemobRecipe> pokemobEvolRecipes = Lists.newArrayList();
        for (PokedexEntry e : Database.getSortedFormes())
        {
            if (e.evolutions != null)
            {
                for (EvolutionData d : e.evolutions)
                {
                    if (d.evolution == null || d.preEvolution == null) continue;
                    pokemobEvolRecipes.add(new PokemobRecipe(d));
                }
            }
        }

        List<PokemobInteractRecipe> pokemobInteractRecipes = Lists.newArrayList();
        Field interactionLogic;
        try
        {
            interactionLogic = PokedexEntry.class.getDeclaredField("interactionLogic");
            interactionLogic.setAccessible(true);
            for (PokedexEntry e : Database.getSortedFormes())
            {
                try
                {
                    InteractionLogic logic = (InteractionLogic) interactionLogic.get(e);
                    for (Interaction action : logic.actions.values())
                    {
                        PokedexEntry form = action.forme;
                        if (form != null)
                        {
                            PokemobInteractRecipe recipe = new PokemobInteractRecipe(e, action,
                                    ItemStack.EMPTY);
                            if (!pokemobInteractRecipes.contains(recipe)) pokemobInteractRecipes.add(recipe);
                        }
                        for (ItemStack stack : action.stacks)
                        {
                            PokemobInteractRecipe recipe = new PokemobInteractRecipe(e, action, stack);
                            if (!pokemobInteractRecipes.contains(recipe)) pokemobInteractRecipes.add(recipe);
                        }
                    }
                }
                catch (IllegalArgumentException | IllegalAccessException e1)
                {
                    e1.printStackTrace();
                }
            }
        }
        catch (NoSuchFieldException | SecurityException e2)
        {
            e2.printStackTrace();
        }
        List<RecipeFossilRevive> clonerRecipes = RecipeFossilRevive.getRecipeList();
        clonerRecipes.removeIf(new Predicate<RecipeFossilRevive>()
        {
            @Override
            public boolean test(RecipeFossilRevive t)
            {
                return t.recipeItems.isEmpty();
            }
        });
        registry.addRecipes(pokemobEvolRecipes, JEICompat.POKEMOB);
        registry.addRecipes(pokemobInteractRecipes, JEICompat.POKEMOBINTERACT);
        IRecipeTransferRegistry recipeTransferRegistry = registry.getRecipeTransferRegistry();
        recipeTransferRegistry.addRecipeTransferHandler(ContainerCloner.class, REANIMATOR, 1, 9, 10, 36);
        registry.addRecipes(clonerRecipes, JEICompat.REANIMATOR);
        List<PokemobMoveRecipe> list = PokemobMoveRecipe.getRecipes();
        if (!list.isEmpty()) registry.addRecipes(list, JEICompat.POKEMOBMOVES);
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistry subtypeRegistry)
    {
    }

    @Override
    public void registerIngredients(IModIngredientRegistration registry)
    {
        System.out.println("JEI INIT INGREDIENTS");
        Set<PokedexEntry> relevant = Sets.newHashSet();
        for (PokedexEntry e : Database.getSortedFormes())
        {
            if (e.evolutions != null && !e.evolutions.isEmpty())
            {
                relevant.add(e);
                for (EvolutionData d : e.evolutions)
                {
                    if (d.evolution == null || d.preEvolution == null) continue;
                    relevant.add(d.evolution);
                }
            }
        }
        for (ItemStack stack : PokecubeItems.fossils.keySet())
        {
            PokedexEntry i = PokecubeItems.fossils.get(stack);
            relevant.add(i);
        }
        for (RecipeFossilRevive r : RecipeFossilRevive.getRecipeList())
        {
            if (!r.recipeItems.isEmpty()) relevant.add(r.getPokedexEntry());
        }
        Field interactionLogic;
        try
        {
            interactionLogic = PokedexEntry.class.getDeclaredField("interactionLogic");
            interactionLogic.setAccessible(true);
            for (PokedexEntry e : Database.getSortedFormes())
            {
                try
                {
                    InteractionLogic logic = (InteractionLogic) interactionLogic.get(e);
                    if (!logic.actions.isEmpty())
                    {
                        relevant.add(e);
                    }
                }
                catch (Exception e1)
                {

                }
            }
        }
        catch (Exception e2)
        {

        }
        registry.register(PokedexEntry.class, relevant, ingredientHelper, ingredientRendererInput);
        if (autoHideJEI)
        {
            for (PokedexEntry e : relevant)
            {
                IngredientBlacklistInternal blacklist = new IngredientBlacklistInternal();
                blacklist.addIngredientToBlacklist(e, ingredientHelper);
                IngredientFilter filter = new IngredientFilter(blacklist);
                try
                {
                    mezz.jei.config.Config.addIngredientToConfigBlacklist(filter, this.registry.getIngredientRegistry(), e,
                            IngredientBlacklistType.ITEM, ingredientHelper);
                }
                catch (Exception e1)
                {
//                    // TODO Auto-generated catch block
//                    e1.printStackTrace();
                }
            }
        }
    }
}
