package pokecube.core.blocks.tradingTable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.PokecubeItems;
import pokecube.core.interfaces.IPokecube;
import pokecube.core.items.pokecubes.PokecubeManager;

public class ContainerTradingTable extends Container
{
    /** Returns true if the item is a filled pokecube.
     *
     * @param itemstack
     *            the itemstack to test
     * @return true if the id is a filled pokecube one, false otherwise */
    protected static boolean isItemValid(ItemStack itemstack)
    {
        return (!PokecubeManager.isFilled(itemstack) && itemstack.hasTagCompound()
                && PokecubeItems.getCubeId(itemstack) == 14)
                || (itemstack.getItem() == Items.EMERALD && itemstack.stackSize == 64)
                || (itemstack.getItem() instanceof IPokecube && itemstack.stackSize == 1);
    }

    TileEntityTradingTable tile;

    public ContainerTradingTable(TileEntityTradingTable tile, InventoryPlayer playerInv)
    {
        this.tile = tile;
        bindInventories(playerInv);
    }

    public void bindInventories(InventoryPlayer playerInv)
    {
        clearSlots();
        if (tile.trade)
        {
            addSlotToContainer(new SlotTrade(tile, 0, 15, 12));
            addSlotToContainer(new SlotTrade(tile, 1, 140, 12));
        }
        bindPlayerInventory(playerInv);
    }

    private void bindPlayerInventory(InventoryPlayer playerInventory)
    {
        // Inventory
        for (int y = 0; y < 3; y++)
            for (int x = 0; x < 9; x++)
                addSlotToContainer(new Slot(playerInventory, x + y * 9 + 9, 8 + x * 18, 84 + y * 18));

        // Action Bar
        for (int x = 0; x < 9; x++)
            addSlotToContainer(new Slot(playerInventory, x, 8 + x * 18, 142));
    }

    @Override
    public boolean canInteractWith(EntityPlayer entityPlayer)
    {
        return tile.isUseableByPlayer(entityPlayer);
    }

    protected void clearSlots()
    {
        this.inventorySlots.clear();
    }

    @Override
    public Slot getSlot(int par1)
    {
        if (par1 < this.inventorySlots.size()) return this.inventorySlots.get(par1);
        return null;
    }

    public TileEntityTradingTable getTile()
    {
        return tile;
    }

    /** args: slotID, itemStack to put in slot */
    @Override
    public void putStackInSlot(int par1, ItemStack par2ItemStack)
    {
        if (this.getSlot(par1) != null) this.getSlot(par1).putStack(par2ItemStack);
    }

    @Override
    @SideOnly(Side.CLIENT)

    /** places itemstacks in first x slots, x being aitemstack.lenght */
    public void putStacksInSlots(ItemStack[] par1ArrayOfItemStack)
    {
        for (int i = 0; i < par1ArrayOfItemStack.length; ++i)
        {
            if (this.getSlot(i) != null) this.getSlot(i).putStack(par1ArrayOfItemStack[i]);
        }
    }

    @Override
    public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player)
    {
        return super.slotClick(slotId, dragType, clickTypeIn, player);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index)
    {
        ItemStack itemstack = null;
        Slot slot = (Slot) this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack())
        {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();

            if (index < 2)
            {
                if (!this.mergeItemStack(itemstack1, 2, this.inventorySlots.size(), false)) { return null; }
            }
            else if (!this.mergeItemStack(itemstack1, 0, 2, false)) { return null; }

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
}
