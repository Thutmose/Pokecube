package pokecube.core.items.berries;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.HappinessType;
import pokecube.core.interfaces.IPokemobUseable;

/**
 * 
 * @author Oracion
 * @author Manchou
 */
public class ItemBerry extends Item implements IMoveConstants, IPokemobUseable
{
	public ItemBerry() {
		super();
		this.setHasSubtypes(true);
	}

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ) {
		Block i = worldIn.getBlockState(pos).getBlock();
		int index = stack.getItemDamage();
		if (i == Blocks.farmland) {
			worldIn.setBlockState(pos.up(), BerryManager.berryCrops.get(index).getStateFromMeta(0), 3);
			
			stack.stackSize--;
		}
		return true;
	}

    @Override
	public boolean useByPokemob(EntityLivingBase mob, ItemStack stack)
	{
		return applyEffect(mob, stack);
	}

    @Override
	public boolean useByPlayerOnPokemob(EntityLivingBase mob, ItemStack stack)
	{
		if(stack.isItemEqual(BerryManager.getBerryItem("oran")))
		{
			float health = mob.getHealth();
			float maxHealth = mob.getMaxHealth();
			
			if(health == maxHealth||health<=0) return false;
			
			if(health + 10< maxHealth)
				mob.setHealth(health + 10);
			else
				mob.setHealth(maxHealth);
			stack.splitStack(1);
			HappinessType.applyHappiness((IPokemob)mob, HappinessType.BERRY);
			return true;
		}
		if(stack.isItemEqual(BerryManager.getBerryItem("sitrus")))
		{
			float health = mob.getHealth();
			float maxHealth = mob.getMaxHealth();

			if(health == maxHealth) return false;
			
			if(health + maxHealth/4< maxHealth)
				mob.setHealth(health + maxHealth/4);
			else
				mob.setHealth(maxHealth);
			stack.splitStack(1);
			HappinessType.applyHappiness((IPokemob)mob, HappinessType.BERRY);
			return true;
		}
		if(stack.isItemEqual(BerryManager.getBerryItem("enigma")))
		{
			float health = mob.getHealth();
			float maxHealth = mob.getMaxHealth();

			if(health == maxHealth) return false;
			
			if(health>=maxHealth/3) return false;
			if(health == 0) return false;
			
			if(health + maxHealth/4< maxHealth)
				mob.setHealth(health + maxHealth/4);
			else
				mob.setHealth(maxHealth);
			stack.splitStack(1);
			HappinessType.applyHappiness((IPokemob)mob, HappinessType.BERRY);
			return true;
		}
		return applyEffect(mob, stack);
	}
	
    
    @SideOnly(Side.CLIENT)
    @Override
    /**
     * returns a list of items with the same ID, but different meta (eg: dye returns 16 items)
     */
    public void getSubItems(Item par1, CreativeTabs par2CreativeTabs, List<ItemStack> par3List) {
    	for(Integer i: BerryManager.berryNames.keySet())
    	{
    		par3List.add(new ItemStack(par1, 1, i));
    	}
    }
    /**
     * Returns the unlocalized name of this item. This version accepts an ItemStack so different stacks can have
     * different names based on their damage or NBT.
     */
    @Override
    public String getUnlocalizedName(ItemStack stack)
    {
    	return "item."+BerryManager.berryNames.get(stack.getItemDamage())+"Berry";
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
	
    /**
     * allows items to add custom lines of information to the mouseover description
     */
    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> list, boolean bool) 
    {
    	String info = "";
    	list.add(StatCollector.translateToLocal("item.berry.desc"));
    	switch (stack.getItemDamage())
    	{
    	case 1:
    		info = StatCollector.translateToLocal("item.cheriBerry.desc");
    		list.add(info);
    		return;
    	case 2:
    		info = StatCollector.translateToLocal("item.chestoBerry.desc");
    		list.add(info);
    		return;
    	case 3:
    		info = StatCollector.translateToLocal("item.pechaBerry.desc");
    		list.add(info);
    		return;
    	case 4:
    		info = StatCollector.translateToLocal("item.rawstBerry.desc");
    		list.add(info);
    		return;
    	case 5:
    		info = StatCollector.translateToLocal("item.aspearBerry.desc");
    		list.add(info);
    		return;
    	case 7:
    		info = StatCollector.translateToLocal("item.oranBerry.desc");
    		list.add(info);
    		return;
    	case 9:
    		info = StatCollector.translateToLocal("item.lumBerry.desc");
    		list.add(info);
    		return;
    	case 10:
    		info = StatCollector.translateToLocal("item.sitrusBerry.desc");
    		list.add(info);
    		return;
    	case 63:
    		info = StatCollector.translateToLocal("item.jabocaBerry.desc");
    		list.add(info);
    		return;
    	case 64:
    		info = StatCollector.translateToLocal("item.rowapBerry.desc");
    		list.add(info);
    		return;
    	}
    }
	
    /**
     * Returns true if this item serves as a potion ingredient (its ingredient information is not null).
     */
    @Override
    public boolean isPotionIngredient(ItemStack p_150892_1_)
    {
        return false;
    }

	@Override
	public boolean applyEffect(EntityLivingBase mob, ItemStack stack) {
		return BerryManager.berryEffect((IPokemob) mob, stack);
	}
}
