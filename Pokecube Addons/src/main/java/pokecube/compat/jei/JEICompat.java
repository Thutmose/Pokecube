package pokecube.compat.jei;

import java.awt.Color;
import java.util.List;
import java.util.Set;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.ISubtypeRegistry;
import mezz.jei.api.ISubtypeRegistry.ISubtypeInterpreter;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IModIngredientRegistration;
import mezz.jei.api.recipe.transfer.IRecipeTransferRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.EntityLiving;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.blocks.cloner.container.ContainerCloner;
import pokecube.adventures.blocks.cloner.recipe.RecipeFossilRevive;
import pokecube.adventures.client.gui.cloner.GuiCloner;
import pokecube.compat.jei.cloner.ClonerRecipeCategory;
import pokecube.compat.jei.cloner.ClonerRecipeHandler;
import pokecube.compat.jei.pokemobs.PokemobCategory;
import pokecube.compat.jei.pokemobs.PokemobRecipe;
import pokecube.compat.jei.pokemobs.PokemobRecipeHandler;
import pokecube.core.PokecubeItems;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.PokedexEntry.EvolutionData;
import pokecube.core.events.handlers.EventsHandlerClient;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.utils.PokeType;

@JEIPlugin
public class JEICompat implements IModPlugin
{
    public static final String                            REANIMATOR               = "pokecube_adventures.reanimator";
    public static final String                            POKEMOB                  = "pokecube_adventures.pokemobs";

    public static final ResourceLocation                  TABS                     = new ResourceLocation(
            PokecubeAdv.ID, "textures/gui/jeitabs.png");

    public static final IIngredientHelper<PokedexEntry>   ingredientHelper         = new IIngredientHelper<PokedexEntry>()
                                                                                   {
                                                                                       @Override
                                                                                       public List<PokedexEntry> expandSubtypes(
                                                                                               List<PokedexEntry> ingredients)
                                                                                       {
                                                                                           return ingredients;
                                                                                       }

                                                                                       @Override
                                                                                       public PokedexEntry getMatch(
                                                                                               Iterable<PokedexEntry> ingredients,
                                                                                               final PokedexEntry ingredientToMatch)
                                                                                       {
                                                                                           for (PokedexEntry e : ingredients)
                                                                                           {
                                                                                               if (e == ingredientToMatch) { return e; }
                                                                                           }
                                                                                           return null;
                                                                                       }

                                                                                       @Override
                                                                                       public String getDisplayName(
                                                                                               PokedexEntry ingredient)
                                                                                       {
                                                                                           return ingredient.getName();
                                                                                       }

                                                                                       @Override
                                                                                       public String getUniqueId(
                                                                                               PokedexEntry ingredient)
                                                                                       {
                                                                                           return ingredient.getName();
                                                                                       }

                                                                                       @Override
                                                                                       public String getWildcardId(
                                                                                               PokedexEntry ingredient)
                                                                                       {
                                                                                           return "pokemob";
                                                                                       }

                                                                                       @Override
                                                                                       public String getModId(
                                                                                               PokedexEntry ingredient)
                                                                                       {
                                                                                           return "pokecube";
                                                                                       }

                                                                                       @Override
                                                                                       public Iterable<Color> getColors(
                                                                                               PokedexEntry ingredient)
                                                                                       {
                                                                                           List<Color> colours = Lists
                                                                                                   .newArrayList();
                                                                                           if (ingredient
                                                                                                   .getType1() != PokeType.unknown)
                                                                                               colours.add(new Color(
                                                                                                       ingredient
                                                                                                               .getType1().colour));
                                                                                           if (ingredient
                                                                                                   .getType2() != PokeType.unknown)
                                                                                               colours.add(new Color(
                                                                                                       ingredient
                                                                                                               .getType2().colour));
                                                                                           return colours;
                                                                                       }

                                                                                       @Override
                                                                                       public String getErrorInfo(
                                                                                               PokedexEntry ingredient)
                                                                                       {
                                                                                           return ingredient.getName();
                                                                                       }
                                                                                   };

    public static final IIngredientRenderer<PokedexEntry> ingredientRendererInput  = new IIngredientRenderer<PokedexEntry>()
                                                                                   {

                                                                                       @Override
                                                                                       public void render(
                                                                                               Minecraft minecraft,
                                                                                               int x,
                                                                                               int y,
                                                                                               PokedexEntry entry)
                                                                                       {
                                                                                           if (entry == null) return;

                                                                                           IPokemob pokemob = EventsHandlerClient.renderMobs
                                                                                                   .get(entry);
                                                                                           if (pokemob == null)
                                                                                           {
                                                                                               pokemob = (IPokemob) PokecubeMod.core
                                                                                                       .createPokemob(
                                                                                                               entry,
                                                                                                               minecraft.world);
                                                                                               if (pokemob == null)
                                                                                                   return;
                                                                                               EventsHandlerClient.renderMobs
                                                                                                       .put(entry,
                                                                                                               pokemob);
                                                                                           }
                                                                                           GL11.glPushMatrix();
                                                                                           GL11.glTranslated(
                                                                                                   x + 8,
                                                                                                   y + 17, 10);
                                                                                           double scale = 1.1;
                                                                                           GL11.glScaled(scale, scale,
                                                                                                   scale);
                                                                                           EntityLiving entity = (EntityLiving) pokemob;

                                                                                           float size = 0;

                                                                                           float mobScale = pokemob
                                                                                                   .getSize();
                                                                                           size = Math.max(
                                                                                                   pokemob.getPokedexEntry().width
                                                                                                           * mobScale,
                                                                                                   Math.max(
                                                                                                           pokemob.getPokedexEntry().height
                                                                                                                   * mobScale,
                                                                                                           pokemob.getPokedexEntry().length
                                                                                                                   * mobScale));

                                                                                           GL11.glPushMatrix();
                                                                                           float zoom = (float) (12f
                                                                                                   / Math.pow(size,
                                                                                                           0.7));
                                                                                           GL11.glScalef(-zoom, zoom,
                                                                                                   zoom);
                                                                                           GL11.glRotatef(180F, 0.0F,
                                                                                                   0.0F, 1.0F);
                                                                                           entity.rotationYawHead = entity.prevRotationYawHead;
                                                                                           RenderHelper
                                                                                                   .enableStandardItemLighting();

                                                                                           GL11.glTranslatef(0.0F,
                                                                                                   (float) entity
                                                                                                           .getYOffset(),
                                                                                                   0.0F);

                                                                                           int i = 15728880;
                                                                                           int j1 = i % 65536;
                                                                                           int k1 = i / 65536;
                                                                                           OpenGlHelper
                                                                                                   .setLightmapTextureCoords(
                                                                                                           OpenGlHelper.lightmapTexUnit,
                                                                                                           j1 / 1.0F,
                                                                                                           k1 / 1.0F);
                                                                                           Minecraft.getMinecraft()
                                                                                                   .getRenderManager()
                                                                                                   .doRenderEntity(
                                                                                                           entity, 0, 0,
                                                                                                           0, 0, 1.5F,
                                                                                                           false);
                                                                                           RenderHelper
                                                                                                   .disableStandardItemLighting();
                                                                                           GlStateManager.disableRescaleNormal();
                                                                                           GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
                                                                                           GlStateManager.disableTexture2D();
                                                                                           GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
                                                                                           GL11.glPopMatrix();
                                                                                           GL11.glPopMatrix();
                                                                                       }

                                                                                       @Override
                                                                                       public List<String> getTooltip(
                                                                                               Minecraft minecraft,
                                                                                               PokedexEntry ingredient)
                                                                                       {
                                                                                           return Lists.newArrayList(
                                                                                                   ingredient
                                                                                                           .getName());
                                                                                       }

                                                                                       @Override
                                                                                       public FontRenderer getFontRenderer(
                                                                                               Minecraft minecraft,
                                                                                               PokedexEntry ingredient)
                                                                                       {
                                                                                           return minecraft.fontRenderer;
                                                                                       }
                                                                                   };
    public static final IIngredientRenderer<PokedexEntry> ingredientRendererOutput = new IIngredientRenderer<PokedexEntry>()
                                                                                   {

                                                                                       @Override
                                                                                       public void render(
                                                                                               Minecraft minecraft,
                                                                                               int x,
                                                                                               int y,
                                                                                               PokedexEntry entry)
                                                                                       {
                                                                                           if (entry == null) return;

                                                                                           IPokemob pokemob = EventsHandlerClient.renderMobs
                                                                                                   .get(entry);
                                                                                           if (pokemob == null)
                                                                                           {
                                                                                               pokemob = (IPokemob) PokecubeMod.core
                                                                                                       .createPokemob(
                                                                                                               entry,
                                                                                                               minecraft.world);
                                                                                               if (pokemob == null)
                                                                                                   return;
                                                                                               EventsHandlerClient.renderMobs
                                                                                                       .put(entry,
                                                                                                               pokemob);
                                                                                           }
                                                                                           GL11.glPushMatrix();
                                                                                           GL11.glTranslated(
                                                                                                   x + 12,
                                                                                                   y + 22, 10);
                                                                                           double scale = 1.375;
                                                                                           GL11.glScaled(scale, scale,
                                                                                                   scale);
                                                                                           EntityLiving entity = (EntityLiving) pokemob;

                                                                                           float size = 0;

                                                                                           float mobScale = pokemob
                                                                                                   .getSize();
                                                                                           size = Math.max(
                                                                                                   pokemob.getPokedexEntry().width
                                                                                                           * mobScale,
                                                                                                   Math.max(
                                                                                                           pokemob.getPokedexEntry().height
                                                                                                                   * mobScale,
                                                                                                           pokemob.getPokedexEntry().length
                                                                                                                   * mobScale));

                                                                                           GL11.glPushMatrix();
                                                                                           float zoom = (float) (12f
                                                                                                   / Math.pow(size,
                                                                                                           0.7));
                                                                                           GL11.glScalef(-zoom, zoom,
                                                                                                   zoom);
                                                                                           GL11.glRotatef(180F, 0.0F,
                                                                                                   0.0F, 1.0F);
                                                                                           entity.rotationYawHead = entity.prevRotationYawHead;
                                                                                           RenderHelper
                                                                                                   .enableStandardItemLighting();

                                                                                           GL11.glTranslatef(0.0F,
                                                                                                   (float) entity
                                                                                                           .getYOffset(),
                                                                                                   0.0F);

                                                                                           int i = 15728880;
                                                                                           int j1 = i % 65536;
                                                                                           int k1 = i / 65536;
                                                                                           OpenGlHelper
                                                                                                   .setLightmapTextureCoords(
                                                                                                           OpenGlHelper.lightmapTexUnit,
                                                                                                           j1 / 1.0F,
                                                                                                           k1 / 1.0F);
                                                                                           Minecraft.getMinecraft()
                                                                                                   .getRenderManager()
                                                                                                   .doRenderEntity(
                                                                                                           entity, 0, 0,
                                                                                                           0, 0, 1.5F,
                                                                                                           false);
                                                                                           RenderHelper
                                                                                                   .disableStandardItemLighting();
                                                                                           GL11.glPopMatrix();
                                                                                           GL11.glPopMatrix();
                                                                                       }

                                                                                       @Override
                                                                                       public List<String> getTooltip(
                                                                                               Minecraft minecraft,
                                                                                               PokedexEntry ingredient)
                                                                                       {
                                                                                           return Lists.newArrayList(
                                                                                                   ingredient
                                                                                                           .getName());
                                                                                       }

                                                                                       @Override
                                                                                       public FontRenderer getFontRenderer(
                                                                                               Minecraft minecraft,
                                                                                               PokedexEntry ingredient)
                                                                                       {
                                                                                           return minecraft.fontRenderer;
                                                                                       }
                                                                                   };

    static boolean                                        added                    = false;

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime)
    {
    }

    @Override
    public void register(IModRegistry registry)
    {
        System.out.println("JEI INIT RECIPES");
        IGuiHelper guiHelper = registry.getJeiHelpers().getGuiHelper();
        registry.addRecipeCategories(new ClonerRecipeCategory(guiHelper));
        registry.addRecipeHandlers(new ClonerRecipeHandler());
        registry.addRecipeClickArea(GuiCloner.class, 88, 32, 28, 23, REANIMATOR);
        registry.addRecipeCategories(new PokemobCategory(guiHelper));
        registry.addRecipeHandlers(new PokemobRecipeHandler());

        List<PokemobRecipe> recipes = Lists.newArrayList();
        for (PokedexEntry e : Database.allFormes)
        {
            if (e.evolutions != null)
            {
                for (EvolutionData d : e.evolutions)
                {
                    if (d.evolution == null || d.preEvolution == null) continue;
                    recipes.add(new PokemobRecipe(d));
                }
            }
        }
        registry.addRecipes(recipes);
        IRecipeTransferRegistry recipeTransferRegistry = registry.getRecipeTransferRegistry();
        recipeTransferRegistry.addRecipeTransferHandler(ContainerCloner.class, REANIMATOR, 1, 9, 10, 36);
        registry.addRecipes(RecipeFossilRevive.getRecipeList());
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistry subtypeRegistry)
    {
        // TODO Auto-generated method stub
        Item item = PokecubeItems.megastone;
        subtypeRegistry.registerSubtypeInterpreter(item, new ISubtypeInterpreter()
        {
            @Override
            public String getSubtypeInfo(ItemStack itemStack)
            {
                if (itemStack.hasTagCompound()) return itemStack.getTagCompound().getString("pokemon");
                return null;
            }
        });
        item = PokecubeItems.held;
        subtypeRegistry.registerSubtypeInterpreter(item, new ISubtypeInterpreter()
        {
            @Override
            public String getSubtypeInfo(ItemStack itemStack)
            {
                if (itemStack.hasTagCompound()) return itemStack.getTagCompound().getString("type");
                return null;
            }
        });
        item = PokecubeItems.fossil;
        subtypeRegistry.registerSubtypeInterpreter(item, new ISubtypeInterpreter()
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
        System.out.println("JEI INIT INGREDIENTS");
        Set<PokedexEntry> relevant = Sets.newHashSet();
        for (PokedexEntry e : Database.allFormes)
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
            relevant.add(r.getPokedexEntry());
        }
        registry.register(PokedexEntry.class, relevant, ingredientHelper, ingredientRendererInput);
    }
}
