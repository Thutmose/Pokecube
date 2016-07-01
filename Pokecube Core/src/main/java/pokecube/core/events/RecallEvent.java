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

}
