package pokecube.compat.jei.ingredients;

import java.util.List;

import javax.vecmath.Vector3f;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;

import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLiving;
import pokecube.adventures.commands.Config;
import pokecube.core.PokecubeCore;
import pokecube.core.database.PokedexEntry;
import pokecube.core.events.handlers.EventsHandlerClient;
import pokecube.core.interfaces.IPokemob;

public class PokedexEntryIngredientRenderer implements IIngredientRenderer<PokedexEntry>
{
    @Override
    public void render(Minecraft minecraft, int x, int y, PokedexEntry entry)
    {
        if (entry == null) return;
        if (Config.instance.jeiModels)
        {
            IPokemob pokemob = EventsHandlerClient.getRenderMob(entry, PokecubeCore.proxy.getWorld());
            if (pokemob == null)
            {
                System.err.println("Error rendering " + entry);
                return;
            }
            GL11.glPushMatrix();
            GL11.glTranslated(x + 8, y + 17, 10);
            double scale = 1.1;
            GL11.glScaled(scale, scale, scale);
            EntityLiving entity = pokemob.getEntity();
            float size = 0;
            float mobScale = pokemob.getSize();
            Vector3f dims = pokemob.getPokedexEntry().getModelSize();
            size = Math.max(dims.z * mobScale, Math.max(dims.y * mobScale, dims.x * mobScale));
            GL11.glPushMatrix();
            float zoom = (float) (12f / Math.pow(size, 0.7));
            GL11.glScalef(-zoom, zoom, zoom);
            GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
            entity.rotationYawHead = entity.prevRotationYawHead;
            RenderHelper.enableStandardItemLighting();
            GL11.glTranslatef(0.0F, (float) entity.getYOffset(), 0.0F);
            int i = 15728880;
            int j1 = i % 65536;
            int k1 = i / 65536;
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, j1 / 1.0F, k1 / 1.0F);
            Minecraft.getMinecraft().getRenderManager().renderEntity(entity, 0, 0, 0, 0, 1.5F, false);
            RenderHelper.disableStandardItemLighting();
            GlStateManager.disableRescaleNormal();
            GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
            GlStateManager.disableTexture2D();
            GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
            GL11.glPopMatrix();
            GL11.glPopMatrix();
        }
        else
        {
            EventsHandlerClient.renderIcon(entry, x, y, 16, 16, false);
        }
    }

    @Override
    public List<String> getTooltip(Minecraft minecraft, PokedexEntry ingredient, ITooltipFlag tooltipFlag)
    {
        return Lists.newArrayList(ingredient.getName());
    }

    @Override
    public FontRenderer getFontRenderer(Minecraft minecraft, PokedexEntry ingredient)
    {
        return minecraft.fontRenderer;
    }

}
