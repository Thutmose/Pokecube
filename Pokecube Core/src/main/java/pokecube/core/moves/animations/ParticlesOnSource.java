package pokecube.core.moves.animations;

import net.minecraft.client.renderer.GlStateManager;
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
        if (type != null) return;
        Vector3 source = info.source;
        Vector3 target = info.target;
        Vector3 temp = Vector3.getNewVector().set(source).subtractFrom(target);
        GlStateManager.translate(temp.x, temp.y, temp.z);
        super.clientAnimation(info, world, partialTick);
    }

    @Override
    public void spawnClientEntities(MovePacketInfo info)
    {
        if (type == null) return;
        PokecubeMod.core.spawnParticle(info.attacked.worldObj, type, info.source, null);
    }
}
