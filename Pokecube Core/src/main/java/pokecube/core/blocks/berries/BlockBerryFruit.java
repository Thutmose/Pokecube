package pokecube.core.blocks.berries;

import java.util.Random;

import net.minecraft.block.BlockBush;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import pokecube.core.PokecubeItems;
import pokecube.core.blocks.berries.TileEntityBerries.Type;
import pokecube.core.interfaces.IBerryFruitBlock;
import pokecube.core.items.berries.BerryManager;

/** @author Oracion
 * @author Manchou */
public class BlockBerryFruit extends BlockBush implements IBerryFruitBlock, ITileEntityProvider
{
    public static int renderID;
    public int        berryIndex = 0;
    String            berryName  = "";

    public BlockBerryFruit()
    {
        super();
        this.setCreativeTab(null);
        this.setTickRandomly(true);
        float var3 = 0.4F;
        // this.setBlockBounds(0.5F - var3, 0F, 0.5F - var3, 0.5F + var3, 0.7F,
        // 0.5F + var3);
        this.setDefaultState(this.blockState.getBaseState().withProperty(BerryManager.type, "cheri"));
    }

    /** Can this block stay at this position. Similar to canPlaceBlockAt except
     * gets checked often with plants. */
    @Override
    public boolean canBlockStay(World worldIn, BlockPos pos, IBlockState state)
    {
        if (BlockBerryCrop.trees.contains(
                berryIndex)) { return worldIn.getBlockState(pos.up()).getBlock().isLeaves(state, worldIn, pos.up()); }
        return worldIn.getBlockState(pos.down()).getBlock() instanceof BlockBerryCrop;
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta)
    {
        return new TileEntityBerries(Type.FRUIT);
    }

    @Override
    /** Determines the damage on the item the block drops. Used in cloth and
     * wood. */
    public int damageDropped(IBlockState state)
    {
        return berryIndex;
    }

    /** Spawns EntityItem in the world for the given ItemStack if the world is
     * not remote. */
    protected void dropBlockAsItem(World p_149642_1_, int p_149642_2_, int p_149642_3_, int p_149642_4_,
            ItemStack p_149642_5_)
    {
        if (!p_149642_1_.isRemote && p_149642_1_.getGameRules().getBoolean("doTileDrops")
                && !p_149642_1_.restoringBlockSnapshots) // do not drop items
                                                         // while restoring
                                                         // blockstates,
                                                         // prevents item dupe
        {
            if (captureDrops.get())
            {
                capturedDrops.get().add(p_149642_5_);
                return;
            }
            float f = 0.7F;
            double d0 = p_149642_1_.rand.nextFloat() * f + (1.0F - f) * 0.5D;
            double d1 = p_149642_1_.rand.nextFloat() * f + (1.0F - f) * 0.5D;
            double d2 = p_149642_1_.rand.nextFloat() * f + (1.0F - f) * 0.5D;
            EntityItem entityitem = new EntityItem(p_149642_1_, p_149642_2_ + d0, p_149642_3_ + d1, p_149642_4_ + d2,
                    p_149642_5_);
            p_149642_1_.spawnEntityInWorld(entityitem);
        }
    }

    public String getBerryName()
    {
        return berryName;
    }

    @Override
    public ItemStack getBerryStack(IBlockAccess world, int x, int y, int z)
    {
        TileEntityBerries tile = (TileEntityBerries) world.getTileEntity(new BlockPos(x, y, z));
        return BerryManager.getBerryItem(BerryManager.berryNames.get(tile.getBerryId()));

        // return BerryManager.getBerryItem(berryName);
    }

    /** Returns the ID of the items to drop on destruction. */
    @Override
    public Item getItemDropped(IBlockState state, Random p_149650_2_, int p_149650_3_)
    {
        return PokecubeItems.berries;
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

    /** Returns the quantity of items to drop on block destruction. */
    @Override
    public int quantityDropped(Random par1Random)
    {
        int i = par1Random.nextInt(2) + 1;
        return i;
    }

    public void setBerry(String berryName)
    {
        this.berryName = berryName;
    }

    public void setBerryIndex(int berryId)
    {
        this.berryIndex = berryId;
    }

    // @Override
    // public void setBlockBoundsBasedOnState(IBlockAccess worldIn, BlockPos
    // pos)
    // {
    // if (BlockBerryCrop.trees.contains(berryIndex))
    // {
    // float f = 0.15F;
    // this.setBlockBounds(0.5F - f, 1 - f * 3.0F, 0.5F - f, 0.5F + f, 1, 0.5F +
    // f);
    // }
    // else
    // {
    // super.setBlockBoundsBasedOnState(worldIn, pos);
    // }
    // }

    /** Convert the BlockState into the correct metadata value */
    public int getMetaFromState(IBlockState state)
    {
        return 0;
    }

    @Override
    /** Get the actual Block state of this Block at the given position. This
     * applies properties not visible in the metadata, such as fence
     * connections. */
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos)
    {
        TileEntityBerries tile = (TileEntityBerries) worldIn.getTileEntity(pos);
        return state.withProperty(BerryManager.type, BerryManager.berryNames.get(tile.getBerryId()));
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, new IProperty[] { BerryManager.type });
    }
}
