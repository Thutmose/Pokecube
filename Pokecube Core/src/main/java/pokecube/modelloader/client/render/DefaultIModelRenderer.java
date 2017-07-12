package pokecube.modelloader.client.render;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import pokecube.modelloader.client.render.AnimationLoader.Model;
import pokecube.modelloader.client.render.wrappers.ModelWrapper;
import thut.api.maths.Vector3;
import thut.api.maths.Vector4;
import thut.core.client.render.model.IAnimationChanger;
import thut.core.client.render.model.IExtendedModelPart;
import thut.core.client.render.model.IPartTexturer;
import thut.core.client.render.tabula.components.Animation;
import thut.core.client.render.x3d.X3dModel;

public class DefaultIModelRenderer<T extends EntityLiving> extends AbstractModelRenderer<T>
{
    public static class Vector5
    {
        public Vector4 rotations;
        public int     time;

        public Vector5()
        {
            this.time = 0;
            this.rotations = new Vector4();
        }

        public Vector5(Vector4 rotation, int time)
        {
            this.rotations = rotation;
            this.time = time;
        }

        public Vector5 interpolate(Vector5 v, float time, boolean wrap)
        {
            if (v.time == 0) return this;

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

        @Override
        public String toString()
        {
            return "|r:" + rotations + "|t:" + time;
        }
    }

    public static final String          DEFAULTPHASE      = "idle";
    public String                       name;
    public String                       currentPhase      = "idle";
    public HashMap<String, PartInfo>    parts             = Maps.newHashMap();
    HashMap<String, ArrayList<Vector5>> global;
    public HashMap<String, Animation>   animations        = new HashMap<String, Animation>();
    public Set<String>                  headParts         = Sets.newHashSet();
    public TextureHelper                texturer;

    public IAnimationChanger            animator;;
    public Vector3                      offset            = Vector3.getNewVector();;
    public Vector3                      scale             = Vector3.getNewVector();

    public Vector5                      rotations         = new Vector5();

    public ModelWrapper                 model;
    public int                          headDir           = 2;
    public int                          headAxis          = 2;
    public int                          headAxis2         = 0;
    /** A set of names of shearable parts. */
    public Set<String>                  shearableParts    = Sets.newHashSet();

    /** A set of namess of dyeable parts. */
    public Set<String>                  dyeableParts      = Sets.newHashSet();
    public float[]                      headCaps          = { -180, 180 };

    public float[]                      headCaps1         = { -20, 40 };
    ResourceLocation                    texture;

    public DefaultIModelRenderer(HashMap<String, ArrayList<Vector5>> global, Model model)
    {
        super(Minecraft.getMinecraft().getRenderManager(), null, 0);
        name = model.name;
        this.model = new ModelWrapper(model, this);
        ModelWrapperEvent evt = new ModelWrapperEvent(this.model, name);
        MinecraftForge.EVENT_BUS.post(evt);
        this.model = evt.wrapper;
        this.mainModel = this.model;
        this.texture = model.texture;
        if (model.model.getResourcePath().contains(".x3d")) this.model.imodel = new X3dModel(model.model);
        if (this.model.imodel == null) { return; }
        initModelParts();
        if (headDir == 2)
        {
            headDir = (this.model.imodel instanceof X3dModel) ? 1 : -1;
        }
        this.global = global;
    }

    @Override
    public void doRender(T entity, double x, double y, double z, float entityYaw, float partialTicks)
    {
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
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
    public IPartTexturer getTexturer()
    {
        return texturer;
    }

    @Override
    public boolean hasPhase(String phase)
    {
        return DefaultIModelRenderer.DEFAULTPHASE.equals(phase) || animations.containsKey(phase);
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

    @Override
    public void setPhase(String phase)
    {
        currentPhase = phase;
    }

    public void updateModel(HashMap<String, ArrayList<Vector5>> global, Model model)
    {
        name = model.name;
        this.texture = model.texture;
        initModelParts();
        this.global = global;
    }

    @Override
    void setStatusRender(boolean value)
    {
        model.statusRender = value;
    }
}
