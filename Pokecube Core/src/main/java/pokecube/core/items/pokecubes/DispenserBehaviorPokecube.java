package pokecube.core.items.pokecubes;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.dispenser.IBehaviorDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraftforge.common.util.FakePlayer;
import pokecube.core.PokecubeItems;
import pokecube.core.interfaces.IPokecube;
import pokecube.core.interfaces.PokecubeMod;
import thut.api.maths.Vector3;

public class DispenserBehaviorPokecube implements IBehaviorDispenseItem
{

    @Override
    public ItemStack dispense(IBlockSource source, ItemStack stack)
    {
        EnumFacing dir = null;
        IBlockState state = source.getBlockState();
        for (IProperty<?> prop : state.getPropertyKeys())
        {
            if (prop.getValueClass() == EnumFacing.class)
            {
                dir = (EnumFacing) state.getValue(prop);
                break;
            }
        }
        if (dir == null) return stack;

        FakePlayer player = PokecubeMod.getFakePlayer(source.getWorld());
        player.posX = source.getX();
        player.posY = source.getY() - player.getEyeHeight();
        player.posZ = source.getZ();

        // Defaults are for south.
        player.rotationPitch = 0;
        player.rotationYaw = 0;

        if (dir == EnumFacing.EAST)
        {
            player.rotationYaw = -90;
        }
        else if (dir == EnumFacing.WEST)
        {
            player.rotationYaw = 90;
        }
        else if (dir == EnumFacing.NORTH)
        {
            player.rotationYaw = 180;
        }
        else if (dir == EnumFacing.UP)
        {
            player.rotationPitch = -90;
        }
        else if (dir == EnumFacing.DOWN)
        {
            player.rotationPitch = 90;
        }

        if (stack.getItem() == PokecubeItems.pokemobEgg)
        {
            player.setHeldItem(EnumHand.MAIN_HAND, stack);
            stack.onItemUse(player, source.getWorld(), source.getBlockPos().offset(dir), EnumHand.MAIN_HAND,
                    EnumFacing.UP, 0.5f, 0.5f, 0.5f);
            player.inventory.clear();
        }
        else if (stack.getItem() instanceof IPokecube)
        {
            IPokecube cube = (IPokecube) stack.getItem();
            Vector3 direction = Vector3.getNewVector().set(dir);
            if (cube.throwPokecube(source.getWorld(), player, stack, direction, 0.25f)) stack.splitStack(1);
        }
        return stack;
    }

}