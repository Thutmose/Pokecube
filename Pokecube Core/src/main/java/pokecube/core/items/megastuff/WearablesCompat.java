package pokecube.core.items.megastuff;

import java.util.Set;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.PokecubeCore;
import pokecube.core.events.PostPostInit;
import pokecube.core.events.handlers.EventsHandlerClient.RingChecker;

public class WearablesCompat
{

    public WearablesCompat()
    {
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void postpost(PostPostInit event)
    {
        pokecube.core.events.handlers.EventsHandlerClient.checker = new RingChecker()
        {
            @Override
            public boolean hasRing(EntityPlayer player)
            {
                Set<ItemStack> worn = thut.wearables.ThutWearables.getWearables(player).getWearables();
                for (ItemStack stack : worn)
                {
                    if (stack != null)
                    {
                        if (stack.hasCapability(MegaCapability.MEGA_CAP, null)) { return true; }
                    }
                }
                for (int i = 0; i < player.inventory.armorInventory.length; i++)
                {
                    ItemStack stack = player.inventory.armorInventory[i];
                    if (stack != null)
                    {
                        if (stack.hasCapability(MegaCapability.MEGA_CAP, null)) { return true; }
                    }
                }
                return false;
            }
        };
    }

    @SubscribeEvent
    public void onItemCapabilityAttach(AttachCapabilitiesEvent.Item event)
    {
        if (event.getItem() instanceof IMegaWearable)
        {
            event.addCapability(new ResourceLocation("pokecube:wearable"), new WearableMega());
        }
    }

    public static class WearableMega implements thut.wearables.IActiveWearable, ICapabilityProvider
    {
        @Override
        public thut.wearables.EnumWearable getSlot(ItemStack stack)
        {
            String name = stack.getItem().getUnlocalizedName(stack).replace("item.", "");
            return thut.wearables.EnumWearable.valueOf(ItemMegawearable.wearables.get(name));
        }

        @SideOnly(Side.CLIENT)
        @Override
        public void renderWearable(thut.wearables.EnumWearable slot, EntityLivingBase wearer, ItemStack stack,
                float partialTicks)
        {
            PokecubeCore.proxy.renderWearable(slot, wearer, stack, partialTicks);
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
        public void onPutOn(EntityLivingBase player, ItemStack itemstack, thut.wearables.EnumWearable slot,
                int subIndex)
        {
        }

        @Override
        public void onTakeOff(EntityLivingBase player, ItemStack itemstack, thut.wearables.EnumWearable slot,
                int subIndex)
        {
        }

        @Override
        public void onUpdate(EntityLivingBase player, ItemStack itemstack, thut.wearables.EnumWearable slot,
                int subIndex)
        {
        }
    }
}
