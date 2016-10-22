package pokecube.core.events;

import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;
import pokecube.core.interfaces.IPokemob;

@Cancelable
public class RecallEvent extends Event
{
    public final IPokemob recalled;

    public RecallEvent(IPokemob pokemob)
    {
        recalled = pokemob;
    }

    @Cancelable
    /** fired before any other logic is done, this should be used if you want to
     * completely cancel recalling, and do no other processing */
    public static class Pre extends RecallEvent
    {
        public Pre(IPokemob pokemob)
        {
            super(pokemob);
        }
    }
}
