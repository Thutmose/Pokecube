package pokecube.core.ai.thread.aiRunnables;

import com.google.common.base.Predicate;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.InvWrapper;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Nature;
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
                                                                return tile != null && tile.getCapability(
                                                                        CapabilityItemHandler.ITEM_HANDLER_CAPABILITY,
                                                                        EnumFacing.UP) instanceof IItemHandlerModifiable;
                                                            }
                                                            return false;
                                                        }
                                                    };

    final EntityLiving      entity;
    final IPokemob          pokemob;
    Vector3                 inventoryLocation       = null;
    int                     searchInventoryCooldown = 0;
    int                     doStorageCooldown       = 0;

    public AIStoreStuff(IPokemob entity)
    {
        this.entity = entity.getEntity();
        this.pokemob = entity;
    }

    private Vector3 checkDir(World world, EnumFacing dir, BlockPos centre)
    {
        if (centre == null) return null;
        TileEntity tile = null;
        if (dir == null)
        {
            tile = world.getTileEntity(centre);
        }
        else
        {
            centre = centre.offset(dir);
            tile = world.getTileEntity(centre);
        }
        if (tile == null) return null;
        if (tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY,
                EnumFacing.UP) instanceof IItemHandlerModifiable)
            return Vector3.getNewVector().set(centre);
        return null;
    }

    @Override
    public void doMainThreadTick(World world)
    {
        super.doMainThreadTick(world);
        if (tameCheck()) return;
        if (searchInventoryCooldown-- < 0)
        {
            searchInventoryCooldown = COOLDOWN;
            inventoryLocation = checkDir(world, null, pokemob.getHome());
            if (inventoryLocation == null) for (EnumFacing dir : EnumFacing.HORIZONTALS)
            {
                inventoryLocation = checkDir(world, dir, pokemob.getHome());
                if (inventoryLocation != null)
                {
                    break;
                }
            }
            if (inventoryLocation == null) searchInventoryCooldown = 50 * COOLDOWN;
        }
        if (inventoryLocation == null || entity.getDistanceSq(pokemob.getHome()) > 16) return;
        IItemHandlerModifiable itemhandler = new InvWrapper(pokemob.getPokemobInventory());
        ItemStack stack;
        ItemStack stack1;
        boolean hasBerry = CompatWrapper.isValid(stack = stack1 = itemhandler.getStackInSlot(2))
                && stack.getItem() instanceof ItemBerry;
        boolean freeSlot = false;
        for (int i = 3; i < itemhandler.getSlots() && !freeSlot; i++)
        {
            freeSlot = !CompatWrapper.isValid(stack = itemhandler.getStackInSlot(i));
        }
        boolean cooldown = doStorageCooldown-- > 0;
        boolean needs = freeSlot && hasBerry;
        if (needs || cooldown) return;
        this.world = world;
        if (!hasBerry)
        {
            IItemHandlerModifiable inv = getBerryInventory();
            if (inv != null) for (int i = 0; i < inv.getSlots(); i++)
            {
                stack = inv.getStackInSlot(i);
                // If it wants a berry, search for a berry item, and take that.
                if (CompatWrapper.isValid(stack) && !hasBerry)
                {
                    if (stack.getItem() instanceof ItemBerry)
                    {
                        inv.setStackInSlot(i, stack1);
                        itemhandler.setStackInSlot(2, stack);
                        hasBerry = true;
                        break;
                    }
                }
            }
        }
        if (!freeSlot)
        {
            IItemHandlerModifiable inv = getStorageInventory();
            if (inv != null)
            {
                // First sort your inventory such that if you have multiple
                // berries, the tastiest one is first
                if (hasBerry)
                {
                    int weight = Integer.MIN_VALUE;
                    int index = -1;
                    Nature nature = pokemob.getNature();
                    for (int i = 2; i < itemhandler.getSlots(); i++)
                    {
                        stack = itemhandler.getStackInSlot(i);
                        // If it wants a berry, search for a berry item, and
                        // take that.
                        if (CompatWrapper.isValid(stack))
                        {
                            if (stack.getItem() instanceof ItemBerry)
                            {
                                int testweight = Nature.getBerryWeight(stack.getItemDamage(), nature);
                                if (testweight > weight)
                                {
                                    weight = testweight;
                                    index = i;
                                }
                            }

                        }
                    }
                    // Swap favourite berry stack to first item.
                    if (index != -1)
                    {
                        ItemStack stack2 = itemhandler.getStackInSlot(index);
                        stack = itemhandler.getStackInSlot(2);
                        itemhandler.setStackInSlot(2, stack2);
                        itemhandler.setStackInSlot(index, stack);
                    }
                }

                for (int i = 3; i < itemhandler.getSlots(); i++)
                {
                    stack = itemhandler.getStackInSlot(i);
                    // If it has full inventory, deposit all but the berry
                    // stack.
                    if (ItemStackTools.addItemStackToInventory(itemhandler.getStackInSlot(i), inv, 0))
                    {
                        itemhandler.setStackInSlot(i, CompatWrapper.nullStack);
                        freeSlot = true;
                    }
                }
            }
        }
        doStorageCooldown = COOLDOWN;
    }

    private IItemHandlerModifiable getBerryInventory()
    {
        IItemHandlerModifiable inv = getForSide(null);
        if (inv != null)
        {
            int size = Math.min(MAXSIZE, inv.getSlots());
            for (int i = 0; i < size; i++)
            {
                ItemStack stack = inv.getStackInSlot(i);
                if (CompatWrapper.isValid(stack) && stack.getItem() instanceof ItemBerry) return inv;
            }
        }
        for (EnumFacing side : EnumFacing.HORIZONTALS)
        {
            inv = getForSide(side);
            if (inv != null)
            {
                int size = Math.min(MAXSIZE, inv.getSlots());
                for (int i = 0; i < size; i++)
                {
                    ItemStack stack = inv.getStackInSlot(i);
                    if (CompatWrapper.isValid(stack) && stack.getItem() instanceof ItemBerry) return inv;
                }
            }
        }
        inv = getForSide(EnumFacing.UP);
        if (inv != null)
        {
            int size = Math.min(MAXSIZE, inv.getSlots());
            for (int i = 0; i < size; i++)
            {
                ItemStack stack = inv.getStackInSlot(i);
                if (CompatWrapper.isValid(stack) && stack.getItem() instanceof ItemBerry) return inv;
            }
        }
        inv = getForSide(EnumFacing.DOWN);
        if (inv != null)
        {
            int size = Math.min(MAXSIZE, inv.getSlots());
            for (int i = 0; i < size; i++)
            {
                ItemStack stack = inv.getStackInSlot(i);
                if (CompatWrapper.isValid(stack) && stack.getItem() instanceof ItemBerry) return inv;
            }
        }
        return null;
    }

    private IItemHandlerModifiable getStorageInventory()
    {
        IItemHandlerModifiable inv = getForSide(null);
        if (inv != null && ItemStackTools.getFirstEmptyStack(inv, 0) >= 0) return inv;
        for (EnumFacing side : EnumFacing.HORIZONTALS)
        {
            inv = getForSide(side);
            if (inv != null)
            {
                if (ItemStackTools.getFirstEmptyStack(inv, 0) >= 0) return inv;
            }
        }
        inv = getForSide(EnumFacing.UP);
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

    private IItemHandlerModifiable getForSide(EnumFacing side)
    {
        TileEntity tile = side != null ? inventoryLocation.getTileEntity(world, side)
                : inventoryLocation.getTileEntity(world);
        if (tile != null && tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY,
                EnumFacing.UP) instanceof IItemHandlerModifiable)
            return (IItemHandlerModifiable) tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY,
                    EnumFacing.UP);
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
        return pokemob.getHome() == null || (pokemob.getPokemonAIState(IMoveConstants.TAMED)
                && !pokemob.getPokemonAIState(IMoveConstants.STAYING));
    }
}
