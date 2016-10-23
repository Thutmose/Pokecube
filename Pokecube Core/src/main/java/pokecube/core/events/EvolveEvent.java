package pokecube.core.events;

import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IPokemob;

/** Fired whenever the pokemob evolves.
 * 
 * @author Thutmose */
public class EvolveEvent extends LevelUpEvent
{
    /** Called after the evolution. */
    public static class Post extends EvolveEvent
    {
        /** @param mob
         *            - the result of the evolution. */
        public Post(IPokemob mob)
        {
            super(mob);
        }
    }

    /** Called before the evolution, if canceled, it will not evolve. */
    public static class Pre extends EvolveEvent
    {
        public PokedexEntry forme;

        /** @param mob
         *            - The mob doing the evolving.
         * @param evolvingTo
         *            - the mob to be evolved to. */
        public Pre(IPokemob mob, PokedexEntry evolvingTo)
        {
            super(mob);
            forme = evolvingTo;
        }
    }

    public EvolveEvent(IPokemob mob)
    {
        super(mob, mob.getLevel(), mob.getLevel());
    }

}
