package pokecube.core.entity.pokemobs;

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
import thut.lib.CompatWrapper;

public class ContainerPokemob extends Container
{
    private IInventory pokemobInv;
    private IPokemob   pokemob;
    private boolean    hasSlots = true;

    public ContainerPokemob(IInventory playerInv, final IInventory pokeInv, final IPokemob e)
    {
        this(playerInv, pokeInv, e, true);
    }

    public ContainerPokemob(IInventory playerInv, final IInventory pokeInv, final IPokemob e, boolean slots)
    {
        this.pokemobInv = pokeInv;
        this.pokemob = e;
        this.hasSlots = slots;
        byte b0 = 3;
        pokeInv.openInventory(null);
        int i = (b0 - 4) * 18;
        int j;
        int k;
        if (slots)
        {
            this.addSlotToContainer(new Slot(pokeInv, 0, 8, 18)
            {
                /** Check if the stack is a valid item for this slot. Always true
                 * beside for the armor slots. */
                @Override
                public boolean isItemValid(ItemStack stack)
                {
                    return super.isItemValid(stack) && stack.getItem() == Items.SADDLE;
                }
            });
            this.addSlotToContainer(new Slot(pokeInv, 1, 8, 36)
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
                public ItemStack onTake(EntityPlayer playerIn, ItemStack stack)
                {
                    ItemStack old = getStack();
                    if(FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
                    {
                        e.getPokedexEntry().onHeldItemChange(stack, old, e);
                    }
                    return super.onTake(playerIn, stack);
                }

                /** Helper method to put a stack in the slot. */
                @Override
                public void putStack(ItemStack stack)
                {
                    // ItemStack old = getStack();
                    super.putStack(stack);
                    if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
                    {
                        e.setHeldItem(stack);
                        // e.getPokedexEntry().onHeldItemChange(old, stack, e);
                    }
                }
            });
            for (j = 0; j < 1; ++j)
            {
                for (k = 0; k < 5; ++k)
                {
                    this.addSlotToContainer(new Slot(pokeInv, 2 + k + j * 5, 80 + k * 18, 18 + j * 18)
                    {
                        /** Check if the stack is a valid item for this slot.
                         * Always true beside for the armor slots. */
                        @Override
                        public boolean isItemValid(ItemStack stack)
                        {
                            return true;// PokecubeItems.isValidHeldItem(stack);
                        }
                    });
                }
            }
        }

        for (j = 0; j < 3; ++j)
        {
            for (k = 0; k < 9; ++k)
            {
                this.addSlotToContainer(new Slot(playerInv, k + j * 9 + 9, 8 + k * 18, 102 + j * 18 + i));
            }
        }

        for (j = 0; j < 9; ++j)
        {
            this.addSlotToContainer(new Slot(playerInv, j, 8 + j * 18, 160 + i));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer p_75145_1_)
    {
        return this.pokemobInv.isUsableByPlayer(p_75145_1_) && this.pokemob.getEntity().isEntityAlive()
                && this.pokemob.getEntity().getDistance(p_75145_1_) < 8.0F;
    }

    /** Called when the container is closed. */
    @Override
    public void onContainerClosed(EntityPlayer p_75134_1_)
    {
        super.onContainerClosed(p_75134_1_);
        this.pokemobInv.closeInventory(p_75134_1_);
    }

    /** Called when a player shift-clicks on a slot. You must override this or
     * you will crash when someone does that. */
    @Override
    public ItemStack transferStackInSlot(EntityPlayer p_82846_1_, int p_82846_2_)
    {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(p_82846_2_);

        if (hasSlots && slot != null && slot.getHasStack())
        {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();
            int size = this.pokemobInv.getSizeInventory();
            if (p_82846_2_ < size)
            {
                if (!this.mergeItemStack(itemstack1, size, size, true)) { return ItemStack.EMPTY; }
            }
            else if (this.getSlot(1).isItemValid(itemstack1) && !this.getSlot(1).getHasStack())
            {
                this.getSlot(1).putStack(slot.getStack().splitStack(1));
            }
            else if (this.getSlot(0).isItemValid(itemstack1))
            {
                if (!this.mergeItemStack(itemstack1, 0, 1, false)) { return ItemStack.EMPTY; }
            }
            else if (size <= 2 || !this.mergeItemStack(itemstack1, 2, size, false)) { return ItemStack.EMPTY; }

            if (!CompatWrapper.isValid(itemstack1))
            {
                slot.putStack(ItemStack.EMPTY);
            }
            else
            {
                slot.onSlotChanged();
            }
        }

        return itemstack;
    }

    public IPokemob getPokemob()
    {
        return pokemob;
    }
}
