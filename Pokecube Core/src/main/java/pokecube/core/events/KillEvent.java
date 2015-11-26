package pokecube.core.events;

import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;
import pokecube.core.interfaces.IPokemob;

@Cancelable
public class KillEvent extends Event {
	public final IPokemob killer;
	public final IPokemob killed;
	
	public KillEvent(IPokemob killer, IPokemob killed)
	{
		this.killed = killed;
		this.killer = killer;
	}
	
}
