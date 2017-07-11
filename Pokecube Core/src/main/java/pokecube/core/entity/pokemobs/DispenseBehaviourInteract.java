package pokecube.core.entity.pokemobs;

import java.util.Set;

import com.google.common.collect.Sets;

import net.minecraft.block.BlockDispenser;
import net.minecraft.dispenser.IBehaviorDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.common.util.FakePlayer;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.utils.Tools;
import thut.lib.CompatWrapper;

public class DispenseBehaviourInteract implements IBehaviorDispenseItem
{
    public static final Set<ItemStack> KNOWNSTACKS = Sets.newHashSet();

    public static void registerBehavior(ItemStack stack)
    {
        if (!CompatWrapper.isValid(stack)) return;
        for (ItemStack known : KNOWNSTACKS)
        {
            if (Tools.isSameStack(stack, known)) return;
        }
        BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(stack.getItem(), new DispenseBehaviourInteract());
    }

    @Override
    public ItemStack dispense(IBlockSource source, ItemStack stack)
    {
        FakePlayer player = PokecubeMod.getFakePlayer(source.getWorld());
        player.posX = source.getX();
        player.posY = source.getY() - player.getEyeHeight();
        player.posZ = source.getZ();
        player.setHeldItem(EnumHand.MAIN_HAND, stack);
        System.out.println(player);
        return BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.getObject(null).dispense(source, stack);
    }

}
