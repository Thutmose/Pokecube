package pokecube.core.moves.animations;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.world.IWorldAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thut.api.maths.Vector3;

public class ParticlesOnSource extends ParticlesOnTarget
{
    public ParticlesOnSource(String particles)
    {
        super(particles);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void clientAnimation(MovePacketInfo info, IWorldAccess world, float partialTick)
    {
        Vector3 source = info.source;
        Vector3 target = info.target;
        Vector3 temp = Vector3.getNewVector().set(source).subtractFrom(target);
        GlStateManager.translate(temp.x, temp.y, temp.z);
        super.clientAnimation(info, world, partialTick);
    }
}
