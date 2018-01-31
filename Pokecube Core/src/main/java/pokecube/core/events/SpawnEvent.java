package pokecube.core.events;

import net.minecraft.entity.EntityLiving;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;
import pokecube.core.database.PokedexEntry;
import pokecube.core.events.handlers.SpawnHandler.Variance;
import pokecube.core.interfaces.IPokemob;
import thut.api.maths.Vector3;

public class SpawnEvent extends Event
{
    @Cancelable
    public static class Despawn extends SpawnEvent
    {
        public final IPokemob pokemob;

        public Despawn(Vector3 location, World world, IPokemob pokemob_)
        {
            super(pokemob_.getPokedexEntry(), location, world);
            pokemob = pokemob_;
        }

    }

    /** Called right before the pokemob is spawned into the world. Cancelling
     * this does nothing.<br>
     * pokemob is the pokemob entity which is about to spawn. */
    @Cancelable
    public static class Post extends SpawnEvent
    {
        public final IPokemob     pokemob;
        public final EntityLiving entity;

        public Post(PokedexEntry entry, Vector3 location, World world, IPokemob pokemob)
        {
            super(entry, location, world);
            this.pokemob = pokemob;
            entity = pokemob.getEntity();
        }
    }

    /** Called before the pokemob is spawned into the world, during the checks
     * for a valid location. <br>
     * Cancelling this will prevent the spawn. */
    @Cancelable
    public static class Pre extends SpawnEvent
    {
        public Pre(PokedexEntry entry, Vector3 location, World world)
        {
            super(entry, location, world);
        }
    }

    /** Called before the pokemob is spawned into the world, during the checks
     * for a valid location. <br>
     * Cancelling this will prevent the spawn. */
    @Cancelable
    @HasResult
    public static class Check extends SpawnEvent
    {
        /** Is this even actually for spawning, or just checking if something
         * can spawn, say in pokedex */
        public final boolean forSpawn;

        public Check(PokedexEntry entry, Vector3 location, World world, boolean forSpawn)
        {
            super(entry, location, world);
            this.forSpawn = forSpawn;
        }
    }

    public static class Pick extends SpawnEvent
    {
        private PokedexEntry pick;

        public Pick(PokedexEntry entry_, Vector3 location_, World world_)
        {
            super(entry_, location_, world_);
            pick = entry_;
        }

        public Vector3 getLocation()
        {
            return location;
        }

        public void setLocation(Vector3 loc)
        {
            location.set(loc);
        }

        public PokedexEntry getPicked()
        {
            return pick;
        }

        public void setPick(PokedexEntry toPick)
        {
            pick = toPick;
        }

        /** Called when a location is initially chosen for spawn. The initial
         * entry handed here will be null, it will be filled in by Pokecube with
         * an appropriate spawn (if is chosen), with event priority of HIGHEST.
         * anything that sets this afterwards will override default pick. */
        public static class Pre extends Pick
        {
            public Pre(PokedexEntry entry_, Vector3 location_, World world_)
            {
                super(entry_, location_, world_);
            }
        }

        /** This is called after Pre is called, but only if the result from Pre
         * was not null. This one allows modifying the spawn based on the spawn
         * that was chosen before. */
        public static class Post extends Pick
        {
            public Post(PokedexEntry entry_, Vector3 location_, World world_)
            {
                super(entry_, location_, world_);
            }
        }

        /** This is used to edit the pokedex entry directly before the mob is
         * constructed. It allows bypassing all of the rest of the spawn */
        public static class Final extends Pick
        {
            public Final(PokedexEntry entry_, Vector3 location_, World worldObj_)
            {
                super(entry_, location_, worldObj_);
            }

            private String args = "";

            public String getSpawnArgs()
            {
                return args;
            }

            public void setSpawnArgs(String args)
            {
                if (args == null) args = "";
                this.args = args;
            }
        }
    }

    /** Called after spawn lvl for a mob is chosen, use setLevel if you wish to
     * change the level that it spawns at. */
    public static class Level extends SpawnEvent
    {
        private int            level;
        private final Variance variance;
        private final int      original;

        public int getLevel()
        {
            return level;
        }

        public int getInitialLevel()
        {
            return original;
        }

        public void setLevel(int level)
        {
            this.level = level;
        }

        public Variance getExpectedVariance()
        {
            return variance;
        }

        public Level(PokedexEntry entry_, Vector3 location_, World world, int level, Variance variance)
        {
            super(entry_, location_, world);
            this.level = level;
            this.original = level;
            this.variance = variance;
        }

    }

    /** Called when a pokemob is sent out from the cube. */
    public static class SendOut extends SpawnEvent
    {
        public final IPokemob     pokemob;
        public final EntityLiving entity;

        protected SendOut(PokedexEntry entry, Vector3 location, World world, IPokemob pokemob)
        {
            super(entry, location, world);
            this.pokemob = pokemob;
            entity = pokemob.getEntity();
        }

        /** Called before sending out, cancelling this will result in the cube
         * either sitting on the ground, or trying to return to sender's
         * inventory. This is called right before spawning the pokemob into the
         * world. */
        @Cancelable
        public static class Pre extends SendOut
        {
            public Pre(PokedexEntry entry, Vector3 location, World world, IPokemob pokemob)
            {
                super(entry, location, world, pokemob);
            }
        }

        public static class Post extends SendOut
        {
            public Post(PokedexEntry entry, Vector3 location, World world, IPokemob pokemob)
            {
                super(entry, location, world, pokemob);
            }
        }
    }

    public final PokedexEntry entry;

    public final Vector3      location;

    public final World        world;

    public SpawnEvent(PokedexEntry entry_, Vector3 location_, World world_)
    {
        entry = entry_;
        location = location_;
        world = world_;
    }
}
