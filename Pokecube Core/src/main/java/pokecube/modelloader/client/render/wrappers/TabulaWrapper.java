package pokecube.modelloader.client.render.wrappers;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.modelloader.client.render.TabulaPackLoader.TabulaModelSet;
import thut.core.client.render.tabula.components.ModelJson;
import thut.core.client.render.tabula.model.IModelParser;
import thut.core.client.render.tabula.model.tabula.TabulaModel;
import thut.core.client.render.tabula.model.tabula.TabulaModelParser;

public class TabulaWrapper extends ModelBase
{
    final TabulaModelSet set;
    public boolean       statusRender = false;
    public String        phase        = "";

    public TabulaWrapper(TabulaModelSet set)
    {
        this.set = set;
    }

    /** Sets the models various rotation angles then renders the model. */
    @Override
    public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
            float headPitch, float scale)
    {
        TabulaModel model = set.model;
        IModelParser<TabulaModel> parser = set.parser;
        if (model == null || parser == null) { return; }
        GlStateManager.pushMatrix();
        set.parser.modelMap.get(set.model).render(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw,
                headPitch, scale);
        GlStateManager.enableCull();
        GlStateManager.popMatrix();

    }

    /** Sets the model's various rotation angles. For bipeds, par1 and par2 are
     * used for animating the movement of arms and legs, where par1 represents
     * the time(so that arms and legs swing back and forth) and par2 represents
     * how "far" arms and legs can swing at most. */
    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
            float headPitch, float scaleFactor, Entity entityIn)
    {
        TabulaModel model = set.model;
        IModelParser<TabulaModel> parser = set.parser;
        if (model == null || parser == null) { return; }
        GlStateManager.disableCull();
        TabulaModelParser pars = ((TabulaModelParser) parser);
        ModelJson modelj = pars.modelMap.get(model);
        if (!statusRender) modelj.texturer = set.texturer;
        else modelj.texturer = null;
        modelj.changer = set;
        float partialTick = ageInTicks = entityIn.ticksExisted;
        if (set.animator != null)
        {
            phase = set.modifyAnimation((EntityLiving) entityIn, partialTick, phase);
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
        float s = 1;
        float sx = (float) set.scale.x;
        float sy = (float) set.scale.y;
        float sz = (float) set.scale.z;
        float dx = (float) set.shift.x;
        float dy = (float) set.shift.y;
        float dz = (float) set.shift.z;
        IPokemob mob = CapabilityPokemob.getPokemobFor(entityIn);
        if (mob != null)
        {
            s = (mob.getSize());
            if (partialTick <= 1)
            {
                int ticks = mob.getEntity().ticksExisted;
                if (mob.getPokemonAIState(IMoveConstants.EXITINGCUBE) && ticks <= 5)
                {
                    float max = 5;
                    s *= (ticks) / max;
                }
            }
            sx *= s;
            sy *= s;
            sz *= s;
        }
        dy += (1 - sy) * 1.5;
        set.rotation.rotations.glRotate();
        GlStateManager.translate(dx, dy, dz);
        GlStateManager.scale(sx, sy, sz);
    }

    /** Used for easily adding entity-dependent animations. The second and third
     * float params here are the same second and third as in the
     * setRotationAngles method. */
    @Override
    public void setLivingAnimations(EntityLivingBase entitylivingbaseIn, float p_78086_2_, float p_78086_3_,
            float partialTickTime)
    {
        // set.parser.modelMap.get(set.model).setLivingAnimations(entitylivingbaseIn,
        // p_78086_2_, p_78086_3_,
        // partialTickTime);
    }
}
