package pokecube.core.ai.thread.aiRunnables;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.InvWrapper;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IMoveConstants.AIRoutine;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.items.berries.ItemBerry;
import thut.lib.CompatWrapper;
import thut.lib.ItemStackTools;

/** This IAIRunnable will result in the mob occasionally emptying its inventory
 * into an inventory near its home location. This, along with AIGatherStuff
 * allows using pokemobs for automatic harvesting and storage of berries and
 * dropped items. */
public class AIStoreStuff extends AIBase implements INBTSerializable<NBTTagCompound>
{
    public static int  COOLDOWN                = 10;
    public static int  MAXSIZE                 = 100;

    final EntityLiving entity;
    final IPokemob     pokemob;
    // Inventory to store stuff in.
    public BlockPos    storageLoc              = null;
    // Inventory to check for berries
    public BlockPos    berryLoc                = null;
    // Inventory to pull stuff out of
    public BlockPos    emptyInventory          = null;
    // Side to store stuff in.
    public EnumFacing  storageFace             = EnumFacing.UP;
    // Side to emtpy things from.
    public EnumFacing  emptyFace               = EnumFacing.UP;
    int                searchInventoryCooldown = 0;
    int                doStorageCooldown       = 0;

    public AIStoreStuff(IPokemob entity)
    {
        this.entity = entity.getEntity();
        this.pokemob = entity;
    }

    private BlockPos checkDir(IBlockAccess world, EnumFacing dir, BlockPos centre, EnumFacing side)
    {
        if (centre == null) return null;
        if (dir != null)
        {
            centre = centre.offset(dir);
        }
        if (getInventory(world, centre, side) != null) return centre;
        return null;
    }

    private IItemHandlerModifiable getInventory(IBlockAccess world, BlockPos pos, EnumFacing side)
    {
        if (pos == null) return null;
        TileEntity tile = world.getTileEntity(pos);
        if (tile == null) return null;
        if (tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side) instanceof IItemHandlerModifiable)
            return (IItemHandlerModifiable) tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side);
        return null;
    }

    private boolean hasItem(Class<? extends Item> tocheck, IItemHandlerModifiable inventory)
    {
        for (int i = 0; i < (Math.min(inventory.getSlots(), MAXSIZE)); i++)
        {
            ItemStack stack = inventory.getStackInSlot(i);
            if (CompatWrapper.isValid(stack) && tocheck.isInstance(stack.getItem())) return true;
        }
        return false;
    }

    private boolean findEmptyStorage()
    {
        return emptyInventory != null && emptyInventory.distanceSq(pokemob.getHome()) < 256;
    }

    private boolean findBerryStorage()
    {
        if (berryLoc != null && pokemob.getPokemonAIState(IMoveConstants.TAMED)) { return true; }
        berryLoc = checkDir(world, null, berryLoc, null);
        if (berryLoc != null) { return true; }
        BlockPos home = pokemob.getHome();
        storageLoc = checkDir(world, null, home, null);
        for (EnumFacing dir : EnumFacing.HORIZONTALS)
        {
            berryLoc = checkDir(world, dir, home, null);
            if (berryLoc != null) { return true; }
        }
        berryLoc = checkDir(world, EnumFacing.DOWN, home, null);
        if (berryLoc != null) { return true; }
        berryLoc = checkDir(world, EnumFacing.UP, home, null);
        if (berryLoc != null) { return true; }
        return berryLoc != null;
    }

    private boolean findItemStorage()
    {
        if (storageLoc != null && pokemob.getPokemonAIState(IMoveConstants.TAMED)) { return true; }
        storageLoc = checkDir(world, null, storageLoc, storageFace);
        if (storageLoc != null) { return true; }
        BlockPos home = pokemob.getHome();
        storageLoc = checkDir(world, null, home, storageFace);
        for (EnumFacing dir : EnumFacing.HORIZONTALS)
        {
            storageLoc = checkDir(world, dir, home, storageFace);
            if (storageLoc != null) { return true; }
        }
        storageLoc = checkDir(world, EnumFacing.DOWN, home, storageFace);
        if (storageLoc != null) { return true; }
        storageLoc = checkDir(world, EnumFacing.UP, home, storageFace);
        if (storageLoc != null) { return true; }
        return storageLoc != null;
    }

    @Override
    public void doMainThreadTick(World world)
    {
        super.doMainThreadTick(world);
        if (tameCheck() || !shouldRun()) return;
        boolean stuff = false;
        this.world = world;
        if (searchInventoryCooldown-- < 0)
        {
            searchInventoryCooldown = COOLDOWN;
            findBerryStorage();
            stuff = findItemStorage();
            if (!stuff) searchInventoryCooldown = 50 * COOLDOWN;
        }
        if (!stuff || entity.getDistanceSq(pokemob.getHome()) > 256 || doStorageCooldown-- > 0) return;
        IItemHandlerModifiable itemhandler = new InvWrapper(pokemob.getPokemobInventory());
        if (doBerryCheck(itemhandler) || doStorageCheck(itemhandler) || doEmptyCheck(itemhandler))
        {
            doStorageCooldown = COOLDOWN;
        }
        else doStorageCooldown = COOLDOWN;// * 20;
    }

    private boolean doBerryCheck(IItemHandlerModifiable pokemobInv)
    {
        ItemStack stack;
        boolean hasBerry = CompatWrapper.isValid(stack = pokemobInv.getStackInSlot(2))
                && stack.getItem() instanceof ItemBerry;
        // Has a berry already, can pass through to storage check.
        if (hasBerry) return false;
        boolean freeSlot = false;
        int berrySlot = -1;
        int firstFreeSlot = -1;
        // Search inventory for free slots or berries.
        for (int i = 2; i < pokemobInv.getSlots(); i++)
        {
            boolean test = !CompatWrapper.isValid(stack = pokemobInv.getStackInSlot(i));
            if (test && firstFreeSlot == -1 && i != 2) firstFreeSlot = i;
            freeSlot = freeSlot || test;
            if (!hasBerry)
            {
                hasBerry = !test && stack.getItem() instanceof ItemBerry;
                if (hasBerry) berrySlot = i;
            }
        }
        // If you have a berry stack elsewhere, swap it into first slot in
        // inventory.
        if (berrySlot != -1)
        {
            ItemStack stack1 = pokemobInv.getStackInSlot(berrySlot);
            pokemobInv.setStackInSlot(berrySlot, pokemobInv.getStackInSlot(2));
            pokemobInv.setStackInSlot(2, stack1);
            // Retrun false to allow storage check.
            return false;
        }
        // No room to pick up a berry if it wanted to, so can pass through
        // to
        // storage check.
        if (!freeSlot || firstFreeSlot == -1) return false;
        // No Berry Storage, so move to normal storage checks.
        if (!findBerryStorage()) return false;
        IItemHandlerModifiable berries = getInventory(world, berryLoc, null);
        // No Storage at berryLoc.
        if (berries == null)
        {
            berryLoc = null;
            return false;
        }
        // No Berries in storage.
        if (!hasItem(ItemBerry.class, berries)) return false;
        if (pokemob.getEntity().getDistanceSq(berryLoc) > 9)
        {
            double speed = entity.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue();
            pokemob.getEntity().getNavigator().tryMoveToXYZ(berryLoc.getX() + 0.5, berryLoc.getY() + 0.5,
                    berryLoc.getZ() + 0.5, speed);
            // We should be pathing to berries, so return true to stop other
            // storage tasks.
            return true;
        }
        for (int i = 0; i < (Math.min(berries.getSlots(), MAXSIZE)); i++)
        {
            stack = berries.getStackInSlot(i);
            if (CompatWrapper.isValid(stack) && stack.getItem() instanceof ItemBerry)
            {
                berries.setStackInSlot(i, CompatWrapper.nullStack);
                pokemobInv.setStackInSlot(firstFreeSlot, pokemobInv.getStackInSlot(2));
                pokemobInv.setStackInSlot(2, stack);
                // Collected our berry, Can pass to storage now.
                return false;
            }
        }
        return false;
    }

    private boolean doStorageCheck(IItemHandlerModifiable pokemobInv)
    {
        boolean freeSlot = false;
        // Search inventory for free slots, ignore first slot, as berry storage
        // deals with it.
        for (int i = 3; i < pokemobInv.getSlots() && !freeSlot; i++)
        {
            freeSlot = !CompatWrapper.isValid(pokemobInv.getStackInSlot(i));
        }
        // Only dump inventory if no free slots.
        if (freeSlot) return false;
        // No ItemStorage
        if (!findItemStorage()) return false;
        // check if should path to storage.
        if (pokemob.getEntity().getDistanceSq(storageLoc) > 9)
        {
            double speed = entity.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue();
            pokemob.getEntity().getNavigator().tryMoveToXYZ(storageLoc.getX() + 0.5, storageLoc.getY() + 0.5,
                    storageLoc.getZ() + 0.5, speed);
            // We should be pathing to storage here, so return true.
            return true;
        }
        IItemHandlerModifiable storage = getInventory(world, storageLoc, storageFace);
        // if Somehow have no storage, should return false.
        if (storage == null) return false;
        // Store every item after berry slot
        for (int i = 3; i < pokemobInv.getSlots(); i++)
        {
            ItemStack stack = pokemobInv.getStackInSlot(i);
            if (ItemStackTools.addItemStackToInventory(stack, storage, 0))
            {
                if (!CompatWrapper.isValid(stack)) stack = CompatWrapper.nullStack;
                pokemobInv.setStackInSlot(i, stack);
            }
        }
        return false;
    }

    private boolean doEmptyCheck(IItemHandlerModifiable pokemobInv)
    {
        // Return true here to make the cooldown not 5x, this means we don't
        // have a setting for empty, so no need to run this AI.
        if (!findEmptyStorage()) return true;
        boolean freeSlot = false;
        // Search inventory for free slots, ignore first slot, as berry storage
        // deals with it.
        int firstFreeSlot = -1;
        for (int i = 3; i < pokemobInv.getSlots() && !freeSlot; i++)
        {
            freeSlot = !CompatWrapper.isValid(pokemobInv.getStackInSlot(i));
            if (freeSlot) firstFreeSlot = i;
        }
        // Can only pick up item if we have a free slot for it.
        if (!freeSlot || firstFreeSlot == -1) return false;
        // Path to the inventory.
        if (pokemob.getEntity().getDistanceSq(emptyInventory) > 9)
        {
            double speed = entity.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue();
            pokemob.getEntity().getNavigator().tryMoveToXYZ(emptyInventory.getX() + 0.5, emptyInventory.getY() + 0.5,
                    emptyInventory.getZ() + 0.5, speed);
            // We should be pathing, so return true.
            return true;
        }
        IItemHandlerModifiable inventory = getInventory(world, emptyInventory, emptyFace);
        // No inventory to empty
        if (inventory == null)
        {
            emptyInventory = null;
            return false;
        }
        // No items to empty
        if (!hasItem(Item.class, inventory)) return false;
        for (int i = 0; i < (Math.min(inventory.getSlots(), MAXSIZE)); i++)
        {
            ItemStack stack = inventory.getStackInSlot(i);
            if (CompatWrapper.isValid(stack))
            {
                inventory.setStackInSlot(i, CompatWrapper.nullStack);
                pokemobInv.setStackInSlot(firstFreeSlot, stack);
                // Collected our item successfully, return true so shorter
                // cooldown incase it needs storing.
                return true;
            }
        }
        return false;
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
        if (!pokemob.isRoutineEnabled(AIRoutine.STORE) || pokemob.getHome() == null) return false;
        return true;
    }

    /** Only tame pokemobs set to "stay" should run this AI.
     * 
     * @return */
    private boolean tameCheck()
    {
        return (pokemob.getPokemonAIState(IMoveConstants.TAMED) && !pokemob.getPokemonAIState(IMoveConstants.STAYING));
    }

    @Override
    public NBTTagCompound serializeNBT()
    {
        NBTTagCompound tag = new NBTTagCompound();
        NBTTagCompound berry = new NBTTagCompound();
        NBTTagCompound storage = new NBTTagCompound();
        NBTTagCompound empty = new NBTTagCompound();
        if (berryLoc != null)
        {
            berry.setInteger("x", berryLoc.getX());
            berry.setInteger("y", berryLoc.getY());
            berry.setInteger("z", berryLoc.getZ());
        }
        if (storageLoc != null)
        {
            storage.setInteger("x", storageLoc.getX());
            storage.setInteger("y", storageLoc.getY());
            storage.setInteger("z", storageLoc.getZ());
            storage.setByte("f", (byte) storageFace.ordinal());
        }
        if (emptyInventory != null)
        {
            empty.setInteger("x", emptyInventory.getX());
            empty.setInteger("y", emptyInventory.getY());
            empty.setInteger("z", emptyInventory.getZ());
            empty.setByte("f", (byte) emptyFace.ordinal());
        }
        tag.setTag("b", berry);
        tag.setTag("s", storage);
        tag.setTag("e", empty);
        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt)
    {
        NBTTagCompound berry = nbt.getCompoundTag("b");
        NBTTagCompound storage = nbt.getCompoundTag("s");
        NBTTagCompound empty = nbt.getCompoundTag("e");
        if (!berry.hasNoTags())
        {
            berryLoc = new BlockPos(berry.getInteger("x"), berry.getInteger("y"), berry.getInteger("z"));
        }
        if (!storage.hasNoTags())
        {
            storageLoc = new BlockPos(storage.getInteger("x"), storage.getInteger("y"), storage.getInteger("z"));
            storageFace = EnumFacing.values()[storage.getByte("f")];
        }
        if (!empty.hasNoTags())
        {
            emptyInventory = new BlockPos(empty.getInteger("x"), empty.getInteger("y"), empty.getInteger("z"));
            emptyFace = EnumFacing.values()[empty.getByte("f")];
        }
    }

    @Override
    public String getIdentifier()
    {
        return "store_stuff";
    }

    @Override
    public boolean sync()
    {
        return true;
    }
}
