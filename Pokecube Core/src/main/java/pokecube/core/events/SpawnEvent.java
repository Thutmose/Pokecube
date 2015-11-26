package pokecube.core.events;

import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IPokemob;
import thut.api.maths.Vector3;

@Cancelable
public class SpawnEvent extends Event
{
	public final PokedexEntry entry;
	public final Vector3 location;
	public final World world;
	public SpawnEvent(PokedexEntry entry_, Vector3 location_, World worldObj_)
	{
		entry = entry_;
		location = location_;
		world = worldObj_;
	}

	public static class Despawn extends SpawnEvent
	{
		public final IPokemob pokemob;
		public Despawn(Vector3 location, World worldObj, IPokemob pokemob_)
		{
			super(pokemob_.getPokedexEntry(), location, worldObj);
			pokemob = pokemob_;
		}
		
	}
	public static class Spawn extends SpawnEvent
	{
		public Spawn(PokedexEntry entry, Vector3 location, World worldObj)
		{
			super(entry, location, worldObj);
		}
	}
}
