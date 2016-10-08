package pokecube.core.items.megastuff;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public class MegaCapability implements ICapabilityProvider, IMegaCapability
{
    @CapabilityInject(IMegaCapability.class)
    public static final Capability<IMegaCapability> MEGA_CAP = null;
    final ItemStack                                 stack;

    public MegaCapability(ItemStack itemStack)
    {
        this.stack = itemStack;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing)
    {
        if (capability != MEGA_CAP) return false;
        if (stack.getItem() instanceof IMegaWearable) return true;
        NBTTagCompound gemTag = stack.getSubCompound("gemTag", false);
        if (gemTag == null || gemTag.hasNoTags()) return false;
        ItemStack stack = ItemStack.loadItemStackFromNBT(gemTag);
        if (stack != null && (stack.hasCapability(MegaCapability.MEGA_CAP, null)
                || stack.getItem() instanceof ItemMegastone)) { return true; }
        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing)
    {
        if (!hasCapability(MEGA_CAP, facing)) return null;
        if (MEGA_CAP != null && capability == MEGA_CAP) return (T) this;
        return null;
    }
}
