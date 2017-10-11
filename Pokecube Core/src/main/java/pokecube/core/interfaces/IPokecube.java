package pokecube.core.interfaces;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.RegistryBuilder;
import pokecube.core.events.CaptureEvent;
import pokecube.core.events.CaptureEvent.Post;
import pokecube.core.events.CaptureEvent.Pre;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import thut.api.maths.Vector3;

public interface IPokecube
{
    public static IForgeRegistry<PokecubeBehavior> BEHAVIORS = new RegistryBuilder<PokecubeBehavior>()
            .setIDRange(0, Short.MAX_VALUE).setType(PokecubeBehavior.class)
            .setName(new ResourceLocation(PokecubeMod.ID, "pokecubes")).create();

    public static abstract class PokecubeBehavior
            extends IForgeRegistryEntry.Impl<PokecubeBehavior>
    {
        // Whoever registers the default pokecube should set this.
        public static ResourceLocation DEFAULTCUBE = null;
        public static ResourceLocation POKESEAL    = null;

        /** Adds it to the list of behaviours to run when a pokecube is used.
         * 
         * @param cubeId
         * @param behavior */
        public static void addCubeBehavior(PokecubeBehavior behavior)
        {
            BEHAVIORS.register(behavior);
        }

        public abstract void onPostCapture(CaptureEvent.Post evt);

        /** Called before the pokemob is captured, cancel the event to prevent
         * capture from occuring.
         * 
         * @param evt */
        public abstract void onPreCapture(CaptureEvent.Pre evt);

        /** this is the capture strength of the cube, 0 is never capture, 255 is
         * always capture. Override this and place something other than -1 if
         * you wish to use it.
         * 
         * @param mob
         * @param pokecubeId
         * @return */
        public abstract double getCaptureModifier(IPokemob mob);

        /** Additional flat bonus to capture rate, this is mostly used for
         * HeavyCube's flat adjustment rate.
         * 
         * @param mob
         * @return */
        public int getAdditionalBonus(IPokemob mob)
        {
            return 0;
        }

        /** Allows modifications of updates for the pokemob.
         * 
         * @param mob */
        public void onUpdate(IPokemob mob)
        {

        }
    }

    /** helper class so extensions don't need to include blank onPostCapture and
     * onPreCapture. */
    public abstract static class DefaultPokecubeBehavior extends PokecubeBehavior
    {

        @Override
        public void onPostCapture(Post evt)
        {
        }

        @Override
        public void onPreCapture(Pre evt)
        {
        }
    }

    public static class NormalPokecubeBehavoir extends DefaultPokecubeBehavior
    {
        final double rate;

        public NormalPokecubeBehavoir(double rate)
        {
            this.rate = rate;
        }

        @Override
        public double getCaptureModifier(IPokemob mob)
        {
            return rate;
        }

    }

    /** this is the capture strength of the cube, 0 is never capture, 255 is
     * always capture.
     * 
     * @param mob
     * @param pokecubeId
     * @return */
    double getCaptureModifier(IPokemob mob, ResourceLocation pokecubeId);

    default double getCaptureModifier(EntityLivingBase mob, ResourceLocation pokecubeId)
    {
        IPokemob pokemob = CapabilityPokemob.getPokemobFor(mob);
        return (pokemob != null) ? getCaptureModifier(pokemob, pokecubeId) : 0;
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
        return CapabilityPokemob.getPokemobFor(hit) != null;
    }

}
