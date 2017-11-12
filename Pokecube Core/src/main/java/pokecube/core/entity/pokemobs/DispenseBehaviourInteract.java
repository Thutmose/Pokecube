package pokecube.core.entity.pokemobs;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;

import net.minecraft.block.BlockDispenser;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
import net.minecraft.dispenser.IBehaviorDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.entity.EntityLiving;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.RegistryDefaulted;
import net.minecraftforge.common.util.FakePlayer;
import pokecube.core.interfaces.PokecubeMod;
import thut.api.maths.Vector3;
import thut.lib.CompatWrapper;

public class DispenseBehaviourInteract implements IBehaviorDispenseItem
{
    public static final Set<Item>                                      KNOWNSTACKS                = Sets.newHashSet();

    // We make our own to try to ensure that any other added behaviour is kept.
    // Hopefully they registered their first, we do this in post init, so should
    // be.
    public static final RegistryDefaulted<Item, IBehaviorDispenseItem> DISPENSE_BEHAVIOR_REGISTRY = new RegistryDefaulted<Item, IBehaviorDispenseItem>(
            new BehaviorDefaultDispenseItem());

    public static void registerBehavior(ItemStack stack)
    {
        if (!CompatWrapper.isValid(stack) || !KNOWNSTACKS.add(stack.getItem())) return;
        DISPENSE_BEHAVIOR_REGISTRY.putObject(stack.getItem(),
                BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.getObject(stack.getItem()));
        BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(stack.getItem(), new DispenseBehaviourInteract());
    }

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
        if (dir == null) return DISPENSE_BEHAVIOR_REGISTRY.getObject(stack.getItem()).dispense(source, stack);
        FakePlayer player = PokecubeMod.getFakePlayer(source.getWorld());
        player.posX = source.getX();
        player.posY = source.getY() - player.getEyeHeight();
        player.posZ = source.getZ();

        Vector3 loc = Vector3.getNewVector().set(source.getBlockPos().offset(dir));
        AxisAlignedBB box = loc.getAABB().grow(2);
        List<EntityLiving> mobs = source.getWorld().getEntitiesWithinAABB(EntityLiving.class, box);
        Collections.shuffle(mobs);
        if (!mobs.isEmpty())
        {
            player.inventory.clear();
            player.setHeldItem(EnumHand.MAIN_HAND, stack);

            EnumActionResult cancelResult = net.minecraftforge.common.ForgeHooks.onInteractEntityAt(player, mobs.get(0),
                    new Vec3d(0, 0, 0), EnumHand.MAIN_HAND);
            if (cancelResult == null) cancelResult = net.minecraftforge.common.ForgeHooks.onInteractEntity(player,
                    mobs.get(0), EnumHand.MAIN_HAND);

            boolean interacted = cancelResult != null || mobs.get(0).processInitialInteract(player, EnumHand.MAIN_HAND);
            boolean result = false;
            if (!interacted)
            {
                result = stack.interactWithEntity(player, mobs.get(0), EnumHand.MAIN_HAND);
            }
            for (ItemStack stack3 : player.inventory.mainInventory)
                if (CompatWrapper.isValid(stack3))
                {
                    if (stack3 != stack)
                    {
                        result = true;
                        // This should result in the object just being
                        // dropped.
                        DISPENSE_BEHAVIOR_REGISTRY.getObject(null).dispense(source, stack3);
                    }
                }

            player.inventory.clear();
            if (result) return stack;
        }
        return DISPENSE_BEHAVIOR_REGISTRY.getObject(stack.getItem()).dispense(source, stack);
    }

}
