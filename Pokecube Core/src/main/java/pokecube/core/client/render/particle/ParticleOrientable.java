package pokecube.core.client.render.particle;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.FMLClientHandler;
import pokecube.core.client.render.PTezzelator;
import thut.api.maths.Vector3;
import thut.api.maths.Vector4;

public class ParticleOrientable extends ParticleBase
{
    private Vector4 orientation;

    public ParticleOrientable(int x, int y)
    {
        super(x, y);
        billboard = false;
    }

    @Override
    public void render(double renderPartialTicks)
    {
        // This will draw a textured, coloured quad
        PTezzelator tez = PTezzelator.instance;
        ResourceLocation texture;
        GL11.glPushMatrix();

        if (billboard)
        {
            RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
            GL11.glRotatef(-renderManager.playerViewY - 45, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
        }
        setColour();

        float alpha = ((rgba >> 24) & 255) / 255f;
        float red = ((rgba >> 16) & 255) / 255f;
        float green = ((rgba >> 8) & 255) / 255f;
        float blue = (rgba & 255) / 255f;

        VertexFormat format = DefaultVertexFormats.POSITION_COLOR;
        texture = new ResourceLocation("pokecube", "textures/particles.png");
        FMLClientHandler.instance().getClient().renderEngine.bindTexture(texture);
        format = DefaultVertexFormats.POSITION_TEX_COLOR;

        int num = (getDuration() / animSpeed) % tex.length;

        int u = tex[num][0], v = tex[num][1];

        Vector3 temp = Vector3.getNewVector();

        double factor = ((lifetime - getDuration()) + renderPartialTicks);
        temp.set(velocity).scalarMultBy(factor);
        if (getDuration() > 149)
        {
            System.out.println(temp + " " + velocity.scalarMult(-lifetime));
            System.out.println(lifetime + " " + getDuration());
        }
        // temp.reverse();
        // GlStateManager.translate(temp.x, temp.y, temp.z);
        // temp.set(velocity).scalarMultBy(lifetime);
        GlStateManager.translate(temp.x, temp.y, temp.z);

        if (orientation != null) orientation.glRotate();

        double u1 = u * 1d / 16d, v1 = v * 1d / 16d;
        double u2 = (u + 1) * 1d / 16d, v2 = (v + 1) * 1d / 16d;
        tez.begin(GL11.GL_QUADS, format);
        // Face 1
        tez.vertex(0.0 - size, 0.0 - size, 0.0).tex(u1, v2).color(red, green, blue, alpha).endVertex();
        tez.vertex(0.0, 0.0 - size, 0.0).tex(u2, v2).color(red, green, blue, alpha).endVertex();
        tez.vertex(0.0, 0.0 + size, 0.0).tex(u2, v1).color(red, green, blue, alpha).endVertex();
        tez.vertex(0.0 - size, 0.0 + size, 0.0).tex(u1, v1).color(red, green, blue, alpha).endVertex();
        // Face 2
        tez.vertex(0.0 - size, 0.0 - size, 0.0).tex(u1, v2).color(red, green, blue, alpha).endVertex();
        tez.vertex(0.0 - size, 0.0 + size, 0.0).tex(u1, v1).color(red, green, blue, alpha).endVertex();
        tez.vertex(0.0, 0.0 + size, 0.0).tex(u2, v1).color(red, green, blue, alpha).endVertex();
        tez.vertex(0.0, 0.0 - size, 0.0).tex(u2, v2).color(red, green, blue, alpha).endVertex();
        tez.end();
        GL11.glPopMatrix();
    }

    public ParticleOrientable setOrientation(Vector4 orientation)
    {
        this.orientation = orientation;
        return this;
    }

    @Override
    public void setVelocity(Vector3 v)
    {
        velocity = v;
    }

}
