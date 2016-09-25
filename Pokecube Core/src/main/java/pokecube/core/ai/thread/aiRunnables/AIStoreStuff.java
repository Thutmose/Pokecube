package pokecube.core.ai.thread.aiRunnables;

import com.google.common.base.Predicate;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.EntityLiving;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ReportedException;
import net.minecraft.world.World;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.items.berries.ItemBerry;
import thut.api.maths.Vector3;

public class AIStoreStuff extends AIBase
{
    public static int COOLDOWN = 500;

    /** Adds the item stack to the inventory, returns false if it is
     * impossible. */
    public static boolean addItemStackToInventory(ItemStack itemStackIn, IInventory toAddTo, int minIndex)
    {
        if (itemStackIn != null && itemStackIn.stackSize != 0 && itemStackIn.getItem() != null)
        {
            try
            {
                if (itemStackIn.isItemDamaged())
                {
                    int j = getFirstEmptyStack(toAddTo, minIndex);

                    if (j >= 0)
                    {
                        toAddTo.setInventorySlotContents(j, ItemStack.copyItemStack(itemStackIn));
                        toAddTo.getStackInSlot(j).animationsToGo = 5;
                        itemStackIn.stackSize = 0;
                        return true;
                    }
                    return false;
                }
                int i;

                while (true)
                {
                    i = itemStackIn.stackSize;
                    itemStackIn.stackSize = storePartialItemStack(itemStackIn, toAddTo, minIndex);

                    if (itemStackIn.stackSize <= 0 || itemStackIn.stackSize >= i)
                    {
                        break;
                    }
                }

                return itemStackIn.stackSize < i;
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
            if (inventory.getStackInSlot(i) == null) { return i; }
        }

        return -1;
    }

    /** stores an itemstack in the users inventory */
    private static int storeItemStack(ItemStack itemStackIn, IInventory inventory, int minIndex)
    {
        for (int i = minIndex; i < inventory.getSizeInventory(); ++i)
        {
            if (inventory.getStackInSlot(i) != null && inventory.getStackInSlot(i).getItem() == itemStackIn.getItem()
                    && inventory.getStackInSlot(i).isStackable()
                    && inventory.getStackInSlot(i).stackSize < inventory.getStackInSlot(i).getMaxStackSize()
                    && inventory.getStackInSlot(i).stackSize < inventory.getInventoryStackLimit()
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
        int i = itemStackIn.stackSize;
        int j = storeItemStack(itemStackIn, inventory, minIndex);

        if (j < 0)
        {
            j = getFirstEmptyStack(inventory, minIndex);
        }

        if (j < 0)
        {
            return i;
        }
        if (inventory.getStackInSlot(j) == null)
        {
            inventory.setInventorySlotContents(j, new ItemStack(item, 0, itemStackIn.getMetadata()));

            if (itemStackIn.hasTagCompound())
            {
                inventory.getStackInSlot(j).setTagCompound((NBTTagCompound) itemStackIn.getTagCompound().copy());
            }
        }

        int k = i;

        if (i > inventory.getStackInSlot(j).getMaxStackSize() - inventory.getStackInSlot(j).stackSize)
        {
            k = inventory.getStackInSlot(j).getMaxStackSize() - inventory.getStackInSlot(j).stackSize;
        }

        if (k > inventory.getInventoryStackLimit() - inventory.getStackInSlot(j).stackSize)
        {
            k = inventory.getInventoryStackLimit() - inventory.getStackInSlot(j).stackSize;
        }

        if (k == 0)
        {
            return i;
        }
        i = i - k;
        inventory.getStackInSlot(j).stackSize += k;
        inventory.getStackInSlot(j).animationsToGo = 5;
        return i;
    }

    final EntityLiving entity;
    Vector3            inventoryLocation       = null;
    int                searchInventoryCooldown = 0;
    int                doStorageCooldown       = 0;

    public AIStoreStuff(EntityLiving entity)
    {
        this.entity = entity;
        this.setMutex(2);
    }

    @Override
    public void doMainThreadTick(World world)
    {
        super.doMainThreadTick(world);
        if (tameCheck()) return;
        IPokemob pokemob = (IPokemob) entity;
        IInventory inventory = pokemob.getPokemobInventory();
        if (searchInventoryCooldown-- < 0)
        {
            searchInventoryCooldown = COOLDOWN;
            Vector3 temp = Vector3.getNewVector();
            temp.set(pokemob.getHome()).offsetBy(EnumFacing.UP);
            Predicate<Object> matcher = new Predicate<Object>()
            {
                @Override
                public boolean apply(Object t)
                {
                    if (!(t instanceof IBlockState)) return false;
                    IBlockState state = (IBlockState) t;
                    if (state.getBlock() instanceof ITileEntityProvider)
                    {
                        TileEntity tile = ((ITileEntityProvider) state.getBlock()).createNewTileEntity(null,
                                state.getBlock().getMetaFromState(state));
                        return tile instanceof IInventory;
                    }
                    return false;
                }
            };
            inventoryLocation = temp.findClosestVisibleObject(world, true, 5, matcher);
            if (inventoryLocation == null) searchInventoryCooldown = 50 * COOLDOWN;
        }
        if (inventoryLocation == null || doStorageCooldown-- > 0) return;
        TileEntity tile = inventoryLocation.getTileEntity(world);
        if (tile != null)
        {
            ItemStack stack;
            ItemStack stack1;
            boolean hasBerry = (stack = stack1 = inventory.getStackInSlot(2)) != null
                    && stack.getItem() instanceof ItemBerry;
            boolean freeSlot = false;

            for (int i = 3; i < inventory.getSizeInventory() && !freeSlot; i++)
            {
                freeSlot = (stack = inventory.getStackInSlot(i)) == null;
            }
            int index = inventory.getSizeInventory() - 1;
            IInventory inv = (IInventory) tile;
            if (!hasBerry) for (int i = 0; i < inv.getSizeInventory(); i++)
            {
                stack = inv.getStackInSlot(i);
                // If it wants a berry, search for a berry item, and take that.
                if (stack != null && !hasBerry)
                {
                    if (stack.getItem() instanceof ItemBerry)
                    {
                        inv.setInventorySlotContents(i, stack1);
                        inventory.setInventorySlotContents(2, stack);
                        hasBerry = true;
                        break;
                    }
                }
                if (!hasBerry) doStorageCooldown = COOLDOWN;
            }
            if (!freeSlot)
            {
                for (int i = 0; i < inv.getSizeInventory(); i++)
                {
                    stack = inv.getStackInSlot(i);
                    // If it has full inventory, deposit all but the berry
                    // stack.
                    if (addItemStackToInventory(inventory.getStackInSlot(index), inv, 0))
                    {
                        inventory.setInventorySlotContents(index, null);
                        freeSlot = true;
                        index--;
                        if (index <= 2) break;
                    }
                }
                if (index <= 2) freeSlot = true;
                if (!freeSlot) doStorageCooldown = COOLDOWN;
            }
        }
    }

    @Override
    public void reset()
    {
    }

    @Override
    public void run()
    {
    }

    @Override
    public boolean shouldRun()
    {
        return false;
    }

    /** Only tame pokemobs set to "stay" should run this AI.
     * 
     * @return */
    private boolean tameCheck()
    {
        IPokemob pokemob = (IPokemob) entity;
        return pokemob.getPokemonAIState(IMoveConstants.TAMED) && !pokemob.getPokemonAIState(IMoveConstants.STAYING);
    }
}
