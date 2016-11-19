package pokecube.core.moves.animations;

import java.util.Random;

import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemDye;
import net.minecraft.world.IWorldEventListener;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.PokecubeCore;
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
            else if (ident.equals("p"))
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
        Vector3 source = reverse ? info.target : info.source;
        Vector3 target = reverse ? info.source : info.target;
        initColour((info.attacker.getEntityWorld().getWorldTime()) * 20, 0, info.move);
        double dist = source.distanceTo(target);
        double frac = dist * info.currentTick / getDuration();
        Vector3 temp = Vector3.getNewVector().set(target).subtractFrom(source).norm();
        Random rand = new Random();
        Vector3 temp1 = Vector3.getNewVector();
        double yF = flat ? 0 : 1;
        for (double i = frac; i < dist; i += 0.1)
        {
            if (density < 1 && Math.random() > density) continue;
            double factor = frac;
            factor *= width * 0.2;
            for (int j = 0; j < density; j++)
            {
                temp1.set(rand.nextGaussian() * factor, rand.nextGaussian() * factor * yF,
                        rand.nextGaussian() * factor);
                PokecubeCore.proxy.spawnParticle(info.attacker.worldObj, type,
                        source.add(temp.scalarMult(i).addTo(temp1)), null, 1, rgba);
            }
        }
    }
}
