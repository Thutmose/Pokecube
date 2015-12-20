package pokecube.core.events;

import pokecube.core.interfaces.IPokemob;

/** Fired whenever the pokemob evolves.
 * 
 * @author Thutmose */
public class EvolveEvent extends LevelUpEvent
{
    public EvolveEvent(IPokemob mob)
    {
        super(mob, mob.getLevel(), mob.getLevel());
    }

    /** Called before the evolution, if canceled, it will not evolve.<br>
     * 
     * @author Thutmose */
    public static class Pre extends EvolveEvent
    {
        public String forme;

        /** @param mob
         *            - The mob doing the evolving.
         * @param evolvingTo
         *            - the mob to be evolved to. */
        public Pre(IPokemob mob, String evolvingTo)
        {
            super(mob);
            forme = evolvingTo;
        }
    }

    /** Called after the evolution.<br>
     * 
     * @author Thutmose */
    public static class Post extends EvolveEvent
    {
        /** @param mob
         *            - the result of the evolution. */
        public Post(IPokemob mob)
        {
            super(mob);
        }
    }

}
