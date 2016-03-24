package pokecube.pokeplayer.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.core.PokecubeItems;
import pokecube.core.interfaces.IPokemob;
import pokecube.pokeplayer.PokePlayer;

public class ContainerPokemob extends Container
{
	private IInventory	pokemobInv;
	public ContainerPokemob(EntityPlayer player)
	{
	    final IPokemob e = PokePlayer.PROXY.getPokemob(player);
	    final IInventory pokeInv;
	    
        pokeInv = PokePlayer.PROXY.getMap().get(player.getUniqueID()).pokeInventory;
	    
	    IInventory playerInv = player.inventory;
		this.pokemobInv = pokeInv;
		byte b0 = 3;
		pokeInv.openInventory(null);
		int i = (b0 - 4) * 18;
		int slot = 0;
		this.addSlotToContainer(new Slot(pokeInv, slot++, 8, 18)
		{
			/** Check if the stack is a valid item for this slot. Always true
			 * beside for the armor slots. */
			@Override
			public boolean isItemValid(ItemStack stack)
			{
				return super.isItemValid(stack) && stack.getItem() == Items.saddle && !this.getHasStack();
			}
		});
		this.addSlotToContainer(new Slot(pokeInv, slot++, 8, 36)
		{
            
            /** Returns the maximum stack size for a given slot (usually the
			 * same as getInventoryStackLimit(), but 1 in the case of armor
			 * slots) */
			@Override
			public int getSlotStackLimit()
			{
				return 1;
			}

            /** Check if the stack is a valid item for this slot. Always true
			 * beside for the armor slots. */
			@Override
			public boolean isItemValid(ItemStack stack)
			{
				return PokecubeItems.isValidHeldItem(stack);
			}
		    
			@Override
            public void onPickupFromSlot(EntityPlayer playerIn, ItemStack stack)
            {
                ItemStack old = getStack();
                super.onPickupFromSlot(playerIn, stack);
                if(FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
                {
                    e.getPokedexEntry().onHeldItemChange(stack, old, e);
                }
            }

			/**
             * Helper method to put a stack in the slot.
             */
            @Override
            public void putStack(ItemStack stack)
            {
                ItemStack old = getStack();
                super.putStack(stack);
                if(FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
                {
                    e.getPokedexEntry().onHeldItemChange(old, stack, e);
                }
            }
		});
		int j;
		int k;

		for (j = 0; j < 1; ++j)
		{
			for (k = 0; k < 5; ++k)
			{
				this.addSlotToContainer(new Slot(pokeInv, slot++, 80 + k * 18, 18 + j * 18)
				{
					/** Check if the stack is a valid item for this slot. Always
					 * true beside for the armor slots. */
					@Override
					public boolean isItemValid(ItemStack stack)
					{
						return true;//PokecubeItems.isValidHeldItem(stack);
					}
				});
			}
		}
		slot = 0;
        for (j = 0; j < 9; ++j)
        {
            this.addSlotToContainer(new Slot(playerInv, slot++, 8 + j * 18, 160 + i));
        }

		for (j = 0; j < 3; ++j)
		{
			for (k = 0; k < 9; ++k)
			{
				this.addSlotToContainer(new Slot(playerInv, slot++, 8 + k * 18, 102 + j * 18 + i));
			}
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer player)
	{
		return true;
	}

	/** Called when the container is closed. */
	@Override
	public void onContainerClosed(EntityPlayer player)
	{
		super.onContainerClosed(player);
		this.pokemobInv.closeInventory(player);
	}

	/** Called when a player shift-clicks on a slot. You must override this or
	 * you will crash when someone does that. */
	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slotId)
	{
		ItemStack itemstack = null;
		Slot slot = this.inventorySlots.get(slotId);

		if (slot != null && slot.getHasStack())
		{
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();

			if (slotId < this.pokemobInv.getSizeInventory())
			{
				if (!this.mergeItemStack(itemstack1, this.pokemobInv.getSizeInventory(), this.inventorySlots.size(),
						true)) { return null; }
			}
			else if (this.getSlot(1).isItemValid(itemstack1) && !this.getSlot(1).getHasStack())
			{
			    this.getSlot(1).putStack(slot.getStack().splitStack(1));
			}
			else if (this.getSlot(0).isItemValid(itemstack1))
			{
				if (!this.mergeItemStack(itemstack1, 0, 1, false)) { return null; }
			}
			else if (this.pokemobInv.getSizeInventory() <= 2
					|| !this.mergeItemStack(itemstack1, 2, this.pokemobInv.getSizeInventory(), false)) { return null; }

			if (itemstack1.stackSize == 0)
			{
				slot.putStack((ItemStack) null);
			}
			else
			{
				slot.onSlotChanged();
			}
		}
		return itemstack;
	}
	

    public ItemStack slotClick(int slotId, int clickedButton, int mode, EntityPlayer playerIn)
    {
        ItemStack stack = super.slotClick(slotId, clickedButton, mode, playerIn);
        return stack;
    }
}
