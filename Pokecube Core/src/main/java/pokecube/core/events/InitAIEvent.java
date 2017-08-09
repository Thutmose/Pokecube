package pokecube.core.events;

import net.minecraftforge.event.entity.EntityEvent;
import pokecube.core.interfaces.IPokemob;

/** Called after initiating the pokemob's AI. */
public class InitAIEvent extends EntityEvent
{
    private final IPokemob pokemob;

    public InitAIEvent(IPokemob entity)
    {
        super(entity.getEntity());
        this.pokemob = entity;
    }

    public IPokemob getPokemob()
    {
        return pokemob;
    }
}
