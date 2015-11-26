package pokecube.core.items.berries;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

/**
 * This is just here so the berries can use the custom renderer.
 * @author Patrick
 *
 */
public class TileEntityBerryFruit extends TileEntity
{
	int berryId = -1;
	String berry = "";
	
	
	public ItemStack getBerry()
	{
		if(berryId == -1)
		{
			for(Integer i: BerryManager.berryFruits.keySet())
			{
				if(BerryManager.berryFruits.get(i) == this.getBlockType())
				{
					berry = BerryManager.berryNames.get(i);
					berryId = i;
					break;
				}
			}
		}
		
		return BerryManager.getBerryItem(berry);
	}
}
