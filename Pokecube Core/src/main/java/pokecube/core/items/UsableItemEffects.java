package pokecube.core.items;

import java.util.Map;

import com.google.common.collect.Maps;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import pokecube.core.PokecubeItems;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.MovePacket;
import pokecube.core.interfaces.IPokemobUseable;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.berries.BerryManager;
import pokecube.core.items.berries.ItemBerry;
import pokecube.core.items.vitamins.ItemVitamin;

public class UsableItemEffects
{
    public static class TMUsable implements IPokemobUseable, ICapabilityProvider
    {
        /** Called when this item is "used". Normally this means via right
         * clicking the pokemob with the itemstack. It can also be called via
         * onTick or onMoveTick, in which case user will be pokemob.getEntity()
         * 
         * @param user
         * @param pokemob
         * @param stack
         * @return something happened */
        @Override
        public boolean onUse(IPokemob pokemob, ItemStack stack, EntityLivingBase user)
        {
            if (user != pokemob.getEntity() && user != pokemob.getOwner()) return false;
            boolean used = ItemTM.applyEffect(pokemob.getEntity(), stack);
            if (used) stack.splitStack(1);
            return used;
        }

        @Override
        public boolean hasCapability(Capability<?> capability, EnumFacing facing)
        {
            return capability == IPokemobUseable.USABLEITEM_CAP;
        }

        @Override
        public <T> T getCapability(Capability<T> capability, EnumFacing facing)
        {
            return hasCapability(capability, facing) ? USABLEITEM_CAP.cast(this) : null;
        }
    }

    public static class VitaminUsable implements IPokemobUseable, ICapabilityProvider
    {
        public static interface VitaminEffect extends IPokemobUseable
        {
        }

        public static Map<String, VitaminEffect> effects = Maps.newHashMap();

        /** Called when this item is "used". Normally this means via right
         * clicking the pokemob with the itemstack. It can also be called via
         * onTick or onMoveTick, in which case user will be pokemob.getEntity()
         * 
         * @param user
         * @param pokemob
         * @param stack
         * @return something happened */
        @Override
        public boolean onUse(IPokemob pokemob, ItemStack stack, EntityLivingBase user)
        {
            if (user != pokemob.getEntity() && user != pokemob.getOwner()) return false;
            if (!stack.hasTagCompound()) return false;
            boolean used = false;
            VitaminEffect effect = effects.get(stack.getTagCompound().getString("vitamin"));
            if (effect != null) used = effect.onUse(pokemob, stack, user);
            if (used) stack.splitStack(1);
            return used;
        }

        @Override
        public boolean hasCapability(Capability<?> capability, EnumFacing facing)
        {
            return capability == IPokemobUseable.USABLEITEM_CAP;
        }

        @Override
        public <T> T getCapability(Capability<T> capability, EnumFacing facing)
        {
            return hasCapability(capability, facing) ? USABLEITEM_CAP.cast(this) : null;
        }
    }

    public static class BerryUsable implements IPokemobUseable, ICapabilityProvider
    {
        public static interface BerryEffect extends IPokemobUseable
        {
        }

        public static Int2ObjectArrayMap<BerryEffect> effects = new Int2ObjectArrayMap<>();

        /** Called every tick while this item is the active held item for the
         * pokemob.
         * 
         * @param pokemob
         * @param stack
         * @return something happened */
        @Override
        public boolean onTick(IPokemob pokemob, ItemStack stack)
        {
            int berryId = stack.getItemDamage();
            if (!BerryManager.berryNames.containsKey(berryId)) return false;
            BerryEffect effect = effects.get(berryId);
            if (effect != null) return effect.onTick(pokemob, stack);
            return false;
        }

        /** Called when this item is "used". Normally this means via right
         * clicking the pokemob with the itemstack. It can also be called via
         * onTick or onMoveTick, in which case user will be pokemob.getEntity()
         * 
         * @param user
         * @param pokemob
         * @param stack
         * @return something happened */
        @Override
        public boolean onUse(IPokemob pokemob, ItemStack stack, EntityLivingBase user)
        {
            int berryId = stack.getItemDamage();
            if (!BerryManager.berryNames.containsKey(berryId)) return false;
            BerryEffect effect = effects.get(berryId);
            if (effect != null) return effect.onUse(pokemob, stack, user);
            return false;
        }

        /** @param pokemob
         * @param stack
         * @return */
        @Override
        public boolean onMoveTick(IPokemob pokemob, ItemStack stack, MovePacket moveuse)
        {
            int berryId = stack.getItemDamage();
            if (!BerryManager.berryNames.containsKey(berryId)) return false;
            BerryEffect effect = effects.get(berryId);
            if (effect != null) return effect.onMoveTick(pokemob, stack, moveuse);
            return false;
        }

        @Override
        public boolean hasCapability(Capability<?> capability, EnumFacing facing)
        {
            return capability == IPokemobUseable.USABLEITEM_CAP;
        }

        @Override
        public <T> T getCapability(Capability<T> capability, EnumFacing facing)
        {
            return hasCapability(capability, facing) ? IPokemobUseable.USABLEITEM_CAP.cast(this) : null;
        }
    }

    public static class BerryJuice implements IPokemobUseable, ICapabilityProvider
    {

        /** Called every tick while this item is the active held item for the
         * pokemob.
         * 
         * @param pokemob
         * @param stack
         * @return something happened */
        @Override
        public boolean onTick(IPokemob pokemob, ItemStack stack)
        {
            return onUse(pokemob, stack, pokemob.getEntity());
        }

        /** Called when this item is "used". Normally this means via right
         * clicking the pokemob with the itemstack. It can also be called via
         * onTick or onMoveTick, in which case user will be pokemob.getEntity()
         * 
         * @param user
         * @param pokemob
         * @param stack
         * @return something happened */
        @Override
        public boolean onUse(IPokemob pokemob, ItemStack stack, EntityLivingBase user)
        {
            EntityLivingBase mob = pokemob.getEntity();
            float health = mob.getHealth();
            if ((int) health <= 0) return false;
            float maxHealth = mob.getMaxHealth();
            if (user == mob)
            {
                if (health >= maxHealth / 3) return false;
            }
            if (health + 20 < maxHealth) mob.setHealth(health + 20);
            else mob.setHealth(maxHealth);
            boolean useStack = true;
            if (user instanceof EntityPlayer && ((EntityPlayer) user).capabilities.isCreativeMode) useStack = false;
            if (useStack) stack.splitStack(1);
            return true;
        }

        @Override
        public boolean hasCapability(Capability<?> capability, EnumFacing facing)
        {
            return capability == IPokemobUseable.USABLEITEM_CAP;
        }

        @Override
        public <T> T getCapability(Capability<T> capability, EnumFacing facing)
        {
            return hasCapability(capability, facing) ? IPokemobUseable.USABLEITEM_CAP.cast(this) : null;
        }

    }

    public static final ResourceLocation USABLE = new ResourceLocation(PokecubeMod.ID, "usables");

    /** 1.12 this needs to be ItemStack instead of item. */
    public static void registerCapabilities(AttachCapabilitiesEvent<ItemStack> event)
    {
        if (event.getCapabilities().containsKey(USABLE)) return;
        Item item = event.getObject().getItem();
        if (item instanceof ItemBerry)
        {
            event.addCapability(USABLE, new BerryUsable());
        }
        if (item instanceof ItemTM)
        {
            event.addCapability(USABLE, new TMUsable());
        }
        if (item instanceof ItemVitamin)
        {
            event.addCapability(USABLE, new VitaminUsable());
        }
        if (item == PokecubeItems.berryJuice)
        {
            event.addCapability(USABLE, new BerryJuice());
        }
    }

}
