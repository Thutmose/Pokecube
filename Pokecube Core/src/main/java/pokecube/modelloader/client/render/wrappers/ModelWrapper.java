package pokecube.modelloader.client.render.wrappers;

import java.util.Collection;
import java.util.HashMap;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.modelloader.client.render.AnimationLoader.Model;
import pokecube.modelloader.client.render.DefaultIModelRenderer;
import pokecube.modelloader.client.render.DefaultIModelRenderer.Vector5;
import thut.api.entity.IMobColourable;
import thut.api.maths.Vector3;
import thut.api.maths.Vector4;
import thut.core.client.render.animation.AnimationHelper;
import thut.core.client.render.model.IExtendedModelPart;
import thut.core.client.render.model.IModel;
import thut.core.client.render.model.IRetexturableModel;
import thut.core.client.render.tabula.components.Animation;

public class ModelWrapper extends ModelBase implements IModel
{
    public final Model                    model;
    public final DefaultIModelRenderer<?> renderer;
    public IModel                         imodel;
    public boolean                        statusRender   = false;
    protected float                       rotationPointX = 0, rotationPointY = 0, rotationPointZ = 0;
    protected float                       rotateAngleX   = 0, rotateAngleY = 0, rotateAngleZ = 0, rotateAngle = 0;

    public ModelWrapper(Model model, DefaultIModelRenderer<?> renderer)
    {
        this.model = model;
        this.renderer = renderer;
    }

    /** Sets the models various rotation angles then renders the model. */
    @Override
    public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
            float headPitch, float scale)
    {
        GlStateManager.pushMatrix();
        GlStateManager.disableCull();
        for (String partName : renderer.parts.keySet())
        {
            IExtendedModelPart part = imodel.getParts().get(partName);
            if (part == null) continue;
            try
            {
                if (renderer.texturer != null && part instanceof IRetexturableModel)
                {
                    renderer.texturer.bindObject(entityIn);
                    if (!statusRender) ((IRetexturableModel) part).setTexturer(renderer.texturer);
                    else((IRetexturableModel) part).setTexturer(null);
                }
                if (part.getParent() == null)
                {
                    GlStateManager.pushMatrix();
                    part.renderAll();
                    GlStateManager.popMatrix();
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        GlStateManager.color(1, 1, 1, 1);
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
        float partialTick = ageInTicks - entityIn.ticksExisted;
        if (renderer.animator != null) renderer.currentPhase = renderer.animator
                .modifyAnimation((EntityLiving) entityIn, partialTick, renderer.currentPhase);
        transformGlobal(renderer.currentPhase, entityIn, Minecraft.getMinecraft().getRenderPartialTicks(), netHeadYaw,
                headPitch);
        updateAnimation(entityIn, renderer.currentPhase, partialTick, netHeadYaw, headPitch, limbSwing);
    }

    /** Used for easily adding entity-dependent animations. The second and third
     * float params here are the same second and third as in the
     * setRotationAngles method. */
    @Override
    public void setLivingAnimations(EntityLivingBase entitylivingbaseIn, float limbSwing, float limbSwingAmount,
            float partialTickTime)
    {
    }

    @Override
    public HashMap<String, IExtendedModelPart> getParts()
    {
        return imodel.getParts();
    }

    @Override
    public void preProcessAnimations(Collection<Animation> animations)
    {
        imodel.preProcessAnimations(animations);
    }

    protected void transformGlobal(String currentPhase, Entity entity, float partialTick, float rotationYaw,
            float rotationPitch)
    {
        if (renderer.rotations == null)
        {
            renderer.rotations = new Vector5();
        }
        this.setRotationAngles(renderer.rotations.rotations);
        this.setRotationPoint(renderer.offset);
        float s = 1;
        float sx = (float) renderer.scale.x;
        float sy = (float) renderer.scale.y;
        float sz = (float) renderer.scale.z;
        float dy = rotationPointY - 1.5f;
        IPokemob mob = CapabilityPokemob.getPokemobFor(entity);
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
            this.setRotationPoint(rotationPointX * s, rotationPointY * s, rotationPointZ * s);
        }
        this.rotate();
        GlStateManager.rotate(180, 0, 1, 0);
        GlStateManager.rotate(90, 1, 0, 0);
        GlStateManager.translate(0, 0, dy);
        this.translate();
        if (!renderer.scale.isEmpty()) GlStateManager.scale(sx, sy, sz);
        else
        {
            GlStateManager.scale(s, s, s);
        }
    }

    protected void rotate()
    {
        GlStateManager.rotate(rotateAngle, rotateAngleX, rotateAngleY, rotateAngleZ);
    }

    private void translate()
    {
        GlStateManager.translate(rotationPointX, rotationPointY, rotationPointZ);
    }

    protected void updateAnimation(Entity entity, String currentPhase, float partialTicks, float headYaw,
            float headPitch, float limbSwing)
    {
        for (String partName : renderer.parts.keySet())
        {
            IExtendedModelPart part = imodel.getParts().get(partName);
            updateSubParts(entity, currentPhase, partialTicks, part, headYaw, headPitch, limbSwing);
        }
    }

    private void updateSubParts(Entity entity, String currentPhase, float partialTick, IExtendedModelPart parent,
            float headYaw, float headPitch, float limbSwing)
    {
        if (parent == null) return;

        parent.resetToInit();
        boolean anim = renderer.animations.containsKey(currentPhase);
        if (anim)
        {
            if (AnimationHelper.doAnimation(renderer.animations.get(currentPhase), entity, parent.getName(), parent,
                    partialTick, limbSwing))
            {
            }
            else if (renderer.animations.containsKey(DefaultIModelRenderer.DEFAULTPHASE))
            {
                AnimationHelper.doAnimation(renderer.animations.get(DefaultIModelRenderer.DEFAULTPHASE), entity,
                        parent.getName(), parent, partialTick, limbSwing);
            }
        }

        if (isHead(parent.getName()))
        {
            float ang;
            float ang2 = -headPitch;
            float head = headYaw + 180;
            float diff = 0;
            if (renderer.headDir != -1) head *= -1;
            diff = (head) % 360;
            diff = (diff + 360) % 360;
            diff = (diff - 180) % 360;
            diff = Math.max(diff, renderer.headCaps[0]);
            diff = Math.min(diff, renderer.headCaps[1]);
            ang = diff;
            ang2 = Math.max(ang2, renderer.headCaps1[0]);
            ang2 = Math.min(ang2, renderer.headCaps1[1]);
            Vector4 dir;
            if (renderer.headAxis == 0)
            {
                dir = new Vector4(renderer.headDir, 0, 0, ang);
            }
            else if (renderer.headAxis == 2)
            {
                dir = new Vector4(0, 0, renderer.headDir, ang);
            }
            else
            {
                dir = new Vector4(0, renderer.headDir, 0, ang);
            }
            Vector4 dir2;
            if (renderer.headAxis2 == 2)
            {
                dir2 = new Vector4(0, 0, renderer.headDir, ang2);
            }
            else if (renderer.headAxis2 == 1)
            {
                dir2 = new Vector4(0, renderer.headDir, 0, ang2);
            }
            else
            {
                dir2 = new Vector4(renderer.headDir, 0, 0, ang2);
            }
            parent.setPostRotations(dir);
            parent.setPostRotations2(dir2);
        }

        int red = 255, green = 255, blue = 255;
        int brightness = entity.getBrightnessForRender();
        int alpha = 255;
        if (entity instanceof IMobColourable)
        {
            IMobColourable poke = (IMobColourable) entity;
            red = poke.getRGBA()[0];
            green = poke.getRGBA()[1];
            blue = poke.getRGBA()[2];
            alpha = poke.getRGBA()[3];
        }
        parent.setRGBAB(new int[] { red, green, blue, alpha, brightness });
        for (String partName : parent.getSubParts().keySet())
        {
            IExtendedModelPart part = parent.getSubParts().get(partName);
            updateSubParts(entity, currentPhase, partialTick, part, headYaw, headPitch, limbSwing);
        }
    }

    private boolean isHead(String partName)
    {
        return renderer.headParts.contains(partName);
    }

    public void setRotationAngles(Vector4 rotations)
    {
        rotateAngle = rotations.w;
        rotateAngleX = rotations.x;
        rotateAngleY = rotations.y;
        rotateAngleZ = rotations.z;
    }

    public void setRotationPoint(float par1, float par2, float par3)
    {
        this.rotationPointX = par1;
        this.rotationPointY = par2;
        this.rotationPointZ = par3;
    }

    public void setRotationPoint(Vector3 point)
    {
        setRotationPoint((float) point.x, (float) point.y, (float) point.z);
    }

}
