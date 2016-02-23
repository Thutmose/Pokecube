package pokecube.modelloader.client.render.tabula.animation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;

import org.w3c.dom.NamedNodeMap;

import com.google.common.collect.Lists;

import pokecube.modelloader.client.render.animation.AnimationRegistry.IPartRenamer;
import pokecube.modelloader.client.render.tabula.components.Animation;
import pokecube.modelloader.client.render.tabula.components.AnimationComponent;

public class BiWalkAnimation extends Animation
{
    public BiWalkAnimation()
    {
        loops = true;
        name = "walking";
    }

    /** Swings legs and arms in opposite directions. Only the parts directly
     * childed to the body need to be added to these sets, any parts childed to
     * them will also be swung by the parent/child system.
     * 
     * @param hl
     *            - left legs
     * @param hr
     *            - right legs
     * @param fl
     *            - left arms
     * @param fr
     *            - right arms
     * @param duration
     *            - time taken for animation
     * @param legAngle
     *            - half - angle covered by legs.
     * @param armAngle
     *            - half - angle covered by arms.
     * @return */
    public BiWalkAnimation init(Set<String> hl, Set<String> hr, Set<String> fl, Set<String> fr, int duration,
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
    public Animation init(NamedNodeMap map, @Nullable IPartRenamer renamer)
    {
        HashSet<String> hl = new HashSet<String>();
        HashSet<String> hr = new HashSet<String>();
        HashSet<String> fl = new HashSet<String>();
        HashSet<String> fr = new HashSet<String>();
        int biwalkdur = 0;
        float walkAngle1 = 20;
        float walkAngle2 = 20;
        String[] lh = map.getNamedItem("leftLeg").getNodeValue().split(":");
        String[] rh = map.getNamedItem("rightLeg").getNodeValue().split(":");
        String[] lf = map.getNamedItem("leftArm").getNodeValue().split(":");
        String[] rf = map.getNamedItem("rightArm").getNodeValue().split(":");

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
        biwalkdur = Integer.parseInt(map.getNamedItem("duration").getNodeValue());
        try
        {
            if (map.getNamedItem("legAngle") != null)
            {
                walkAngle1 = Float.parseFloat(map.getNamedItem("legAngle").getNodeValue());
            }
            if (map.getNamedItem("armAngle") != null)
            {
                walkAngle2 = Float.parseFloat(map.getNamedItem("armAngle").getNodeValue());
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        init(hl, hr, fl, fr, biwalkdur, walkAngle1, walkAngle2);
        return this;
    }
}
