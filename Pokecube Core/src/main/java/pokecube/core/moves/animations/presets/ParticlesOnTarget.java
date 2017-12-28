package pokecube.core.moves.animations.presets;

import java.util.Random;

import net.minecraft.world.IWorldEventListener;
import pokecube.core.interfaces.IMoveAnimation;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.moves.animations.AnimPreset;
import pokecube.core.moves.animations.MoveAnimationBase;
import thut.api.maths.Vector3;

@AnimPreset(getPreset = "pont")
public class ParticlesOnTarget extends MoveAnimationBase
{
    public ParticlesOnTarget()
    {
    }

    @Override
    public void spawnClientEntities(MovePacketInfo info)
    {
        if (Math.random() > density) return;
        initColour((info.attacker.getEntityWorld().getWorldTime()), 0, info.move);
        Vector3 temp = Vector3.getNewVector().set(info.target);
        Random rand = new Random();
        float dw = 0.25f;
        if (info.attacked != null) dw = info.attacked.width;
        float width = this.width * dw;
        temp.addTo(rand.nextGaussian() * width, rand.nextGaussian() * width, rand.nextGaussian() * width);
        PokecubeMod.core.spawnParticle(info.attacker.getEntityWorld(), particle, temp, null, rgba);
    }

    @Override
    public void clientAnimation(MovePacketInfo info, IWorldEventListener world, float partialTick)
    {
    }

    @Override
    public IMoveAnimation init(String preset)
    {
        this.particle = "misc";
        String[] args = preset.split(":");
        for (int i = 1; i < args.length; i++)
        {
            String ident = args[i].substring(0, 1);
            String val = args[i].substring(1);
            try
            {
                if (ident.equals("w"))
                {
                    width = Float.parseFloat(val);
                }
                else if (ident.equals("d"))
                {
                    density = Float.parseFloat(val);
                }
                else if (ident.equals("p"))
                {
                    particle = val;
                }
                else if (ident.equals("l"))
                {
                    particleLife = Integer.parseInt(val);
                }
                else if (ident.equals("c"))
                {
                    initRGBA(val);
                }
            }
            catch (NumberFormatException e)
            {
                System.err.println(preset);
            }
        }
        return this;
    }
}
