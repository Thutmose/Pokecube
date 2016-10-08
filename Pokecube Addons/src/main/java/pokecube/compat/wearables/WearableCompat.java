package pokecube.compat.wearables;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.adventures.PokecubeAdv;
import pokecube.core.items.megastuff.IMegaWearable;

public class WearableCompat
{
    public WearableCompat()
    {
    }

    @SubscribeEvent
    public void onItemCapabilityAttach(AttachCapabilitiesEvent.Item event)
    {
        if (event.getItem() instanceof IMegaWearable)
        {
            event.addCapability(new ResourceLocation("pokecube_adv:wearable"), new WearableBag());
        }
    }

    public static class WearableBag implements thut.wearables.IActiveWearable, ICapabilityProvider
    {
        @Override
        public thut.wearables.EnumWearable getSlot(ItemStack stack)
        {
            return thut.wearables.EnumWearable.BACK;
        }

        @SideOnly(Side.CLIENT)
        @Override
        public void renderWearable(thut.wearables.EnumWearable slot, EntityLivingBase wearer, ItemStack stack, float partialTicks)
        {
            PokecubeAdv.proxy.renderWearable(slot, wearer, stack, partialTicks);
        }

        @Override
        public boolean hasCapability(Capability<?> capability, EnumFacing facing)
        {
            return capability == WEARABLE_CAP;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T getCapability(Capability<T> capability, EnumFacing facing)
        {
            if (WEARABLE_CAP != null && capability == WEARABLE_CAP) return (T) this;
            return null;
        }

        @Override
        public void onPutOn(EntityLivingBase player, ItemStack itemstack, thut.wearables.EnumWearable slot, int subIndex)
        {
        }

        @Override
        public void onTakeOff(EntityLivingBase player, ItemStack itemstack, thut.wearables.EnumWearable slot, int subIndex)
        {
        }

        @Override
        public void onUpdate(EntityLivingBase player, ItemStack itemstack, thut.wearables.EnumWearable slot, int subIndex)
        {
        }

    }

}
