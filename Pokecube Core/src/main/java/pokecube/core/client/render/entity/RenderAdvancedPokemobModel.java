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
import pokecube.core.interfaces.Move_Base;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.interfaces.pokemob.ai.LogicStates;
import pokecube.core.moves.MovesUtils;
import pokecube.modelloader.client.render.AbstractModelRenderer;
import pokecube.modelloader.client.render.AnimationLoader;
import pokecube.modelloader.client.render.ModelWrapper;
import pokecube.modelloader.common.IEntityAnimator;
import thut.core.client.render.model.IModelRenderer;

public class RenderAdvancedPokemobModel<T extends EntityLiving> extends RenderPokemobInfos<T>
{
    public static IModelRenderer<?> getRenderer(String name, EntityLiving entity)
    {
        return AnimationLoader.getModel(name);
    }

    // Used to check if it has a custom sleeping animation.
    private boolean          checkedForContactAttack   = false;
    private boolean          hasContactAttackAnimation = false;

    // Used to check if it has a custom sleeping animation.
    private boolean          checkedForRangedAttack    = false;
    private boolean          hasRangedAttackAnimation  = false;

    public IModelRenderer<T> model;
    final String             modelName;
    public boolean           overrideAnim              = false;
    private ModelWrapper     wrapper;

    public String            anim                      = "";

    boolean                  blend;

    boolean                  normalize;

    int                      src;
    int                      dst;

    public RenderAdvancedPokemobModel(String name, RenderManager manager, float par2)
    {
        super(manager, new ModelWrapper(name), par2);
        wrapper = (ModelWrapper) mainModel;
        modelName = name;
    }

    @SuppressWarnings("unchecked")
    public void preload()
    {
        PokedexEntry entry = Database.getEntry(modelName);
        model = (IModelRenderer<T>) getRenderer(entry.getTrimmedName(), null);
        if (model == null && entry.getBaseForme() != null)
        {
            model = (IModelRenderer<T>) getRenderer(entry.getBaseForme().getTrimmedName(), null);
            AnimationLoader.modelMaps.put(entry.getTrimmedName(), model);
        }
        if (model != null && model instanceof RenderLivingBase)
        {
            wrapper.setWrapped(((RenderLivingBase<?>) model).getMainModel());
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
        model = (IModelRenderer<T>) getRenderer(mob.getPokedexEntry().getTrimmedName(), entity);
        if (model == null && mob.getPokedexEntry().getBaseForme() != null)
        {
            model = (IModelRenderer<T>) getRenderer(mob.getPokedexEntry().getBaseForme().getTrimmedName(), entity);
            AnimationLoader.modelMaps.put(mob.getPokedexEntry().getTrimmedName(), model);
        }
        if (model != null && model instanceof RenderLivingBase)
        {
            wrapper.setWrapped(((RenderLivingBase<?>) model).getMainModel());
        }
        if (model == null) return;
        if (MinecraftForge.EVENT_BUS.post(new RenderLivingEvent.Pre(entity, this, partialTick, x, y, z))) return;
        GL11.glPushMatrix();
        this.preRenderCallback(entity, partialTick);
        GL11.glPushMatrix();
        GL11.glTranslated(x, y, z);
        if ((partialTick <= 1))
        {
            boolean exitCube = mob.getGeneralState(GeneralStates.EXITINGCUBE);
            if (mob.isEvolving()) RenderPokemob.renderEvolution(mob, yaw);
            if (exitCube) RenderPokemob.renderExitCube(mob, yaw);
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
        if (!model.hasAnimation(phase, entity)) phase = "idle";
        model.setAnimation(phase, entity);
        model.doRender(toRender, x, y, z, yaw, partialTick);
        model.renderStatus(toRender, x, y, z, yaw, partialTick);
        MinecraftForge.EVENT_BUS.post(new RenderLivingEvent.Post(entity, this, partialTick, x, y, z));
        GL11.glPopMatrix();
        this.postRenderCallback();
        GL11.glPopMatrix();
    }

    @Override
    protected ResourceLocation getEntityTexture(T entity)
    {
        ResourceLocation ret = null;
        IPokemob pokemob = CapabilityPokemob.getPokemobFor(entity);
        if (model instanceof AbstractModelRenderer)
        {
            AbstractModelRenderer<?> render = (AbstractModelRenderer<?>) model;
            if (render.model_holder != null)
            {
                ret = render.model_holder.texture;

                if (ret != null && pokemob != null)
                {
                    PokedexEntry entry = pokemob.getPokedexEntry();
                    if (ret.equals(new ResourceLocation(entry.getModId(),
                            entry.texturePath + entry.getTrimmedName() + ".png")))
                    {
                        ret = null;
                        render.model_holder.texture = null;
                    }
                }
            }
        }
        if (ret == null) ret = RenderPokemobs.getInstance().getEntityTexturePublic(entity);
        else if (pokemob != null) ret = pokemob.modifyTexture(ret);
        return ret;
    }

    private String getPhase(EntityLiving entity, IPokemob pokemob, float partialTick)
    {
        String phase = "idle";
        float walkspeed = entity.prevLimbSwingAmount
                + (entity.limbSwingAmount - entity.prevLimbSwingAmount) * partialTick;

        boolean asleep = pokemob.getStatus() == IMoveConstants.STATUS_SLP
                || pokemob.getLogicState(LogicStates.SLEEPING);

        if (!checkedForContactAttack)
        {
            hasContactAttackAnimation = model.hasAnimation("attack_contact", entity);
            checkedForContactAttack = true;
        }
        if (!checkedForRangedAttack)
        {
            hasRangedAttackAnimation = model.hasAnimation("attack_ranged", entity);
            checkedForRangedAttack = true;
        }
        if (pokemob.getCombatState(CombatStates.EXECUTINGMOVE))
        {
            int index = pokemob.getMoveIndex();
            Move_Base move;
            if (index < 4 && (move = MovesUtils.getMoveFromName(pokemob.getMove(index))) != null)
            {
                if (hasContactAttackAnimation && (move.getAttackCategory() & IMoveConstants.CATEGORY_CONTACT) > 0)
                {
                    phase = "attack_contact";
                    return phase;
                }
                if (hasRangedAttackAnimation && (move.getAttackCategory() & IMoveConstants.CATEGORY_DISTANCE) > 0)
                {
                    phase = "attack_ranged";
                    return phase;
                }
            }
        }

        if (asleep && model.hasAnimation("sleeping", entity))
        {
            phase = "sleeping";
            return phase;
        }
        if (asleep && model.hasAnimation("asleep", entity))
        {
            phase = "asleep";
            return phase;
        }
        if (pokemob.getLogicState(LogicStates.SITTING) && model.hasAnimation("sitting", entity))
        {
            phase = "sitting";
            return phase;
        }
        if (!entity.onGround && model.hasAnimation("flight", entity))
        {
            phase = "flight";
            return phase;
        }
        if (!entity.onGround && model.hasAnimation("flying", entity))
        {
            phase = "flying";
            return phase;
        }
        if (entity.isInWater() && model.hasAnimation("swimming", entity))
        {
            phase = "swimming";
            return phase;
        }
        if (entity.onGround && walkspeed > 0.1 && model.hasAnimation("walking", entity))
        {
            phase = "walking";
            return phase;
        }
        if (entity.onGround && walkspeed > 0.1 && model.hasAnimation("walk", entity))
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
