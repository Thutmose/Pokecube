package pokecube.modelloader.client.custom.animation;

import java.util.ArrayList;

import javax.annotation.Nullable;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.collect.Lists;

import pokecube.modelloader.client.custom.animation.AnimationRegistry.IPartRenamer;
import pokecube.modelloader.client.tabula.components.Animation;
import pokecube.modelloader.client.tabula.components.AnimationComponent;

public class AnimationBuilder
{
    /** Constructs a new Animation, and assigns components based on the
     * definitions in the XML node.
     * 
     * @param node
     * @param renamer
     * @return */
    public static Animation build(Node node, @Nullable IPartRenamer renamer)
    {
        Animation ret = null;
        if (node.getAttributes().getNamedItem("type") == null) { return null; }
        String animName = node.getAttributes().getNamedItem("type").getNodeValue();

        ret = new Animation();
        ret.name = animName;

        NodeList parts = node.getChildNodes();
        Node temp;
        for (int i = 0; i < parts.getLength(); i++)
        {
            Node part = parts.item(i);
            if (part.getNodeName().equals("part"))
            {
                NodeList components = part.getChildNodes();
                String partName = part.getAttributes().getNamedItem("name").getNodeValue();
                if (renamer != null)
                {
                    String[] names = { partName };
                    renamer.convertToIdents(names);
                    partName = names[0];
                }
                ArrayList<AnimationComponent> set = Lists.newArrayList();
                for (int j = 0; j < components.getLength(); j++)
                {
                    Node component = components.item(j);
                    if (component.getNodeName().equals("component"))
                    {
                        AnimationComponent comp = new AnimationComponent();
                        if ((temp = component.getAttributes().getNamedItem("name")) != null)
                        {
                            comp.name = temp.getNodeValue();
                        }
                        if ((temp = component.getAttributes().getNamedItem("rotChange")) != null)
                        {
                            String[] vals = temp.getNodeValue().split(",");
                            comp.rotChange[0] = Double.parseDouble(vals[0]);
                            comp.rotChange[1] = Double.parseDouble(vals[1]);
                            comp.rotChange[2] = Double.parseDouble(vals[2]);
                        }
                        if ((temp = component.getAttributes().getNamedItem("posChange")) != null)
                        {
                            String[] vals = temp.getNodeValue().split(",");
                            comp.posChange[0] = Double.parseDouble(vals[0]);
                            comp.posChange[1] = Double.parseDouble(vals[1]);
                            comp.posChange[2] = Double.parseDouble(vals[2]);
                        }
                        if ((temp = component.getAttributes().getNamedItem("scaleChange")) != null)
                        {
                            String[] vals = temp.getNodeValue().split(",");
                            comp.scaleChange[0] = Double.parseDouble(vals[0]);
                            comp.scaleChange[1] = Double.parseDouble(vals[1]);
                            comp.scaleChange[2] = Double.parseDouble(vals[2]);
                        }
                        if ((temp = component.getAttributes().getNamedItem("rotOffset")) != null)
                        {
                            String[] vals = temp.getNodeValue().split(",");
                            comp.rotOffset[0] = Double.parseDouble(vals[0]);
                            comp.rotOffset[1] = Double.parseDouble(vals[1]);
                            comp.rotOffset[2] = Double.parseDouble(vals[2]);
                        }
                        if ((temp = component.getAttributes().getNamedItem("posOffset")) != null)
                        {
                            String[] vals = temp.getNodeValue().split(",");
                            comp.posOffset[0] = Double.parseDouble(vals[0]);
                            comp.posOffset[1] = Double.parseDouble(vals[1]);
                            comp.posOffset[2] = Double.parseDouble(vals[2]);
                        }
                        if ((temp = component.getAttributes().getNamedItem("scaleOffset")) != null)
                        {
                            String[] vals = temp.getNodeValue().split(",");
                            comp.scaleOffset[0] = Double.parseDouble(vals[0]);
                            comp.scaleOffset[1] = Double.parseDouble(vals[1]);
                            comp.scaleOffset[2] = Double.parseDouble(vals[2]);
                        }
                        if ((temp = component.getAttributes().getNamedItem("length")) != null)
                        {
                            comp.length = Integer.parseInt(temp.getNodeValue());
                        }
                        if ((temp = component.getAttributes().getNamedItem("startKey")) != null)
                        {
                            comp.startKey = Integer.parseInt(temp.getNodeValue());
                        }
                        if ((temp = component.getAttributes().getNamedItem("opacityChange")) != null)
                        {
                            comp.opacityChange = Double.parseDouble(temp.getNodeValue());
                        }
                        if ((temp = component.getAttributes().getNamedItem("opacityOffset")) != null)
                        {
                            comp.opacityOffset = Double.parseDouble(temp.getNodeValue());
                        }
                        if ((temp = component.getAttributes().getNamedItem("hidden")) != null)
                        {
                            comp.hidden = Boolean.parseBoolean(temp.getNodeValue());
                        }
                        set.add(comp);
                    }
                }
                if (!set.isEmpty())
                {
                    ret.sets.put(partName, set);
                }
            }
        }
        return ret;
    }

    /** Merges animation data from from to to.
     * 
     * @param from
     * @param to */
    public static void merge(Animation from, Animation to)
    {
        for (String s1 : from.sets.keySet())
        {
            // Prioritize to, if to already has animations for that part,
            // skip it.
            if (!to.sets.containsKey(s1))
            {
                to.sets.put(s1, from.sets.get(s1));
            }
        }
    }
}
