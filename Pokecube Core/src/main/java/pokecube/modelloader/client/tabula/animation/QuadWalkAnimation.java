package pokecube.modelloader.client.tabula.animation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.w3c.dom.NamedNodeMap;

import com.google.common.collect.Lists;

import pokecube.modelloader.client.custom.animation.AnimationRegistry.IPartRenamer;
import pokecube.modelloader.client.tabula.components.Animation;
import pokecube.modelloader.client.tabula.components.AnimationComponent;

public class QuadWalkAnimation extends Animation
{
    public QuadWalkAnimation()
    {
        loops = true;
        name = "walking";
    }

    /** Swings legs and arms in opposite directions. Only the parts directly
     * childed to the body need to be added to these sets, any parts childed to
     * them will also be swung by the parent/child system.
     * 
     * @param hl
     *            - left hind legs
     * @param hr
     *            - right hind legs
     * @param fl
     *            - left front legs
     * @param fr
     *            - right front legs
     * @param duration
     *            - time taken for animation
     * @param legAngle
     *            - half - angle covered by hind legs.
     * @param armAngle
     *            - half - angle covered by front legs.
     * @return */
    public QuadWalkAnimation init(Set<String> hl, Set<String> hr, Set<String> fl, Set<String> fr, int duration,
            float legAngle, float armAngle)
    {
        for (String s : hr)
        {
            String ident = "";
            AnimationComponent component1 = new AnimationComponent();
            component1.length = duration / 4;
            component1.name = ident + "1";
            component1.identifier = ident + "1";
            component1.startKey = 0;
            component1.rotChange[0] = legAngle;

            AnimationComponent component2 = new AnimationComponent();
            component2.length = duration / 2;
            component2.name = ident + "2";
            component2.identifier = ident + "2";
            component2.startKey = duration / 4;
            component2.rotChange[0] = -2 * legAngle;

            AnimationComponent component3 = new AnimationComponent();
            component3.length = duration / 4;
            component3.name = ident + "3";
            component3.identifier = ident + "3";
            component3.startKey = 3 * duration / 4;
            component3.rotChange[0] = -legAngle;

            ArrayList<AnimationComponent> set = Lists.newArrayList();

            set.add(component1);
            set.add(component2);
            set.add(component3);
            sets.put(s, set);
        }
        for (String s : hl)
        {
            String ident = "";
            AnimationComponent component1 = new AnimationComponent();
            component1.length = duration / 4;
            component1.name = ident + "1";
            component1.identifier = ident + "1";
            component1.startKey = 0;
            component1.rotChange[0] = -legAngle;

            AnimationComponent component2 = new AnimationComponent();
            component2.length = duration / 2;
            component2.name = ident + "2";
            component2.identifier = ident + "2";
            component2.startKey = duration / 4;
            component2.rotChange[0] = 2 * legAngle;

            AnimationComponent component3 = new AnimationComponent();
            component3.length = duration / 4;
            component3.name = ident + "3";
            component3.identifier = ident + "3";
            component3.startKey = 3 * duration / 4;
            component3.rotChange[0] = legAngle;

            ArrayList<AnimationComponent> set = Lists.newArrayList();

            set.add(component1);
            set.add(component2);
            set.add(component3);
            sets.put(s, set);
        }
        for (String s : fr)
        {
            String ident = "";
            AnimationComponent component1 = new AnimationComponent();
            component1.length = duration / 4;
            component1.name = ident + "1";
            component1.identifier = ident + "1";
            component1.startKey = 0;
            component1.rotChange[0] = armAngle;

            AnimationComponent component2 = new AnimationComponent();
            component2.length = duration / 2;
            component2.name = ident + "2";
            component2.identifier = ident + "2";
            component2.startKey = duration / 4;
            component2.rotChange[0] = -2 * armAngle;

            AnimationComponent component3 = new AnimationComponent();
            component3.length = duration / 4;
            component3.name = ident + "3";
            component3.identifier = ident + "3";
            component3.startKey = 3 * duration / 4;
            component3.rotChange[0] = -armAngle;

            ArrayList<AnimationComponent> set = Lists.newArrayList();

            set.add(component1);
            set.add(component2);
            set.add(component3);
            sets.put(s, set);
        }
        for (String s : fl)
        {
            String ident = "";
            AnimationComponent component1 = new AnimationComponent();
            component1.length = duration / 4;
            component1.name = ident + "1";
            component1.identifier = ident + "1";
            component1.startKey = 0;
            component1.rotChange[0] = -armAngle;

            AnimationComponent component2 = new AnimationComponent();
            component2.length = duration / 2;
            component2.name = ident + "2";
            component2.identifier = ident + "2";
            component2.startKey = duration / 4;
            component2.rotChange[0] = 2 * armAngle;

            AnimationComponent component3 = new AnimationComponent();
            component3.length = duration / 4;
            component3.name = ident + "3";
            component3.identifier = ident + "3";
            component3.startKey = 3 * duration / 4;
            component3.rotChange[0] = armAngle;

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
        HashSet<String> hl = new HashSet<String>();
        HashSet<String> hr = new HashSet<String>();
        HashSet<String> fl = new HashSet<String>();
        HashSet<String> fr = new HashSet<String>();
        int quadwalkdur = 0;
        float walkAngle1 = 20;
        float walkAngle2 = 20;
        String[] lh = map.getNamedItem("leftHind").getNodeValue().split(":");
        String[] rh = map.getNamedItem("rightHind").getNodeValue().split(":");
        String[] lf = map.getNamedItem("leftFront").getNodeValue().split(":");
        String[] rf = map.getNamedItem("rightFront").getNodeValue().split(":");

        if (renamer != null)
        {
            renamer.convertToIdents(lh);
            renamer.convertToIdents(rh);
            renamer.convertToIdents(lf);
            renamer.convertToIdents(rf);
        }
        for (String s : lh)
            if (s != null) hl.add(s);
        for (String s : rh)
            if (s != null) hr.add(s);
        for (String s : rf)
            if (s != null) fr.add(s);
        for (String s : lf)
            if (s != null) fl.add(s);
        if (map.getNamedItem("angle") != null)
        {
            walkAngle1 = Float.parseFloat(map.getNamedItem("angle").getNodeValue());
        }
        if (map.getNamedItem("frontAngle") != null)
        {
            walkAngle2 = Float.parseFloat(map.getNamedItem("frontAngle").getNodeValue());
        }
        else
        {
            walkAngle2 = walkAngle1;
        }
        quadwalkdur = Integer.parseInt(map.getNamedItem("duration").getNodeValue());

        init(hl, hr, fl, fr, quadwalkdur, walkAngle1, walkAngle2);
        return this;
    }
}
