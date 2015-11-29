package pokecube.adventures.items;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import pokecube.core.items.ItemPokemobUseable;

public class ItemExpShare extends ItemPokemobUseable 
{
	public ItemExpShare()
	{
		super();
		setHasSubtypes(true);
	}
    
    @Override
    public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer player)
    {
    	if(world.isRemote)
    		return itemstack;
    	return itemstack;
    }
    
}
