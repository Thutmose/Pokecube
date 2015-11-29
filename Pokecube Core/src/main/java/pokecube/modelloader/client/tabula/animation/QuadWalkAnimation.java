package pokecube.modelloader.client.tabula.animation;

import java.util.ArrayList;
import java.util.Set;

import com.google.common.collect.Lists;

import pokecube.modelloader.client.tabula.components.Animation;
import pokecube.modelloader.client.tabula.components.AnimationComponent;

public class QuadWalkAnimation extends Animation
{
    public QuadWalkAnimation()
    {
        loops = true;
    }
    
    public QuadWalkAnimation init(Set<String> hl, Set<String> hr, Set<String>fl, Set<String>fr, int duration, float legAngle, float armAngle)
    {
        for(String s: hr)
        {
            String ident = "";
            AnimationComponent component1 = new AnimationComponent();
            component1.length = duration/4;
            component1.name = ident+"1";
            component1.identifier = ident+"1";
            component1.startKey = 0;
            component1.rotChange[0] = legAngle;
            
            AnimationComponent component2 = new AnimationComponent();
            component2.length = duration/2;
            component2.name = ident+"2";
            component2.identifier = ident+"2";
            component2.startKey = duration/4;
            component2.rotChange[0] = -2*legAngle;
            
            AnimationComponent component3 = new AnimationComponent();
            component3.length = duration/4;
            component3.name = ident+"3";
            component3.identifier = ident+"3";
            component3.startKey = 3*duration/4;
            component3.rotChange[0] = -legAngle;
            
            ArrayList<AnimationComponent> set = Lists.newArrayList();

            set.add(component1);
            set.add(component2);
            set.add(component3);
            sets.put(s, set);
        }
        for(String s: hl)
        {
            String ident = "";
            AnimationComponent component1 = new AnimationComponent();
            component1.length = duration/4;
            component1.name = ident+"1";
            component1.identifier = ident+"1";
            component1.startKey = 0;
            component1.rotChange[0] = -legAngle;
            
            AnimationComponent component2 = new AnimationComponent();
            component2.length = duration/2;
            component2.name = ident+"2";
            component2.identifier = ident+"2";
            component2.startKey = duration/4;
            component2.rotChange[0] = 2*legAngle;
            
            AnimationComponent component3 = new AnimationComponent();
            component3.length = duration/4;
            component3.name = ident+"3";
            component3.identifier = ident+"3";
            component3.startKey = 3*duration/4;
            component3.rotChange[0] = legAngle;
            
            ArrayList<AnimationComponent> set = Lists.newArrayList();

            set.add(component1);
            set.add(component2);
            set.add(component3);
            sets.put(s, set);
        }
        for(String s: fr)
        {
            String ident = "";
            AnimationComponent component1 = new AnimationComponent();
            component1.length = duration/4;
            component1.name = ident+"1";
            component1.identifier = ident+"1";
            component1.startKey = 0;
            component1.rotChange[0] = armAngle;
            
            AnimationComponent component2 = new AnimationComponent();
            component2.length = duration/2;
            component2.name = ident+"2";
            component2.identifier = ident+"2";
            component2.startKey = duration/4;
            component2.rotChange[0] = -2*armAngle;
            
            AnimationComponent component3 = new AnimationComponent();
            component3.length = duration/4;
            component3.name = ident+"3";
            component3.identifier = ident+"3";
            component3.startKey = 3*duration/4;
            component3.rotChange[0] = -armAngle;
            
            ArrayList<AnimationComponent> set = Lists.newArrayList();

            set.add(component1);
            set.add(component2);
            set.add(component3);
            sets.put(s, set);
        }
        for(String s: fl)
        {
            String ident = "";
            AnimationComponent component1 = new AnimationComponent();
            component1.length = duration/4;
            component1.name = ident+"1";
            component1.identifier = ident+"1";
            component1.startKey = 0;
            component1.rotChange[0] = -armAngle;
            
            AnimationComponent component2 = new AnimationComponent();
            component2.length = duration/2;
            component2.name = ident+"2";
            component2.identifier = ident+"2";
            component2.startKey = duration/4;
            component2.rotChange[0] = 2*armAngle;
            
            AnimationComponent component3 = new AnimationComponent();
            component3.length = duration/4;
            component3.name = ident+"3";
            component3.identifier = ident+"3";
            component3.startKey = 3*duration/4;
            component3.rotChange[0] = armAngle;
            
            ArrayList<AnimationComponent> set = Lists.newArrayList();

            set.add(component1);
            set.add(component2);
            set.add(component3);
            sets.put(s, set);
        }
        
        return this;
    }
}
