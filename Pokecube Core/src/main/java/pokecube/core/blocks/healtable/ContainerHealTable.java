package pokecube.core.blocks.healtable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import pokecube.core.interfaces.IHealer;
import pokecube.core.items.pokecubes.PokecubeManager;
import thut.lib.CompatWrapper;

public class ContainerHealTable extends Container implements IHealer
{
    /** Returns true if the item is a filled pokecube.
     *
     * @param itemstack
     *            the itemstack to test
     * @return true if the id is a filled pokecube one, false otherwise */
    protected static boolean isItemValid(ItemStack itemstack)
    {
        return PokecubeManager.isFilled(itemstack) && itemstack.hasTagCompound();
    }

    InventoryHealTable inventoryHealTable;

    public ContainerHealTable(InventoryPlayer player_inventory)
    {
        inventoryHealTable = new InventoryHealTable(this, "heal");
        int index = 0;

        for (int i = 0; i < 3; i++)
        {
            for (int l = 0; l < 2; l++)
            {
                addSlotToContainer(new SlotHealTable(player_inventory.player, inventoryHealTable, index++, 62 + l * 18,
                        17 + i * 18));
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
                addSlotToContainer(new Slot(player_inventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
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
        return true;
    }

    /** Heals all the Pokecubes in the heal inventory. It means, it sets the
     * damage with the value for a full healthy Pokemob for each of the 6
     * pokecubes. */
    @Override
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
    public void onContainerClosed(EntityPlayer player)
    {
        super.onContainerClosed(player);

        if (!player.getEntityWorld().isRemote)
        {
            for (int var2 = 0; var2 < this.inventoryHealTable.getSizeInventory(); ++var2)
            {
                ItemStack var3 = this.inventoryHealTable.removeStackFromSlot(var2);

                if (var3 != CompatWrapper.nullStack)
                {
                    if (player.isDead || player.getHealth() <= 0 || player.inventory.getFirstEmptyStack() == -1)
                    {
                        ItemTossEvent toss = new ItemTossEvent(player.entityDropItem(var3, 0F), null);
                        MinecraftForge.EVENT_BUS.post(toss);
                        if (!toss.isCanceled())
                        {
                            player.dropItem(var3, true);
                        }
                    }
                    else if (var3.getItem() != null
                            && (player.isDead || !player.inventory.addItemStackToInventory(var3)))
                    {
                        ItemTossEvent toss = new ItemTossEvent(player.entityDropItem(var3, 0F), null);
                        MinecraftForge.EVENT_BUS.post(toss);
                    }
                    else player.dropItem(var3, true);
                    if (player instanceof EntityPlayerMP)
                    {
                        ((EntityPlayerMP) player).sendContainerToPlayer(player.inventoryContainer);
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

            if (stack_in_slot.stackSize == 0)
            {
                slot_object.putStack(CompatWrapper.nullStack);
            }
            else
            {
                slot_object.onSlotChanged();
            }
        }
    }

    @Override
    public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player)
    {
        if (slotId < 0) return CompatWrapper.nullStack;
        if (clickTypeIn != ClickType.PICKUP && clickTypeIn != ClickType.PICKUP_ALL)
        {
            ItemStack itemstack = CompatWrapper.nullStack;
            Slot slot = inventorySlots.get(slotId);

            if (slot != null && slot.getHasStack())
            {
                ItemStack itemstack1 = slot.getStack();
                itemstack = itemstack1.copy();

                if (slotId < 6)
                {
                    if (!mergeItemStack(itemstack1, 6, 42, true)) { return CompatWrapper.nullStack; }
                }
                else
                {
                    if (itemstack != null && !isItemValid(itemstack1)) { return CompatWrapper.nullStack; }

                    if (!mergeItemStack(itemstack1, 0, 6, false)) { return CompatWrapper.nullStack; }
                }

                if (!CompatWrapper.isValid(itemstack1))
                {
                    slot.putStack(CompatWrapper.nullStack);
                }
                else
                {
                    slot.onSlotChanged();
                }

                if (CompatWrapper.getStackSize(itemstack1) != CompatWrapper.getStackSize(itemstack))
                {
                    // slot.onPickupFromSlot(itemstack1);
                }
                else
                {
                    return CompatWrapper.nullStack;
                }
            }

            if (itemstack != CompatWrapper.nullStack && isItemValid(itemstack)) { return itemstack; }
            return CompatWrapper.nullStack;
        }
        return super.slotClick(slotId, dragType, clickTypeIn, player);
    }
}