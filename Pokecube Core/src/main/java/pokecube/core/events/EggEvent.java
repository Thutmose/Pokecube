package pokecube.core.events;

import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;
import pokecube.core.items.pokemobeggs.EntityPokemobEgg;

public class EggEvent extends Event 
{
    public static class Hatch extends EggEvent
    {
        public Hatch(Entity egg)
        {
            super((EntityPokemobEgg) egg);
        }
    }
    @Cancelable
    public static class PreHatch extends EggEvent
    {
        public PreHatch(Entity egg)
        {
            super((EntityPokemobEgg) egg);
        }
    }
	@Cancelable
	public static class Lay extends EggEvent
	{
		public Lay(Entity egg)
		{
			super((EntityPokemobEgg) egg);
		}
	}
	
	public static class Place extends EggEvent
	{
		public Place(Entity egg)
		{
			super((EntityPokemobEgg) egg);
		}
	}

	public final Entity placer;

	public final EntityPokemobEgg egg;
	
	private EggEvent(EntityPokemobEgg egg)
	{
		this.placer = egg.getEggOwner();
		this.egg = egg;
	}
}
