package pokecube.core.moves.animations;

import net.minecraft.world.IWorldEventListener;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.interfaces.PokecubeMod;
import thut.api.maths.Vector3;

public class ParticlesOnSource extends ParticlesOnTarget
{
    public ParticlesOnSource(String particles)
    {
        super(particles);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void clientAnimation(MovePacketInfo info, IWorldEventListener world, float partialTick)
    {
    }

    @Override
    public void spawnClientEntities(MovePacketInfo info)
    {
        if (type == null || Math.random() > density) return;
        initColour((info.attacker.getEntityWorld().getWorldTime()), 0, info.move);
        Vector3 temp = Vector3.getNewVector().set(info.source);
        PokecubeMod.core.spawnParticle(info.attacker.worldObj, type, temp, null, rgba);
    }
}
