package pokecube.core.interfaces;

import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;

/**
 * have blocks which are to be eaten by pokemon as berries implement this interface.
 * 
 * @author thutmose
 *
 */
public interface IBerryFruitBlock {
	public ItemStack getBerryStack(IBlockAccess world, int x, int y, int z);
}
