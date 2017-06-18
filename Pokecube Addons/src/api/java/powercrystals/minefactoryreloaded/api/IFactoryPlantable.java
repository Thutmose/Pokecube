package powercrystals.minefactoryreloaded.api;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Defines a plantable object for use in the Planter.
 *
 * @author PowerCrystals
 */
public interface IFactoryPlantable {

	/**
	 * @return The item this plantable is managing.
	 */
	public Item getSeed();

	/**
	 * @param stack
	 *            The stack being planted.
	 * @param forFermenting
	 *            True if this stack will be converted to biofuel
	 *
	 * @return True if this plantable can be planted (useful for metadata
	 *         items).
	 */
	public boolean canBePlanted(ItemStack stack, boolean forFermenting);

	/**
	 * @param world
	 *            The world instance this block or item will be placed into.
	 * @param pos
	 *            The position.
	 * @param stack
	 *            The stack being planted.
	 *
	 * @return The block that will be placed into the world.
	 */
	public ReplacementBlock getPlantedBlock(World world, BlockPos pos, ItemStack stack);

	/**
	 * @param world
	 *            The world instance this block or item will be placed into.
	 * @param pos
	 *            The position.
	 * @param stack
	 *            The stack being planted.
	 *
	 * @return True if this plantable can be placed at the provided coordinates.
	 */
	public boolean canBePlantedHere(World world, BlockPos pos, ItemStack stack);

}
