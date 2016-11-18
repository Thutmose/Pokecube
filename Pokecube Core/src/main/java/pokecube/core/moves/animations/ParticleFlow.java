package pokecube.core.moves.animations;

import java.util.Random;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemDye;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IWorldEventListener;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.PokecubeCore;
import pokecube.core.client.render.PTezzelator;
import pokecube.core.interfaces.Move_Base;
import thut.api.maths.Vector3;

public class ParticleFlow extends MoveAnimationBase
{
    String  type    = null;
    float   width   = 1;
    float   density = 1;
    boolean flat    = false;
    boolean reverse = false;

    public ParticleFlow(String particle)
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
            else if (ident.equals("f"))
            {
                flat = Boolean.parseBoolean(val);
            }
            else if (ident.equals("r"))
            {
                reverse = Boolean.parseBoolean(val);
            }
            else if (ident.equals("v"))
            {
                type = val;
            }
        }
        if (type == null) type = "misc";// TODO test this.
    }

    public ParticleFlow(String particle, float width)
    {
        this(particle);
        this.width = width;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void clientAnimation(MovePacketInfo info, IWorldEventListener world, float partialTick)
    {
        if (type != null) return;
        Vector3 source = info.source;
        Vector3 target = info.target;
        ResourceLocation texture = new ResourceLocation("pokecube", "textures/blank.png");
        FMLClientHandler.instance().getClient().renderEngine.bindTexture(texture);
        double dist = source.distanceTo(target);
        Vector3 temp = Vector3.getNewVector().set(source).subtractFrom(target);

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

        VertexFormat format = DefaultVertexFormats.POSITION_COLOR;
        Random rand = new Random(info.currentTick);
        factor *= width * 0.2;
        double yF = flat ? 0 : 1;
        double dir = reverse ? -1 : 1;
        for (int i = 0; i < 500 * density; i++)
        {
            tez.begin(2, format);
            temp.set(rand.nextGaussian() * factor, rand.nextGaussian() * factor * yF, rand.nextGaussian() * factor);
            temp.scalarMult(0.010);
            temp.addTo(temp2).scalarMultBy(rand.nextDouble() * dir);
            double size = 0.01;

            tez.vertex(temp.x, temp.y + size, temp.z).color(red, green, blue, alpha).endVertex();
            tez.vertex(temp.x - size, temp.y - size, temp.z - size).color(red, green, blue, alpha).endVertex();
            tez.vertex(temp.x - size, temp.y + size, temp.z - size).color(red, green, blue, alpha).endVertex();
            tez.vertex(temp.x, temp.y - size, temp.z).color(red, green, blue, alpha).endVertex();
            tez.end();
        }

        GL11.glPopMatrix();
    }

    @Override
    public void initColour(long time, float partialTicks, Move_Base move)
    {
        if (particle.equals("airbubble"))
        {
            rgba = 0x78000000 + EnumDyeColor.CYAN.getMapColor().colorValue;
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
        else if (particle.equals("spark"))
        {
            rgba = 0x78000000 + EnumDyeColor.YELLOW.getMapColor().colorValue;
        }
        else
        {
            rgba = getColourFromMove(move, 255);
        }
    }

    @Override
    public void spawnClientEntities(MovePacketInfo info)
    {
        if (type == null) return;
        Vector3 source = info.source;
        Vector3 target = info.target;
        initColour((info.attacker.getEntityWorld().getWorldTime()) * 20, 0, info.move);
        double dist = source.distanceTo(target);
        double frac = dist * info.currentTick / getDuration();
        // TODO make reversing work.
        double dir = reverse ? -1 : 1;

        Vector3 temp = Vector3.getNewVector().set(target).subtractFrom(source).norm();
        Random rand = new Random();
        Vector3 temp1 = Vector3.getNewVector();
        double yF = flat ? 0 : 1;
        for (double i = frac; i < dist; i += 0.1)
        {
            double factor = frac;
            factor *= width * 0.2;
            for (int j = 0; j < density; j++)
            {
                temp1.set(rand.nextGaussian() * factor, rand.nextGaussian() * factor * yF,
                        rand.nextGaussian() * factor);
                PokecubeCore.proxy.spawnParticle(info.attacker.worldObj, type,
                        source.add(temp.scalarMult(i).addTo(temp1)), null, 5, rgba);
            }
        }
    }
}
