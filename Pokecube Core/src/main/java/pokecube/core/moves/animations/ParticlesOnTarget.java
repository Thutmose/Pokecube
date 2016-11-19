package pokecube.core.moves.animations;

import net.minecraft.item.EnumDyeColor;
import net.minecraft.world.IWorldEventListener;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.interfaces.PokecubeMod;
import thut.api.maths.Vector3;

public class ParticlesOnTarget extends MoveAnimationBase
{
    String type    = null;
    String particle;
    float  width   = 1;
    float  density = 1;
    int    meshId  = 0;

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
            else if (ident.equals("p"))
            {
                type = val;
            }
            else if (ident.equals("c"))
            {
                int alpha = 255;
                rgba = EnumDyeColor.byDyeDamage(Integer.parseInt(val)).getMapColor().colorValue + 0x01000000 * alpha;
                customColour = true;
            }
        }
        if (type == null) type = "powder";
    }

    @Override
    public void initColour(long time, float partialTicks, Move_Base move)
    {
        if (customColour) return;
        rgba = getColourFromMove(move, 255);
    }

    @Override
    public void spawnClientEntities(MovePacketInfo info)
    {
        if (type == null || Math.random() > density) return;
        initColour((info.attacker.getEntityWorld().getWorldTime()), 0, info.move);
        Vector3 temp = Vector3.getNewVector().set(info.target);
        PokecubeMod.core.spawnParticle(info.attacker.worldObj, type, temp, null, rgba);
    }

    @Override
    public void clientAnimation(MovePacketInfo info, IWorldEventListener world, float partialTick)
    {
    }
}
