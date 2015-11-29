package pokecube.adventures.items;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAITasks.EntityAITaskEntry;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.adventures.entity.trainers.EntityLeader;
import pokecube.adventures.entity.trainers.EntityTrainer;
import pokecube.adventures.entity.trainers.TypeTrainer;
import pokecube.adventures.entity.villager.EntityTrader;
import pokecube.core.ai.properties.GuardAIProperties;
import pokecube.core.ai.utils.GuardAI;
import pokecube.core.utils.TimePeriod;
import thut.api.maths.Vector3;

public class ItemTrainer extends Item 
{
	public ItemTrainer()
	{
		super();
		this.setHasSubtypes(true);
	}
	
    @Override
    public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer player)
    {
    	if(world.isRemote)
    	{
    		return itemstack;
    	}
    	
    	Vector3 location = Vector3.getNewVectorFromPool().set(player).add(Vector3.getNewVectorFromPool().set(player.getLookVec()));
    	if(player.capabilities.isCreativeMode)
    	{
	    	if(player.isSneaking())
	    	{   
	    		if(itemstack.getItemDamage()==0)
	    		{
					TypeTrainer type = TypeTrainer.getTrainer("");
					EntityTrainer t = new EntityTrainer(world, type, 1000, location.offset(EnumFacing.UP), true);
					world.spawnEntityInWorld(t);
	    		}
	    		else if(itemstack.getItemDamage()==1)
	    		{
					TypeTrainer type = TypeTrainer.getTrainer("");
					EntityLeader t = new EntityLeader(world, type, 1000, location.offset(EnumFacing.UP));
					world.spawnEntityInWorld(t);
	    		}
	    	}
    	}
    	if(itemstack.getItemDamage()==2)
		{
    		boolean item = false;
    		for(int i = 0; i<player.inventory.getSizeInventory(); i++)
    		{
    			ItemStack stack = player.inventory.getStackInSlot(i);
    			if(stack != null && stack.getItem() == Item.getItemFromBlock(Blocks.emerald_block))
    			{
    				item = true;
    				break;
    			}
    		}
    		if(!item)
    			return itemstack;
    		
			EntityTrader t = new EntityTrader(world);
			location.offset(EnumFacing.UP).moveEntity(t);
			world.spawnEntityInWorld(t);
    		t.tasks.addTask(2, new GuardAI(t, new BlockPos(location.intX(), location.intY(), location.intZ()),
    				1.0f, 48.0f, new TimePeriod(0.00, 1), false));
		}
    	
    	location.freeVectorFromPool();
		return itemstack;
    }
    
    @Override
    public boolean onItemUse(ItemStack itemStack,
            EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ)
    {
    	
    	if(!player.capabilities.isCreativeMode)
    	{
	    	if(!itemStack.hasTagCompound())
	    	{
	    		itemStack.setTagCompound(new NBTTagCompound());
	    	}
	    	itemStack.getTagCompound().setIntArray("coords", new int[]{pos.getX(), pos.getY(), pos.getZ()});
	    	System.out.println("test");
	    	return true;
    	}
    	
    	Vector3 v = Vector3.getNewVectorFromPool().set(pos);
    	Block b = v.getBlock(world);
    	b.rotateBlock(world, pos, EnumFacing.DOWN);
    	
    	v.freeVectorFromPool();
    	
    	return false;
    }
    
    /**
     * Returns true if the item can be used on the given entity, e.g. shears on sheep.
     */
    public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer player, EntityLivingBase target)
    {
    	if(stack.getItemDamage()!=2)
    		return false;
    	if(!(target instanceof EntityVillager))
    		return false;
    	EntityVillager v = (EntityVillager) target;
		for(Object o2: v.tasks.taskEntries)
		{
			EntityAITaskEntry taskEntry = (EntityAITaskEntry)o2;
			if( taskEntry.action instanceof GuardAI )
			{
				v.tasks.removeTask(taskEntry.action);
				break;
			}
		}
		
		Vector3 pos = Vector3.getNewVectorFromPool().set(target);
		v.tasks.addTask(2,new GuardAI(v, new BlockPos(pos.intX(), pos.intY(), pos.intZ()),
				1.0f, 48.0f, new TimePeriod(0.00, 0.5), false));
		GuardAIProperties props = new GuardAIProperties();
		props.init(v, v.worldObj);
		NBTTagCompound nbt = new NBTTagCompound();
		v.writeToNBT(nbt);
		props.saveNBTData(nbt);
		v.readFromNBT(nbt);
        return true;
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    /**
     * returns a list of items with the same ID, but different meta (eg: dye returns 16 items)
     */
    public void getSubItems(Item par1, CreativeTabs par2CreativeTabs, List<ItemStack> par3List) {
    	par3List.add(new ItemStack(par1, 1, 0));
    	par3List.add(new ItemStack(par1, 1, 1));
    	par3List.add(new ItemStack(par1, 1, 2));
    }
    
    /**
     * Returns the unlocalized name of this item. This version accepts an ItemStack so different stacks can have
     * different names based on their damage or NBT.
     */
    @Override
	public String getUnlocalizedName(ItemStack stack)
    {
    	int i = stack.getItemDamage();
    	
    	if(i==0)
    		return "item.trainerSpawner";
    	if(i==1)
    		return "item.leaderSpawner";
    	if(i==2)
    	{
    		return "item.traderSpawner";
    	}
    	
    	return super.getUnlocalizedName();
    }
    
}
