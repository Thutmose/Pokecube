package pokecube.core.blocks.tradingTable;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.PokecubeItems;
import pokecube.core.interfaces.IPokecube;
import pokecube.core.items.pokecubes.PokecubeManager;
import thut.lib.CompatWrapper;

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
                || (itemstack.getItem() == Items.EMERALD && CompatWrapper.getStackSize(itemstack) == 64)
                || (itemstack.getItem() instanceof IPokecube && CompatWrapper.getStackSize(itemstack) == 1);
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
            addSlotToContainer(new SlotTrade(tile, 0, 35, 14));
            addSlotToContainer(new SlotTrade(tile, 1, 125, 14));
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

    // 1.11
    @SideOnly(Side.CLIENT)
    public void func_190896_a(List<ItemStack> stacks)
    {
        for (int i = 0; i < stacks.size(); ++i)
        {
            this.getSlot(i).putStack((ItemStack) stacks.get(i));
        }
    }

    // 1.10
    @SideOnly(Side.CLIENT)
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
        ItemStack itemstack = CompatWrapper.nullStack;
        Slot slot = this.inventorySlots.get(index);
        if (slot != null && slot.getHasStack())
        {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();
            if (index < 2)
            {
                if (!this.mergeItemStack(itemstack1, 2, this.inventorySlots.size(),
                        false)) { return CompatWrapper.nullStack; }
            }
            else if (!this.mergeItemStack(itemstack1, 0, 2, false)) { return CompatWrapper.nullStack; }
            if (!CompatWrapper.isValid(itemstack1))
            {
                slot.putStack(CompatWrapper.nullStack);
            }
            else
            {
                slot.onSlotChanged();
            }
        }
        return itemstack;
    }

    @Override
    public void addListener(IContainerListener listener)
    {
        super.addListener(listener);
        listener.sendAllWindowProperties(this, this.tile);
    }

    int id1 = -1;
    int id2 = -1;

    @Override
    /** Looks for changes made in the container, sends them to every
     * listener. */
    public void detectAndSendChanges()
    {
        for (int i = 0; i < this.listeners.size(); ++i)
        {
            IContainerListener icrafting = this.listeners.get(i);
            if (id1 != tile.getField(0)) icrafting.sendProgressBarUpdate(this, 0, this.tile.getField(0));
            if (id2 != tile.getField(1)) icrafting.sendProgressBarUpdate(this, 1, this.tile.getField(1));
        }
        id1 = tile.getField(0);
        id2 = tile.getField(1);
        super.detectAndSendChanges();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void updateProgressBar(int id, int data)
    {
        this.tile.setField(id, data);
    }

    /** Called when the container is closed. */
    @Override
    public void onContainerClosed(EntityPlayer player)
    {
        super.onContainerClosed(player);
        this.tile.closeInventory(player);
    }
}
