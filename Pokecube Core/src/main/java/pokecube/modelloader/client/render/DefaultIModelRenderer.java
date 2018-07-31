package pokecube.modelloader.client.render;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.common.collect.Maps;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import thut.api.maths.Vector3;
import thut.core.client.render.animation.ModelHolder;
import thut.core.client.render.model.IExtendedModelPart;
import thut.core.client.render.model.ModelFactory;
import thut.core.client.render.tabula.components.Animation;
import thut.core.client.render.wrappers.ModelWrapper;

public class DefaultIModelRenderer<T extends EntityLiving> extends AbstractModelRenderer<T>
{
    public String                           name;
    public HashMap<String, PartInfo>        parts        = Maps.newHashMap();
    HashMap<String, ArrayList<Vector5>>     global;
    public HashMap<String, List<Animation>> animations   = Maps.newHashMap();
    public Vector3                          offset       = Vector3.getNewVector();;
    public Vector3                          scale        = Vector3.getNewVector();

    public Vector5                          rotations    = new Vector5();

    public ModelWrapper                     model;

    ResourceLocation                        texture;

    public DefaultIModelRenderer(HashMap<String, ArrayList<Vector5>> global, ModelHolder model)
    {
        super(Minecraft.getMinecraft().getRenderManager(), null, 0);
        name = model.name;
        this.model = new ModelWrapper(model, this);
        ModelWrapperEvent evt = new ModelWrapperEvent(this.model, name);
        MinecraftForge.EVENT_BUS.post(evt);
        this.model = evt.wrapper;
        this.mainModel = this.model;
        this.texture = model.texture;
        this.model.imodel = ModelFactory.create(model);
        if (this.model.imodel == null) { return; }
        initModelParts();
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
    public boolean hasAnimation(String phase, Entity entity)
    {
        return DefaultIModelRenderer.DEFAULTPHASE.equals(phase) || animations.containsKey(phase)
                || model.imodel.getBuiltInAnimations().contains(phase);
    }

    public void updateModel(HashMap<String, ArrayList<Vector5>> global, ModelHolder model)
    {
        name = model.name;
        this.texture = model.texture;
        initModelParts();
        this.global = global;
    }

    @Override
    public HashMap<String, List<Animation>> getAnimations()
    {
        return animations;
    }

    @Override
    public Vector3 getScale()
    {
        return scale;
    }

    @Override
    public Vector3 getRotationOffset()
    {
        return offset;
    }

    @Override
    public Vector5 getRotations()
    {
        return rotations;
    }
}
