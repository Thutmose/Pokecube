package pokecube.core.moves.animations;

import java.util.Random;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemDye;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IWorldAccess;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.client.render.PTezzelator;
import pokecube.core.interfaces.Move_Base;
import thut.api.maths.Vector3;

public class ThrowParticle extends MoveAnimationBase
{

    float width = 1;

    public ThrowParticle(String particle)
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
        }
        rgba = 0xFFFFFFFF;
    }

    public ThrowParticle(String particle, float width)
    {
        this(particle);
        this.width = width;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void clientAnimation(MovePacketInfo info, IWorldAccess world, float partialTick)
    {
        Vector3 source = info.source;
        Vector3 target = info.target;
        ResourceLocation texture = new ResourceLocation("pokecube", "textures/blank.png");
        FMLClientHandler.instance().getClient().renderEngine.bindTexture(texture);
        double dist = source.distanceTo(target);
        Vector3 temp = Vector3.getNewVector().set(source).subtractFrom(target);

        GlStateManager.translate(temp.x, temp.y, temp.z);
        double factor = (info.currentTick + partialTick) / (double) getDuration();
        factor = Math.min(1, factor);
        temp.set(temp.normalize());
        temp.scalarMultBy(-dist * factor);
        Vector3 temp2 = temp.copy();
        PTezzelator tez = PTezzelator.instance;

        GL11.glPushMatrix();

        initColour(info.currentTick * 300, partialTick, info.move);
        float alpha = ((rgba >> 24) & 255) / 255f;
        float red = ((rgba >> 16) & 255) / 255f;
        float green = ((rgba >> 8) & 255) / 255f;
        float blue = (rgba & 255) / 255f;

        long hash = (long) (temp.x * 1000000l + temp.z * 1000000000000l);
        Random rand = new Random(hash);
        factor = width * 0.2;
        tez.begin(6);

        for (int i = 0; i < 500; i++)
        {
            temp.set(rand.nextGaussian() * factor, rand.nextGaussian() * factor, rand.nextGaussian() * factor);
            temp.scalarMult(0.010);
            temp.addTo(temp2);
            double size = 0.01;

            tez.vertex(temp.x, temp.y + size, temp.z).color(red, green, blue, alpha).endVertex();
            tez.vertex(temp.x - size, temp.y - size, temp.z - size).color(red, green, blue, alpha).endVertex();
            tez.vertex(temp.x - size, temp.y + size, temp.z - size).color(red, green, blue, alpha).endVertex();
            tez.vertex(temp.x, temp.y - size, temp.z).color(red, green, blue, alpha).endVertex();
        }
        tez.end();

        GL11.glPopMatrix();
    }

    @Override
    public int getDuration()
    {
        return duration;
    }

    public void initColour(long time, float partialTicks, Move_Base move)
    {
        if (particle.equals("airbubble"))
        {
            rgba = 0x78000000 + EnumDyeColor.CYAN.getMapColor().colorValue;
        }
        else if (particle.equals("aurora"))
        {
            int rand = ItemDye.dyeColors[new Random(time / 10).nextInt(ItemDye.dyeColors.length)];
            rgba = 0x61000000 + rand;
        }
        else if (particle.equals("iceshard"))
        {
            rgba = 0x78000000 + EnumDyeColor.CYAN.getMapColor().colorValue;
        }
        else if (particle.equals("spark"))
        {
            rgba = 0x78000000 + EnumDyeColor.YELLOW.getMapColor().colorValue;
        }
        else
        {
            rgba = getColourFromMove(move, 255);
        }
    }
}
