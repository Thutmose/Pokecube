package pokecube.adventures.client.render.blocks;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
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
        GL11.glPushMatrix();
        float offset = 1.2f;
        GL11.glTranslatef((float) x + 0.5F, (float) y + offset, (float) z + 0.5F);

        GL11.glRotatef(180, 0, 0, 1);
        GL11.glColor4f(1, 1, 1, 0.5f);
        GL11.glScaled(0.0625, 0.0625, 0.0625);
        if (mob instanceof IMobColourable)
        {
            int[] col = ((IMobColourable) mob).getRGBA();
            col[3] = 127;
            ((IMobColourable) mob).setRGBA(col);
        }
        EventsHandlerClient.renderMob(mob, partialTicks);

        GL11.glPopMatrix();
        GL11.glPopMatrix();
    }

}
