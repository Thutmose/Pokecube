package pokecube.core.ai.thread.aiRunnables;

import com.google.common.base.Predicate;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.items.berries.ItemBerry;
import thut.api.maths.Vector3;
import thut.lib.CompatWrapper;
import thut.lib.ItemStackTools;

/** This IAIRunnable will result in the mob occasionally emptying its inventory
 * into an inventory near its home location. This, along with AIGatherStuff
 * allows using pokemobs for automatic harvesting and storage of berries and
 * dropped items. */
public class AIStoreStuff extends AIBase
{
    public static int       COOLDOWN                = 50;
    public static int       MAXSIZE                 = 100;

    final Predicate<Object> matcher                 = new Predicate<Object>()
                                                    {
                                                        @Override
                                                        public boolean apply(Object t)
                                                        {
                                                            if (!(t instanceof IBlockState)) return false;
                                                            IBlockState state = (IBlockState) t;
                                                            if (state.getBlock() instanceof ITileEntityProvider)
                                                            {
                                                                TileEntity tile = ((ITileEntityProvider) state
                                                                        .getBlock()).createNewTileEntity(null,
                                                                                state.getBlock()
                                                                                        .getMetaFromState(state));
                                                                return tile instanceof IInventory;
                                                            }
                                                            return false;
                                                        }
                                                    };

    final EntityLiving      entity;
    Vector3                 inventoryLocation       = null;
    int                     searchInventoryCooldown = 0;
    int                     doStorageCooldown       = 0;

    public AIStoreStuff(EntityLiving entity)
    {
        this.entity = entity;
    }

    private Vector3 checkDir(EnumFacing dir, BlockPos centre)
    {
        if (dir == null)
        {
            if (world.getTileEntity(centre) instanceof IInventory) return Vector3.getNewVector().set(centre);
            else return null;
        }
        else
        {
            centre = centre.offset(dir);
            if (world.getTileEntity(centre) instanceof IInventory) return Vector3.getNewVector().set(centre);
            else return null;
        }
    }

    @Override
    public void doMainThreadTick(World world)
    {
        super.doMainThreadTick(world);
        IPokemob pokemob = (IPokemob) entity;
        if (tameCheck()) return;
        IInventory inventory = pokemob.getPokemobInventory();
        if (searchInventoryCooldown-- < 0)
        {
            searchInventoryCooldown = COOLDOWN;
            inventoryLocation = checkDir(null, pokemob.getHome());
            if (inventoryLocation != null) for (EnumFacing dir : EnumFacing.HORIZONTALS)
            {
                inventoryLocation = checkDir(dir, pokemob.getHome());
                if (inventoryLocation != null)
                {
                    break;
                }
            }
            if (inventoryLocation == null) searchInventoryCooldown = 50 * COOLDOWN;
        }
        if (inventoryLocation == null || entity.getDistanceSq(pokemob.getHome()) > 16) return;
        ItemStack stack;
        ItemStack stack1;
        boolean hasBerry = CompatWrapper.isValid(stack = stack1 = inventory.getStackInSlot(2))
                && stack.getItem() instanceof ItemBerry;
        boolean freeSlot = false;
        for (int i = 3; i < inventory.getSizeInventory() && !freeSlot; i++)
        {
            freeSlot = !CompatWrapper.isValid(stack = inventory.getStackInSlot(i));
        }
        boolean cooldown = doStorageCooldown-- > 0;
        boolean needs = freeSlot && hasBerry;
        if (needs || cooldown) return;
        this.world = world;
        if (!hasBerry)
        {
            IInventory inv = getBerryInventory();
            if (inv != null) for (int i = 0; i < inv.getSizeInventory(); i++)
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
            }
        }
        if (!freeSlot)
        {
            IInventory inv = getStorageInventory();
            if (inv != null) for (int i = 3; i < inventory.getSizeInventory(); i++)
            {
                stack = inventory.getStackInSlot(i);
                // If it has full inventory, deposit all but the berry
                // stack.
                if (ItemStackTools.addItemStackToInventory(inventory.getStackInSlot(i), inv, 0))
                {
                    inventory.setInventorySlotContents(i, CompatWrapper.nullStack);
                    freeSlot = true;
                }
            }
        }
        doStorageCooldown = COOLDOWN;
    }

    private IInventory getBerryInventory()
    {
        TileEntity tile = inventoryLocation.getTileEntity(world);
        if (tile instanceof IInventory)
        {
            IInventory inv = (IInventory) tile;
            int size = Math.min(MAXSIZE, inv.getSizeInventory());
            for (int i = 0; i < size; i++)
            {
                ItemStack stack = inv.getStackInSlot(i);
                if (CompatWrapper.isValid(stack) && stack.getItem() instanceof ItemBerry) return inv;
            }
        }
        for (EnumFacing side : EnumFacing.HORIZONTALS)
        {
            IInventory inv = getForSide(side);
            if (inv != null)
            {
                int size = Math.min(MAXSIZE, inv.getSizeInventory());
                for (int i = 0; i < size; i++)
                {
                    ItemStack stack = inv.getStackInSlot(i);
                    if (CompatWrapper.isValid(stack) && stack.getItem() instanceof ItemBerry) return inv;
                }
            }
        }
        IInventory inv = getForSide(EnumFacing.UP);
        if (inv != null)
        {
            int size = Math.min(MAXSIZE, inv.getSizeInventory());
            for (int i = 0; i < size; i++)
            {
                ItemStack stack = inv.getStackInSlot(i);
                if (CompatWrapper.isValid(stack) && stack.getItem() instanceof ItemBerry) return inv;
            }
        }
        inv = getForSide(EnumFacing.DOWN);
        if (inv != null)
        {
            int size = Math.min(MAXSIZE, inv.getSizeInventory());
            for (int i = 0; i < size; i++)
            {
                ItemStack stack = inv.getStackInSlot(i);
                if (CompatWrapper.isValid(stack) && stack.getItem() instanceof ItemBerry) return inv;
            }
        }
        return null;
    }

    private IInventory getStorageInventory()
    {
        TileEntity tile = inventoryLocation.getTileEntity(world);
        if (tile instanceof IInventory)
        {
            IInventory inv = (IInventory) tile;
            if (ItemStackTools.getFirstEmptyStack(inv, 0) >= 0) return inv;
        }
        for (EnumFacing side : EnumFacing.HORIZONTALS)
        {
            IInventory inv = getForSide(side);
            if (inv != null)
            {
                if (ItemStackTools.getFirstEmptyStack(inv, 0) >= 0) return inv;
            }
        }
        IInventory inv = getForSide(EnumFacing.UP);
        if (inv != null)
        {
            if (ItemStackTools.getFirstEmptyStack(inv, 0) >= 0) return inv;
        }
        inv = getForSide(EnumFacing.DOWN);
        if (inv != null)
        {
            if (ItemStackTools.getFirstEmptyStack(inv, 0) >= 0) return inv;
        }
        return null;
    }

    private IInventory getForSide(EnumFacing side)
    {
        TileEntity tile = inventoryLocation.getTileEntity(world, side);
        if (tile instanceof IInventory) return (IInventory) tile;
        return null;
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
        return pokemob.getHome() == null || (pokemob.getPokemonAIState(IMoveConstants.TAMED)
                && !pokemob.getPokemonAIState(IMoveConstants.STAYING));
    }
}
