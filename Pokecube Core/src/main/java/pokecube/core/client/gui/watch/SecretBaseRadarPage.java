package pokecube.core.client.gui.watch;

import java.io.IOException;
import java.util.Set;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Sets;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import pokecube.core.client.gui.watch.util.PageButton;
import pokecube.core.client.gui.watch.util.WatchPage;
import pokecube.core.world.dimensions.secretpower.SecretBaseManager.Coordinate;
import thut.api.maths.Vector3;
import thut.api.maths.Vector4;

public class SecretBaseRadarPage extends WatchPage
{
    public static Set<Coordinate> bases         = Sets.newHashSet();
    public static Vector4         closestMeteor = null;
    public static float           baseRange     = 64;
    private static boolean        meteors       = false;
    int                           button        = -1;

    public SecretBaseRadarPage(GuiPokeWatch watch)
    {
        super(watch);
        if (meteors)
        {
            this.setTitle(I18n.format("pokewatch.title.meteorradar"));
        }
        else
        {
            this.setTitle(I18n.format("pokewatch.title.baseradar"));
        }
    }

    @Override
    public void onPageOpened()
    {
        super.onPageOpened();
        int x = watch.width / 2;
        int y = watch.height / 2 - 5;
        this.watch.getButtons().add(
                new PageButton(button = watch.getButtons().size(), x + 64, y - 70, 12, 12, "", this));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {

        GL11.glPushMatrix();
        int x = (watch.width - 160) / 2 + 80;
        int y = (watch.height - 160) / 2 + 8;

        GL11.glTranslated(x, y + 72, 0);
        double xCoord = 0;
        double yCoord = 0;
        float zCoord = this.zLevel;
        float maxU = 1;
        float maxV = 1;
        float minU = -1;
        float minV = -1;
        float r = 0;
        float g = 1;
        float b = 0;
        float a = 1;
        GlStateManager.disableTexture2D();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder vertexbuffer = tessellator.getBuffer();
        vertexbuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        vertexbuffer.pos((xCoord + minU), (yCoord + maxV), zCoord).color(r, g, b, a).endVertex();
        vertexbuffer.pos((xCoord + maxU), (yCoord + maxV), zCoord).color(r, g, b, a).endVertex();
        vertexbuffer.pos((xCoord + maxU), (yCoord + minV), zCoord).color(r, g, b, a).endVertex();
        vertexbuffer.pos((xCoord + minU), (yCoord + minV), zCoord).color(r, g, b, a).endVertex();
        tessellator.draw();
        r = 1;
        g = 0;
        Vector3 here = Vector3.getNewVector().set(watch.player);
        double angle = -watch.player.rotationYaw % 360 + 180;
        GL11.glRotated(angle, 0, 0, 1);
        if (!meteors) for (Coordinate c : bases)
        {
            Vector3 loc = Vector3.getNewVector().set(c.x + 0.5, c.y + 0.5, c.z + 0.5);
            GL11.glPushMatrix();
            Vector3 v = here.subtract(loc);
            v.reverse();
            double max = 30;
            double hDistSq = (v.x) * (v.x) + (v.z) * (v.z);
            float vDist = (float) Math.abs(v.y);
            v.set(v.normalize());
            a = ((64 - vDist) / baseRange);
            a = Math.min(a, 1);
            a = Math.max(a, 0.125f);
            double dist = max * Math.sqrt(hDistSq) / baseRange;
            v.scalarMultBy(dist);
            GL11.glTranslated(v.x, v.z, 0);
            xCoord = v.x;
            yCoord = v.y;
            vertexbuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
            vertexbuffer.pos((xCoord + minU), (yCoord + maxV), zCoord).color(r, g, b, a).endVertex();
            vertexbuffer.pos((xCoord + maxU), (yCoord + maxV), zCoord).color(r, g, b, a).endVertex();
            vertexbuffer.pos((xCoord + maxU), (yCoord + minV), zCoord).color(r, g, b, a).endVertex();
            vertexbuffer.pos((xCoord + minU), (yCoord + minV), zCoord).color(r, g, b, a).endVertex();
            tessellator.draw();
            GL11.glPopMatrix();
        }
        g = 1;
        if (meteors && closestMeteor != null)
        {
            Vector3 loc = Vector3.getNewVector().set(closestMeteor.x + 0.5, closestMeteor.y + 0.5,
                    closestMeteor.z + 0.5);
            GL11.glPushMatrix();
            Vector3 v = here.subtract(loc);
            v.reverse();
            double max = 30;
            double hDistSq = (v.x) * (v.x) + (v.z) * (v.z);
            v.y = 0;
            v.set(v.normalize());
            a = 1;
            double dist = hDistSq / (baseRange * baseRange);
            dist = Math.min(dist, max);
            v.scalarMultBy(dist);
            GL11.glTranslated(v.x, v.z, 0);
            xCoord = v.x;
            yCoord = v.y;
            vertexbuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
            vertexbuffer.pos((xCoord + minU), (yCoord + maxV), zCoord).color(r, g, b, a).endVertex();
            vertexbuffer.pos((xCoord + maxU), (yCoord + maxV), zCoord).color(r, g, b, a).endVertex();
            vertexbuffer.pos((xCoord + maxU), (yCoord + minV), zCoord).color(r, g, b, a).endVertex();
            vertexbuffer.pos((xCoord + minU), (yCoord + minV), zCoord).color(r, g, b, a).endVertex();
            tessellator.draw();
            GL11.glPopMatrix();
        }
        GL11.glPopMatrix();
        GlStateManager.enableTexture2D();
        drawCenteredString(fontRenderer, getTitle(), x, y, 0x78C850);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public void actionPerformed(GuiButton button) throws IOException
    {
        if (button.id != this.button) return;
        meteors = !meteors;
        if (meteors)
        {
            this.setTitle(I18n.format("pokewatch.title.meteorradar"));
        }
        else
        {
            this.setTitle(I18n.format("pokewatch.title.baseradar"));
        }

    }
}
