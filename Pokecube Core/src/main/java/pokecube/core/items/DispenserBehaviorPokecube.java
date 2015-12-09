package pokecube.core.items;

import net.minecraft.block.BlockDispenser;
import net.minecraft.dispenser.IBehaviorDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.util.FakePlayer;
import pokecube.core.interfaces.PokecubeMod;

public class DispenserBehaviorPokecube implements IBehaviorDispenseItem {

	@Override
	public ItemStack dispense(IBlockSource iblocksource, ItemStack itemstack) {

		FakePlayer player = PokecubeMod.getFakePlayer();
		player.worldObj = iblocksource.getWorld();
		player.posX = iblocksource.getX();
		player.posY = iblocksource.getY() - player.getEyeHeight();
		player.posZ = iblocksource.getZ();
		EnumFacing dir = BlockDispenser.getFacing(iblocksource.getBlockMetadata());
		float yaw = 0;
		if(dir==EnumFacing.NORTH)
		{
			yaw = 180;
		}
		if(dir==EnumFacing.EAST)
		{
			yaw = -90;
		}
		if(dir==EnumFacing.WEST)
		{
			yaw = 90;
		}

        player.rotationYaw = yaw;
        player.rotationYawHead = yaw;
		
        System.out.println(player.getLookVec()+" "+dir);
        
		itemstack.useItemRightClick(iblocksource.getWorld(), player);
		itemstack.splitStack(1);
		return itemstack;
	}

}
