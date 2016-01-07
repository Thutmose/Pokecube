package pokecube.modelloader.client.custom.animation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.entity.EntityLiving;
import pokecube.modelloader.client.custom.IAnimationChanger;
import pokecube.modelloader.client.tabula.components.Animation;
import pokecube.modelloader.client.tabula.components.AnimationComponent;

public class AnimationRandomizer implements IAnimationChanger
{
    // TODO way to clean this up.
    Map<Integer, AnimationSet>         running = Maps.newHashMap();
    Map<String, List<RandomAnimation>> sets    = Maps.newHashMap();

    Map<String, Set<LoadedAnimSet>> loadedSets = Maps.newHashMap();

    public AnimationRandomizer(Node node)
    {
        NodeList parts = node.getChildNodes();
        for (int i = 0; i < parts.getLength(); i++)
        {
            Node part = parts.item(i);
            if (part.getAttributes() == null) continue;
            String parent = part.getAttributes().getNamedItem("parent").getNodeValue();
            String name = part.getAttributes().getNamedItem("name").getNodeValue();
            double chance = Double.parseDouble(part.getAttributes().getNamedItem("chance").getNodeValue());
            LoadedAnimSet set = new LoadedAnimSet();
            set.chance = chance;
            set.name = name;
            Set<LoadedAnimSet> sets = loadedSets.get(parent);
            if (sets == null) loadedSets.put(parent, sets = Sets.newHashSet());
            sets.add(set);
        }
    }

    public void init(Set<Animation> existingAnimations)
    {
        Set<String> animations = Sets.newHashSet();
        for (Animation existing : existingAnimations)
        {
            if (loadedSets.containsKey(existing.name)) animations.add(existing.name);
        }
        for (String s : animations)
        {
            Set<LoadedAnimSet> set = loadedSets.get(s);
            for (LoadedAnimSet loaded : set)
            {
                for (Animation anim : existingAnimations)
                {
                    if (anim.name.equals(loaded.name))
                    {
                        addAnimationSet(anim, loaded.chance, s);
                        break;
                    }
                }
            }
        }
        System.out.println(sets);
    }

    private void addAnimationSet(Animation animation, double chance, String parent)
    {
        List<RandomAnimation> anims = sets.get(parent);
        if (anims == null) sets.put(parent, anims = Lists.newArrayList());
        anims.add(new RandomAnimation(animation, chance));
    }

    @Override
    public String modifyAnimation(EntityLiving entity, float partialTicks, String phase)
    {
        if (running.containsKey(entity.getEntityId()))
        {
            AnimationSet anim = running.get(entity.getEntityId());
            phase = anim.anim.name;
            if (anim.set < entity.ticksExisted)
            {
                running.remove(entity.getEntityId());
            }
            return phase;
        }
        else if (sets.containsKey(phase))
        {
            List<RandomAnimation> set = sets.get(phase);
            int rand = entity.getRNG().nextInt(set.size());
            RandomAnimation anim = set.get(rand);
            if (Math.random() < anim.chance)
            {
                AnimationSet aSet = new AnimationSet(anim);
                aSet.set = entity.ticksExisted + aSet.anim.duration;
                running.put(entity.getEntityId(), aSet);
                phase = anim.name;
            }
        }
        return phase;
    }

    private static class RandomAnimation
    {
        final String name;
        double       chance   = 0.005;
        int          duration = 0;

        public RandomAnimation(Animation animation, double chance)
        {
            this.chance = chance;
            this.name = animation.name;
            for (Entry<String, ArrayList<AnimationComponent>> entry : animation.sets.entrySet())
            {
                for (AnimationComponent component : entry.getValue())
                {
                    if (component.startKey + component.length > duration)
                    {
                        duration = component.startKey + component.length;
                    }
                }
            }
        }
    }

    private static class AnimationSet
    {
        final RandomAnimation anim;
        int                   set;

        public AnimationSet(RandomAnimation anim)
        {
            this.anim = anim;
        }

    }

    private static class LoadedAnimSet
    {
        String name;
        double chance;
    }
}
