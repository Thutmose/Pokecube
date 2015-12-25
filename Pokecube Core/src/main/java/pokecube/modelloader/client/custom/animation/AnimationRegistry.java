package pokecube.modelloader.client.custom.animation;

import java.util.HashMap;

import javax.annotation.Nullable;

import org.w3c.dom.NamedNodeMap;

import com.google.common.collect.Maps;

import pokecube.modelloader.client.tabula.animation.AdvancedFlapAnimation;
import pokecube.modelloader.client.tabula.animation.BasicFlapAnimation;
import pokecube.modelloader.client.tabula.animation.BiWalkAnimation;
import pokecube.modelloader.client.tabula.animation.QuadWalkAnimation;
import pokecube.modelloader.client.tabula.animation.SnakeMovement;
import pokecube.modelloader.client.tabula.components.Animation;

/** Used for determining what animation to make when reading from XMLs
 * 
 * @author Thutmose */
public class AnimationRegistry
{
    /** Map of XML node name to animation to read in. */
    public static HashMap<String, Class<? extends Animation>> animations      = Maps.newHashMap();
    /** Map of XML name to animation phase, will overwrite animation name with
     * the value. */
    public static HashMap<String, String>                     animationPhases = Maps.newHashMap();

    /** Add in defaults. */
    static
    {
        animations.put("quadWalk", QuadWalkAnimation.class);
        animations.put("biWalk", BiWalkAnimation.class);
        animations.put("flap", BasicFlapAnimation.class);
        animations.put("advFlap", AdvancedFlapAnimation.class);
        animations.put("snakeWalk", SnakeMovement.class);
        animations.put("snakeFly", SnakeMovement.class);
        animationPhases.put("snakeFly", "flying");
        animations.put("snakeSwim", SnakeMovement.class);
        animationPhases.put("snakeSwim", "swimming");
    }

    /** Generates the animation for the given name, and nodemap. Renamer is used
     * to convert to identifiers in the cases where that is needed. <br>
     * <br>
     * This method will also then change the name of the animation to
     * animationName if it is not null.
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
                if (animationPhases.containsKey(name))
                {
                    ret.name = animationPhases.get(name);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return ret;
    }

    /** Used to convert from part names to identifiers if needed.
     * 
     * @author Thutmose */
    public static interface IPartRenamer
    {
        void convertToIdents(String[] names);
    }
}
