package pokecube.core.items;

import java.util.Map;

import com.google.common.collect.Maps;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
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
import pokecube.core.items.vitamins.ItemCandy;
import pokecube.core.items.vitamins.ItemVitamin;
import pokecube.core.utils.Tools;

public class UsableItemEffects
{
    public static class CandyUsable implements IPokemobUseable, ICapabilityProvider
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
        public ActionResult<ItemStack> onUse(IPokemob pokemob, ItemStack stack, EntityLivingBase user)
        {
            if (user != pokemob.getEntity() && user != pokemob.getOwner())
                return new ActionResult<ItemStack>(EnumActionResult.FAIL, stack);
            boolean used = true;
            int xp = Tools.levelToXp(pokemob.getExperienceMode(),
                    pokemob.getLevel() + (PokecubeItems.isValid(stack) ? 1 : -1));
            pokemob.setExp(xp, true);
            if (used)
            {
                stack.splitStack(1);
                PokecubeItems.deValidate(stack);
            }
            stack.setTagCompound(null);
            return new ActionResult<ItemStack>(used ? EnumActionResult.SUCCESS : EnumActionResult.FAIL, stack);
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
        public ActionResult<ItemStack> onUse(IPokemob pokemob, ItemStack stack, EntityLivingBase user)
        {
            if (user != pokemob.getEntity() && user != pokemob.getOwner())
                return new ActionResult<ItemStack>(EnumActionResult.FAIL, stack);
            boolean used = ItemTM.applyEffect(pokemob.getEntity(), stack);
            if (used) stack.splitStack(1);
            return new ActionResult<ItemStack>(used ? EnumActionResult.SUCCESS : EnumActionResult.FAIL, stack);
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
        public ActionResult<ItemStack> onUse(IPokemob pokemob, ItemStack stack, EntityLivingBase user)
        {
            if (user != pokemob.getEntity() && user != pokemob.getOwner() && !(stack.getItem() instanceof ItemVitamin))
                return new ActionResult<ItemStack>(EnumActionResult.FAIL, stack);
            ItemVitamin vitamin = (ItemVitamin) stack.getItem();
            ActionResult<ItemStack> result = null;
            VitaminEffect effect = effects.get(vitamin.type);
            if (effect != null) result = effect.onUse(pokemob, stack, user);
            else return new ActionResult<ItemStack>(EnumActionResult.FAIL, stack);
            if (result.getType() == EnumActionResult.SUCCESS) stack.splitStack(1);
            return result;
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
        public ActionResult<ItemStack> onTick(IPokemob pokemob, ItemStack stack)
        {
            if (stack.getItem() instanceof ItemBerry)
            {
                int berryId = ((ItemBerry) stack.getItem()).index;
                if (!BerryManager.berryNames.containsKey(berryId))
                    return new ActionResult<ItemStack>(EnumActionResult.FAIL, stack);
                BerryEffect effect = effects.get(berryId);
                if (effect != null) return effect.onTick(pokemob, stack);
            }
            return new ActionResult<ItemStack>(EnumActionResult.FAIL, stack);
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
        public ActionResult<ItemStack> onUse(IPokemob pokemob, ItemStack stack, EntityLivingBase user)
        {
            if (stack.getItem() instanceof ItemBerry)
            {
                int berryId = ((ItemBerry) stack.getItem()).index;
                if (!BerryManager.berryNames.containsKey(berryId))
                    return new ActionResult<ItemStack>(EnumActionResult.FAIL, stack);
                BerryEffect effect = effects.get(berryId);
                if (effect != null) return effect.onUse(pokemob, stack, user);
            }
            return new ActionResult<ItemStack>(EnumActionResult.FAIL, stack);
        }

        /** @param pokemob
         * @param stack
         * @return */
        @Override
        public ActionResult<ItemStack> onMoveTick(IPokemob pokemob, ItemStack stack, MovePacket moveuse)
        {
            if (stack.getItem() instanceof ItemBerry)
            {
                int berryId = ((ItemBerry) stack.getItem()).index;
                if (!BerryManager.berryNames.containsKey(berryId))
                    return new ActionResult<ItemStack>(EnumActionResult.FAIL, stack);
                BerryEffect effect = effects.get(berryId);
                if (effect != null) return effect.onMoveTick(pokemob, stack, moveuse);
            }
            return new ActionResult<ItemStack>(EnumActionResult.FAIL, stack);
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

    public static class PotionUse implements IPokemobUseable, ICapabilityProvider
    {
        @Override
        public ActionResult<ItemStack> onUse(IPokemob pokemob, ItemStack stack, EntityLivingBase user)
        {
            EntityLiving mob = pokemob.getEntity();
            boolean applied = false;
            for (PotionEffect potioneffect : PotionUtils.getEffectsFromStack(stack))
            {
                if (potioneffect.getPotion().isInstant())
                {
                    potioneffect.getPotion().affectEntity(mob, mob, mob, potioneffect.getAmplifier(), 1.0D);
                }
                else
                {
                    mob.addPotionEffect(new PotionEffect(potioneffect));
                }
                applied = true;
            }
            if (applied)
            {
                stack.shrink(1);
                if (stack.isEmpty())
                {
                    stack = new ItemStack(Items.GLASS_BOTTLE);
                }
                else
                {
                    // Add to inventory or drop
                }
                return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
            }
            return new ActionResult<ItemStack>(EnumActionResult.FAIL, stack);
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
        public ActionResult<ItemStack> onTick(IPokemob pokemob, ItemStack stack)
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
        public ActionResult<ItemStack> onUse(IPokemob pokemob, ItemStack stack, EntityLivingBase user)
        {
            EntityLivingBase mob = pokemob.getEntity();
            float health = mob.getHealth();
            if ((int) health <= 0) return new ActionResult<ItemStack>(EnumActionResult.FAIL, stack);
            float maxHealth = mob.getMaxHealth();
            if (user == mob)
            {
                if (health >= maxHealth / 3) return new ActionResult<ItemStack>(EnumActionResult.FAIL, stack);
            }
            if (health + 20 < maxHealth) mob.setHealth(health + 20);
            else mob.setHealth(maxHealth);
            boolean useStack = true;
            if (user instanceof EntityPlayer && ((EntityPlayer) user).capabilities.isCreativeMode) useStack = false;
            if (useStack) stack.splitStack(1);
            return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
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
        if (item instanceof ItemCandy)
        {
            event.addCapability(USABLE, new CandyUsable());
        }
        if (item instanceof ItemVitamin)
        {
            event.addCapability(USABLE, new VitaminUsable());
        }
        if (item instanceof ItemPotion)
        {
            event.addCapability(USABLE, new PotionUse());
        }
        if (item == PokecubeItems.berryJuice)
        {
            event.addCapability(USABLE, new BerryJuice());
        }
    }

}
