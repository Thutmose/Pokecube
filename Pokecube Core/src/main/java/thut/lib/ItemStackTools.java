package thut.lib;

import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ReportedException;

public class ItemStackTools
{
    /** Adds the item stack to the inventory, returns false if it is
     * impossible. */
    public static boolean addItemStackToInventory(ItemStack itemStackIn, IInventory toAddTo, int minIndex)
    {
        if (CompatWrapper.getStackSize(itemStackIn) != 0 && itemStackIn.getItem() != null)
        {
            try
            {
                if (itemStackIn.isItemDamaged())
                {
                    int j = getFirstEmptyStack(toAddTo, minIndex);

                    if (j >= 0)
                    {
                        toAddTo.setInventorySlotContents(j, CompatWrapper.copy(itemStackIn));
                        toAddTo.getStackInSlot(j).animationsToGo = 5;
                        CompatWrapper.setStackSize(itemStackIn, 0);
                        return true;
                    }
                    return false;
                }
                int i;

                while (true)
                {
                    i = CompatWrapper.getStackSize(itemStackIn);
                    CompatWrapper.setStackSize(itemStackIn, storePartialItemStack(itemStackIn, toAddTo, minIndex));
                    int size = CompatWrapper.getStackSize(itemStackIn);
                    if (size <= 0 || size >= i)
                    {
                        break;
                    }
                }
                return CompatWrapper.getStackSize(itemStackIn) < i;
            }
            catch (Throwable throwable)
            {
                CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Adding item to inventory");
                CrashReportCategory crashreportcategory = crashreport.makeCategory("Item being added");
                crashreportcategory.addCrashSection("Item ID",
                        Integer.valueOf(Item.getIdFromItem(itemStackIn.getItem())));
                crashreportcategory.addCrashSection("Item data", Integer.valueOf(itemStackIn.getMetadata()));
                throw new ReportedException(crashreport);
            }
        }
        return false;
    }

    /** Returns the first item stack that is empty. */
    private static int getFirstEmptyStack(IInventory inventory, int minIndex)
    {
        for (int i = minIndex; i < inventory.getSizeInventory(); ++i)
        {
            if (inventory.getStackInSlot(i) == CompatWrapper.nullStack) { return i; }
        }
        return -1;
    }

    /** stores an itemstack in the users inventory */
    private static int storeItemStack(ItemStack itemStackIn, IInventory inventory, int minIndex)
    {
        for (int i = minIndex; i < inventory.getSizeInventory(); ++i)
        {
            int size = CompatWrapper.getStackSize(itemStackIn);
            if (inventory.getStackInSlot(i) != CompatWrapper.nullStack
                    && inventory.getStackInSlot(i).getItem() == itemStackIn.getItem()
                    && inventory.getStackInSlot(i).isStackable() && size < inventory.getStackInSlot(i).getMaxStackSize()
                    && size < inventory.getInventoryStackLimit()
                    && (!inventory.getStackInSlot(i).getHasSubtypes()
                            || inventory.getStackInSlot(i).getMetadata() == itemStackIn.getMetadata())
                    && ItemStack.areItemStackTagsEqual(inventory.getStackInSlot(i), itemStackIn)) { return i; }
        }

        return -1;
    }

    /** This function stores as many items of an ItemStack as possible in a
     * matching slot and returns the quantity of left over items. */
    private static int storePartialItemStack(ItemStack itemStackIn, IInventory inventory, int minIndex)
    {
        Item item = itemStackIn.getItem();
        int i = CompatWrapper.getStackSize(itemStackIn);
        int j = storeItemStack(itemStackIn, inventory, minIndex);

        if (j < 0)
        {
            j = getFirstEmptyStack(inventory, minIndex);
        }

        if (j < 0) { return i; }
        if (inventory.getStackInSlot(j) == CompatWrapper.nullStack)
        {
            inventory.setInventorySlotContents(j, new ItemStack(item, 0, itemStackIn.getMetadata()));

            if (itemStackIn.hasTagCompound())
            {
                inventory.getStackInSlot(j).setTagCompound((NBTTagCompound) itemStackIn.getTagCompound().copy());
            }
        }

        int k = i;
        int size = CompatWrapper.getStackSize(inventory.getStackInSlot(j));
        if (i > inventory.getStackInSlot(j).getMaxStackSize() - size)
        {
            k = inventory.getStackInSlot(j).getMaxStackSize() - size;
        }

        if (k > inventory.getInventoryStackLimit() - size)
        {
            k = inventory.getInventoryStackLimit() - size;
        }

        if (k == 0) { return i; }
        i = i - k;
        CompatWrapper.setStackSize(inventory.getStackInSlot(j), size + k);
        inventory.getStackInSlot(j).animationsToGo = 5;
        return i;
    }
}
