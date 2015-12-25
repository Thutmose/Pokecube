package pokecube.modelloader.client.custom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
import pokecube.core.client.render.entity.RenderPokemobs;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.utils.Vector4;
import pokecube.modelloader.client.custom.animation.AnimationHelper;
import pokecube.modelloader.client.custom.animation.AnimationLoader.Model;
import pokecube.modelloader.client.custom.x3d.X3dModel;
import pokecube.modelloader.client.tabula.components.Animation;
import thut.api.maths.Vector3;

public class LoadedModel<T extends EntityLiving> extends RendererLivingEntity<T>
{
    public static final String          DEFAULTPHASE = "idle";
    public String                       name;
    public String                       currentPhase = "idle";
    HashMap<String, PartInfo>           parts;
    HashMap<String, ArrayList<Vector5>> global;
    public HashMap<String, Animation>   animations   = new HashMap<String, Animation>();
    public Set<String>                  headParts    = Sets.newHashSet();

    public Vector3 offset    = Vector3.getNewVectorFromPool();;
    public Vector3 scale     = Vector3.getNewVectorFromPool();;
    public Vector5 rotations = new Vector5();

    public IModel model;

    public int         headDir        = 2;
    public int         headAxis       = -3;
    public int         headAxis2      = -3;
    /** A set of names of shearable parts. */
    public Set<String> shearableParts = Sets.newHashSet();
    /** A set of namess of dyeable parts. */
    public Set<String> dyeableParts   = Sets.newHashSet();

    public float[] headCaps = { -180, 180 };

    public float     rotationPointX = 0, rotationPointY = 0, rotationPointZ = 0;
    public float     rotateAngleX   = 0, rotateAngleY = 0, rotateAngleZ = 0, rotateAngle = 0;
    ResourceLocation texture;

    public LoadedModel(HashMap<String, PartInfo> parts, HashMap<String, ArrayList<Vector5>> global, Model model)
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
        if (headAxis == -3)
        {
            headAxis = (this.model instanceof X3dModel) ? 1 : 2;
        }
        if (headAxis2 == -3)
        {
            headAxis2 = (this.model instanceof X3dModel) ? 1 : 2;
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

    public HashSet<IExtendedModelPart> getAllParts()
    {
        HashSet<IExtendedModelPart> ret = new HashSet<IExtendedModelPart>();

        ret.addAll(model.getParts().values());

        return ret;
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
        this.preRenderCallback(entity, partialTick);
        float f6 = entity.prevLimbSwingAmount + (entity.limbSwingAmount - entity.prevLimbSwingAmount) * partialTick;

        if (f6 > 1.0F)
        {
            f6 = 1.0F;
        }
        GL11.glPushMatrix();

        transformGlobal(currentPhase, entity, d, d1, d2, partialTick, f3 - f2, f13);
        updateAnimation(entity, currentPhase, partialTick);

        for (String partName : parts.keySet())
        {
            IExtendedModelPart part = model.getParts().get(partName);
            if (part == null) continue;
            try
            {
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
            float ang = (entity.rotationYaw) % 360;
            float ang2 = -entity.rotationPitch;
            float te = -entity.getRotationYawHead();

            ang += te;

            if (ang > 180) ang -= 360;

            ang = Math.max(ang, headCaps[0]);
            ang = Math.min(ang, headCaps[1]);

            ang2 = Math.max(ang2, headCaps[0]);
            ang2 = Math.min(ang2, headCaps[1]);
            Vector4 dir;

            headAxis = 2;
            headAxis2 = 0;
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
        if (entity instanceof IPokemob)
        {
            IPokemob poke = (IPokemob) entity;
            red = poke.getColours()[0] * 2;
            green = poke.getColours()[1] * 2;
            blue = poke.getColours()[2] * 2;
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

    /** Returns a rotation angle that is inbetween two other rotation angles.
     * par1 and par2 are the angles between which to interpolate, par3 is
     * probably a float between 0.0 and 1.0 that tells us where "between" the
     * two angles we are. Example: par1 = 30, par2 = 50, par3 = 0.5, then return
     * = 40 */
    public float interpolateRotation(float low, float high, float diff)
    {
        float f3;

        for (f3 = high - low; f3 < -180.0F; f3 += 360.0F)
        {
            ;
        }

        while (f3 >= 180.0F)
        {
            f3 -= 360.0F;
        }

        return low + diff * f3;
    }
}
