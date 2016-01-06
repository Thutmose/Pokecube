package pokecube.core.interfaces;

import java.util.HashMap;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import pokecube.core.events.CaptureEvent;
import thut.api.maths.Vector3;

public interface IPokecube
{

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

    /** Called when the pokecube is attempted to be thrown.
     * 
     * @param world
     * @param player
     * @param cube
     * @param targetLocation
     * @param target
     * @return */
    boolean throwPokecube(World world, EntityPlayer player, ItemStack cube, Vector3 targetLocation, Entity target);

    public static abstract class PokecubeBehavior
    {
        /** Called before the pokemob is captured, cancel the event to prevent
         * capture from occuring.
         * 
         * @param evt */
        public abstract void onPreCapture(CaptureEvent.Pre evt);

        public abstract void onPostCapture(CaptureEvent.Post evt);

        /** Adds it to the list of behaviours to run when a pokecube is used.
         * 
         * @param cubeId
         * @param behavior */
        public static void addCubeBehavior(int cubeId, PokecubeBehavior behavior)
        {
            map.put(cubeId, behavior);
        }
    }
}
