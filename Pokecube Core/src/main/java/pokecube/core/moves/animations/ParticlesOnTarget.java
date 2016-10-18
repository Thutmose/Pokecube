package pokecube.core.moves.animations;

import net.minecraft.world.IWorldEventListener;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.interfaces.PokecubeMod;

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
            else if (ident.equals("v"))
            {
                type = val;
            }
        }
        if (type == null) type = "powder";
    }

    @Override
    public void initColour(long time, float partialTicks, Move_Base move)
    {
        rgba = getColourFromMove(move, 255);
    }

    @Override
    public void spawnClientEntities(MovePacketInfo info)
    {
        if (type == null) return;
        PokecubeMod.core.spawnParticle(info.attacker.worldObj, type, info.target, null);
    }

    @Override
    public void clientAnimation(MovePacketInfo info, IWorldEventListener world, float partialTick)
    {
    }
}
