package pokecube.compat.jei.ingredients;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.ResourceLocation;
import pokecube.core.database.PokedexEntry;
import pokecube.core.events.handlers.EventsHandlerClient;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;

public class PokedexEntryIngredientRenderer implements IIngredientRenderer<PokedexEntry>
{
    // Pokemobs that don't have an icon.
    Set<PokedexEntry>                   manualRender = Sets.newHashSet();
    // Has icons, so don't check.
    Map<PokedexEntry, ResourceLocation> iconRender   = Maps.newHashMap();

    @Override
    public void render(Minecraft minecraft, int x, int y, PokedexEntry entry)
    {
        if (entry == null) return;

        if (manualRender.contains(entry))
        {
            IPokemob pokemob = EventsHandlerClient.renderMobs.get(entry);
            if (pokemob == null)
            {
                pokemob = CapabilityPokemob.getPokemobFor(PokecubeMod.core.createPokemob(entry, minecraft.world));
                if (pokemob == null) return;
                EventsHandlerClient.renderMobs.put(entry, pokemob);
            }
            GL11.glPushMatrix();
            GL11.glTranslated(x + 8, y + 17, 10);
            double scale = 1.1;
            GL11.glScaled(scale, scale, scale);
            EntityLiving entity = pokemob.getEntity();
            float size = 0;
            float mobScale = pokemob.getSize();
            size = Math.max(pokemob.getPokedexEntry().width * mobScale,
                    Math.max(pokemob.getPokedexEntry().height * mobScale, pokemob.getPokedexEntry().length * mobScale));
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
            Minecraft.getMinecraft().getRenderManager().doRenderEntity(entity, 0, 0, 0, 0, 1.5F, false);
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

            ResourceLocation tex = iconRender.get(entry);
            if (tex == null)
            {
                String texture = entry.getModId() + ":"
                        + entry.getTexture((byte) 0).replace("/entity/", "/entity_icon/");
                tex = new ResourceLocation(texture);
                try
                {
                    Minecraft.getMinecraft().getResourceManager().getResource(tex).getInputStream().close();
                }
                catch (IOException e)
                {
                    manualRender.add(entry);
                    return;
                }
                iconRender.put(entry, tex);
            }
            int colour = 0xFFFFFFFF;

            int left = x;
            int right = left + 16;
            int top = y;
            int bottom = y + 16;

            if (left < right)
            {
                int i = left;
                left = right;
                right = i;
            }

            if (top < bottom)
            {
                int j = top;
                top = bottom;
                bottom = j;
            }
            minecraft.getTextureManager().bindTexture(tex);
            float f3 = (float) (colour >> 24 & 255) / 255.0F;
            float f = (float) (colour >> 16 & 255) / 255.0F;
            float f1 = (float) (colour >> 8 & 255) / 255.0F;
            float f2 = (float) (colour & 255) / 255.0F;
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuffer();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                    GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
                    GlStateManager.DestFactor.ZERO);
            GlStateManager.color(f, f1, f2, f3);
            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
            bufferbuilder.pos((double) left, (double) bottom, 0.0D).tex(0, 0).endVertex();
            bufferbuilder.pos((double) right, (double) bottom, 0.0D).tex(1, 0).endVertex();
            bufferbuilder.pos((double) right, (double) top, 0.0D).tex(1, 1).endVertex();
            bufferbuilder.pos((double) left, (double) top, 0.0D).tex(0, 1).endVertex();
            tessellator.draw();
            GlStateManager.disableBlend();
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
