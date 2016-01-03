package pokecube.adventures.client.render.blocks;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import pokecube.adventures.blocks.afa.TileEntityAFA;
import pokecube.core.events.handlers.EventsHandlerClient;
import pokecube.core.interfaces.IMobColourable;
import pokecube.core.interfaces.IPokemob;

public class RenderAFA extends TileEntitySpecialRenderer<TileEntityAFA>
{

    @Override
    public void renderTileEntityAt(TileEntityAFA te, double x, double y, double z, float partialTicks, int destroyStage)
    {
        IPokemob mob = te.pokemob;
        if (mob == null) return;

        GL11.glPushMatrix();

        GL11.glTranslatef((float) x + 0.5F, (float) y, (float) z + 0.5F);
        GL11.glPushMatrix();
        float offset = 0.2f;
        
        float dx, dy, dz;
        dx = te.shift[0]/100f;
        dy = te.shift[1]/100f;
        dz = te.shift[2]/100f;
        float scale = te.scale/1000f;
        GL11.glTranslatef(dx, offset + dy, dz);

        GL11.glRotatef(180, 0, 0, 1);
        GL11.glColor4f(1, 1, 1, 0.5f);
        GL11.glScaled(0.0625 * scale, 0.0625 * scale, 0.0625 * scale);
        if (mob instanceof IMobColourable)
        {
            int[] col = ((IMobColourable) mob).getRGBA();
            col[3] = 127;
            ((IMobColourable) mob).setRGBA(col);
        }
        EventsHandlerClient.renderMob(mob, partialTicks);
        
        GL11.glPopMatrix();
        GL11.glTranslatef(0.405f, 0.645f, -0.5f);
        GL11.glScaled(0.15, 0.15, 0.15);

        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        ItemStack item = te.getStackInSlot(0);
        Minecraft.getMinecraft().getItemRenderer().renderItem(player, item, null);//TransformType.NONE);
        GL11.glPopMatrix();
    }

}
