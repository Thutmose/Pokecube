package pokecube.modelloader.client.render;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import pokecube.core.client.render.entity.RenderPokemob;
import pokecube.core.client.render.entity.RenderPokemobs;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.modelloader.client.render.TabulaPackLoader.TabulaModelSet;
import thut.core.client.render.model.IModelRenderer;
import thut.core.client.render.model.IPartTexturer;
import thut.core.client.render.tabula.components.ModelJson;
import thut.core.client.render.tabula.model.IModelParser;
import thut.core.client.render.tabula.model.tabula.TabulaModel;
import thut.core.client.render.tabula.model.tabula.TabulaModelParser;

public class TabulaModelRenderer<T extends EntityLiving> extends RendererLivingEntity<T> implements IModelRenderer<T>
{
    public static final ResourceLocation FRZ = new ResourceLocation(PokecubeMod.ID, "textures/FRZ.png");
    public static final ResourceLocation PAR = new ResourceLocation(PokecubeMod.ID, "textures/PAR.png");
    private String        phase        = "";
    public TabulaModelSet set;
    private boolean       statusRender = false;

    boolean               blend;

    boolean               light;

    int                   src;

    int                   dst;

    public TabulaModelRenderer(TabulaModelSet set)
    {
        super(Minecraft.getMinecraft().getRenderManager(), null, 0);
        this.set = set;
    }

    @Override
    public void doRender(T entity, double d, double d1, double d2, float f, float partialTick)
    {
        PokedexEntry entry = null;
        if (entity instanceof IPokemob) entry = ((IPokemob) entity).getPokedexEntry();
        else return;

        float f2 = this.interpolateRotation(entity.prevRenderYawOffset, entity.renderYawOffset, partialTick);
        float f3 = this.interpolateRotation(entity.prevRotationYawHead, entity.rotationYawHead, partialTick);
        float f4;
        if (entity.isRiding() && entity.ridingEntity instanceof EntityLivingBase)
        {
            EntityLivingBase entitylivingbase1 = (EntityLivingBase) entity.ridingEntity;
            f2 = this.interpolateRotation(entitylivingbase1.prevRenderYawOffset, entitylivingbase1.renderYawOffset,
                    partialTick);
            f4 = MathHelper.wrapAngleTo180_float(f3 - f2);

            if (f4 < -85.0F)
            {
                f4 = -85.0F;
            }

            if (f4 >= 85.0F)
            {
                f4 = 85.0F;
            }

            f2 = f3 - f4;

            if (f4 * f4 > 2500.0F)
            {
                f2 += f4 * 0.2F;
            }
        }

        f4 = this.handleRotationFloat(entity, partialTick);

        if (set == null)
        {
            System.err.println(entry);
            set = TabulaPackLoader.modelMap.get(entry.baseForme);
        }

        TabulaModel model = set.model;
        IModelParser<TabulaModel> parser = set.parser;

        if (model == null || parser == null) { return; }

        GlStateManager.pushMatrix();
        GlStateManager.disableCull();
        TabulaModelParser pars = ((TabulaModelParser) parser);
        ModelJson modelj = pars.modelMap.get(model);
        if (!statusRender) modelj.texturer = set.texturer;
        else modelj.texturer = null;
        modelj.changer = set;
        if (set.animator != null)
        {
            phase = set.modifyAnimation(entity, partialTick, phase);
        }
        boolean inSet = false;
        if (modelj.animationMap.containsKey(phase) || (inSet = set.loadedAnimations.containsKey(phase)))
        {
            if (!inSet) modelj.startAnimation(phase);
            else modelj.startAnimation(set.loadedAnimations.get(phase));
        }
        else if (modelj.isAnimationInProgress())
        {
            modelj.stopAnimation();
        }

        GlStateManager.rotate(180f, 0f, 0f, 1f);

        GlStateManager.rotate(entity.rotationYaw + 180, 0, 1, 0);

        set.rotation.rotations.glRotate();

        if (entity instanceof IPokemob)
        {
            IPokemob mob = (IPokemob) entity;
            float scale = (mob.getSize());
            GL11.glScalef(scale, scale, scale);
            shadowSize = entry.width * mob.getSize();
        }

        GlStateManager.scale(set.scale.x, set.scale.y, set.scale.z);
        GlStateManager.translate(0, -1.5, 0);
        GlStateManager.translate(set.shift.x, set.shift.y, set.shift.z);
        
        
        parser.render(model, entity);

        GlStateManager.enableCull();
        GlStateManager.popMatrix();
    }

    @Override
    protected ResourceLocation getEntityTexture(T entity)
    {
        return RenderPokemobs.getInstance().getEntityTexturePublic(entity);
    }

    @Override
    public IPartTexturer getTexturer()
    {
        return set.texturer;
    }

    @Override
    public boolean hasPhase(String phase)
    {
        ModelJson modelj = null;
        if (set != null) modelj = set.parser.modelMap.get(set.model);
        return set.loadedAnimations.containsKey(phase) || (modelj != null && modelj.animationMap.containsKey(phase));
    }

    private void postRenderStatus()
    {
        if (light) GL11.glEnable(GL11.GL_LIGHTING);
        if (!blend) GL11.glDisable(GL11.GL_BLEND);
        GL11.glBlendFunc(src, dst);
        statusRender = false;
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
        statusRender = true;
    }

    @Override
    public void renderStatus(T entity, double d0, double d1, double d2, float f, float partialTick)
    {
        preRenderStatus();
        RenderPokemob.renderStatus(this, entity, d0, d1, d2, f, partialTick);
        postRenderStatus();
    }

    @Override
    public void setPhase(String phase)
    {
        this.phase = phase;
    }
}
