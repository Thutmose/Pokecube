package pokecube.adventures.client.render.blocks;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import pokecube.adventures.blocks.cloner.ClonerCraftMatrix;
import pokecube.adventures.blocks.cloner.RecipeFossilRevive;
import pokecube.adventures.blocks.cloner.TileEntityCloner;
import pokecube.core.PokecubeCore;
import pokecube.core.database.PokedexEntry;
import pokecube.core.events.handlers.EventsHandlerClient;
import pokecube.core.interfaces.IPokemob;
import thut.api.entity.IMobColourable;

public class RenderCloner extends TileEntitySpecialRenderer<TileEntityCloner>
{

    @Override
    public void renderTileEntityAt(TileEntityCloner te, double x, double y, double z, float partialTicks,
            int destroyStage)
    {
        ClonerCraftMatrix matrix = te.craftMatrix;
        RecipeFossilRevive currentRecipe = null;
        ;
        for (RecipeFossilRevive recipe : RecipeFossilRevive.getRecipeList())
        {
            if (recipe.matches(matrix, getWorld()))
            {
                currentRecipe = recipe;
                break;
            }
        }
        if (currentRecipe == null) return;
        PokedexEntry entry = currentRecipe.pokedexEntry;
        IPokemob pokemob = EventsHandlerClient.renderMobs.get(entry);
        if (pokemob == null)
        {
            pokemob = (IPokemob) PokecubeCore.instance.createPokemob(entry, getWorld());
            if (pokemob == null) return;
            EventsHandlerClient.renderMobs.put(entry, pokemob);
        }
        GL11.glPushMatrix();

        GL11.glTranslatef((float) x + 0.5F, (float) y, (float) z + 0.5F);
        GL11.glPushMatrix();
        float offset = 0.2f;

        float dx, dy, dz;
        dx = 0;
        dy = 0.65f;
        dz = 0;

        float max = Math.max(entry.width, entry.length);

        float scale = (float) (0.5f / Math.sqrt(max));
        GL11.glTranslatef(dx, offset + dy, dz);

        GL11.glRotatef(180, 0, 0, 1);
        GL11.glColor4f(1, 1, 1, 0.5f);
        GL11.glScaled(0.0625 * scale, 0.0625 * scale, 0.0625 * scale);
        if (pokemob instanceof IMobColourable)
        {
            int[] col = ((IMobColourable) pokemob).getRGBA();
            col[3] = 128;
            ((IMobColourable) pokemob).setRGBA(col);
        }
        EventsHandlerClient.renderMob(pokemob, partialTicks, true);
        GL11.glPopMatrix();
        GL11.glTranslatef(0.405f, 0.645f, -0.5f);
        GL11.glScaled(0.15, 0.15, 0.15);
        GL11.glPopMatrix();
    }

}
