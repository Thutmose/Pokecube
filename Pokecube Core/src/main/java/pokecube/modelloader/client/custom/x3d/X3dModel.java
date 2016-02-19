package pokecube.modelloader.client.custom.x3d;

import static java.lang.Math.toDegrees;

import java.util.ArrayList;
import java.util.HashMap;

import net.minecraft.util.ResourceLocation;
import pokecube.modelloader.client.custom.model.IExtendedModelPart;
import pokecube.modelloader.client.custom.model.IModel;
import pokecube.modelloader.client.custom.model.IModelCustom;
import pokecube.modelloader.client.custom.model.IPartTexturer;
import pokecube.modelloader.client.custom.model.IRetexturableModel;
import thut.api.maths.Vector3;

public class X3dModel implements IModelCustom, IModel, IRetexturableModel
{
    private HashMap<String, IExtendedModelPart> parts = new HashMap<String, IExtendedModelPart>();
    public String                               name;

    public X3dModel()
    {

    }

    public X3dModel(ResourceLocation l)
    {
        this();
        loadModel(l);

    }

    HashMap<String, IExtendedModelPart> makeObjects(X3dXMLParser parser) throws Exception
    {
        HashMap<String, HashMap<String, String>> partTranslations = parser.partTranslations;
        HashMap<String, ArrayList<String>> childMap = parser.partChildren;

        String name = parser.partName;
        this.name = name;

        for (String s : parser.shapeMap.keySet())
        {
            X3dObject o = new X3dObject(s);
            o.shapes = parser.shapeMap.get(s);
            String[] offset = partTranslations.get(s).get("translation").split(" ");
            o.offset = Vector3.getNewVector().set(Float.parseFloat(offset[0]), Float.parseFloat(offset[1]),
                    Float.parseFloat(offset[2]));
            offset = partTranslations.get(s).get("scale").split(" ");
            o.scale = new Vertex(Float.parseFloat(offset[0]), Float.parseFloat(offset[1]), Float.parseFloat(offset[2]));
            offset = partTranslations.get(s).get("rotation").split(" ");
            o.rotations.set(Float.parseFloat(offset[0]), Float.parseFloat(offset[1]), Float.parseFloat(offset[2]),
                    (float) toDegrees(Float.parseFloat(offset[3])));
            parts.put(s, o);
        }
        for (String s : parts.keySet())
        {
            if (childMap.containsKey(s))
            {
                for (String s1 : childMap.get(s))
                {
                    if (parts.get(s1) != null) parts.get(s).addChild(parts.get(s1));
                }
            }
        }
        return parts;
    }

    public void loadModel(ResourceLocation model)
    {
        X3dXMLParser parser = new X3dXMLParser(model);
        parser.parse();

        try
        {
            makeObjects(parser);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    @Override
    public void renderAll()
    {
        for (IExtendedModelPart o : parts.values())
        {
            o.renderAll();
        }
    }

    @Override
    public void renderOnly(String... groupNames)
    {
        for (String s : groupNames)
            if (parts.containsKey(s)) parts.get(s).renderAll();
    }

    @Override
    public void renderAllExcept(String... excludedGroupNames)
    {
        for (String s : parts.keySet())
        {
            boolean skipPart = false;
            for (String excludedGroupName : excludedGroupNames)
            {
                if (excludedGroupName.equalsIgnoreCase(s))
                {
                    skipPart = true;
                }
            }
            if (!skipPart)
            {
                parts.get(s).renderAll();
            }
        }
    }

    @Override
    public void renderPart(String partName)
    {
        if (parts.containsKey(partName)) parts.get(partName).renderPart(partName);
    }

    @Override
    public HashMap<String, IExtendedModelPart> getParts()
    {
        return parts;
    }

    @Override
    public void setTexturer(IPartTexturer texturer)
    {
        for (IExtendedModelPart part : parts.values())
        {
            if (part instanceof IRetexturableModel) ((IRetexturableModel) part).setTexturer(texturer);
        }
    }
}
