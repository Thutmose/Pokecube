package pokecube.adventures.blocks.cloner.container;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import pokecube.adventures.blocks.cloner.ClonerHelper;
import pokecube.adventures.blocks.cloner.crafting.CraftMatrix;
import pokecube.adventures.blocks.cloner.crafting.CraftResult;
import pokecube.adventures.blocks.cloner.tileentity.TileEntityCloner;
import thut.core.common.blocks.SmartSlot;
import thut.lib.CompatWrapper;

public class ContainerCloner extends ContainerBase
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
            ItemStack vanilla = CraftingManager.getInstance().findMatchingRecipe(cloner.getCraftMatrix(),
                    cloner.getWorld());
            if (vanilla != null)
            {
                super.onPickupFromSlot(playerIn, stack);
                return;
            }
            if (cloner.currentProcess != null) cloner.currentProcess.reset();
            else cloner.currentProcess = null;
            net.minecraftforge.fml.common.FMLCommonHandler.instance().firePlayerCraftingEvent(playerIn, stack,
                    cloner.getCraftMatrix());
            this.onCrafting(stack);
            net.minecraftforge.common.ForgeHooks.setCraftingPlayer(playerIn);
            List<ItemStack> aitemstack = ClonerHelper.getStacks(cloner);
            net.minecraftforge.common.ForgeHooks.setCraftingPlayer(null);
            for (int i = 0; i < aitemstack.size(); ++i)
            {
                ItemStack itemstack = cloner.getCraftMatrix().getStackInSlot(i);
                ItemStack itemstack1 = aitemstack.get(i);

                if (CompatWrapper.isValid(itemstack))
                {
                    cloner.getCraftMatrix().decrStackSize(i, 1);
                    itemstack = cloner.getCraftMatrix().getStackInSlot(i);
                }
                if (CompatWrapper.isValid(itemstack1))
                {
                    if (!CompatWrapper.isValid(itemstack))
                    {
                        cloner.getCraftMatrix().setInventorySlotContents(i, itemstack1);
                    }
                    else if (ItemStack.areItemsEqual(itemstack, itemstack1)
                            && ItemStack.areItemStackTagsEqual(itemstack, itemstack1))
                    {
                        CompatWrapper.increment(itemstack1, CompatWrapper.getStackSize(itemstack));
                        cloner.getCraftMatrix().setInventorySlotContents(i, itemstack1);
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

    public World            worldObj;
    public TileEntityCloner tile;
    public BlockPos         pos;

    public ContainerCloner(InventoryPlayer inv, TileEntityCloner tile)
    {
        super(inv, tile);
        this.tile = tile;
        this.worldObj = tile.getWorld();
        this.pos = tile.getPos();
        tile.setCraftMatrix(new CraftMatrix(this, tile, 3, 3));
        tile.result = new CraftResult(tile, 9);

        this.addSlotToContainer(
                new SlotClonerCrafting(tile, inv.player, tile.getCraftMatrix(), tile.result, 0, 124, 35));

        int di = 17;
        int di2 = 9;
        int dj = 32;

        int i = 0;
        int j = 0;
        this.addSlotToContainer(new SmartSlot(tile.getCraftMatrix(), 0, dj - 21 + j * 18, di + i * 18)
                .setTex("minecraft:textures/items/potion_bottle_empty.png"));
        i = 2;
        this.addSlotToContainer(new SmartSlot(tile.getCraftMatrix(), 1, dj - 21 + j * 18, di + i * 18)
                .setTex("minecraft:textures/items/spawn_egg.png"));

        i = 0;
        this.addSlotToContainer(new SmartSlot(tile.getCraftMatrix(), 2, dj + j * 18, di + di2 + i * 18));
        i = 1;
        this.addSlotToContainer(new SmartSlot(tile.getCraftMatrix(), 3, dj + j * 18, di + di2 + i * 18));

        i = 0;
        j = 1;
        this.addSlotToContainer(new SmartSlot(tile.getCraftMatrix(), 5, dj + j * 18, di + i * 18));
        i = 1;
        this.addSlotToContainer(new SmartSlot(tile.getCraftMatrix(), 6, dj + j * 18, di + i * 18));
        i = 2;
        this.addSlotToContainer(new SmartSlot(tile.getCraftMatrix(), 7, dj + j * 18, di + i * 18));

        j = 2;
        i = 0;
        this.addSlotToContainer(new SmartSlot(tile.getCraftMatrix(), 8, dj + j * 18, di + di2 + i * 18));
        i = 1;
        this.addSlotToContainer(new SmartSlot(tile.getCraftMatrix(), 4, dj + j * 18, di + di2 + i * 18));

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

        this.onCraftMatrixChanged(tile.getCraftMatrix());
        tile.openInventory(inv.player);
    }

    @Override
    /** Callback for when the crafting matrix is changed. */
    public void onCraftMatrixChanged(IInventory inv)
    {
        ItemStack vanilla = CraftingManager.getInstance().findMatchingRecipe(tile.getCraftMatrix(), this.worldObj);
        if (vanilla != null)
        {
            tile.result.setInventorySlotContents(0, vanilla);
        }
        else if (tile.currentProcess != null)
        {
            if (!tile.currentProcess.valid())
            {
                tile.result.setInventorySlotContents(0, CompatWrapper.nullStack);
                if (tile.currentProcess != null) tile.currentProcess.reset();
                tile.setField(0, 0);
                tile.setField(1, 0);
            }
        }
    }

    @Override
    protected void updateCrafting()
    {
        onCraftMatrixChanged(tile);
    }
}
