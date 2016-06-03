package pokecube.core.events;

import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.common.eventhandler.Event;
import pokecube.core.interfaces.IPokemob;

public class MoveMessageEvent extends Event
{
    public ITextComponent         message;
    public final IPokemob sender;

    public MoveMessageEvent(IPokemob sender, ITextComponent message)
    {
        this.message = message;
        this.sender = sender;
    }
}
