package pokecube.core.moves.animations;

import java.util.Random;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IWorldAccess;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.client.render.PTezzelator;
import pokecube.core.interfaces.Move_Base;
import thut.api.maths.Vector3;

public class ParticlesOnTarget extends MoveAnimationBase
{

    String particle;
    float  width   = 1;
    float  density = 1;

    public ParticlesOnTarget(String particle)
    {
        this.particle = particle;
        String[] args = particle.split(":");
        for (int i = 1; i < args.length; i++)
        {
            String ident = args[i].substring(0, 1);
            String val = args[i].substring(1);
            if (ident.equals("w"))
            {
                width = Float.parseFloat(val);
            }
            else if (ident.equals("d"))
            {
                density = Float.parseFloat(val);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void clientAnimation(MovePacketInfo info, IWorldAccess world, float partialTick)
    {
        Vector3 source = info.source;
        Vector3 target = info.target;
        ResourceLocation texture = new ResourceLocation("pokecube", "textures/blank.png");
        FMLClientHandler.instance().getClient().renderEngine.bindTexture(texture);
        Vector3 temp = Vector3.getNewVectorFromPool().set(source).subtractFrom(target);

        PTezzelator tez = PTezzelator.instance;

        GL11.glPushMatrix();

        initColour(info.currentTick * 300, partialTick, info.move);
        float alpha = ((rgba >> 24) & 255)/255f;
        float red = ((rgba >> 16) & 255)/255f;
        float green = ((rgba >> 8) & 255)/255f;
        float blue = (rgba & 255)/255f;

        VertexFormat format = DefaultVertexFormats.POSITION_COLOR;
        Random rand = new Random(info.currentTick);
        for (int i = 0; i < 500 * density; i++)
        {
            tez.begin(2, format);
            temp.set(rand.nextGaussian(), rand.nextGaussian(), rand.nextGaussian());
            temp.scalarMult(0.001 * width);
            double size = 0.01;

            tez.vertex(temp.x, temp.y + size, temp.z).color(red, green, blue, alpha).endVertex();
            tez.vertex(temp.x - size, temp.y - size, temp.z - size).color(red, green, blue, alpha).endVertex();
            tez.vertex(temp.x - size, temp.y + size, temp.z - size).color(red, green, blue, alpha).endVertex();
            tez.vertex(temp.x, temp.y - size, temp.z).color(red, green, blue, alpha).endVertex();
            tez.end();
        }

        GL11.glPopMatrix();

        temp.freeVectorFromPool();
    }

    @Override
    public void initColour(long time, float partialTicks, Move_Base move)
    {
        rgba = getColourFromMove(move, 255);
    }
}
