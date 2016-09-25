package pokecube.core.items;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import pokecube.core.PokecubeItems;
import pokecube.core.interfaces.IPokemobUseable;

public class ItemPokemobUseableFood extends ItemFood implements IPokemobUseable
{

    public ItemPokemobUseableFood(int p_i45339_1_, float p_i45339_2_, boolean p_i45339_3_)
    {
        super(p_i45339_1_, p_i45339_2_, p_i45339_3_);
    }

    @Override
    public boolean applyEffect(EntityLivingBase mob, ItemStack stack)
    {
        if (stack.getItem() == PokecubeItems.berryJuice)
        {
            float health = mob.getHealth();
            float maxHealth = mob.getMaxHealth();
            if (health + 20 < maxHealth) mob.setHealth(health + 20);
            else mob.setHealth(maxHealth);
            stack.splitStack(1);
            return true;
        }
        return false;
    }

    @Override
    public boolean itemUse(ItemStack stack, Entity user, EntityPlayer player)
    {
        if (user instanceof EntityLivingBase)
        {
            EntityLivingBase mob = (EntityLivingBase) user;
            if (player != null) return useByPlayerOnPokemob(mob, stack);
            return useByPokemob(mob, stack);
        }

        return false;
    }

    @Override
    public boolean useByPlayerOnPokemob(EntityLivingBase mob, ItemStack stack)
    {
        return applyEffect(mob, stack);
    }

    @Override
    public boolean useByPokemob(EntityLivingBase mob, ItemStack stack)
    {
        if (mob.isDead) return false;
        if (stack.getItem() == PokecubeItems.berryJuice)
        {
            float health = mob.getHealth();
            float maxHealth = mob.getMaxHealth();

            if (health >= maxHealth / 3) return false;
            if (health == 0) return false;

            if (applyEffect(mob, stack))
            {
                mob.setHeldItem(EnumHand.MAIN_HAND, null);
                return true;
            }
        }

        return false;
    }

}
