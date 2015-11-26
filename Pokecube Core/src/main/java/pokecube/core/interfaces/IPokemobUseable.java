package pokecube.core.interfaces;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public interface IPokemobUseable {

	public boolean itemUse(ItemStack stack, Entity user, EntityPlayer player);
	

	public boolean useByPlayerOnPokemob(EntityLivingBase mob, ItemStack stack);
	

	public boolean useByPokemob(EntityLivingBase mob, ItemStack stack);
	
	public boolean applyEffect(EntityLivingBase mob, ItemStack stack);
}
