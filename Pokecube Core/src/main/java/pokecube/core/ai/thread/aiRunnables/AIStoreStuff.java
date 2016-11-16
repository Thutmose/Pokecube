package pokecube.core.ai.thread.aiRunnables;

import com.google.common.base.Predicate;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.items.berries.ItemBerry;
import thut.api.maths.Vector3;
import thut.lib.ItemStackTools;

/** This IAIRunnable will result in the mob occasionally emptying its inventory
 * into an inventory near its home location. This, along with AIGatherStuff
 * allows using pokemobs for automatic harvesting and storage of berries and
 * dropped items. */
public class AIStoreStuff extends AIBase
{
    public static int COOLDOWN = 500;

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
                    if (ItemStackTools.addItemStackToInventory(inventory.getStackInSlot(index), inv, 0))
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
