package pokecube.core.events;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

public class StarterEvent extends Event {

	@Cancelable
	@HasResult
	public static class Pre extends StarterEvent
	{
		public Pre(EntityPlayer player)
		{
			super(player, null, 0);
		}
	}
	public final EntityPlayer player;
	public ItemStack[] starterPack;
	
	public final int pick;

	public StarterEvent(EntityPlayer player, ItemStack[] pack, int numberPicked) {
		this.player = player;
		starterPack = pack;
		pick = numberPicked;
	}
	
}
