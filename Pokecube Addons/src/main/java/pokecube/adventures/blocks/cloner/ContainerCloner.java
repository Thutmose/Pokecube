package pokecube.adventures.blocks.cloner;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ContainerCloner extends Container
{
    public static class SlotClonerCrafting extends SlotCrafting
    {
        final TileEntityCloner cloner;

        public SlotClonerCrafting(TileEntityCloner cloner, EntityPlayer player, InventoryCrafting craftingInventory,
                IInventory inventoryIn, int slotIndex, int xPosition, int yPosition)
        {
            super(player, craftingInventory, inventoryIn, slotIndex, xPosition, yPosition);
            this.cloner = cloner;
        }

        /** the itemStack passed in is the output - ie, iron ingots, and
         * pickaxes, not ore and wood. */
        @Override
        protected void onCrafting(ItemStack stack)
        {
            super.onCrafting(stack);
        }

        @Override
        public void onPickupFromSlot(EntityPlayer playerIn, ItemStack stack)
        {
            ItemStack vanilla = CraftingManager.getInstance().findMatchingRecipe(cloner.craftMatrix, cloner.getWorld());
            if (vanilla != null)
            {
                super.onPickupFromSlot(playerIn, stack);
                return;
            }
            if (cloner.currentProcess != null) cloner.currentProcess.reset();
            else cloner.currentProcess = cloner.cloneProcess;
            net.minecraftforge.fml.common.FMLCommonHandler.instance().firePlayerCraftingEvent(playerIn, stack,
                    cloner.craftMatrix);
            this.onCrafting(stack);
            net.minecraftforge.common.ForgeHooks.setCraftingPlayer(playerIn);
            ItemStack[] aitemstack = cloner.currentProcess.recipe.getRemainingItems(cloner.craftMatrix);
            net.minecraftforge.common.ForgeHooks.setCraftingPlayer(null);
            for (int i = 0; i < aitemstack.length; ++i)
            {
                ItemStack itemstack = cloner.craftMatrix.getStackInSlot(i);
                ItemStack itemstack1 = aitemstack[i];

                if (itemstack != null)
                {
                    cloner.craftMatrix.decrStackSize(i, 1);
                    itemstack = cloner.craftMatrix.getStackInSlot(i);
                }
                if (itemstack1 != null)
                {
                    if (itemstack == null || itemstack.stackSize <= 0)
                    {
                        cloner.craftMatrix.setInventorySlotContents(i, itemstack1);
                    }
                    else if (ItemStack.areItemsEqual(itemstack, itemstack1)
                            && ItemStack.areItemStackTagsEqual(itemstack, itemstack1))
                    {
                        itemstack1.stackSize += itemstack.stackSize;
                        cloner.craftMatrix.setInventorySlotContents(i, itemstack1);
                    }
                    else if (!playerIn.inventory.addItemStackToInventory(itemstack1))
                    {
                        playerIn.dropItem(itemstack1, false);
                    }
                }
            }
            cloner.setField(0, 0);
        }

    }

    public InventoryPlayer inv;
    public World           worldObj;
    int                    progress;
    int                    total;
    TileEntityCloner       tile;
    public BlockPos        pos;
    ItemStack              cube   = null;
    ItemStack              egg    = null;
    ItemStack              star   = null;
    ItemStack              result = null;

    public ContainerCloner(InventoryPlayer inv, TileEntityCloner tile)
    {
        super();
        this.inv = inv;
        this.worldObj = tile.getWorld();
        this.tile = tile;
        this.pos = tile.getPos();

        tile.craftMatrix = new TileEntityCloner.CraftMatrix(this, tile);
        tile.result = new TileEntityCloner.CraftResult(tile);

        this.addSlotToContainer(new SlotClonerCrafting(tile, inv.player, tile.craftMatrix, tile.result, 0, 124, 35));

        for (int i = 0; i < 3; ++i)
        {
            for (int j = 0; j < 3; ++j)
            {
                this.addSlotToContainer(new Slot(tile.craftMatrix, j + i * 3, 30 + j * 18, 17 + i * 18));
            }
        }

        for (int k = 0; k < 3; ++k)
        {
            for (int i1 = 0; i1 < 9; ++i1)
            {
                this.addSlotToContainer(new Slot(inv, i1 + k * 9 + 9, 8 + i1 * 18, 84 + k * 18));
            }
        }

        for (int l = 0; l < 9; ++l)
        {
            this.addSlotToContainer(new Slot(inv, l, 8 + l * 18, 142));
        }

        this.onCraftMatrixChanged(tile.craftMatrix);
        tile.openInventory(inv.player);
    }

    @Override
    public void addListener(IContainerListener listener)
    {
        super.addListener(listener);
        listener.sendAllWindowProperties(this, this.tile);
    }

    @Override
    public boolean canInteractWith(EntityPlayer p)
    {
        return true;
    }

    @Override
    /** Looks for changes made in the container, sends them to every
     * listener. */
    public void detectAndSendChanges()
    {
        for (int i = 0; i < this.listeners.size(); ++i)
        {
            IContainerListener icrafting = this.listeners.get(i);
            if (progress != tile.getField(0)) icrafting.sendProgressBarUpdate(this, 0, this.tile.getField(0));
            if (total != tile.getField(1)) icrafting.sendProgressBarUpdate(this, 1, this.tile.getField(1));
            this.onCraftMatrixChanged(tile.craftMatrix);
        }
        progress = tile.getField(0);
        total = tile.getField(1);
        super.detectAndSendChanges();
    }

    /** Called when the container is closed. */
    @Override
    public void onContainerClosed(EntityPlayer playerIn)
    {
        super.onContainerClosed(playerIn);
        tile.closeInventory(playerIn);
    }

    @Override
    /** Callback for when the crafting matrix is changed. */
    public void onCraftMatrixChanged(IInventory inv)
    {
        ItemStack vanilla = CraftingManager.getInstance().findMatchingRecipe(tile.craftMatrix, this.worldObj);
        if (vanilla != null)
        {
            tile.result.setInventorySlotContents(0, vanilla);
        }
        else if (tile.currentProcess != null)
        {
            if (!tile.currentProcess.valid())
            {
                tile.result.setInventorySlotContents(0, null);
                if (tile.currentProcess != null) tile.currentProcess.reset();
                tile.setField(0, 0);
                tile.setField(1, 0);
            }
        }
    }

    @Override
    /** Handles slot click.
     * 
     * @param mode
     *            0 = basic click, 1 = shift click, 2 = hotbar, 3 = pick block,
     *            4 = drop, 5 = ?, 6 = double click */
    public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player)
    {
        return super.slotClick(slotId, dragType, clickTypeIn, player);
    }

    @Override
    /** This is called on shift click Take a stack from the specified inventory
     * slot. */
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index)
    {
        ItemStack itemstack = null;
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack())
        {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();
            if (index == 0)
            {
                if (!this.mergeItemStack(itemstack1, 10, 46, true)) { return null; }

                slot.onSlotChange(itemstack1, itemstack);
            }
            else if (index >= 10 && index < 37)
            {
                if (!this.mergeItemStack(itemstack1, 37, 46, false)) { return null; }
            }
            else if (index >= 37 && index < 46)
            {
                if (!this.mergeItemStack(itemstack1, 10, 37, false)) { return null; }
            }
            else if (!this.mergeItemStack(itemstack1, 10, 46, false)) { return null; }

            if (itemstack1.stackSize == 0)
            {
                slot.putStack((ItemStack) null);
            }
            else
            {
                slot.onSlotChanged();
            }

            if (itemstack1.stackSize == itemstack.stackSize) { return null; }

            slot.onPickupFromSlot(playerIn, itemstack1);
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
