package pokecube.core.blocks.healtable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import pokecube.core.items.pokecubes.PokecubeManager;

public class ContainerHealTable extends Container
{
    /**
     * Returns true if the item is a filled pokecube.
     *
     * @param itemstack the itemstack to test
     * @return true if the id is a filled pokecube one, false otherwise
     */
    protected static boolean isItemValid(ItemStack itemstack)
    {
    	return PokecubeManager.isFilled(itemstack) && itemstack.hasTagCompound();
//        int itemId = itemstack.itemID;
//        return (itemId == mod_Pokecube.pokecubeFilled.blockID
//                || itemId == mod_Pokecube.greatcubeFilled.blockID
//                || itemId == mod_Pokecube.ultracubeFilled.blockID
//                || itemId == mod_Pokecube.mastercubeFilled.blockID);
    }
    protected TileHealTable tile_entity;

    InventoryHealTable inventoryHealTable;

    public ContainerHealTable(TileHealTable tile_entity,
            InventoryPlayer player_inventory)
    {
        this.tile_entity = tile_entity;
        inventoryHealTable = new InventoryHealTable(this, "heal");
        int index = 0;

        for (int i = 0; i < 3; i++)
        {
            for (int l = 0; l < 2; l++)
            {
                addSlotToContainer(new SlotHealTable(player_inventory.player, inventoryHealTable, index++, 62 + l * 18, 17 + i * 18));
            }
        }

        bindPlayerInventory(player_inventory);
    }

    protected void bindPlayerInventory(InventoryPlayer player_inventory)
    {
        for (int i = 0; i < 3; i++)
        {
            for (int j = 0; j < 9; j++)
            {
                addSlotToContainer(new Slot(player_inventory,
                        j + i * 9 + 9,
                        8 + j * 18,
                        84 + i * 18));
            }
        }

        for (int i = 0; i < 9; i++)
        {
            addSlotToContainer(new Slot(player_inventory, i, 8 + i * 18, 142));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player)
    {
        return tile_entity.isUseableByPlayer(player);
    }

    /**
     * Heals all the Pokecubes in the heal inventory.
     * It means, it sets the damage with the value for a full healthy Pokemob for
     * each of the 6 pokecubes.
     */
    public void heal()
    {
        for (int i = 0; i < 6; i++)
        {
            Slot slot = getSlot(i);

            if (slot instanceof SlotHealTable)
            {
                ((SlotHealTable) slot).heal();
            }
        }
    }

    @Override
    public void onContainerClosed(EntityPlayer player) {
    	super.onContainerClosed(player);
    	
        if (!this.tile_entity.getWorld().isRemote)
        {
            for (int var2 = 0; var2 < this.inventoryHealTable.getSizeInventory(); ++var2)
            {
                ItemStack var3 = this.inventoryHealTable.removeStackFromSlot(var2);

                if (var3 != null)
                {
                	
    	            if(player.inventory.getFirstEmptyStack()==-1)
    	            {
    	            	
    	            	ItemTossEvent toss = new ItemTossEvent(player.entityDropItem(var3, 0F), null);
    	            	MinecraftForge.EVENT_BUS.post(toss);
    	            	//InventoryPC.addPokecubeToPC(itemstack);
    	            }
    	            else if (var3.getItem()!=null&&(player.isDead || !player.inventory.addItemStackToInventory(var3)))
    	            {
    	            	ItemTossEvent toss = new ItemTossEvent(player.entityDropItem(var3, 0F), null);
    	            	MinecraftForge.EVENT_BUS.post(toss);
    	            	//InventoryPC.addPokecubeToPC(itemstack);
    	            }
    	            else
    	            	player.dropItem(var3, true);
    	            if(player instanceof EntityPlayerMP)
    	            {
    		    		((EntityPlayerMP)player).sendContainerToPlayer(player.inventoryContainer);
    	            }
                }
            }
        }
    }

    @Override
    public void putStackInSlot(int slot_index, ItemStack itemStack)
    {
        Slot slot_object = inventorySlots.get(slot_index);

        if (slot_object != null && slot_object.getHasStack())
        {
            ItemStack stack_in_slot = slot_object.getStack();
            

//			if (slot_index == 0) {
//				if (!mergeItemStack(stack_in_slot, 1, inventorySlots.size(),
//						true)) {
//					return;
//				}
//			} else if (!mergeItemStack(stack_in_slot, 0, 1, false)) {
//				return;
//			}

            if (stack_in_slot.stackSize == 0)
            {
                slot_object.putStack(null);
            }
            else
            {
                slot_object.onSlotChanged();
            }
        }

        // return stack;
    }

//    @Override//TODO slot click
//    public ItemStack slotClick(int i, int j, int flag,
//            EntityPlayer entityplayer)
//    {
//    	if (i < 0)
//    		return null;
////    	System.out.println("i="+i+" | j="+j+" | flag="+flag);
//        if (flag != 0 && flag != 5)
//        {
//            ItemStack itemstack = null;
//            Slot slot = inventorySlots.get(i);
//
//            if (slot != null && slot.getHasStack())
//            {
//                ItemStack itemstack1 = slot.getStack();
//                itemstack = itemstack1.copy();
//
//                if (i < 6)
//                {
//                    if (!mergeItemStack(itemstack1, 6, 42, true))
//                    {
//                        return null;
//                    }
//                }
//                else
//                {
//                    if (itemstack != null && !isItemValid(itemstack1))
//                    {
//                        return null;
//                    }
//
//                    if (!mergeItemStack(itemstack1, 0, 6, false))
//                    {
//                        return null;
//                    }
//                }
//
//                if (itemstack1.stackSize == 0)
//                {
//                    slot.putStack(null);
//                }
//                else
//                {
//                    slot.onSlotChanged();
//                }
//
//                if (itemstack1.stackSize != itemstack.stackSize)
//                {
////					slot.onPickupFromSlot(itemstack1);
//                }
//                else
//                {
//                    return null;
//                }
//            }
//
//            if (itemstack != null && isItemValid(itemstack))
//            {
//                return itemstack;
//            }
//            else
//            {
//                return null;
//            }
//        }
//        else
//        {
//            return super.slotClick(i, j, flag, entityplayer);
//        }
//    }
}