package pokecube.core.interfaces;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

/** have blocks which are to be eaten by pokemon as berries implement this
 * interface.
 * 
 * @author Thutmose */
public interface IBerryFruitBlock
{
    public ItemStack getBerryStack(IBlockAccess world, BlockPos pos);
}
