package pokecube.adventures.blocks.cloner.container;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import pokecube.adventures.blocks.cloner.ClonerHelper;
import pokecube.adventures.blocks.cloner.tileentity.TileClonerBase;
import thut.lib.CompatWrapper;

public class SlotClonerCrafting extends SlotCrafting
{
    final TileClonerBase cloner;

    public SlotClonerCrafting(TileClonerBase cloner, EntityPlayer player, InventoryCrafting craftingInventory,
            IInventory inventoryIn, int slotIndex, int xPosition, int yPosition)
    {
        super(player, craftingInventory, inventoryIn, slotIndex, xPosition, yPosition);
        this.cloner = cloner;
    }

    /** the itemStack passed in is the output - ie, iron ingots, and pickaxes,
     * not ore and wood. */
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
        if (cloner.getProcess() != null) cloner.getProcess().reset();
        else cloner.getProcess().recipe = null;
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