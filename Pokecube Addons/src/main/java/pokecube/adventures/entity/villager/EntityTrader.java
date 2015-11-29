package pokecube.adventures.entity.villager;

import java.util.HashMap;
import java.util.HashSet;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.World;
import pokecube.adventures.handlers.TeamManager;
import pokecube.adventures.items.ItemTrainer;
import pokecube.core.PokecubeItems;
import pokecube.core.utils.ChunkCoordinate;
import thut.api.maths.Vector3;

public class EntityTrader extends EntityVillager
{

	EntityPlayer customer;
	
	MerchantRecipeList list = new MerchantRecipeList();
	MerchantRecipeList list2 = new MerchantRecipeList();
	
	HashSet<BlockPos> chests = new HashSet<BlockPos>();
	HashMap<BlockPos, MerchantRecipe> map = new HashMap<BlockPos, MerchantRecipe>();
	
	public String texture = "male";
	
	public EntityTrader(World world) {
		super(world, 1);
		this.setSize(0.6F, 1.8F);
		ItemStack shards = PokecubeItems.getStack("emerald_shard");
		shards.stackSize = 9;
		list.add(new MerchantRecipe(shards.copy(), new ItemStack(Items.emerald)));
		shards.stackSize = 18;
		list.add(new MerchantRecipe(new ItemStack(Items.emerald), new ItemStack(Items.emerald), shards));
	}

	@Override
	public void setCustomer(EntityPlayer p) {
		customer = p;
	}

	@Override
	public EntityPlayer getCustomer() {
		return customer;
	}

	@Override
	public MerchantRecipeList getRecipes(EntityPlayer p) {
		
		if(chests.isEmpty())
		{
			return list;
		}
		list2.clear();
		
		addRecipes(p);
		
		return list2;
	}
	
	private void addRecipes(EntityPlayer p)
	{
		for(BlockPos c1: chests)
		{
			Vector3 chest = Vector3.getNewVectorFromPool().set(c1);
			TileEntity te = chest.getTileEntity(worldObj);
			chest.freeVectorFromPool();
			if(te instanceof IInventory)
			{
				IInventory inv = (IInventory) te;
				if(inv.getSizeInventory()>3)
				{
					ItemStack a = inv.getStackInSlot(0);
					ItemStack b = inv.getStackInSlot(1);
					ItemStack c = inv.getStackInSlot(2);
					if(c==null)
					{
						continue;
					}
					
					boolean has = false;
					int count = 0;
					for(int i = 3; i<inv.getSizeInventory(); i++)
					{
						if(inv.getStackInSlot(i)!=null&&c.isItemEqual(inv.getStackInSlot(i)))
						{
							count += inv.getStackInSlot(i).stackSize;
						}
						if(count>=c.stackSize)
						{
							has = true;
							break;
						}
					}
					if(!has)
					{
						continue;
					}
					
					MerchantRecipe rec = new MerchantRecipe(a,b,c);
					map.put(c1, rec);
					list2.add(rec);
				}
			}
		}
		
	}

	@Override
	public void setRecipes(MerchantRecipeList p) {
		
		if(chests.isEmpty())
		{
			list = p;
		}
	}

	@Override
	public void useRecipe(MerchantRecipe p) {
		
		Vector3 chest = Vector3.getNewVectorFromPool();
		
//		for(BlockPos c1: map.keySet())
		{//TODO write a method for comparing sales
//			MerchantRecipe p1 = map.get(c1);
//			if(p1!=null && p1.hasSameIDsAs(p) && p1.getItemToSell().isItemEqual(p.getItemToSell()))
//			{
//				chest = Vector3.getNewVectorFromPool().set(c1);
//				break;
//			}
		}
		System.out.println(chest+" "+map+" "+p);
		if(!chest.isEmpty()&&!list2.isEmpty())
		{
			ItemStack a = p.getItemToBuy();
			ItemStack b = p.getSecondItemToBuy();
			ItemStack c = p.getItemToSell();

			if(a!=null)
				a = a.copy();
			if(b!=null)
				b = b.copy();
			if(c!=null)
				c = c.copy();
			
			TileEntity te = chest.getTileEntity(worldObj);
			chest.freeVectorFromPool();
			if(te instanceof IInventory)
			{
				IInventory inv = (IInventory) te;
				if(inv.getSizeInventory()>3)
				{
					int count = 0;
					for(int i = 3; i<inv.getSizeInventory(); i++)
					{
						if(inv.getStackInSlot(i)!=null&&c.isItemEqual(inv.getStackInSlot(i)))
						{
							count +=inv.getStackInSlot(i).stackSize; 
							inv.decrStackSize(i, Math.min(c.stackSize, inv.getStackInSlot(i).stackSize));
						}
						if(count >= c.stackSize)
							break;
					}
					if(a!=null)
					{
						count = a.stackSize;
						for(int i = 3; i<inv.getSizeInventory(); i++)
						{
							if(inv.getStackInSlot(i)==null||a.isItemEqual(inv.getStackInSlot(i)))
							{
								if(inv.getStackInSlot(i)!=null&&inv.getStackInSlot(i).stackSize + a.stackSize < 65)
								{
									a.stackSize = inv.getStackInSlot(i).stackSize + a.stackSize;
									count = 0;
									inv.setInventorySlotContents(i, a.copy());
								}
								else
								{
									count = 0;
									inv.setInventorySlotContents(i, a.copy());
								}
							}
							if(count == 0)
								break;
						}
					}
					if(b!=null)
					{
						count = b.stackSize;
						for(int i = 3; i<inv.getSizeInventory(); i++)
						{
							if(inv.getStackInSlot(i)==null||b.isItemEqual(inv.getStackInSlot(i)))
							{
								if(inv.getStackInSlot(i)!=null&&inv.getStackInSlot(i).stackSize + b.stackSize < 65)
								{
									b.stackSize = inv.getStackInSlot(i).stackSize + b.stackSize;
									count = 0;
									inv.setInventorySlotContents(i, b);
								}
								else
								{
									count = 0;
									inv.setInventorySlotContents(i, b);
								}
							}
							if(count == 0)
								break;
						}
					}
				}
			}
		}
	}

    @Override
	public void writeEntityToNBT(NBTTagCompound nbt)
    {
        super.writeEntityToNBT(nbt);
        nbt.setTag("goods", list.getRecipiesAsTags());
        NBTTagList chestsList = new NBTTagList();
        for(BlockPos c: chests)
        {
        	NBTTagCompound tag = new NBTTagCompound();
        	tag.setIntArray("loc", new int[]{c.getX(), c.getY(), c.getZ()});
        	chestsList.appendTag(tag);
        }
        nbt.setTag("chests", chestsList);
        nbt.setString("texture", texture);
    }
    
    @Override
    public void readEntityFromNBT(NBTTagCompound nbt)
    {
        super.readEntityFromNBT(nbt);
        list.clear();
        list.readRecipiesFromTags(nbt.getCompoundTag("goods"));
        NBTBase temp = nbt.getTag("chests");
        if(temp instanceof NBTTagList)
        {
        	NBTTagList tagList = (NBTTagList) temp;
        	for(int i = 0; i< tagList.tagCount(); i++){
        		NBTTagCompound tag = tagList.getCompoundTagAt(i);
        		int[] loc = tag.getIntArray("loc");
        		if(loc.length == 3)
        			chests.add(new BlockPos(loc[0], loc[1], loc[2]));
        	}
        }
        
        
        texture = nbt.getString("texture");
    }

	@Override
	public EntityVillager createChild(EntityAgeable p_90011_1_) {
		return null;
	}

    @Override
    public boolean attackEntityFrom(DamageSource source, float i)
    {
    	Entity e = source.getSourceOfDamage();
    	if(e instanceof EntityPlayer && (((EntityPlayer)e).capabilities.isCreativeMode)
    			|| (((EntityPlayer)e).getHeldItem()!=null &&(((EntityPlayer)e).getHeldItem().getItem() instanceof ItemTrainer)))
    	{
    		
    		EntityPlayer p = (EntityPlayer) e;
    		if(!p.capabilities.isCreativeMode)
    		{
				ChunkCoordinate c = new ChunkCoordinate(MathHelper.floor_double(posX/16f), (int)posY/16,MathHelper.floor_double(posZ/16f), dimension);
				String owner = TeamManager.getInstance().getLandOwner(c);
				Vector3 v = Vector3.getNewVectorFromPool().set(this);
				String owner1 = TeamManager.getInstance().getLandOwner(new ChunkCoordinate(MathHelper.floor_double(v.intX()/16f), v.intY()/16, MathHelper.floor_double(v.intZ()/16f), dimension));
				v.freeVectorFromPool();
				if(owner1==null||!owner1.equals(owner))
				{
					return false;
				}
	        	String team = worldObj.getScoreboard().getPlayersTeam(p.getCommandSenderName()).getRegisteredName();
	        	if(owner==null)
	        		return false;
	        	if(!owner.equals(team)||!TeamManager.getInstance().isAdmin(p.getCommandSenderName(), p.getTeam()))
	        	{
	        		return false;
	        	}
    		}
    		this.setDead();
    		return true;
    	}
    	return false;
    }
    
    @Override
    public void onUpdate()
    {
        super.onUpdate();
    }
    
    
    @Override
    protected boolean canDespawn()
    {
    	return false;
    }
    /**
     * Called when a player interacts with a mob. e.g. gets milk from a cow, gets into the saddle on a pig.
     */
    @Override
	public boolean interact(EntityPlayer p)
    {
        ItemStack itemstack = p.inventory.getCurrentItem();
        boolean flag = itemstack != null && itemstack.getItem() == Items.spawn_egg;

        if (!flag && this.isEntityAlive() && !this.isChild() && !p.isSneaking())
        {
            if (!this.worldObj.isRemote)
            {
                this.setCustomer(p);
               // p.displayGUIMerchant(this, this.getCustomNameTag());//TODO find the merchant gui
            }

            return true;
        }
        else if(p.isSneaking())
        {
        	if(p.getHeldItem()!=null&&p.getHeldItem().hasTagCompound()&&p.getHeldItem().getItem() instanceof ItemTrainer)
        	{
        		int[] loc = p.getHeldItem().getTagCompound().getIntArray("coords");
        		if(loc.length==3)
        		{
        			ChunkCoordinate c = new ChunkCoordinate(MathHelper.floor_double(loc[0]/16f), loc[1]/16,MathHelper.floor_double(loc[2]/16f), dimension);
        			String owner = TeamManager.getInstance().getLandOwner(c);
        			Vector3 v = Vector3.getNewVectorFromPool().set(this);
        			String owner1 = TeamManager.getInstance().getLandOwner(new ChunkCoordinate(MathHelper.floor_double(v.intX()/16f), v.intY()/16, MathHelper.floor_double(v.intZ()/16f), dimension));
        			if(owner1==null||!owner1.equals(owner))
        			{
        				return false;
        			}
                	String team = worldObj.getScoreboard().getPlayersTeam(p.getCommandSenderName()).getRegisteredName();
                	if(owner==null)
                		return false;
                	if(!owner.equals(team)||!TeamManager.getInstance().isAdmin(p.getCommandSenderName(), p.getTeam()))
                	{
                		return false;
                	}
                	BlockPos c1 = new BlockPos(loc[0], loc[1], loc[2]);
                	if(chests.contains(c1))
                		chests.remove(c1);
                	else
                		chests.add(c1);
                	v.freeVectorFromPool();
        		}
        	}
        	return true;
        }
        else
        {
            return super.interact(p);
        }
    }

}
