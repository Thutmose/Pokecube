package pokecube.core.ai.thread.aiRunnables;

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
import pokecube.core.ai.utils.StorableAI;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.items.berries.ItemBerry;
import thut.api.TickHandler;
import thut.api.maths.Vector3;

public class AIStoreStuff extends AIBase implements StorableAI
{
    public static int  COOLDOWN  = 2000;
    final EntityLiving entity;
    final boolean[]    states    = { false, false };
    final int[]        cooldowns = { 0, 0 };
    Vector3            seeking   = Vector3.getNewVectorFromPool();

    public AIStoreStuff(EntityLiving entity)
    {
        this.entity = entity;
    }

    @Override
    public boolean shouldRun()
    {
        world = TickHandler.getInstance().getWorldCache(entity.dimension);

        if (world == null || entity.ticksExisted % 10 > 0 || tameCheck()) return false;
        IPokemob pokemob = (IPokemob) entity;

        if (pokemob.getHome() == null) return false;

        IInventory inventory = pokemob.getPokemobInventory();
        ItemStack stack;
        states[0] = (stack = inventory.getStackInSlot(2)) != null && stack.getItem() instanceof ItemBerry;

        if (!states[0] && cooldowns[0] < 0) return true;

        for (int i = 3; i < inventory.getSizeInventory() && !states[1]; i++)
        {
            states[1] = (stack = inventory.getStackInSlot(i)) == null;
        }
        return !states[1] && cooldowns[1] < 0;
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
        IPokemob pokemob = (IPokemob) entity;
        Vector3 temp = Vector3.getNewVectorFromPool();
        temp.set(pokemob.getHome()).offsetBy(EnumFacing.UP);

        temp.set(temp.findClosestVisibleObject(world, true, 10, IInventory.class));
        seeking.set(temp);
        // If too far away, path to the nest for items.
        if (!temp.isEmpty() && temp.distToEntity(entity) > 3)
        {
            PathEntity path = this.entity.getNavigator().getPathToPos(temp.getPos());
            addEntityPath(entity.getEntityId(), entity.dimension, path, entity.getAIMoveSpeed());
            temp.freeVectorFromPool();
            return;
        }
        temp.freeVectorFromPool();
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
        IPokemob pokemob = (IPokemob) entity;
        IInventory inventory = pokemob.getPokemobInventory();
        Vector3 temp = seeking;
        cooldowns[0]--;
        cooldowns[1]--;

        // If too far away, path to the nest for items, that is done on other
        // thread.
        if (cooldowns[0] > 0 || cooldowns[1] > 0 || temp.distToEntity(entity) > 3) { return; }

        TileEntity tile = temp.getTileEntity(world);
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
    }

    @Override
    public void writeToNBT(NBTTagCompound data)
    {
        // TODO Auto-generated method stub

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
                // crashreportcategory.addCrashSectionCallable("Item name", new
                // Callable<String>()
                // {
                // public String call() throws Exception
                // {
                // return itemStackIn.getDisplayName();
                // }
                // });
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

    public static AIStoreStuff createFromNBT(EntityLiving living, NBTTagCompound data)
    {
        AIStoreStuff ai = new AIStoreStuff(living);
        int priority = 4;

        ai.setPriority(priority);
        return ai;
    }
}
