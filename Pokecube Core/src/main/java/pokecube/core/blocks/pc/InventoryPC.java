package pokecube.core.blocks.pc;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.World;
import pokecube.core.PokecubeCore;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.network.PCPacketHandler.MessageClient;
import pokecube.core.network.PokecubePacketHandler;
import pokecube.core.utils.PCSaveHandler;
import pokecube.core.utils.Tools;

public class InventoryPC implements IInventory
{
	public static HashMap<String, InventoryPC> map = new HashMap<String, InventoryPC>();
	public static UUID defaultId = new UUID(1234,4321);
	public static int PAGECOUNT = 32;
	public static void addPokecubeToPC(ItemStack mob, World world)
    {
    	if(!(PokecubeManager.isFilled(mob))) return;
    	String player = PokecubeManager.getOwner(mob);
    	boolean isUUID = false;
		try {
			UUID.fromString(player);
			isUUID = true;
		} catch (Exception e) {
			
		}
    	if(isUUID)
    	{
    		if(player==null)
    		{
	    		IPokemob poke = PokecubeManager.itemToPokemob(mob, world);
	    		if(poke!=null)
	    		{
	    			((EntityLivingBase)poke).setDead();
	    		}
    		}
    		else
    		{
    			addStackToPC(player, mob);
    		}
    	}
    	else
    	{
    		addStackToPC(player, mob);
    	}
        String uuid = player;
		if(!PokecubeCore.isOnClientSide() && PokecubeCore.proxy.getPlayer(uuid)!=null)
		{
			PCSaveHandler.getInstance().savePC(uuid);
	        NBTTagCompound nbt = new NBTTagCompound();
	        NBTTagList tags = InventoryPC.saveToNBT(player);
	        
	        nbt.setTag("pc", tags);
	        
	        MessageClient packet = new MessageClient(MessageClient.PERSONALPC, nbt);
	        PokecubePacketHandler.sendToClient(packet, PokecubeCore.proxy.getPlayer(uuid));
		}
    }
	public static void addStackToPC(String uuid, ItemStack mob)
    {
    	if(uuid==null || mob==null)
    	{
    		System.err.println("Could not find the owner of this item "+mob+" "+uuid);
    		return;
    	}
    	InventoryPC pc = getPC(uuid);
		String message = mob.getDisplayName()+" was sent to your PC";
		
		if(pc==null)
		{
			return;
		}
		
		if(PokecubeManager.isFilled(mob))
		{
	        ItemStack stack = mob;
	        heal(stack);
			if(PokecubeCore.proxy.getPlayer(uuid)!=null)
				PokecubeCore.proxy.getPlayer(uuid).addChatMessage(new ChatComponentText(message));
		}
    	pc.addItem(mob.copy());
    	PCSaveHandler.getInstance().savePC(uuid);
    	mob = null;
    }
	public static void clearPC()
    {
    	map.clear();
    }
	public static InventoryPC getPC(Entity player)
    {
    	return getPC(player.getUniqueID().toString());
    }
	public static InventoryPC getPC(String uuid)
    {
    	if(uuid!=null)
    	{
    		if(map.containsKey(uuid))
    		{
    			if(PokecubeCore.proxy.getPlayer(uuid)!=null)
    			{
    				String username = PokecubeCore.proxy.getPlayer(uuid).getName();
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
	    			return getPC(PokecubeMod.fakeUUID.toString());
    			return new InventoryPC(uuid);
    		}
    	}
    	return null;
    }
	public static void heal(ItemStack stack)
    {

        if (stack != null)
        {
            int serialization = Tools.getHealedPokemobSerialization();
            stack.setItemDamage(serialization);
            try {
                byte oldStatus = PokecubeManager.getStatus(stack);
                if (oldStatus > IMoveConstants.STATUS_NON){
                	String itemName = stack.getDisplayName();
                	if (itemName.contains(" ("))
                		itemName = itemName.substring(0, itemName.lastIndexOf(" "));
                	stack.setStackDisplayName(itemName);
                }
            }
            catch(Throwable e){
            	e.printStackTrace();
            }
            PokecubeManager.setStatus(stack, IMoveConstants.STATUS_NON);
        }
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
            if(!replace && map.containsKey(uuid))
            	continue;
            
        	InventoryPC load = null;
            for(int k = 0; k<PAGECOUNT; k++)
            {
            	if(k==0)
            	{
            		load = replace?new InventoryPC(uuid):getPC(uuid);
            		
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
	                load.setInventorySlotContents(j, itemstack );
	            }
            }
            map.put(uuid, load);
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
			        ItemStack itemstack = map.get(uuid).getStackInSlot(i);
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
			        ItemStack itemstack = map.get(player).getStackInSlot(i);
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
	   
	   private int page = 0;
    
	  public boolean autoToPC = false;
	   
    public boolean[] opened = new boolean[PAGECOUNT];

    public String[] boxes = new String[PAGECOUNT];
    private HashMap<Integer, ItemStack> contents = new HashMap<Integer, ItemStack>();
    
    public final String owner;
    
    public boolean seenOwner = false;

    public InventoryPC(InventoryPC from)
	{
		owner = from.owner;
		opened = from.opened.clone();
		boxes = from.boxes.clone();
		page = from.page;
	}
    
    public InventoryPC(String player) {
		if(!player.equals("") && !map.containsKey(player))
			map.put(player, this);
		opened = new boolean[PAGECOUNT];
		boxes = new String[PAGECOUNT];
		owner = player;
		for(int i = 0; i<PAGECOUNT; i++)
		{
			boxes[i] = "Box "+String.valueOf(i+1);
		}
	}
    
    
    public void addItem(ItemStack stack)
    {
    	for(int i = page*54; i<getSizeInventory(); i++)
    	{
    		if(this.getStackInSlot(i)==null)
    		{
    			this.setInventorySlotContents(i, stack);
    			return;
    		}
    	}
    	for(int i = 0; i<page*54; i++)
    	{
    		if(this.getStackInSlot(i)==null)
    		{
    			this.setInventorySlotContents(i, stack);
    			return;
    		}
    	}
    }
    
    @Override
	public void clear() {
		
	}
    
    @Override
	public void closeInventory(EntityPlayer player) 
	{
		PCSaveHandler.getInstance().savePC(player.getUniqueID().toString());
	}
    
    @Override
	public ItemStack decrStackSize(int i, int j) {
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

	@Override
	public IChatComponent getDisplayName() {
		return null;
	}

	@Override
	public int getField(int id) {
		return 0;
	}	
	
	@Override
	public int getFieldCount() {
		return 0;
	}

	@Override
    public int getInventoryStackLimit()
    {
		return ContainerPC.STACKLIMIT;
    }
	
	@Override
	public String getName() {
		EntityPlayer player = PokecubeCore.getPlayer(owner);
		String name = "Public";
		
		if(!owner.equals(defaultId.toString()))
		{
			name = "Private bound";
		}
		
		if(player!=null)
		{
			name = player.getName()+"'s";
		}
		return name+" PC";
	}
	
	public int getPage() {
		return page;
	}
	
	@Override
	public int getSizeInventory()
    {
    	return PAGECOUNT*54;
    }

	@Override
	public ItemStack getStackInSlot(int i)
	{
		return contents.get(i);
	}

	@Override
	public boolean hasCustomName() {
		return false;
	}

	/**
     * Returns true if automation is allowed to insert the given stack (ignoring stack size) into the given slot.
     */
    @Override
	public boolean isItemValidForSlot(int par1, ItemStack stack)
    {
        return ContainerPC.isItemValid(stack);
    }

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer) {
		return true;
	}

	@Override
	public void markDirty() 
	{
	}

	@Override
	public void openInventory(EntityPlayer player) {}

	@Override
	public ItemStack removeStackFromSlot(int i) 
	{
		return contents.get(i);
	}

	@Override
	public void setField(int id, int value) {
		
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack) 
	{
		contents.put(i, itemstack);
	}

	public void setPage(int page) {
		this.page = page;
	}

	@Override
	public String toString()
    {
    	String ret = "Owner: "+owner+", Current Page, "+(getPage()+1)+": Auto Move, "+autoToPC+": ";
    	String eol = System.getProperty("line.separator");
    	ret += eol;
    	for(Integer i: contents.keySet())
    	{
    		if(this.getStackInSlot(i)!=null)
    		{
    			ret+="Slot "+i+", "+this.getStackInSlot(i).getDisplayName()+"; ";
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
	
}
