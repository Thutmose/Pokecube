package pokecube.core.events;

import net.minecraftforge.fml.common.eventhandler.Event;
import pokecube.core.interfaces.IPokemob;

public class MoveMessageEvent extends Event
{
    public String         message;
    public final IPokemob sender;

    public MoveMessageEvent(IPokemob sender, String message)
    {
        this.message = message;
        this.sender = sender;
    }
}
