package pokecube.modelloader.client.tabula.animation;

import java.util.ArrayList;
import java.util.Set;

import com.google.common.collect.Lists;

import pokecube.modelloader.client.tabula.components.Animation;
import pokecube.modelloader.client.tabula.components.AnimationComponent;

public class BasicFlapAnimation extends Animation
{
    public BasicFlapAnimation()
    {
        loops = true;
        name = "flying";
    }
    
    /**
     * Moves the wings to angle of start, then flaps up to angle, down to -angle and back to start.
     * Only the parts directly childed to the body
     * need to be added to these sets, any parts childed
     * to them will also be swung by the parent/child system.
     * 
     * @param lw - set of left wings
     * @param rw - set of right wings
     * @param duration - time taken for entire flap.
     * @param angle - half - angle flapped over
     * @param start - initial angle moved to to start flapping
     * @param axis - axis used for flapping around.
     * @return
     */
    public BasicFlapAnimation init(Set<String> lw, Set<String> rw, int duration, float angle, float start, int axis)
    {
        for(String s: rw)
        {
            String ident = "";
            //Sets right wing to -start angle (up), then swings it down by angle.
            AnimationComponent component1 = new AnimationComponent();
            component1.length = duration/4;
            component1.name = ident+"1";
            component1.identifier = ident+"1";
            component1.startKey = 0;
            component1.rotOffset[axis] = -start;
            component1.rotChange[axis] = angle;
            //Swings the wing from angle up to -angle.  Start key is right after end of 1
            AnimationComponent component2 = new AnimationComponent();
            component2.length = duration/2;
            component2.name = ident+"2";
            component2.identifier = ident+"2";
            component2.startKey = duration/4;
            component2.rotChange[axis] = -2*angle;
            //Swings the wing from -angle back down to starting angle.  Start key is right after end of 2
            AnimationComponent component3 = new AnimationComponent();
            component3.length = duration/4;
            component3.name = ident+"3";
            component3.identifier = ident+"3";
            component3.startKey = 3*duration/4;
            component3.rotChange[axis] = -angle;
            
            ArrayList<AnimationComponent> set = Lists.newArrayList();

            set.add(component1);
            set.add(component2);
            set.add(component3);
            sets.put(s, set);
        }
        //Angles and timing are same numbers for Right Wings, but angles are reversed, as are opposite sides.
        for(String s: lw)
        {
            String ident = "";
            AnimationComponent component1 = new AnimationComponent();
            component1.length = duration/4;
            component1.name = ident+"1";
            component1.identifier = ident+"1";
            component1.startKey = 0;
            component1.rotOffset[axis] = start;
            component1.rotChange[axis] = -angle;
            
            AnimationComponent component2 = new AnimationComponent();
            component2.length = duration/2;
            component2.name = ident+"2";
            component2.identifier = ident+"2";
            component2.startKey = duration/4;
            component2.rotChange[axis] = 2*angle;
            
            AnimationComponent component3 = new AnimationComponent();
            component3.length = duration/4;
            component3.name = ident+"3";
            component3.identifier = ident+"3";
            component3.startKey = 3*duration/4;
            component3.rotChange[axis] = angle;
            
            ArrayList<AnimationComponent> set = Lists.newArrayList();

            set.add(component1);
            set.add(component2);
            set.add(component3);
            sets.put(s, set);
        }
        return this;
    }
}
