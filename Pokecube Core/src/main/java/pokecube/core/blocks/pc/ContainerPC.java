package pokecube.core.blocks.pc;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.PokecubeItems;
import pokecube.core.PokecubeCore;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.network.PCPacketHandler.MessageServer;
import pokecube.core.network.PokecubePacketHandler;
import pokecube.core.network.PokecubePacketHandler.PokecubeServerPacket;
import pokecube.core.utils.PCSaveHandler;

public class ContainerPC extends Container{

	public final InventoryPC inv;
	public final InventoryPlayer invPlayer;
	public final TileEntityPC pcTile;
	public static int STACKLIMIT = 64;
	public static int yOffset;
	public static int xOffset;
	public boolean release = false;
	//private GuiPC gpc;
	
	public boolean[] toRelease = new boolean[54];
	
	public ContainerPC(InventoryPlayer ivplay, TileEntityPC pc)
	{
		super();
		xOffset = 0;
		yOffset = 0;
		InventoryPC temp = pc!=null?pc.getPC()!=null?pc.getPC():InventoryPC.getPC(ivplay.player.getUniqueID().toString()):InventoryPC.getPC(ivplay.player.getUniqueID().toString());
		if(FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
			inv = new InventoryPC(temp);
		else
			inv = temp;
		invPlayer = ivplay;
		pcTile = pc;
		
		bindInventories();
	}
	
	protected void bindInventories()
	{
	//	System.out.println("bind");
		clearSlots();
		bindPlayerInventory();
		bindPCInventory();
	}
	
	protected void bindPCInventory()
	{
		int n = 0;
		n = inv.getPage()*54;
		for (int i = 0; i < 6; i++) 
		{
            for (int j = 0; j < 9; j++) 
            {
                 addSlotToContainer(new SlotPC(inv,n +j + i * 9, 8 + j * 18 + xOffset, 18 + i * 18 + yOffset));
            }
		}
		//int k = 0;
		for(Object o:inventorySlots)
		{
			if(o instanceof Slot)
			{
				((Slot)o).onSlotChanged();
			}
		}
	}
	
	 protected void bindPlayerInventory() 
	 {
		 int offset = 64 + yOffset;

         for (int i = 0; i < 9; i++) 
         {
                 addSlotToContainer(new Slot(invPlayer,  i, 8 + i * 18 + xOffset, 142+offset));
         }
         for (int i = 0; i < 3; i++) 
         {
                 for (int j = 0; j < 9; j++) 
                 {
                         addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9,
                                         8 + j * 18  + xOffset, 84 + i * 18+offset));
                 }
         }
	 }
	
    /**
     * 
     */
    @Override
	protected Slot addSlotToContainer(Slot par1Slot)
    {
        par1Slot.slotNumber = this.inventorySlots.size();
        this.inventorySlots.add(par1Slot);
        this.inventoryItemStacks.add(inv.getStackInSlot(par1Slot.getSlotIndex()));
        return par1Slot;
    }
    
    protected void clearSlots()
    {
    	this.inventorySlots.clear();
    }
	
	@Override
	 public void onContainerClosed(EntityPlayer player)
	 {
		PCSaveHandler.getInstance().savePC(player.getUniqueID().toString());
		 super.onContainerClosed(player);
	 }
	
	public void updateInventoryPages(int dir, InventoryPlayer invent)
	{
		inv.setPage((inv.getPage()==0)&&(dir==-1)?InventoryPC.PAGECOUNT-1:(inv.getPage()+dir)%InventoryPC.PAGECOUNT);
		bindInventories();
	}
	
	public void gotoInventoryPage(int page)
	{
		if(page - 1 == inv.getPage()) return;
		
		inv.setPage(page-1);

		bindInventories();
	}
	
	public void changeName(String name)
	{
		inv.boxes[inv.getPage()] = name;
		
		if(PokecubeCore.isOnClientSide())
		{
			byte[] string = name.getBytes();
			byte[] message = new byte[string.length+2];
			
			message[0] = 11;
			message[1] = (byte) string.length;
			for(int i = 2; i<message.length; i++)
			{
				message[i] = string[i-2];
			}//TODO move this to PC packet handler instead
	        PokecubeServerPacket packet = PokecubePacketHandler.makeServerPacket(PokecubeServerPacket.STATS, message);
	        PokecubePacketHandler.sendToServer(packet);
			return;
		}
	}
	 
	@Override
	public boolean canInteractWith(EntityPlayer entityplayer) {
		return true;
	}

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int slot) {
            ItemStack stack = null;
            return stack;
    }
    
    @Override
    public ItemStack slotClick(int i, int j, int flag,
            EntityPlayer entityplayer)
    {
    	
    	if(release && i>35){
            SlotPC slot = (SlotPC) inventorySlots.get(i);
            toRelease[slot.getSlotIndex() - 54 * inv.getPage()] = !toRelease[slot.getSlotIndex() - 54 * inv.getPage()];
        	return null;
        	
        }
    	
//    	if(true)
//    		return super.slotClick(i, j, flag, entityplayer);
//    	
    	if (i < 0)
    		return null;
		if(PokecubeCore.isOnClientSide()&&FMLClientHandler.instance().getServer()!=null)
		{
			return clientSlotClick(i, j, flag, entityplayer);
		}
    //	i = i + inv.getPage()*54;
        if (flag != 0 && flag != 5)
        {
            ItemStack itemstack = null;
            Slot slot = (Slot) inventorySlots.get(i);

            if (slot != null && slot.getHasStack())
            {
                ItemStack itemstack1 = slot.getStack();
                itemstack = itemstack1.copy();
                if(!ContainerPC.isItemValid(itemstack1)) return null;

                if (i > 35)
                {
                    if (!mergeItemStack(itemstack1, 0, 36, false))
                    {
                        return null;
                    }
                }
                else
                {
                    if (!mergeItemStack(itemstack1, 36, 89, false))
                    {
                        return null;
                    }
                }

                if (itemstack1.stackSize == 0)
                {
                    slot.putStack(null);
                }
                else
                {
                    slot.onSlotChanged();
                }

                if (itemstack1.stackSize != itemstack.stackSize)
                {
					slot.onPickupFromSlot(entityplayer, itemstack1);
                }
                else
                {
                    return null;
                }
            }
            
            //release = gpc.getReleaseState();

            if (itemstack != null && isItemValid(itemstack))
            {
                return null;
            }
            else
            {
                return null;
            }
        }
        else
        {
            return super.slotClick(i, j, flag, entityplayer);
        }
    }
	
    
    public ItemStack clientSlotClick(int i, int j, int flag,
            EntityPlayer player)
    {
    	ItemStack itemstack = invPlayer.getItemStack();
        Slot slot = (Slot) inventorySlots.get(i);
        ItemStack inSlot = slot.getStack();
    	if(flag == 0 || flag == 5)
    	{
	    	invPlayer.setItemStack(inSlot!=null?inSlot.copy():null);
	    	inSlot = itemstack;
	    	return inSlot;
    	}
        
    	return  inSlot;
    }
    
    /**
     * Returns true if the item is a filled pokecube.
     *
     * @param itemstack the itemstack to test
     * @return true if the id is a filled pokecube one, false otherwise
     */
    public static boolean isItemValid(ItemStack itemstack)
    {
    //	System.out.println(ConfigHandler.ONLYPOKECUBES);
    	if(itemstack==null)
    		return false;
    	
    	boolean eggorCube = PokecubeManager.isFilled(itemstack) || itemstack.getItem() == PokecubeItems.pokemobEgg;
    	
        return eggorCube;
    }
    
    @Override
	public Slot getSlot(int par1)
    {
    	return (Slot)this.inventorySlots.get(par1);
    }
    
    /**
     * args: slotID, itemStack to put in slot
     */
    @Override
	public void putStackInSlot(int par1, ItemStack par2ItemStack)
    {
    	this.getSlot(par1).putStack(par2ItemStack);
    }
    
    @Override
	@SideOnly(Side.CLIENT)

    /**
     * places itemstacks in first x slots, x being aitemstack.lenght
     */
    public void putStacksInSlots(ItemStack[] par1ArrayOfItemStack)
    {
        for (int i = 0; i < par1ArrayOfItemStack.length; ++i)
        {
        	if(this.getSlot(i)!=null)
            this.getSlot(i).putStack(par1ArrayOfItemStack[i]);
        }
    }
    
    @SideOnly(Side.CLIENT)
    public String getPage()
    {
    	return inv.boxes[inv.getPage()];
    }
    
    @SideOnly(Side.CLIENT)
    public String getPageNb()
    {
    	return Integer.toString(inv.getPage()+1);
    }
    
    public boolean getRelease(){
    	return release;
    }
    public void setRelease(boolean bool)
    {
    	if(release && !bool)
    	{
    		NBTTagCompound nbt = new NBTTagCompound();
    		nbt.setInteger("page", inv.getPage());
    		
    		for(int i = 0; i<54; i++)
    		{
    			if(toRelease[i])
    			{
    				nbt.setBoolean("val"+i, true);
    			}
    		}
    		MessageServer mess = new MessageServer(MessageServer.PCRELEASE, nbt);
    		PokecubePacketHandler.sendToServer(mess);
    	}
    	release = bool;
    }
}
