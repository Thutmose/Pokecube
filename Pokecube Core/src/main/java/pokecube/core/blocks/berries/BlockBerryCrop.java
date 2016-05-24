package pokecube.core.blocks.berries;

import java.util.ArrayList;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.event.terraingen.TerrainGen;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.blocks.berries.TileEntityBerries.Type;
import pokecube.core.items.berries.BerryManager;
import pokecube.core.world.gen.WorldGenBerries;

/** @author Oracion
 * @author Manchou */
public class BlockBerryCrop extends BlockCrops implements ITileEntityProvider
{

    static ArrayList<Integer>                 trees  = new ArrayList<Integer>();
    public static ArrayList<BlockBerryLeaves> leaves = new ArrayList<BlockBerryLeaves>();

    public static ArrayList<BlockBerryLog>    logs   = new ArrayList<BlockBerryLog>();
    static
    {
        trees.add(3);
        trees.add(6);
        trees.add(7);
        trees.add(10);
        trees.add(18);
        trees.add(60);
    }
    int    berryIndex = 0;

    String berryName  = "";

    public BlockBerryCrop()
    {
        super();
        this.setTickRandomly(true);
        disableStats();
        float var3 = 0.3F;
        this.setBlockBounds(0.5F - var3, -0.05F, 0.5F - var3, 0.5F + var3, 1F, 0.5F + var3);
        this.setDefaultState(this.blockState.getBaseState().withProperty(AGE, Integer.valueOf(0)).withProperty(BerryManager.type, "cheri"));
    }

    /** Gets passed in the blockID of the block below and supposed to return
     * true if its allowed to grow on the type of blockID passed in. Args:
     * blockID */
    protected boolean canThisPlantGrowOnThisBlockID(Block par1)
    {
        return par1 == Blocks.farmland;
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta)
    {
        return new TileEntityBerries(Type.CROP);
    }

    @Override
    /** Get the damage value that this Block should drop */
    public int damageDropped(IBlockState state)
    {//TODO find location senstive version of this.
        return berryIndex;
    }

    public String getBerryName()
    {
        return berryName;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Item getItem(World worldIn, BlockPos pos)
    {
        return BerryManager.getBerryItem(berryName).getItem();
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune)
    {
        return BerryManager.getBerryItem(berryName).getItem();
    }

    @Override
    /** Called when a user uses the creative pick block button on this block
     *
     * @param target
     *            The full target the player is looking at
     * @return A ItemStack to add to the player's inventory, Null if nothing
     *         should be added. */
    public ItemStack getPickBlock(MovingObjectPosition target, World world, BlockPos pos, EntityPlayer player)
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

    /** Attempts to grow a sapling into a tree */
    public void growTree(World par1World, BlockPos pos, IBlockState wood, IBlockState leaves)
    {
        if (!TerrainGen.saplingGrowTree(par1World, par1World.rand, pos)) return;

        WorldGenBerries object = new WorldGenBerries();

        par1World.setBlockState(pos, wood);

        if (isPalm())
        {
            object.generatePalmTree(par1World, par1World.rand, pos, wood, leaves);
        }
        else
        {
            object.generateTree(par1World, par1World.rand, pos, wood, leaves);
        }
    }

    private boolean isPalm()
    {
        boolean ret = false;
        ret = ret || berryIndex == 18;
        return ret;
    }

    /** Returns the quantity of items to drop on block destruction. */
    @Override
    public int quantityDropped(Random par1Random)
    {
        return 1;
    }

    public void setBerry(String berryName)
    {
        this.berryName = berryName;
    }

    public void setBerryIndex(int berryId)
    {
        this.berryIndex = berryId;
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand)
    {
    }

    @Override
    /**
     * Get the actual Block state of this Block at the given position. This applies properties not visible in the
     * metadata, such as fence connections.
     */
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos)
    {
        TileEntityBerries tile = (TileEntityBerries) worldIn.getTileEntity(pos);
        return state.withProperty(BerryManager.type, BerryManager.berryNames.get(tile.getBerryId()));
    }

    @Override
    protected BlockState createBlockState()
    {
        return new BlockState(this, new IProperty[] {AGE, BerryManager.type});
    }

}
