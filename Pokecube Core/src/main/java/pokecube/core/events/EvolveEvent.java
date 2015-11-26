package pokecube.core.events;

import pokecube.core.interfaces.IPokemob;

public class EvolveEvent extends LevelUpEvent
{

	public EvolveEvent(IPokemob mob)
	{
		super(mob, mob.getLevel(), mob.getLevel());
	}

}
