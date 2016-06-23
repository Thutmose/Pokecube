package pokecube.core.items.pokecubes;

import net.minecraft.block.BlockDispenser;
import net.minecraft.dispenser.IBehaviorDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.dispenser.IPosition;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraftforge.common.util.FakePlayer;
import pokecube.core.PokecubeItems;
import pokecube.core.interfaces.PokecubeMod;
import thut.api.maths.Vector3;

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
        Vector3 loc = Vector3.getNewVector().set(iblocksource.getX(), iblocksource.getY(), iblocksource.getZ());
        IPosition dir = BlockDispenser.getDispensePosition(iblocksource);
        Vector3 facing = Vector3.getNewVector().set(dir.getX(), dir.getY(), dir.getZ());
        float yaw = 0;
        float pitch = 0;
        // TODO properly find angles from vector.
        if (dir.getZ() < 0)
        {
            yaw = 180;
        }
        if (dir.getX() > 0)
        {
            yaw = -90;
        }
        if (dir.getX() < 0)
        {
            yaw = 90;
        }
        if (dir.getY() > 0)
        {
            pitch = -90;
        }
        if (dir.getY() < 0)
        {
            pitch = 90;
        }

        player.rotationYaw = yaw;
        player.rotationPitch = pitch;
        player.rotationYawHead = yaw;

        if (itemstack.getItem() == PokecubeItems.pokemobEgg)
        {
            //TODO dir to enumfacing for this.
            itemstack.onItemUse(player, iblocksource.getWorld(), loc.addTo(facing).getPos(), EnumHand.MAIN_HAND,
                    EnumFacing.UP, 0.5f, 0.5f, 0.5f);
        }
        else
        {
            itemstack.useItemRightClick(iblocksource.getWorld(), player, EnumHand.MAIN_HAND);
        }
        itemstack.splitStack(1);
        return itemstack;
    }

}
