package pokecube.modelloader.client.tabula.components;

import com.google.common.collect.Ordering;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.modelloader.client.tabula.animation.AnimationRegistry.IPartRenamer;

import java.util.ArrayList;
import java.util.TreeMap;
import java.util.Map.Entry;

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
