package pokecube.core.events;

import net.minecraft.entity.EntityLiving;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IPokemob;
import thut.api.maths.Vector3;

@Cancelable
public class SpawnEvent extends Event
{
    public static class Despawn extends SpawnEvent
    {
        public final IPokemob pokemob;

        public Despawn(Vector3 location, World worldObj, IPokemob pokemob_)
        {
            super(pokemob_.getPokedexEntry(), location, worldObj);
            pokemob = pokemob_;
        }

    }

    /** Called right before the pokemob is spawned into the world. Cancelling
     * this does nothing.<br>
     * pokemob is the pokemob entity which is about to spawn.
     * 
     * @author Thutmose */
    public static class Post extends SpawnEvent
    {
        public final IPokemob     pokemob;
        public final EntityLiving entity;

        public Post(PokedexEntry entry, Vector3 location, World worldObj, IPokemob pokemob)
        {
            super(entry, location, worldObj);
            this.pokemob = pokemob;
            entity = (EntityLiving) pokemob;
        }
    }

    /** Called before the pokemob is spawned into the world, during the checks
     * for a valid location. <br>
     * Cancelling this will prevent the spawn.
     * 
     * @author Thutmose */
    public static class Pre extends SpawnEvent
    {
        public Pre(PokedexEntry entry, Vector3 location, World worldObj)
        {
            super(entry, location, worldObj);
        }
    }

    /** Called before the pokemob is spawned into the world, during the checks
     * for a valid location. <br>
     * Cancelling this will prevent the spawn.
     * 
     * @author Thutmose */
    public static class Check extends SpawnEvent
    {
        /** Is this even actually for spawning, or just checking if something
         * can spawn, say in pokedex */
        public final boolean forSpawn;

        public Check(PokedexEntry entry, Vector3 location, World worldObj, boolean forSpawn)
        {
            super(entry, location, worldObj);
            this.forSpawn = forSpawn;
        }
    }

    /** Called when a pokemob is sent out from the cube. */
    public static class SendOut extends SpawnEvent
    {
        public final IPokemob     pokemob;
        public final EntityLiving entity;

        protected SendOut(PokedexEntry entry, Vector3 location, World worldObj, IPokemob pokemob)
        {
            super(entry, location, worldObj);
            this.pokemob = pokemob;
            entity = (EntityLiving) pokemob;
        }

        /** Called before sending out, cancelling this will result in the cube
         * either sitting on the ground, or trying to return to sender's
         * inventory. This is called right before spawning the pokemob into the
         * world. */
        @Cancelable
        public static class Pre extends SendOut
        {
            public Pre(PokedexEntry entry, Vector3 location, World worldObj, IPokemob pokemob)
            {
                super(entry, location, worldObj, pokemob);
                // TODO Auto-generated constructor stub
            }
        }

        public static class Post extends SendOut
        {
            public Post(PokedexEntry entry, Vector3 location, World worldObj, IPokemob pokemob)
            {
                super(entry, location, worldObj, pokemob);
                // TODO Auto-generated constructor stub
            }
        }
    }

    public final PokedexEntry entry;

    public final Vector3      location;

    public final World        world;

    public SpawnEvent(PokedexEntry entry_, Vector3 location_, World worldObj_)
    {
        entry = entry_;
        location = location_;
        world = worldObj_;
    }
}
