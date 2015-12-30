package pokecube.adventures.client.render.blocks;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import pokecube.adventures.blocks.afa.TileEntityAFA;
import pokecube.core.events.handlers.EventsHandlerClient;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.items.pokecubes.PokecubeManager;

public class RenderAFA extends TileEntitySpecialRenderer<TileEntityAFA>
{

    @Override
    public void renderTileEntityAt(TileEntityAFA te, double x, double y, double z, float partialTicks, int destroyStage)
    {
        GL11.glPushMatrix();
        GL11.glPushMatrix();
        GL11.glTranslatef((float) x + 0.5F, (float) y + 1.15F, (float) z + 0.5F);
        GL11.glRotatef(180, 0, 0, 1);
        GL11.glColor4f(1, 1, 1, 0.5f);
        GL11.glScaled(0.125, 0.125, 0.125);
        
        ItemStack stack = te.getStackInSlot(0);
        if (stack != null && PokecubeManager.isFilled(stack))
        {
            IPokemob mob = PokecubeManager.itemToPokemob(stack, getWorld());
            if (mob != null) EventsHandlerClient.renderMob(mob, partialTicks);
        }

        GL11.glPopMatrix();
        GL11.glPopMatrix();
    }

}
