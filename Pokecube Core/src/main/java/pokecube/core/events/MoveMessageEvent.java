package pokecube.core.events;

import net.minecraft.util.IChatComponent;
import net.minecraftforge.fml.common.eventhandler.Event;
import pokecube.core.interfaces.IPokemob;

public class MoveMessageEvent extends Event
{
    public IChatComponent message;
    public final IPokemob sender;

    public MoveMessageEvent(IPokemob sender, IChatComponent message)
    {
        this.message = message;
        this.sender = sender;
    }
}
