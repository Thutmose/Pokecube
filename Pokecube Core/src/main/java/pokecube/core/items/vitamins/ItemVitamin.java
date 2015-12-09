package pokecube.core.items.vitamins;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.items.ItemPokemobUseable;

public class ItemVitamin extends ItemPokemobUseable implements IMoveConstants{

	int vitaminIndex = 0;
	String vitaminName = "";

	public ItemVitamin() {
		super();
	}

	public void setVitaminIndex(int vitaminId) {
		this.vitaminIndex = vitaminId;
	}

	public void setVitamin(String vitaminName) {
		this.vitaminName = vitaminName;
	}
	
	public String getVitaminName(){
		return vitaminName;
	}
	
    public static boolean feedToPokemob(ItemStack stack, Entity entity)
	{
		if(entity instanceof IPokemob)
		{
			if(stack.getItem() == VitaminManager.getVitaminItem("hpup"))
			{
				((IPokemob)entity).addEVs(new byte[]{10,0,0,0,0,0});
				return true;
			}
			if(stack.getItem() == VitaminManager.getVitaminItem("protein"))
			{
				((IPokemob)entity).addEVs(new byte[]{0,10,0,0,0,0});
				return true;
			}
			if(stack.getItem() == VitaminManager.getVitaminItem("iron"))
			{
				((IPokemob)entity).addEVs(new byte[]{0,0,10,0,0,0});
				return true;
			}
			if(stack.getItem() == VitaminManager.getVitaminItem("calcium"))
			{
				((IPokemob)entity).addEVs(new byte[]{0,0,0,10,0,0});
				return true;
			}
			if(stack.getItem() == VitaminManager.getVitaminItem("zinc"))
			{
				((IPokemob)entity).addEVs(new byte[]{0,0,0,0,10,0});
				return true;
			}
			if(stack.getItem() == VitaminManager.getVitaminItem("carbos"))
			{
				((IPokemob)entity).addEVs(new byte[]{0,0,0,0,0,10});
				return true;
			}
		}
		return false;
	}	
    
    @Override
	public boolean applyEffect(EntityLivingBase mob, ItemStack stack)
	{
		boolean ret = feedToPokemob(stack, mob);
		if(ret)
		{
			stack.splitStack(1);
		}
		return feedToPokemob(stack, mob);
		
	}
}
