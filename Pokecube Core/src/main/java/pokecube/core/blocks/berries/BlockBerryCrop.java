package pokecube.core.blocks.berries;

import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import pokecube.core.blocks.berries.TileEntityBerries.Type;
import pokecube.core.items.berries.BerryManager;

/** @author Oracion
 * @author Manchou */
public class BlockBerryCrop extends BlockCrops implements ITileEntityProvider
{
    protected static final AxisAlignedBB BUSH_AABB = new AxisAlignedBB(0.3D, 0.0D, 0.3D, 0.7D, 1D, 0.7D);

    public BlockBerryCrop()
    {
        super();
        this.setTickRandomly(true);
        disableStats();
        this.setDefaultState(this.blockState.getBaseState().withProperty(AGE, Integer.valueOf(0))
                .withProperty(BerryManager.type, "null"));
    }

    /** Gets passed in the blockID of the block below and supposed to return
     * true if its allowed to grow on the type of blockID passed in. Args:
     * blockID */
    protected boolean canThisPlantGrowOnThisBlockID(Block par1)
    {
        return par1 == Blocks.FARMLAND;
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, new IProperty[] { AGE, BerryManager.type });
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta)
    {
        return new TileEntityTickBerries(Type.CROP);
    }

    @Override
    /** Get the actual Block state of this Block at the given position. This
     * applies properties not visible in the metadata, such as fence
     * connections. */
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos)
    {
        TileEntityBerries tile = (TileEntityBerries) worldIn.getTileEntity(pos);
        String name = BerryManager.berryNames.get(tile.getBerryId());
        if (name == null) name = "null";
        int age = state.getValue(AGE);
        if (worldIn.getBlockState(pos.up()).getBlock() == BerryManager.berryFruit) age = 7;
        return state.withProperty(BerryManager.type, name).withProperty(AGE, age);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos)
    {
        return BUSH_AABB;
    }

    @Override
    /** This returns a complete list of items dropped from this block.
     *
     * @param world
     *            The current world
     * @param pos
     *            Block position in world
     * @param state
     *            Current state
     * @param fortune
     *            Breakers fortune level
     * @return A ArrayList containing all items this block drops */
    public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune)
    {
        List<ItemStack> ret = new java.util.ArrayList<ItemStack>();

        Random rand = world instanceof World ? ((World) world).rand : RANDOM;

        int count = quantityDropped(state, fortune, rand);
        TileEntityBerries tile = (TileEntityBerries) world.getTileEntity(pos);
        if (tile != null) for (int i = 0; i < count; i++)
        {
            ItemStack stack = BerryManager.getBerryItem(BerryManager.berryNames.get(tile.getBerryId()));
            if (stack != null)
            {
                ret.add(stack);
            }
        }
        return ret;
    }

    @Override
    /** Called when a user uses the creative pick block button on this block
     *
     * @param target
     *            The full target the player is looking at
     * @return A ItemStack to add to the player's inventory, Null if nothing
     *         should be added. */
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos,
            EntityPlayer player)
    {
        TileEntityBerries tile = (TileEntityBerries) world.getTileEntity(pos);
        return BerryManager.getBerryItem(tile.getBerryId());
    }

    @Override
    public void grow(World worldIn, BlockPos pos, IBlockState state)
    {
        TileEntityBerries tile = (TileEntityBerries) worldIn.getTileEntity(pos);
        tile.growCrop();
    }

    /** Returns the quantity of items to drop on block destruction. */
    @Override
    public int quantityDropped(Random par1Random)
    {
        return 1;
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand)
    {
    }
}
