package pokecube.modelloader.client.custom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Sets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.FMLClientHandler;
import pokecube.core.client.render.entity.RenderPokemobs;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IMobColourable;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.utils.Vector4;
import pokecube.modelloader.client.custom.animation.AnimationHelper;
import pokecube.modelloader.client.custom.animation.AnimationLoader.Model;
import pokecube.modelloader.client.custom.model.IAnimationChanger;
import pokecube.modelloader.client.custom.model.IExtendedModelPart;
import pokecube.modelloader.client.custom.model.IModel;
import pokecube.modelloader.client.custom.model.IModelRenderer;
import pokecube.modelloader.client.custom.model.IPartTexturer;
import pokecube.modelloader.client.custom.model.IRetexturableModel;
import pokecube.modelloader.client.custom.animation.TextureHelper;
import pokecube.modelloader.client.custom.x3d.X3dModel;
import pokecube.modelloader.client.tabula.components.Animation;
import thut.api.maths.Vector3;

public class DefaultIModelRenderer<T extends EntityLiving> extends RendererLivingEntity<T>implements IModelRenderer<T>
{
    public static final String          DEFAULTPHASE = "idle";
    public String                       name;
    public String                       currentPhase = "idle";
    HashMap<String, PartInfo>           parts;
    HashMap<String, ArrayList<Vector5>> global;
    public HashMap<String, Animation>   animations   = new HashMap<String, Animation>();
    public Set<String>                  headParts    = Sets.newHashSet();
    public TextureHelper                texturer;
    public IAnimationChanger            animator;

    public Vector3 offset    = Vector3.getNewVector();;
    public Vector3 scale     = Vector3.getNewVector();;
    public Vector5 rotations = new Vector5();

    public IModel model;

    public int         headDir        = 2;
    public int         headAxis       = 2;
    public int         headAxis2      = 0;
    /** A set of names of shearable parts. */
    public Set<String> shearableParts = Sets.newHashSet();
    /** A set of namess of dyeable parts. */
    public Set<String> dyeableParts   = Sets.newHashSet();

    public float[] headCaps  = { -180, 180 };
    public float[] headCaps1 = { -20, 40 };

    public float     rotationPointX = 0, rotationPointY = 0, rotationPointZ = 0;
    public float     rotateAngleX   = 0, rotateAngleY = 0, rotateAngleZ = 0, rotateAngle = 0;
    ResourceLocation texture;

    public DefaultIModelRenderer(HashMap<String, PartInfo> parts, HashMap<String, ArrayList<Vector5>> global,
            Model model)
    {
        super(Minecraft.getMinecraft().getRenderManager(), null, 0);
        name = model.name;
        this.parts = parts;
        this.texture = model.texture;
        if (model.model.getResourcePath().contains(".x3d")) this.model = new X3dModel(model.model);

        if (this.model == null) { return; }

        initModelParts();
        if (headDir == 2)
        {
            headDir = (this.model instanceof X3dModel) ? 1 : -1;
        }
        this.global = global;
    }

    public void updateModel(HashMap<String, PartInfo> parts, HashMap<String, ArrayList<Vector5>> global, Model model)
    {
        name = model.name;
        this.parts = parts;
        this.texture = model.texture;
        if (model.model.getResourcePath().contains(".x3d")) this.model = new X3dModel(model.model);
        initModelParts();
        this.global = global;
    }

    private void initModelParts()
    {
        if (model == null) return;

        for (String s : model.getParts().keySet())
        {
            if (model.getParts().get(s).getParent() == null && !parts.containsKey(s))
            {
                PartInfo p = getPartInfo(s);
                parts.put(s, p);
            }
        }
    }

    private HashMap<String, PartInfo> getChildren(IExtendedModelPart part)
    {
        HashMap<String, PartInfo> partsList = new HashMap<String, PartInfo>();
        for (String s : part.getSubParts().keySet())
        {
            PartInfo p = new PartInfo(s);
            IExtendedModelPart subPart = part.getSubParts().get(s);
            p.children = getChildren(subPart);
            partsList.put(s, p);
        }
        return partsList;
    }

    @Override
    public void doRender(T entity, double d, double d1, double d2, float f, float partialTick)
    {
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

        float f13 = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTick;

        f4 = this.handleRotationFloat(entity, partialTick);
        float f6 = entity.prevLimbSwingAmount + (entity.limbSwingAmount - entity.prevLimbSwingAmount) * partialTick;

        if (f6 > 1.0F)
        {
            f6 = 1.0F;
        }
        GL11.glPushMatrix();
        if (animator != null) currentPhase = animator.modifyAnimation(entity, partialTick, currentPhase);
        GlStateManager.disableCull();
        transformGlobal(currentPhase, entity, d, d1, d2, partialTick, f3 - f2, f13);
        updateAnimation(entity, currentPhase, partialTick);

        for (String partName : parts.keySet())
        {
            IExtendedModelPart part = model.getParts().get(partName);
            if (part == null) continue;
            try
            {
                if (texturer != null && part instanceof IRetexturableModel)
                {
                    texturer.bindObject(entity);
                    ((IRetexturableModel) part).setTexturer(texturer);
                }
                if (part.getParent() == null)
                {
                    GL11.glPushMatrix();
                    part.renderAll();
                    GL11.glPopMatrix();
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        GL11.glPopMatrix();

    }

    private void transformGlobal(String currentPhase, Entity entity, double x, double y, double z, float partialTick,
            float rotationYaw, float rotationPitch)
    {
        if (rotations == null)
        {
            rotations = new Vector5();
        }

        this.setRotationAngles(rotations.rotations);
        this.setRotationPoint(offset);

        rotationYaw = -entity.rotationYaw + 180;

        if (entity instanceof IPokemob)
        {
            IPokemob mob = (IPokemob) entity;
            PokedexEntry entry = mob.getPokedexEntry();
            float scale = (mob.getSize());
            GL11.glScalef(scale, scale, scale);
            shadowSize = entry.width * mob.getSize();
        }
        Vector4 yaw = new Vector4(0, 1, 0, rotationYaw);
        this.rotate();
        yaw.glRotate();
        GlStateManager.rotate(-90, 1, 0, 0);
        this.translate();

        if (!scale.isEmpty()) GL11.glScaled(scale.x, scale.y, scale.z);

    }

    private void updateAnimation(Entity entity, String currentPhase, float partialTicks)
    {
        for (String partName : parts.keySet())
        {
            IExtendedModelPart part = model.getParts().get(partName);
            updateSubParts(entity, currentPhase, partialTicks, part);
        }
    }

    private void updateSubParts(Entity entity, String currentPhase, float partialTick, IExtendedModelPart parent)
    {
        if (parent == null) return;

        parent.resetToInit();

        boolean anim = animations.containsKey(currentPhase);

        if (anim)
        {
            if (AnimationHelper.doAnimation(animations.get(currentPhase), entity, parent.getName(), parent,
                    partialTick))
            {
            }
            else if (animations.containsKey(DEFAULTPHASE))
            {
                AnimationHelper.doAnimation(animations.get(DEFAULTPHASE), entity, parent.getName(), parent,
                        partialTick);
            }
        }

        if (isHead(parent.getName()))
        {
            float ang;
            float ang2 = -entity.rotationPitch;
            float head = (entity.getRotationYawHead()) % 360 + 180;
            float diff = 0;
            float body = (entity.rotationYaw) % 360;
            if (headDir == -1) body *= -1;
            else head *= -1;

            diff = (head + body) % 360;

            diff = (diff + 360) % 360;
            diff = (diff - 180) % 360;
            diff = Math.max(diff, headCaps[0]);
            diff = Math.min(diff, headCaps[1]);

            ang = diff;

            ang2 = Math.max(ang2, headCaps1[0]);
            ang2 = Math.min(ang2, headCaps1[1]);

            Vector4 dir;

            if (headAxis == 0)
            {
                dir = new Vector4(headDir, 0, 0, ang);
            }
            else if (headAxis == 2)
            {
                dir = new Vector4(0, 0, headDir, ang);
            }
            else
            {
                dir = new Vector4(0, headDir, 0, ang);
            }
            Vector4 dir2;
            if (headAxis2 == 2)
            {
                dir2 = new Vector4(0, 0, headDir, ang2);
            }
            else if (headAxis2 == 1)
            {
                dir2 = new Vector4(0, headDir, 0, ang2);
            }
            else
            {
                dir2 = new Vector4(headDir, 0, 0, ang2);
            }
            parent.setPostRotations(dir);
            parent.setPostRotations2(dir2);
        }

        int red = 255, green = 255, blue = 255;
        int brightness = entity.getBrightnessForRender(partialTick);
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
            updateSubParts(entity, currentPhase, partialTick, part);
        }
    }

    private boolean isHead(String partName)
    {
        return headParts.contains(partName);
    }

    public void translate()
    {
        GL11.glTranslated(rotationPointX, rotationPointY, rotationPointZ);
    }

    protected void rotate()
    {
        GL11.glRotatef(rotateAngle, rotateAngleX, rotateAngleY, rotateAngleZ);
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

    public void setRotationAngles(Vector4 rotations)
    {
        rotateAngle = rotations.w;
        rotateAngleX = rotations.x;
        rotateAngleY = rotations.y;
        rotateAngleZ = rotations.z;
    }

    private PartInfo getPartInfo(String partName)
    {
        PartInfo ret = null;
        for (PartInfo part : parts.values())
        {
            if (part.name.equalsIgnoreCase(partName)) return part;
            ret = getPartInfo(partName, part);
            if (ret != null) return ret;
        }
        for (IExtendedModelPart part : model.getParts().values())
        {
            if (part.getName().equals(partName))
            {
                PartInfo p = new PartInfo(part.getName());
                p.children = getChildren(part);
                boolean toAdd = true;
                IExtendedModelPart parent = part.getParent();
                while (parent != null && toAdd)
                {
                    toAdd = !parts.containsKey(parent.getName());
                    parent = parent.getParent();
                }
                if (toAdd) parts.put(partName, p);
                return p;
            }
        }

        return ret;
    }

    private PartInfo getPartInfo(String partName, PartInfo parent)
    {
        PartInfo ret = null;
        for (PartInfo part : parent.children.values())
        {
            if (part.name.equalsIgnoreCase(partName)) return part;
            ret = getPartInfo(partName, part);
            if (ret != null) return ret;
        }

        return ret;
    }

    @Override
    protected ResourceLocation getEntityTexture(T var1)
    {
        return RenderPokemobs.getInstance().getEntityTexturePublic(var1);
    }

    @Override
    protected void preRenderCallback(T entity, float f)
    {
        GL11.glEnable(GL11.GL_NORMALIZE);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    }

    @Override
    public void setPhase(String phase)
    {
        currentPhase = phase;
    }

    @Override
    public void renderStatus(T entity, double d, double d1, double d2, float f, float partialTick)
    {
        IPokemob pokemob = (IPokemob) entity;
        byte status;
        if ((status = pokemob.getStatus()) == IMoveConstants.STATUS_NON) return;
        ResourceLocation texture = null;
        if (status == IMoveConstants.STATUS_FRZ)
        {
            texture = FRZ;
        }
        else if (status == IMoveConstants.STATUS_PAR)
        {
            texture = PAR;
        }
        if (texture == null) return;

        FMLClientHandler.instance().getClient().renderEngine.bindTexture(texture);

        float time = (((Entity) pokemob).ticksExisted + partialTick);
        GL11.glPushMatrix();

        float speed = status == IMoveConstants.STATUS_FRZ ? 0.001f : 0.005f;

        GL11.glMatrixMode(GL11.GL_TEXTURE);
        GL11.glLoadIdentity();
        float var5 = time * speed;
        float var6 = time * speed;
        GL11.glTranslatef(var5, var6, 0.0F);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);

        GL11.glEnable(GL11.GL_BLEND);
        float var7 = status == IMoveConstants.STATUS_FRZ ? 0.5f : 1F;
        GL11.glColor4f(var7, var7, var7, 0.5F);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);
        var7 = status == IMoveConstants.STATUS_FRZ ? 1.08f : 1.05F;
        GL11.glScalef(var7, var7, var7);

        doRender(entity, d, d1, d2, f, partialTick);

        GL11.glMatrixMode(GL11.GL_TEXTURE);
        GL11.glLoadIdentity();
        GL11.glMatrixMode(GL11.GL_MODELVIEW);

        GL11.glPopMatrix();
    }

    @Override
    public IPartTexturer getTexturer()
    {
        return texturer;
    }

    @Override
    public HashMap<String, Animation> getAnimations()
    {
        return animations;
    }

    @Override
    public boolean hasPhase(String phase)
    {
        return DefaultIModelRenderer.DEFAULTPHASE.equals(phase) || getAnimations().containsKey(phase);
    }

    public static class Vector5
    {
        public Vector4 rotations;
        public int     time;

        public Vector5(Vector4 rotation, int time)
        {
            this.rotations = rotation;
            this.time = time;
        }

        public Vector5()
        {
            this.time = 0;
            this.rotations = new Vector4();
        }

        @Override
        public String toString()
        {
            return "|r:" + rotations + "|t:" + time;
        }

        public Vector5 interpolate(Vector5 v, float time, boolean wrap)
        {
            if (v.time == 0) return this;
            // wrap = true;

            if (Double.isNaN(rotations.x))
            {
                rotations = new Vector4();
            }
            Vector4 rotDiff = rotations.copy();

            if (rotations.x == rotations.z && rotations.z == rotations.y && rotations.y == rotations.w
                    && rotations.w == 0)
            {
                rotations.x = 1;
            }

            if (!v.rotations.equals(rotations))
            {
                rotDiff = v.rotations.subtractAngles(rotations);

                rotDiff = rotations.addAngles(rotDiff.scalarMult(time));
            }
            if (Double.isNaN(rotDiff.x))
            {
                rotDiff = new Vector4(0, 1, 0, 0);
            }
            Vector5 ret = new Vector5(rotDiff, v.time);
            return ret;
        }
    }
}
