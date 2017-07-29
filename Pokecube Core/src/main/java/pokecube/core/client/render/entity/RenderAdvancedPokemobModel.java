package pokecube.core.client.render.entity;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLClientHandler;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
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

    public RenderAdvancedPokemobModel(String name, RenderManager manager, float par2)
    {
        super(manager, null, par2);
        modelName = name;
    }

    @SuppressWarnings("unchecked")
    public void preload()
    {
        PokedexEntry entry = Database.getEntry(modelName);
        model = (IModelRenderer<T>) getRenderer(modelName, null);
        if (model == null && entry.getBaseForme() != null)
        {
            model = (IModelRenderer<T>) getRenderer(entry.getBaseForme().getName(), null);
            AnimationLoader.modelMaps.put(entry.getName(), model);
        }
        if (model != null && model instanceof RenderLivingBase)
        {
            this.mainModel = ((RenderLivingBase<?>) model).getMainModel();
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void doRender(T entity, double x, double y, double z, float yaw, float partialTick)
    {
        if (!RenderPokemobs.shouldRender(entity, x, y, z, yaw, partialTick)) return;
        IPokemob mob = CapabilityPokemob.getPokemobFor(entity);
        T toRender = entity;
        IPokemob temp;
        if ((temp = CapabilityPokemob.getPokemobFor(mob.getTransformedTo())) != null)
        {
            toRender = (T) mob.getTransformedTo();
            mob = temp;
        }
        model = (IModelRenderer<T>) getRenderer(mob.getPokedexEntry().getName(), entity);
        if (model == null && mob.getPokedexEntry().getBaseForme() != null)
        {
            model = (IModelRenderer<T>) getRenderer(mob.getPokedexEntry().getBaseForme().getName(), entity);
            AnimationLoader.modelMaps.put(mob.getPokedexEntry().getName(), model);
        }
        if (model != null && model instanceof RenderLivingBase)
        {
            this.mainModel = ((RenderLivingBase<?>) model).getMainModel();
        }
        if (model == null) return;
        if (MinecraftForge.EVENT_BUS.post(new RenderLivingEvent.Pre(entity, this, x, y, z))) return;
        GL11.glPushMatrix();
        this.preRenderCallback(entity, partialTick);
        GL11.glPushMatrix();
        GL11.glTranslated(x, y, z);
        if ((partialTick <= 1))
        {
            RenderPokemob.renderEvolution(mob, yaw);
            RenderPokemob.renderExitCube(mob, yaw);
        }
        float s = (mob.getSize());
        this.shadowSize = (float) (entity.addedToChunk ? Math.sqrt(s * mob.getPokedexEntry().width) : 0);
        GL11.glPopMatrix();
        GL11.glPushMatrix();
        if (model.getTexturer() == null)
            FMLClientHandler.instance().getClient().renderEngine.bindTexture(getEntityTexture(entity));
        String phase;
        if (overrideAnim) phase = anim;
        else if (entity instanceof IEntityAnimator)
        {
            phase = ((IEntityAnimator) entity).getAnimation(partialTick);
        }
        else
        {
            phase = getPhase(entity, mob, partialTick);
        }
        if (!model.hasAnimation(phase)) phase = "idle";
        model.setAnimation(phase);
        model.doRender(toRender, x, y, z, yaw, partialTick);
        model.renderStatus(toRender, x, y, z, yaw, partialTick);
        MinecraftForge.EVENT_BUS.post(new RenderLivingEvent.Post(entity, this, x, y, z));
        GL11.glPopMatrix();
        this.postRenderCallback();
        GL11.glPopMatrix();
    }

    @Override
    protected ResourceLocation getEntityTexture(T entity)
    {
        return RenderPokemobs.getInstance().getEntityTexturePublic(entity);
    }

    private String getPhase(EntityLiving entity, IPokemob pokemob, float partialTick)
    {
        String phase = "idle";
        float walkspeed = entity.prevLimbSwingAmount
                + (entity.limbSwingAmount - entity.prevLimbSwingAmount) * partialTick;

        boolean asleep = pokemob.getStatus() == IMoveConstants.STATUS_SLP
                || pokemob.getPokemonAIState(IMoveConstants.SLEEPING);

        if (asleep && model.hasAnimation("sleeping"))
        {
            phase = "sleeping";
            return phase;
        }
        if (asleep && model.hasAnimation("asleep"))
        {
            phase = "asleep";
            return phase;
        }
        if (pokemob.getPokemonAIState(IMoveConstants.SITTING) && model.hasAnimation("sitting"))
        {
            phase = "sitting";
            return phase;
        }
        if (!entity.onGround && model.hasAnimation("flight"))
        {
            phase = "flight";
            return phase;
        }
        if (!entity.onGround && model.hasAnimation("flying"))
        {
            phase = "flying";
            return phase;
        }
        if (entity.isInWater() && model.hasAnimation("swimming"))
        {
            phase = "swimming";
            return phase;
        }
        if (entity.onGround && walkspeed > 0.1 && model.hasAnimation("walking"))
        {
            phase = "walking";
            return phase;
        }
        if (entity.onGround && walkspeed > 0.1 && model.hasAnimation("walk"))
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
