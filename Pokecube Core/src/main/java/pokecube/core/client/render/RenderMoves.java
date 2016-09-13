package pokecube.core.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import pokecube.core.interfaces.IMoveAnimation;
import pokecube.core.interfaces.IMoveAnimation.MovePacketInfo;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.moves.animations.EntityMoveUse;

public class RenderMoves<T extends EntityMoveUse> extends Render<T>
{
    public RenderMoves(RenderManager renderManager)
    {
        super(renderManager);
    }

    @Override
    public void doRender(T entity, double x, double y, double z, float entityYaw, float partialTicks)
    {
        Move_Base move = entity.getMove();
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x, (float) y, (float) z);
        IMoveAnimation animation;
        if (move != null && (animation = move.getAnimation((IPokemob) entity.getUser())) != null
                && entity.getUser() != null)
        {
            MovePacketInfo info = entity.getMoveInfo();
            info.currentTick = animation.getDuration() - entity.getAge();
            animation.clientAnimation(info, Minecraft.getMinecraft().renderGlobal, partialTicks);
        }
        GlStateManager.popMatrix();
    }

    @Override
    protected ResourceLocation getEntityTexture(T entity)
    {
        return null;
    }
}
