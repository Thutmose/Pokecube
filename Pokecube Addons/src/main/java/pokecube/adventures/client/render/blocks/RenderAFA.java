package pokecube.adventures.client.render.blocks;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import pokecube.adventures.blocks.afa.TileEntityAFA;
import pokecube.core.client.render.entity.RenderAdvancedPokemobModel;
import pokecube.core.client.render.entity.RenderPokemobs;
import pokecube.core.events.handlers.EventsHandlerClient;
import pokecube.core.interfaces.IPokemob;
import thut.api.entity.IMobColourable;

public class RenderAFA extends TileEntitySpecialRenderer<TileEntityAFA>
{

    @Override
    public void render(TileEntityAFA te, double x, double y, double z, float partialTicks, int destroyStage, float f)
    {
        IPokemob mob = te.pokemob;
        if (mob == null) return;

        GL11.glPushMatrix();

        GL11.glTranslatef((float) x + 0.5F, (float) y, (float) z + 0.5F);
        GL11.glPushMatrix();

        GlStateManager.pushMatrix();

        GL11.glTranslatef(0.405f, 0.645f, -0.5f);
        GL11.glScaled(0.15, 0.15, 0.15);

        EntityPlayer player = Minecraft.getMinecraft().player;
        ItemStack item = te.getStackInSlot(0);
        Minecraft.getMinecraft().getItemRenderer().renderItem(player, item, null);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.popMatrix();

        float offset = 0.2f;

        float dx, dy, dz;
        dx = te.shift[0] / 100f;
        dy = te.shift[1] / 100f;
        dz = te.shift[2] / 100f;
        float scale = te.scale / 1000f;
        GL11.glTranslatef(dx, offset + dy, dz);

        GL11.glRotatef(180, 0, 0, 1);
        GL11.glColor4f(1, 1, 1, 0.5f);
        GL11.glScaled(0.0625 * scale, 0.0625 * scale, 0.0625 * scale);
        if (mob.getEntity() instanceof IMobColourable)
        {
            int[] col = ((IMobColourable) mob.getEntity()).getRGBA();
            col[3] = te.transparency;
            ((IMobColourable) mob.getEntity()).setRGBA(col);
        }
        if (!te.rotates)
        {
            GL11.glRotatef(te.angle, 0, 1, 0);
        }
        if (te.frozen)
        {
            // partialTicks = te.animationTime % 1;
            partialTicks = 0;
            mob.getEntity().ticksExisted = (int) te.animationTime;
            mob.getEntity().limbSwing = 0;
            mob.getEntity().limbSwingAmount = 0;
            mob.getEntity().prevLimbSwingAmount = 0;
        }

        GlStateManager.enableDepth();
        GlStateManager.enableCull();
        Object o;
        if ((o = RenderPokemobs.getInstance().getRenderer(mob.getPokedexEntry())) instanceof RenderAdvancedPokemobModel)
        {
            RenderAdvancedPokemobModel<?> render = (RenderAdvancedPokemobModel<?>) o;
            render.anim = te.animation;
            render.overrideAnim = true;
        }
        EventsHandlerClient.renderMob(mob, partialTicks, te.rotates);
        if ((o = RenderPokemobs.getInstance().getRenderer(mob.getPokedexEntry())) instanceof RenderAdvancedPokemobModel)
        {
            RenderAdvancedPokemobModel<?> render = (RenderAdvancedPokemobModel<?>) o;
            render.anim = "";
            render.overrideAnim = false;
        }
        GL11.glPopMatrix();
        GL11.glPopMatrix();
    }

}
