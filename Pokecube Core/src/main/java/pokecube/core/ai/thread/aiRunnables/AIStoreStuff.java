package pokecube.core.ai.thread.aiRunnables;

import java.util.function.Predicate;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.EntityLiving;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ReportedException;
import net.minecraft.world.World;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.items.berries.ItemBerry;
import thut.api.TickHandler;
import thut.api.maths.Vector3;

public class AIStoreStuff extends AIBase
{
    public static int  COOLDOWN  = 500;
    final EntityLiving entity;
    final boolean[]    states    = { false, false };
    final boolean[]    run       = { true };
    final int[]        cooldowns = { 0, 0 };
    Vector3            seeking   = Vector3.getNewVector();

    public AIStoreStuff(EntityLiving entity)
    {
        this.entity = entity;
        this.setMutex(2);
    }

    @Override
    public boolean shouldRun()
    {
        world = TickHandler.getInstance().getWorldCache(entity.dimension);

        if (world == null || entity.ticksExisted % 10 > 0 || tameCheck() || cooldowns[0] > 0 || cooldowns[1] > 0)
            return false;
        IPokemob pokemob = (IPokemob) entity;

        if (pokemob.getHome() == null) return false;

        IInventory inventory = pokemob.getPokemobInventory();
        ItemStack stack;
        states[0] = (stack = inventory.getStackInSlot(2)) != null && stack.getItem() instanceof ItemBerry;

        if (!states[0] && cooldowns[0] <= 0) return true;

        for (int i = 3; i < inventory.getSizeInventory() && !states[1]; i++)
        {
            states[1] = (stack = inventory.getStackInSlot(i)) == null;
        }
        return !states[1] && cooldowns[1] <= 0;
    }

    /** Only tame pokemobs set to "stay" should run this AI.
     * 
     * @return */
    private boolean tameCheck()
    {
        IPokemob pokemob = (IPokemob) entity;
        return pokemob.getPokemonAIState(IPokemob.TAMED) && !pokemob.getPokemonAIState(IPokemob.STAYING);
    }

    @Override
    public void run()
    {
        synchronized (seeking)
        {
            seeking.clear();
            run[0] = true;
        }
    }

    @Override
    public void reset()
    {
        states[0] = states[1] = false;
    }

    @Override
    public void doMainThreadTick(World world)
    {
        super.doMainThreadTick(world);
        world = entity.worldObj;
        IPokemob pokemob = (IPokemob) entity;
        IInventory inventory = pokemob.getPokemobInventory();
        if (run[0])
        {
            run[0] = false;
            Vector3 temp = Vector3.getNewVector();
            temp.set(pokemob.getHome()).offsetBy(EnumFacing.UP);

            Predicate<Object> matcher = new Predicate<Object>()
            {
                @Override
                public boolean test(Object t)
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
            temp = temp.findClosestVisibleObject(world, true, 5, matcher);
            if (temp != null) seeking.set(temp);
            else seeking.clear();
            boolean empty = seeking.intX() == 0 && seeking.intZ() == 0 && (seeking.intY() == 0 || seeking.intY() == 1);
            // If too far away, path to the nest for items.
            if (!empty && seeking.distToEntity(entity) > 3)
            {
                PathEntity path = this.entity.getNavigator().getPathToPos(seeking.getPos());
                addEntityPath(entity.getEntityId(), entity.dimension, path, entity.getAIMoveSpeed());
                return;
            }
        }

        if (seeking.isEmpty()) return;

        cooldowns[0]--;
        cooldowns[1]--;

        TileEntity tile = seeking.getTileEntity(world);
        // If too far away, path to the nest for items, that is done on other
        // thread. here is just returns if too far, or on cooldown
        if (cooldowns[0] > 0 || cooldowns[1] > 0 || seeking.distToEntity(entity) > 3) { return; }

        if (tile != null)
        {
            ItemStack stack;
            ItemStack stack1;
            states[0] = (stack = stack1 = inventory.getStackInSlot(2)) != null && stack.getItem() instanceof ItemBerry;

            for (int i = 3; i < inventory.getSizeInventory() && !states[1]; i++)
            {
                states[1] = (stack = inventory.getStackInSlot(i)) == null;
            }
            int index = inventory.getSizeInventory() - 1;
            IInventory inv = (IInventory) tile;
            if (!states[0]) for (int i = 0; i < inv.getSizeInventory(); i++)
            {
                stack = inv.getStackInSlot(i);
                // If it wants a berry, search for a berry item, and take that.
                if (stack != null && !states[0])
                {
                    if (stack.getItem() instanceof ItemBerry)
                    {
                        inv.setInventorySlotContents(i, stack1);
                        inventory.setInventorySlotContents(2, stack);
                        states[0] = true;
                        break;
                    }
                }
                if (!states[0]) cooldowns[0] = COOLDOWN;
            }
            if (!states[1])
            {
                for (int i = 0; i < inv.getSizeInventory(); i++)
                {
                    stack = inv.getStackInSlot(i);
                    // If it has full inventory, deposit all but the berry
                    // stack.
                    if (addItemStackToInventory(inventory.getStackInSlot(index), inv, 0))
                    {
                        inventory.setInventorySlotContents(index, null);
                        index--;
                        if (index <= 2) break;
                    }
                }
                if (index <= 2) states[1] = true;
                if (!states[1]) cooldowns[1] = COOLDOWN;
            }
        }
        else
        {
            // Longer cooldown if there is no inventory found at all.
            cooldowns[1] = cooldowns[0] = 50 * COOLDOWN;
        }
    }

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
                    else
                    {
                        return false;
                    }
                }
                else
                {
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
        else
        {
            return false;
        }
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
        else
        {
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
            else
            {
                i = i - k;
                inventory.getStackInSlot(j).stackSize += k;
                inventory.getStackInSlot(j).animationsToGo = 5;
                return i;
            }
        }
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
}
