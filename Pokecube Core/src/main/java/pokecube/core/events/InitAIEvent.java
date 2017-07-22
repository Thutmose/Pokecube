package pokecube.core.events;

import net.minecraft.entity.Entity;
import net.minecraftforge.event.entity.EntityEvent;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;

/** Called after initiating the pokemob's AI. */
public class InitAIEvent extends EntityEvent
{
    public InitAIEvent(Entity entity)
    {
        super(entity);
    }

    public IPokemob getPokemob()
    {
        return CapabilityPokemob.getPokemobFor(getEntity());
    }
}
