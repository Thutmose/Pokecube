package powercrystals.minefactoryreloaded.api;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface INeedleAmmo {

	public boolean onHitEntity(ItemStack stac, EntityPlayer owner, Entity hit, double distance);

	public void onHitBlock(ItemStack stac, EntityPlayer owner, World world, BlockPos pos, EnumFacing side, double distance);
	// TODO: needle entity should be available

	public float getSpread(ItemStack stack);

}
