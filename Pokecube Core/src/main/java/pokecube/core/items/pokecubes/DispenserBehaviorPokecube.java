package pokecube.core.items.pokecubes;

import net.minecraft.block.BlockDispenser;
import net.minecraft.dispenser.IBehaviorDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.util.FakePlayer;
import pokecube.core.PokecubeItems;
import pokecube.core.interfaces.PokecubeMod;

public class DispenserBehaviorPokecube implements IBehaviorDispenseItem
{
    @Override
    public ItemStack dispense(IBlockSource iblocksource, ItemStack itemstack)
    {
        FakePlayer player = PokecubeMod.getFakePlayer();
        player.worldObj = iblocksource.getWorld();
        player.posX = iblocksource.getX();
        player.posY = iblocksource.getY() - player.getEyeHeight();
        player.posZ = iblocksource.getZ();
        EnumFacing dir = BlockDispenser.getFacing(iblocksource.getBlockMetadata());
        float yaw = 0;
        float pitch = 0;
        if (dir == EnumFacing.NORTH)
        {
            yaw = 180;
        }
        if (dir == EnumFacing.EAST)
        {
            yaw = -90;
        }
        if (dir == EnumFacing.WEST)
        {
            yaw = 90;
        }
        if (dir == EnumFacing.UP)
        {
            pitch = -90;
        }
        if (dir == EnumFacing.DOWN)
        {
            pitch = 90;
        }

        player.rotationYaw = yaw;
        player.rotationPitch = pitch;
        player.rotationYawHead = yaw;

        if (itemstack.getItem() == PokecubeItems.pokemobEgg)
        {
            itemstack.onItemUse(player, iblocksource.getWorld(), iblocksource.getBlockPos().offset(dir), dir, 0.5f, 0.5f, 0.5f);
        }
        else
        {
            itemstack.useItemRightClick(iblocksource.getWorld(), player);
        }
        // itemstack.splitStack(1);
        return itemstack;
    }
}
