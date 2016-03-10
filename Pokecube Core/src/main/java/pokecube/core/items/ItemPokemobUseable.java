package pokecube.core.items;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import pokecube.core.PokecubeItems;
import pokecube.core.interfaces.IPokemobUseable;

public class ItemPokemobUseable extends Item implements IPokemobUseable {
	
	public ItemPokemobUseable() {
		super();
	}
	
	@Override
	public boolean itemUse(ItemStack stack, Entity user, EntityPlayer player)
	{
		if(user instanceof EntityLivingBase)
		{
			EntityLivingBase mob = (EntityLivingBase)user;
			if(player!=null)
				return useByPlayerOnPokemob(mob, stack);
			else
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
		if(mob.isDead) return false;
		if(stack.getItem() == PokecubeItems.berryJuice)
		{
			float health = mob.getHealth();
			float maxHealth = mob.getMaxHealth();

			if(health>=maxHealth/3) return false;
			if(health == 0) return false;
			
			if(applyEffect(mob, stack))
			{
				mob.setCurrentItemOrArmor(0, null);
				return true;
			}
		}
		
		return false;
	}

	@Override
	public boolean applyEffect(EntityLivingBase mob, ItemStack stack)
	{
		if(stack.getItem() == PokecubeItems.berryJuice)
		{
			float health = mob.getHealth();
			float maxHealth = mob.getMaxHealth();

			if(health==maxHealth) return false;
			
			if(health + 20< maxHealth)
				mob.setHealth(health + 20);
			else
				mob.setHealth(maxHealth);
			stack.splitStack(1);
			return true;
		}
		return false;
	}
}
