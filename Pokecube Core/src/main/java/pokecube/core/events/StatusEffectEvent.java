package pokecube.core.events;

import net.minecraft.entity.Entity;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import pokecube.core.interfaces.IPokemob;

/** This event is called to apply the effects of the status. It will by default
 * be handled by Pokecube, with priority listener of LOWEST. Cancel this event
 * to prevent pokecube dealing with it */
@Cancelable
public class StatusEffectEvent extends EntityEvent
{
    final byte     status;
    final IPokemob pokemob;

    public StatusEffectEvent(Entity entity, byte status)
    {
        super(entity);
        this.status = status;
        this.pokemob = (IPokemob) entity;
    }

    public byte getStatus()
    {
        return status;
    }

    public IPokemob getPokemob()
    {
        return pokemob;
    }

}
