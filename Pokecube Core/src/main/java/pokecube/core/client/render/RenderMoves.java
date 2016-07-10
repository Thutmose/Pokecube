package pokecube.core.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import pokecube.core.interfaces.IMoveAnimation;
import pokecube.core.interfaces.IMoveAnimation.MovePacketInfo;
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
        if (move != null && move.getAnimation() != null)
        {
            IMoveAnimation animation = move.getAnimation();
            MovePacketInfo info = new MovePacketInfo(move, entity.getUser(), entity.getTarget(), entity.getEnd(),
                    entity.getStart());
            info.currentTick = move.getAnimation().getDuration() - entity.getAge();
            animation.clientAnimation(info, Minecraft.getMinecraft().renderGlobal, 0);
        }
        GlStateManager.popMatrix();
    }

    @Override
    protected ResourceLocation getEntityTexture(T entity)
    {
        return null;
    }
}
