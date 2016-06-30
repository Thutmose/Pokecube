package pokecube.core.moves.animations;

import java.util.Random;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemDye;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IWorldEventListener;
import net.minecraftforge.fml.client.FMLClientHandler;
import pokecube.core.client.render.PTezzelator;
import pokecube.core.interfaces.Move_Base;
import thut.api.maths.Vector3;

public class ParticleBeam extends MoveAnimationBase
{
    Vector3 v  = Vector3.getNewVector();

    Vector3 v1 = Vector3.getNewVector();
    public ParticleBeam(String particle)
    {
        this.particle = particle;
        rgba = 0xFFFFFFFF;
    }

    @Override
    public void clientAnimation(MovePacketInfo info, IWorldEventListener world, float partialTick)
    {
        Vector3 source = info.source;
        Vector3 target = info.target;
        ResourceLocation texture = new ResourceLocation("pokecube", "textures/blank.png");
        FMLClientHandler.instance().getClient().renderEngine.bindTexture(texture);
        // GL11.glScaled(0.75, 0.75, 0.75);
        double dist = source.distanceTo(target);
        Vector3 temp = Vector3.getNewVector().set(source).subtractFrom(target);

        GlStateManager.translate(temp.x, temp.y, temp.z);
        double factor = (info.currentTick + partialTick) / (double) getDuration();
        factor = Math.min(1, factor);
        temp.set(temp.normalize());
        temp.scalarMultBy(-dist * factor);

        PTezzelator tez = PTezzelator.instance;

        GL11.glPushMatrix();
        GL11.glPushAttrib(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_BLEND);

        initColour((long) (factor * 1000 + info.attacker.getEntityWorld().getWorldTime()) * 20, partialTick, info.move);
        float alpha = ((rgba >> 24) & 255) / 255f;
        float red = ((rgba >> 16) & 255) / 255f;
        float green = ((rgba >> 8) & 255) / 255f;
        float blue = (rgba & 255) / 255f;

        tez.begin(7);

        tez.vertex(temp.x, temp.y + .1, temp.z).color(red, green, blue, alpha).endVertex();
        tez.vertex(0, .1, 0).color(red, green, blue, alpha).endVertex();
        tez.vertex(0, -.1, 0).color(red, green, blue, alpha).endVertex();
        tez.vertex(temp.x, temp.y - .1, temp.z).color(red, green, blue, alpha).endVertex();

        tez.vertex(0, .1, 0).color(red, green, blue, alpha).endVertex();
        tez.vertex(temp.x, temp.y + .1, temp.z).color(red, green, blue, alpha).endVertex();
        tez.vertex(temp.x, temp.y - .1, temp.z).color(red, green, blue, alpha).endVertex();
        tez.vertex(0, -.1, 0).color(red, green, blue, alpha).endVertex();

        tez.vertex(temp.x + .1, temp.y, temp.z).color(red, green, blue, alpha).endVertex();
        tez.vertex(.1, 0, 0).color(red, green, blue, alpha).endVertex();
        tez.vertex(-.1, 0, 0).color(red, green, blue, alpha).endVertex();
        tez.vertex(temp.x - .1, temp.y, temp.z).color(red, green, blue, alpha).endVertex();

        tez.vertex(.1, 0, 0).color(red, green, blue, alpha).endVertex();
        tez.vertex(temp.x + .1, temp.y, temp.z).color(red, green, blue, alpha).endVertex();
        tez.vertex(temp.x - .1, temp.y, temp.z).color(red, green, blue, alpha).endVertex();
        tez.vertex(-.1, 0, 0).color(red, green, blue, alpha).endVertex();

        tez.end();

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopAttrib();
        GL11.glPopMatrix();

    }

    @Override
    public void initColour(long time, float partialTicks, Move_Base move)
    {
        if (particle.equals("airbubble"))
        {

        }
        else if (particle.equals("aurora"))
        {
            int rand = ItemDye.DYE_COLORS[new Random(time / 10).nextInt(ItemDye.DYE_COLORS.length)];
            rgba = 0x61000000 + rand;
        }
        else if (particle.equals("iceshard"))
        {
            rgba = 0x78000000 + EnumDyeColor.CYAN.getMapColor().colorValue;
        }
        else
        {
            rgba = getColourFromMove(move, 255);
        }
    }
}
