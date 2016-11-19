package pokecube.core.moves.animations;

import java.util.Random;

import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemDye;
import net.minecraft.world.IWorldEventListener;
import pokecube.core.PokecubeCore;
import pokecube.core.interfaces.Move_Base;
import thut.api.maths.Vector3;

public class ParticleBeam extends MoveAnimationBase
{
    Vector3 v    = Vector3.getNewVector();
    boolean old  = false;
    float   tick = 0.5f;
    Vector3 v1   = Vector3.getNewVector();

    public ParticleBeam(String particle)
    {
        this.particle = particle;
        rgba = 0xFFFFFFFF;
        String[] args = particle.split(":");
        this.particle = "misc";
        for (int i = 1; i < args.length; i++)
        {
            String ident = args[i].substring(0, 1);
            String val = args[i].substring(1);
            if (ident.equals("d"))
            {
                tick = Float.parseFloat(val);
            }
            else if (ident.equals("p"))
            {
                this.particle = val;
            }
            else if (ident.equals("c"))
            {
                int alpha = 255;
                rgba = EnumDyeColor.byDyeDamage(Integer.parseInt(val)).getMapColor().colorValue + 0x01000000 * alpha;
                customColour = true;
            }
        }
    }

    @Override
    public void clientAnimation(MovePacketInfo info, IWorldEventListener world, float partialTick)
    {

    }

    @Override
    public void initColour(long time, float partialTicks, Move_Base move)
    {
        if (customColour) return;
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

    @Override
    public void spawnClientEntities(MovePacketInfo info)
    {
        Vector3 source = info.source;
        Vector3 target = info.target;
        initColour((info.attacker.getEntityWorld().getWorldTime()) * 20, 0, info.move);
        double dist = source.distanceTo(target);
        double frac = dist * info.currentTick / getDuration();
        Vector3 temp = Vector3.getNewVector().set(target).subtractFrom(source).norm();
        for (double i = frac; i < dist; i += 0.1)
            PokecubeCore.proxy.spawnParticle(info.attacker.worldObj, particle, source.add(temp.scalarMult(i)), null,
                    rgba, 5);
    }
}
