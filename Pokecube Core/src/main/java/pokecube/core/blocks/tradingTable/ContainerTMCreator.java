package pokecube.core.blocks.tradingTable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerTMCreator extends Container
{
	/**
     * Returns true if the item is a filled pokecube.
     *
     * @param itemstack the itemstack to test
     * @return true if the id is a filled pokecube one, false otherwise
     */
    protected static boolean isItemValid(ItemStack itemstack)
    {
        return true;
    }
	
	TileEntityTradingTable tile;
	
    public ContainerTMCreator(TileEntityTradingTable tile, InventoryPlayer playerInv)
	{
		this.tile = tile;
		tile.moves(playerInv.player);
		bindInventories(playerInv);
	}
    
    public void bindInventories(InventoryPlayer playerInv)
    {
    	clearSlots();
		bindPlayerInventory(playerInv);
		addSlotToContainer(new SlotTMCreator(tile, 0, 15, 12));
    }
    
	private void bindPlayerInventory(InventoryPlayer playerInventory)
	{
		// Inventory
		for(int y = 0; y < 3; y++)
			for(int x = 0; x < 9; x++)
				addSlotToContainer(new Slot(playerInventory, x + y * 9 + 9, 8 + x * 18, 84 + y * 18));
		
		// Action Bar
		for(int x = 0; x < 9; x++)
			addSlotToContainer(new Slot(playerInventory, x, 8 + x * 18, 142));
	}
	
	@Override
	public boolean canInteractWith(EntityPlayer entityPlayer) {
		return tile.isUseableByPlayer(entityPlayer);
	}
	
	protected void clearSlots()
    {
    	this.inventorySlots.clear();
    }

	 public TileEntityTradingTable getTile()
	{
		return tile;
	}
	 
    @Override
    public ItemStack slotClick(int i, int j, int flag,
            EntityPlayer entityplayer)
    {
    	if (i == -999)
    		return null;
//	    	LoggerPokecube.logMessage("i="+i+" | j="+j+" | flag="+flag);
        if (flag != 0 && flag != 5)
        {
            ItemStack itemstack = null;
            Slot slot = inventorySlots.get(i);

            if (slot != null && slot.getHasStack())
            {
                ItemStack itemstack1 = slot.getStack();
                itemstack = itemstack1.copy();

                if (i < 6)
                {
                    if (!mergeItemStack(itemstack1, 1, 37, true))
                    {
                        return null;
                    }
                }
                else
                {
                    if (itemstack != null && !tile.isItemValidForSlot(36,itemstack1))
                    {
                        return null;
                    }

                    if (!mergeItemStack(itemstack1, 0, 1, false))
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
//						slot.onPickupFromSlot(itemstack1);
                }
                else
                {
                    return null;
                }
            }

            if (itemstack != null && isItemValid(itemstack))
            {
                return itemstack;
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
}
