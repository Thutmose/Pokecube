package pokecube.adventures.blocks.afa;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.PokecubeItems;
import pokecube.core.items.pokecubes.PokecubeManager;
import thut.lib.CompatWrapper;

public class ContainerAFA extends Container
{
    private static class AFASlot extends Slot
    {

        public AFASlot(IInventory inventoryIn, int index, int x, int y)
        {
            super(inventoryIn, index, x, y);
        }

        @Override
        public boolean isItemValid(ItemStack itemstack)
        {
            return PokecubeManager.isFilled(itemstack)
                    || ItemStack.areItemStackTagsEqual(PokecubeItems.getStack("shiny_charm"), itemstack);
        }

        @Override
        public boolean canTakeStack(EntityPlayer playerIn)
        {
            TileEntityAFA tile = (TileEntityAFA) inventory;
            return tile.placer.equals(playerIn.getUniqueID());
        }
    }

    public TileEntityAFA tile;
    public World         world;
    int                  energy = 0;

    public BlockPos      pos;

    public ContainerAFA(TileEntityAFA tile, InventoryPlayer playerInv)
    {
        super();
        this.tile = tile;
        this.world = tile.getWorld();
        this.pos = tile.getPos();
        bindInventories(playerInv);
    }

    public void bindInventories(InventoryPlayer playerInv)
    {
        clearSlots();
        bindPlayerInventory(playerInv);
        addSlotToContainer(new AFASlot(tile, 0, 15, 12));
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
    public boolean canInteractWith(EntityPlayer playerIn)
    {
        return true;
    }

    protected void clearSlots()
    {
        this.inventorySlots.clear();
    }

    @Override
    public void addListener(IContainerListener listener)
    {
        super.addListener(listener);
        listener.sendAllWindowProperties(this, this.tile);
    }

    @Override
    /** Looks for changes made in the container, sends them to every
     * listener. */
    public void detectAndSendChanges()
    {
        for (int i = 0; i < this.listeners.size(); ++i)
        {
            IContainerListener icrafting = this.listeners.get(i);
            if (energy != tile.getField(0)) icrafting.sendWindowProperty(this, 0, this.tile.getField(0));
        }
        energy = tile.getField(0);
        super.detectAndSendChanges();
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index)
    {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);
        if (slot != null && slot.getHasStack())
        {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();
            if (index < 1)
            {
                if (!this.mergeItemStack(itemstack1, 1, this.inventorySlots.size(),
                        false)) { return ItemStack.EMPTY; }
            }
            else if (!this.mergeItemStack(itemstack1, 0, 1, false)) { return ItemStack.EMPTY; }
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

    @Override
    @SideOnly(Side.CLIENT)
    public void updateProgressBar(int id, int data)
    {
        this.tile.setField(id, data);
    }
}
