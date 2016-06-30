package pokecube.core.client.render.entity;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLClientHandler;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.modelloader.client.render.AnimationLoader;
import pokecube.modelloader.common.IEntityAnimator;
import thut.core.client.render.model.IModelRenderer;

public class RenderAdvancedPokemobModel<T extends EntityLiving> extends RenderPokemobInfos<T>
{
    public static IModelRenderer<?> getRenderer(String name, EntityLiving entity)
    {
        return AnimationLoader.getModel(name);
    }

    public IModelRenderer<T> model;
    final String             modelName;
    public boolean           overrideAnim = false;

    public String            anim         = "";

    boolean                  blend;

    boolean                  normalize;

    int                      src;
    int                      dst;

    public RenderAdvancedPokemobModel(String name, float par2)
    {
        super(Minecraft.getMinecraft().getRenderManager(), null, par2);
        modelName = name;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void doRender(T entity, double d0, double d1, double d2, float yaw, float partialTick)
    {
        IPokemob mob = (IPokemob) entity;
        T toRender = entity;
        if (mob.getTransformedTo() instanceof IPokemob)
        {
            toRender = (T) mob.getTransformedTo();
        }
        model = (IModelRenderer<T>) getRenderer(modelName, entity);

        if (MinecraftForge.EVENT_BUS.post(new RenderLivingEvent.Pre(entity, this, d0, d1, d2))) return;

        GL11.glPushMatrix();
        this.preRenderCallback(entity, partialTick);
        GL11.glPushMatrix();
        GL11.glTranslated(d0, d1, d2);
        if ((partialTick <= 1))
        {
            RenderPokemob.renderEvolution((IPokemob) entity, yaw);
            RenderPokemob.renderExitCube((IPokemob) entity, yaw);
        }
        GL11.glPopMatrix();
        GL11.glPushMatrix();
        GL11.glTranslated(d0, d1, d2);
        if (model.getTexturer() == null)
            FMLClientHandler.instance().getClient().renderEngine.bindTexture(getEntityTexture(entity));
        float f8 = this.handleRotationFloat(entity, partialTick);
        if (entity.getHealth() <= 0) this.rotateCorpse(entity, f8, yaw, partialTick);
        String phase;
        if (overrideAnim) phase = anim;
        else if (entity instanceof IEntityAnimator)
        {
            phase = ((IEntityAnimator) entity).getAnimation(partialTick);
        }
        else
        {
            phase = getPhase(entity, partialTick);
        }
        if (partialTick <= 1)
        {
            int ticks = ((Entity) mob).ticksExisted;
            if (mob.getPokemonAIState(IMoveConstants.EXITINGCUBE) && ticks <= 5)
            {
                float max = 5;
                float s = (ticks) / max;
                GL11.glScalef(s, s, s);
            }
        }
        if (!model.hasPhase(phase)) phase = "idle";
        model.setPhase(phase);
        model.doRender(toRender, d0, d1, d2, yaw, partialTick);
        model.renderStatus(toRender, d0, d1, d2, yaw, partialTick);
        MinecraftForge.EVENT_BUS.post(new RenderLivingEvent.Post(entity, this, d0, d1, d2));
        GL11.glPopMatrix();
        renderHp(entity, d0, d1, d2, yaw, partialTick);
        this.postRenderCallback();
        GL11.glPopMatrix();
    }

    @Override
    protected ResourceLocation getEntityTexture(T entity)
    {
        return RenderPokemobs.getInstance().getEntityTexturePublic(entity);
    }

    private String getPhase(EntityLiving entity, float partialTick)
    {
        String phase = "idle";

        IPokemob pokemob = (IPokemob) entity;
        float walkspeed = entity.prevLimbSwingAmount
                + (entity.limbSwingAmount - entity.prevLimbSwingAmount) * partialTick;

        boolean asleep = pokemob.getStatus() == IMoveConstants.STATUS_SLP
                || pokemob.getPokemonAIState(IMoveConstants.SLEEPING);

        if (asleep && model.hasPhase("sleeping"))
        {
            phase = "sleeping";
            return phase;
        }
        if (asleep && model.hasPhase("asleep"))
        {
            phase = "asleep";
            return phase;
        }
        if (pokemob.getPokemonAIState(IMoveConstants.SITTING) && model.hasPhase("sitting"))
        {
            phase = "sitting";
            return phase;
        }
        if (!entity.onGround && model.hasPhase("flight"))
        {
            phase = "flight";
            return phase;
        }
        if (!entity.onGround && model.hasPhase("flying"))
        {
            phase = "flying";
            return phase;
        }
        if (entity.isInWater() && model.hasPhase("swimming"))
        {
            phase = "swimming";
            return phase;
        }
        if (entity.onGround && walkspeed > 0.1 && model.hasPhase("walking"))
        {
            phase = "walking";
            return phase;
        }
        if (entity.onGround && walkspeed > 0.1 && model.hasPhase("walk"))
        {
            phase = "walk";
            return phase;
        }

        return phase;
    }

    protected void postRenderCallback()
    {
        // Reset to original state. This fixes changes to guis when rendered in
        // them.
        if (!normalize) GL11.glDisable(GL11.GL_NORMALIZE);
        if (!blend) GL11.glDisable(GL11.GL_BLEND);
        GL11.glBlendFunc(src, dst);
    }

    @Override
    protected void preRenderCallback(T entity, float f)
    {
        blend = GL11.glGetBoolean(GL11.GL_BLEND);
        normalize = GL11.glGetBoolean(GL11.GL_NORMALIZE);
        src = GL11.glGetInteger(GL11.GL_BLEND_SRC);
        dst = GL11.glGetInteger(GL11.GL_BLEND_DST);
        if (!normalize) GL11.glEnable(GL11.GL_NORMALIZE);
        if (!blend) GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    }
}
