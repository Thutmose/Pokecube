package pokecube.modelloader.client.tabula.animation;

import java.util.HashMap;

import javax.annotation.Nullable;

import org.w3c.dom.NamedNodeMap;

import com.google.common.collect.Maps;

import pokecube.modelloader.client.tabula.components.Animation;

/** Used for determining what animation to make when reading from XMLs
 * 
 * @author Thutmose */
public class AnimationRegistry
{
    /** Map of XML node name to animation to read in. */
    public static HashMap<String, Class<? extends Animation>> animations = Maps.newHashMap();

    /** Add in defaults. */
    static
    {
        animations.put("quadWalk", QuadWalkAnimation.class);
        animations.put("biWalk", BiWalkAnimation.class);
        animations.put("flap", BasicFlapAnimation.class);
        animations.put("advFlap", AdvancedFlapAnimation.class);
    }

    /** Generates the animation for the given name, and nodemap. Renamer is used
     * to convert to identifiers in the cases where that is needed.
     * 
     * @param name
     * @param map
     * @param renamer
     * @return */
    public static Animation make(String name, NamedNodeMap map, @Nullable IPartRenamer renamer)
    {
        Animation ret = null;
        Class<? extends Animation> toMake = animations.get(name);
        if (toMake != null)
        {
            try
            {
                ret = toMake.newInstance();
                ret.init(map, renamer);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return ret;
    }

    public static interface IPartRenamer
    {
        void convertToIdents(String[] names);
    }
}
