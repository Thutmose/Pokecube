package pokecube.modelloader.client.render;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.ResourceLocation;
import pokecube.core.client.render.entity.RenderPokemob;
import pokecube.core.client.render.entity.RenderPokemobs;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import thut.core.client.render.model.IModelRenderer;

public abstract class AbstractModelRenderer<T extends EntityLiving> extends RenderLivingBase<T>
        implements IModelRenderer<T>
{

    // Used to check if it has a custom sleeping animation.
    private boolean checkedForSleep   = false;
    private boolean hasSleepAnimation = false;

    // Values used to properly reset the GL state after rendering.
    boolean         blend;
    boolean         light;
    int             src;
    int             dst;

    public AbstractModelRenderer(RenderManager renderManagerIn, ModelBase modelBaseIn, float shadowSizeIn)
    {
        super(renderManagerIn, modelBaseIn, shadowSizeIn);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected void rotateCorpse(T par1EntityLiving, float par2, float par3, float par4)
    {
        super.rotateCorpse(par1EntityLiving, par2, par3, par4);
        if (!checkedForSleep)
        {
            checkedForSleep = true;
            hasSleepAnimation = hasPhase("sleeping") || hasPhase("sleep") || hasPhase("asleep");
        }
        if (hasSleepAnimation) return;
        boolean status = ((IPokemob) par1EntityLiving).getStatus() == IMoveConstants.STATUS_SLP;
        if (status || ((IPokemob) par1EntityLiving).getPokemonAIState(IMoveConstants.SLEEPING))
        {
            short timer = ((IPokemob) par1EntityLiving).getStatusTimer();
            float ratio = 1F;
            if (status)
            {
                if (timer <= 200 && timer > 175)
                {
                    ratio = 1F - ((timer - 175F) / 25F);
                }
                if (timer >= 0 && timer <= 25)
                {
                    ratio = 1F - ((25F - timer) / 25F);
                }
            }
            GL11.glTranslatef(0.5F * ratio, 0.2F * ratio, 0.0F);
            GL11.glRotatef(80 * ratio, 0.0F, 0.0F, 1F);
        }
    }

    private void postRenderStatus()
    {
        if (light) GL11.glEnable(GL11.GL_LIGHTING);
        if (!blend) GL11.glDisable(GL11.GL_BLEND);
        GL11.glBlendFunc(src, dst);
        setStatusRender(false);
    }

    private void preRenderStatus()
    {
        blend = GL11.glGetBoolean(GL11.GL_BLEND);
        light = GL11.glGetBoolean(GL11.GL_LIGHTING);
        src = GL11.glGetInteger(GL11.GL_BLEND_SRC);
        dst = GL11.glGetInteger(GL11.GL_BLEND_DST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        setStatusRender(true);
    }

    @Override
    public void renderStatus(T entity, double d0, double d1, double d2, float f, float partialTick)
    {
        preRenderStatus();
        RenderPokemob.renderStatus(this, entity, d0, d1, d2, f, partialTick);
        postRenderStatus();
    }

    @Override
    protected boolean canRenderName(T entity)
    {
        return entity.getEntityData().getBoolean("isPlayer");
    }

    @Override
    protected ResourceLocation getEntityTexture(T entity)
    {
        return RenderPokemobs.getInstance().getEntityTexturePublic(entity);
    }

    abstract void setStatusRender(boolean value);

}
