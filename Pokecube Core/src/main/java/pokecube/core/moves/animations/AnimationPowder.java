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

public class AnimationPowder extends MoveAnimationBase
{

    String  particle;
    float   width        = 1;
    float   density      = 1;
    boolean reverse      = false;
    int     meshId       = 0;

    public AnimationPowder(String particle)
    {
        this.particle = "powder";
        duration = 50;
        for (EnumDyeColor colour : EnumDyeColor.values())
        {
            if (colour.getName().equalsIgnoreCase(particle))
            {
                rgba = colour.getMapColor().colorValue + 0xFF000000;
                break;
            }
        }
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
            else if (ident.equals("r"))
            {
                reverse = Boolean.parseBoolean(val);
            }
            else if (ident.equals("c"))
            {
                int alpha = 255;
                rgba = EnumDyeColor.byDyeDamage(Integer.parseInt(val)).getMapColor().colorValue + 0x01000000 * alpha;
                customColour = true;
            }
            else if (ident.equals("t"))
            {
                duration = Integer.parseInt(val);
            }
            else if (ident.equals("p"))
            {
                this.particle = val;
            }
        }

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
        else if (!customColour)
        {
            rgba = getColourFromMove(move, 255);
        }
    }

    @Override
    public void spawnClientEntities(MovePacketInfo info)
    {
        Vector3 target = info.target;
        initColour((info.attacker.getEntityWorld().getWorldTime()) * 20, 0, info.move);
        Vector3 temp = Vector3.getNewVector();
        Random rand = new Random();
        for (int i = 0; i < 100 * density; i++)
        {
            temp.set(rand.nextGaussian(), rand.nextGaussian(), rand.nextGaussian());
            temp.scalarMult(0.010 * width);
            temp.addTo(target);
            PokecubeCore.proxy.spawnParticle(info.attacker.worldObj, particle, temp.copy(), null, rgba, 1);
        }
    }

}
