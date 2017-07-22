package pokecube.core.events;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;

/** This event is called before the pokemob receives orders to execute an
 * attack. Cancelling this event will prevent the pokemob from actually
 * recieving said order. */
@Cancelable
public class CommandAttackEvent extends EntityEvent
{
    private Entity target;

    public CommandAttackEvent(@Nonnull Entity entity, @Nullable Entity target)
    {
        super(entity);
        this.target = target;
    }

    /** Target of this attack command.
     * 
     * @return */
    public Entity getTarget()
    {
        return target;
    }

    /** Sets the target of the attack command.
     * 
     * @param target */
    public void setTarget(@Nullable Entity target)
    {
        this.target = target;
    }

    public IPokemob getPokemob()
    {
        return CapabilityPokemob.getPokemobFor(getEntity());
    }
}
