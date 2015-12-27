package pokecube.modelloader.client.tabula.components;

import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.modelloader.client.custom.animation.AnimationRegistry.IPartRenamer;

import java.util.ArrayList;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Nullable;

import org.w3c.dom.NamedNodeMap;

/** Container for Tabula animations.
 *
 * @author Gegy1000
 * @since 0.1.0 */
@SideOnly(Side.CLIENT)
public class Animation
{
    public String name;
    public String identifier;
    public int    length = -1;

    public boolean loops;
    
    private Set<String> checked = Sets.newHashSet();

    public TreeMap<String, ArrayList<AnimationComponent>> sets = new TreeMap<String, ArrayList<AnimationComponent>>(
            Ordering.natural()); // cube identifier to animation component

    public String toString()
    {
        return name + "|" + identifier + "|" + loops;
    }

    public Animation init(NamedNodeMap map, @Nullable IPartRenamer renamer)
    {
        return this;
    }

    public int getLength()
    {
        return length;
    }
    
    public ArrayList<AnimationComponent> getComponents(String key)
    {
        if(!checked.contains(key))
        {
            ArrayList<AnimationComponent> comps = null;
            for(String s: sets.keySet())
            {
                if(s.startsWith("*") && key.matches(s.substring(1)))
                {
                    comps = sets.get(s);
                    break;
                }
            }
            if(comps!=null)
            {
                sets.put(key, comps);
            }
            checked.add(key);
        }
        return sets.get(key);
    }
    
    public void initLength()
    {
        for (Entry<String, ArrayList<AnimationComponent>> entry : sets.entrySet())
        {
            for (AnimationComponent component : entry.getValue())
            {
                if (component.startKey + component.length > length)
                {
                    length = component.startKey + component.length;
                }
            }
        }
    }
}
