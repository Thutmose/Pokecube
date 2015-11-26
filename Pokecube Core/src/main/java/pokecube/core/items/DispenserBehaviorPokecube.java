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

//    private final BehaviorDefaultDispenseItem behaviourDefaultDispenseItem = new BehaviorDefaultDispenseItem();
    
	@Override
	public ItemStack dispense(IBlockSource iblocksource, ItemStack itemstack) {

		FakePlayer player = PokecubeMod.getFakePlayer();
		
		player.posX = iblocksource.getX();
		player.posY = iblocksource.getY() - player.getEyeHeight();
		player.posZ = iblocksource.getZ();
		
		float yaw = 0;
		if(BlockDispenser.getFacing(iblocksource.getBlockMetadata())==EnumFacing.NORTH)
		{
			yaw = 180;
		}
		if(BlockDispenser.getFacing(iblocksource.getBlockMetadata())==EnumFacing.EAST)
		{
			yaw = 90;
		}
		if(BlockDispenser.getFacing(iblocksource.getBlockMetadata())==EnumFacing.WEST)
		{
			yaw = -90;
		}
		
		float f = 1.5f;
		
		double motionX = -MathHelper.sin((player.rotationYaw / 180F) * (float)Math.PI) * MathHelper.cos((player.rotationPitch / 180F) * (float)Math.PI) * f;
        double motionZ = MathHelper.cos((player.rotationYaw / 180F) * (float)Math.PI) * MathHelper.cos((player.rotationPitch / 180F) * (float)Math.PI) * f;
        double motionY = -MathHelper.sin((player.rotationPitch / 180F) * (float)Math.PI) * f;
		
        player.posX+= motionX;
        player.posY+= motionY;
        player.posZ+= motionZ;
        
		player.rotationYaw = yaw;
		
		itemstack.useItemRightClick(iblocksource.getWorld(), player);
		itemstack.splitStack(1);
		return itemstack;
	}

}
