package pokecube.core.interfaces;

import java.util.HashMap;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import pokecube.core.events.CaptureEvent;
import thut.api.maths.Vector3;

public interface IPokecube
{

    public static abstract class PokecubeBehavior
    {
        /** Adds it to the list of behaviours to run when a pokecube is used.
         * 
         * @param cubeId
         * @param behavior */
        public static void addCubeBehavior(int cubeId, PokecubeBehavior behavior)
        {
            map.put(cubeId, behavior);
        }

        public abstract void onPostCapture(CaptureEvent.Post evt);

        /** Called before the pokemob is captured, cancel the event to prevent
         * capture from occuring.
         * 
         * @param evt */
        public abstract void onPreCapture(CaptureEvent.Pre evt);
    }

    /** These are used for custom behavior which can be modified during the
     * various capture events. */
    public static HashMap<Integer, PokecubeBehavior> map = new HashMap<Integer, PokecubeBehavior>();

    /** this is the capture strength of the cube, 0 is never capture, 255 is
     * always capture.
     * 
     * @param mob
     * @param pokecubeId
     * @return */
    double getCaptureModifier(IPokemob mob, int pokecubeId);

    default double getCaptureModifier(EntityLivingBase mob, int pokecubeId)
    {
        return (mob instanceof IPokemob) ? getCaptureModifier((IPokemob) mob, pokecubeId) : 0;
    }

    /** Used for throwing cubes out into the air without a specific target.
     * 
     * @param world
     * @param player
     * @param cube
     * @param direction
     * @param power
     * @return */
    boolean throwPokecube(World world, EntityLivingBase thrower, ItemStack cube, Vector3 direction, float power);

    /** Used to throw the pokecube at a specific target
     * 
     * @param world
     * @param player
     * @param cube
     * @param targetLocation
     * @param target
     * @return */
    boolean throwPokecubeAt(World world, EntityLivingBase thrower, ItemStack cube, @Nullable Vector3 targetLocation,
            @Nullable Entity target);

    default boolean canCapture(EntityLiving hit, ItemStack cube)
    {
        return hit instanceof IPokemob;
    }
}
