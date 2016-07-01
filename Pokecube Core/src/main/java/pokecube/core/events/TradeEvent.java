package pokecube.core.events;

import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Event;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.items.pokecubes.PokecubeManager;

public class TradeEvent extends Event
{
	public final ItemStack cube;
	public final IPokemob mob;
	public TradeEvent(World world, ItemStack pokecube)
	{
		cube = pokecube;
		mob = PokecubeManager.itemToPokemob(pokecube, world);
	}

}
