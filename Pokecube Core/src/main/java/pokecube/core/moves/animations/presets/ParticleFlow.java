package pokecube.core.moves.animations.presets;

import java.util.Random;

import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemDye;
import net.minecraft.world.IWorldEventListener;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.PokecubeCore;
import pokecube.core.interfaces.IMoveAnimation;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.moves.animations.AnimPreset;
import pokecube.core.moves.animations.MoveAnimationBase;
import thut.api.maths.Vector3;

@AnimPreset(getPreset="flow")
public class ParticleFlow extends MoveAnimationBase
{
    String  type    = null;
    float   width   = 1;
    float   angle   = 0;
    boolean flat    = false;
    boolean reverse = false;

    public ParticleFlow()
    {
    }

    @SideOnly(Side.CLIENT)
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
        double frac2 = info.currentTick / (float) getDuration();
        double frac = dist * frac2;
        double frac3 = dist * (info.currentTick + 1) / getDuration();
        Vector3 temp = Vector3.getNewVector().set(target).subtractFrom(source).norm();
        Random rand = new Random();
        Vector3 temp1 = Vector3.getNewVector();
        Vector3 angleF = temp.horizonalPerp();
        if (flat)
        {
            angleF.rotateAboutLine(temp.normalize(), angle, temp1);
            angleF.set(temp1);
        }
        for (double i = frac; i < frac3; i += 0.1)
        {
            if (density < 1 && Math.random() > density) continue;
            double factor = Math.min(frac2, 1);
            factor *= width * 2;
            for (int j = 0; j < density; j++)
            {
                if (flat)
                {
                    temp1.set(angleF.scalarMult(factor * (0.5 - rand.nextDouble())));
                }
                else
                {
                    temp1.set(factor * (0.5 - rand.nextDouble()), factor * (0.5 - rand.nextDouble()),
                            factor * (0.5 - rand.nextDouble()));
                }
                PokecubeCore.proxy.spawnParticle(info.attacker.worldObj, type,
                        source.add(temp.scalarMult(i).addTo(temp1)), null, rgba, particleLife);
            }
        }
    }

    @Override
    public IMoveAnimation init(String preset)
    {
        this.particle = preset;
        String[] args = preset.split(":");
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
                flat = true;
                angle = (float) (Float.parseFloat(val) * Math.PI) / 180f;
            }
            else if (ident.equals("r"))
            {
                reverse = Boolean.parseBoolean(val);
            }
            else if (ident.equals("p"))
            {
                type = val;
            }
            else if (ident.equals("l"))
            {
                particleLife = Integer.parseInt(val);
            }
            else if (ident.equals("c"))
            {
                int alpha = 255;
                rgba = EnumDyeColor.byDyeDamage(Integer.parseInt(val)).getMapColor().colorValue + 0x01000000 * alpha;
                customColour = true;
            }
        }
        if (type == null) type = "misc";// TODO test this.
        return this;
    }
}
