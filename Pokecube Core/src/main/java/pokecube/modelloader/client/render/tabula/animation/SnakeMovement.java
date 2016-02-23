package pokecube.modelloader.client.render.tabula.animation;

import java.util.ArrayList;

import org.w3c.dom.NamedNodeMap;

import com.google.common.collect.Lists;

import pokecube.modelloader.client.render.animation.AnimationRegistry.IPartRenamer;
import pokecube.modelloader.client.render.tabula.components.Animation;
import pokecube.modelloader.client.render.tabula.components.AnimationComponent;

public class SnakeMovement extends Animation
{
    public SnakeMovement()
    {
        loops = true;
        name = "walking";
    }

    public SnakeMovement init(ArrayList<String> parts, int duration, float maxAngle, int axis)
    {
        float angle = maxAngle;
        int num = parts.size();
        int mod = num > 6 ? 3 : 2;
        for (int i = 0; i < parts.size(); i++)
        {
            String ident = "";
            String s = parts.get(i);

            angle = (float) (Math.sin(i * Math.PI / (2 * mod) - Math.PI / 2) * maxAngle);
            
            // Sets right wing to -start angle (up), then swings it down by
            // angle.
            AnimationComponent component1 = new AnimationComponent();
            component1.length = duration / 4;
            component1.name = ident + "1";
            component1.identifier = ident + "1";
            component1.startKey = 0;
            component1.rotChange[axis] = angle;
            // Swings the wing from angle up to -angle. Start key is right after
            // end of 1
            AnimationComponent component2 = new AnimationComponent();
            component2.length = duration / 2;
            component2.name = ident + "2";
            component2.identifier = ident + "2";
            component2.startKey = duration / 4;
            component2.rotChange[axis] = -2 * angle;
            // Swings the wing from -angle back down to starting angle. Start
            // key is right after end of 2
            AnimationComponent component3 = new AnimationComponent();
            component3.length = duration / 4;
            component3.name = ident + "3";
            component3.identifier = ident + "3";
            component3.startKey = 3 * duration / 4;
            component3.rotChange[axis] = angle;

            ArrayList<AnimationComponent> set = Lists.newArrayList();

            set.add(component1);
            set.add(component2);
            set.add(component3);
            sets.put(s, set);
        }
        return this;
    }

    @Override
    public Animation init(NamedNodeMap map, IPartRenamer renamer)
    {
        ArrayList<String> parts = new ArrayList<String>();
        int duration = 40;
        int axis = 1;
        float maxAngle = 10;

        String[] partsArr = map.getNamedItem("parts").getNodeValue().split(":");

        if (renamer != null)
        {
            renamer.convertToIdents(partsArr);
        }

        for (String s : partsArr)
            if (s != null) parts.add(s);

        if (map.getNamedItem("angle") != null)
        {
            maxAngle = Float.parseFloat(map.getNamedItem("angle").getNodeValue());
        }
        if (map.getNamedItem("axis") != null)
        {
            axis = Integer.parseInt(map.getNamedItem("axis").getNodeValue());
        }
        if (map.getNamedItem("length") != null)
        {
            duration = Integer.parseInt(map.getNamedItem("length").getNodeValue());
        }
        if (map.getNamedItem("duration") != null)
        {
            duration = Integer.parseInt(map.getNamedItem("duration").getNodeValue());
        }
        this.init(parts, duration, maxAngle, axis);
        return this;
    }
}
