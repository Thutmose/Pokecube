package pokecube.adventures.items.bags;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.IChatComponent;
import pokecube.adventures.handlers.PASaveHandler;
import pokecube.core.mod_Pokecube;
import pokecube.core.interfaces.PokecubeMod;

public class InventoryBag implements IInventory
{
	private int page = 0;
	public boolean autoToPC = false;
	public static HashMap<String, InventoryBag> map = new HashMap<String, InventoryBag>();
	public static int PAGECOUNT = 32;
	public boolean[] opened = new boolean[PAGECOUNT];
	public String[] boxes = new String[PAGECOUNT];
	private HashMap<Integer, ItemStack> contents = new HashMap<Integer, ItemStack>();
	public final String owner;
	public boolean seenOwner = false;
	
	public InventoryBag(String player) {
		if(!player.equals(""))
			map.put(player, this);
		opened = new boolean[PAGECOUNT];
		boxes = new String[PAGECOUNT];
		owner = player;
		for(int i = 0; i<PAGECOUNT; i++)
		{
			boxes[i] = "Box "+String.valueOf(i+1);
		}
	}

	   public static NBTTagList saveToNBT()
	    {
	    	NBTTagList nbttag = new NBTTagList();
	    	
	    	HashSet<String> keys = new HashSet<String>();
	    	for(String s: map.keySet())
	    		keys.add(s);
	    	
	    	for(String uuid: keys)
	    	{
	    		if(map.get(uuid)==null || uuid.equals(""))
				{
					continue;
				}
	    		
	    		boolean isUid = true;
	    		try {
					UUID.fromString(uuid);
				} catch (Exception e) {
					isUid = false;
				}
	    		if(!isUid)
	    			continue;
	    		
	    		NBTTagCompound items = new NBTTagCompound();
			    NBTTagCompound boxes = new NBTTagCompound();
			    boxes.setString("UUID", uuid);
			    boxes.setBoolean("seenOwner", map.get(uuid).seenOwner);
			    boxes.setBoolean("autoSend", map.get(uuid).autoToPC);
			    boxes.setInteger("page", map.get(uuid).page);
		        
			    for(int i = 0; i<PAGECOUNT; i++)
			    {
			    	boxes.setString("name"+i, map.get(uuid).boxes[i]);
			    }
				items.setInteger("page", map.get(uuid).getPage());
			    for (int i = 0; i < map.get(uuid).getSizeInventory(); i++)
			    {
			        ItemStack itemstack = map.get(uuid).getStackInSlot2(i);
		        	NBTTagCompound nbttagcompound = new NBTTagCompound();
		        	
			        if (itemstack != null)
			        {
			            nbttagcompound.setShort("Slot", (short)i);
			            itemstack.writeToNBT(nbttagcompound);
			            items.setTag("item"+i, nbttagcompound);
			        }
			    }
			    items.setTag("boxes", boxes);
			    nbttag.appendTag(items);
	    	}
	    	
	        return nbttag;
	    }
	   
	   public static NBTTagList saveToNBT(Entity owner)
	   {
		   return saveToNBT(owner.getUniqueID().toString());
	   }
	   
	   public static NBTTagList saveToNBT(String uuid)
	    {
	    	NBTTagList nbttag = new NBTTagList();
	    	
	    	String name = "";
	    	
	    	for(String player: map.keySet())
	    	{
	    		if(map.get(player)==null || player.equals("") || !(name.equalsIgnoreCase(player)||uuid.equals(player)))
				{
					continue;
				}
	    		NBTTagCompound items = new NBTTagCompound();
			    NBTTagCompound boxes = new NBTTagCompound();
			    boxes.setString("UUID", player);
			    boxes.setBoolean("seenOwner", map.get(player).seenOwner);
			//    System.out.println(map.get(player).seenOwner);
			    boxes.setBoolean("autoSend", map.get(player).autoToPC);
			    boxes.setInteger("page", map.get(player).page);
		        
			    for(int i = 0; i<PAGECOUNT; i++)
			    {
			    	boxes.setString("name"+i, map.get(player).boxes[i]);
			    }
				items.setInteger("page", map.get(player).getPage());
			    for (int i = 0; i < map.get(player).getSizeInventory(); i++)
			    {
			        ItemStack itemstack = map.get(player).getStackInSlot2(i);
		        	NBTTagCompound nbttagcompound = new NBTTagCompound();
		        	
			        if (itemstack != null)
			        {
			            nbttagcompound.setShort("Slot", (short)i);
			            itemstack.writeToNBT(nbttagcompound);
			            items.setTag("item"+i, nbttagcompound);
			        }
			    }
			    items.setTag("boxes", boxes);
			    nbttag.appendTag(items);
	    	}
	    	
	        return nbttag;
	    }
    
	  public static void loadFromNBT(NBTTagList nbt)
	  {
		  loadFromNBT(nbt, false);
	  }
	   
    public static void loadFromNBT(NBTTagList nbt, boolean replace)
    {
        int i;
        tags:
        for (i = 0; i < nbt.tagCount(); i++)
        {
        	
            NBTTagCompound items = nbt.getCompoundTagAt(i);
            NBTTagCompound boxes = items.getCompoundTag("boxes");
            String player = boxes.getString("username");
            
            String uuid = boxes.getString("UUID");
            
            if((uuid==""||uuid == null)&&(player==""||player==null))
        	{
        		continue;
        	}
            if(uuid==""||uuid == null)
            {
            	uuid = player;
            }
            
        	InventoryBag load = null;
            for(int k = 0; k<PAGECOUNT; k++)
            {
            	if(k==0)
            	{
            		load = replace?new InventoryBag(uuid):getBag(uuid);
            		
            		if(load==null)
            			continue tags;
            		load.autoToPC = boxes.getBoolean("autoSend");
            		load.seenOwner = boxes.getBoolean("seenOwner");
    			   // System.out.println(map.get(uuid).seenOwner);
            		load.setPage(boxes.getInteger("page"));
            	}
            	if(boxes.getString("name"+k)!=null)
            	{
            		load.boxes[k] = boxes.getString("name"+k);
            	}
            }
            
            load.contents.clear();
            for (int k = 0; k < load.getSizeInventory(); k++)
            {
            	if(!items.hasKey("item"+k)) continue;
            	NBTTagCompound nbttagcompound = items.getCompoundTag("item"+k);
	            int j = nbttagcompound.getShort("Slot");
	            if (j >= 0 && j < load.getSizeInventory())
	            {
	            	if(load.contents.containsKey(j)) continue;
	                ItemStack itemstack = ItemStack.loadItemStackFromNBT(nbttagcompound);
	                load.setInventorySlotContents2(j, itemstack );
	            }
            }
            map.put(uuid, load);
        }
    }

    public int getSizeInventory()
    {
    	return PAGECOUNT*54;
    }
    /**
     * Returns true if automation is allowed to insert the given stack (ignoring stack size) into the given slot.
     */
    public boolean isItemValidForSlot(int par1, ItemStack stack)
    {
        return ContainerBag.isItemValid(stack);
    }
    
    public void addItem(ItemStack stack)
    {
    	for(int i = page*54; i<getSizeInventory(); i++)
    	{
    		if(this.getStackInSlot2(i)==null)
    		{
    			this.setInventorySlotContents2(i, stack);
    			return;
    		}
    	}
    	for(int i = 0; i<page*54; i++)
    	{
    		if(this.getStackInSlot2(i)==null)
    		{
    			this.setInventorySlotContents2(i, stack);
    			return;
    		}
    	}
    }
    
    @Override
    public int getInventoryStackLimit()
    {
		return ContainerBag.STACKLIMIT;
    }

    public static InventoryBag getBag(Entity player)
    {
    	return getBag(player.getUniqueID().toString());
    }
    
    public static InventoryBag getBag(String uuid)
    {
    	if(uuid!=null)
    	{
    		if(map.containsKey(uuid))
    		{
    			if(mod_Pokecube.proxy.getPlayer(uuid)!=null)
    			{
    				String username = mod_Pokecube.proxy.getPlayer(uuid).getName();
    				map.remove(username);
    			}
    			return map.get(uuid);
    		}
    		else
    		{
	    		boolean isUid = true;
	    		try {
					UUID.fromString(uuid);
				} catch (Exception e) {
					isUid = false;
				}
	    		if(!isUid)
	    			return getBag(PokecubeMod.getFakePlayer().getUniqueID().toString());
    			return new InventoryBag(uuid);
    		}
    	}
    	return null;
    }
    
    public String toString()
    {
    	String ret = "Owner: "+owner+", Current Page, "+(getPage()+1)+": Auto Move, "+autoToPC+": ";
    	String eol = System.getProperty("line.separator");
    	ret += eol;
    	for(Integer i: contents.keySet())
    	{
    		if(this.getStackInSlot2(i)!=null)
    		{
    			ret+="Slot "+i+", "+this.getStackInSlot2(i).getDisplayName()+"; ";
    		}
    	}
    	ret += eol;
    	for(int i = 0; i< boxes.length; i++)
    	{
    		ret += "Box "+(i+1)+", "+boxes[i]+"; ";
    	}
    	ret += eol;
    	return ret;
    }
    
    public static void clearInventory()
    {
    	map.clear();
    }

	@Override
	public ItemStack decrStackSize(int i, int j) {
		i = i + getPage()*54;
        if (contents.get(i) != null)
        {
            ItemStack itemstack;

            if (contents.get(i).stackSize <= j)
            {
                itemstack = contents.get(i);
                contents.put(i, null);
                return itemstack;
            }
            else
            {
                itemstack = contents.get(i).splitStack(j);

                if (contents.get(i).stackSize == 0)
                {
                    contents.put(i, null);
                }
                return itemstack;
            }
        }
        else
        {
            return null;
        }
	}

	public ItemStack decrStackSize2(int i, int j) {
        if (contents.get(i) != null)
        {
            ItemStack itemstack;

            if (contents.get(i).stackSize <= j)
            {
                itemstack = contents.get(i);
                contents.put(i, null);
                return itemstack;
            }
            else
            {
                itemstack = contents.get(i).splitStack(j);

                if (contents.get(i).stackSize == 0)
                {
                    contents.put(i, null);
                }
                return itemstack;
            }
        }
        else
        {
            return null;
        }
	}

	@Override
	public ItemStack getStackInSlot(int i)
	{
		i = i + getPage()*54;
		return contents.get(i);
	}	
	
	public ItemStack getStackInSlot2(int i)
	{
		return contents.get(i);
	}

	@Override
	public ItemStack removeStackFromSlot(int i) 
	{
		i = i + getPage()*54;
		return contents.get(i);
	}

	public ItemStack getStackInSlotOnClosing2(int i) 
	{
		return contents.get(i);
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer) {
		return true;
	}
	
	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack) 
	{
		i = i + getPage()*54;
		contents.put(i, itemstack);
	}

	public void setInventorySlotContents2(int i, ItemStack itemstack) 
	{
		contents.put(i, itemstack);
	}
	
	public HashSet<ItemStack> getContents()
	{
		HashSet<ItemStack> ret = new HashSet<ItemStack>();
		for(Integer i: contents.keySet())
		{
			if(contents.get(i)!=null)
				ret.add(contents.get(i));
		}
		return ret;
	}
	
	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public boolean hasCustomName() {
		return false;
	}

	boolean dirty = false;
	@Override
	public void markDirty() 
	{
		dirty = true;
	}

	@Override
	public void openInventory(EntityPlayer player) {}

	@Override
	public void closeInventory(EntityPlayer player) 
	{
		PASaveHandler.getInstance().saveBag(player.getUniqueID().toString());
	}

	@Override
	public IChatComponent getDisplayName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getField(int id) {
		return 0;
	}

	@Override
	public void setField(int id, int value) {
		
	}

	@Override
	public int getFieldCount() {
		return 0;
	}

	@Override
	public void clear() {
	}
	
}
