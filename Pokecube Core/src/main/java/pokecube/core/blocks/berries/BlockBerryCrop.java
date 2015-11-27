package pokecube.core.blocks.berries;

import java.util.ArrayList;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.event.terraingen.TerrainGen;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.items.berries.BerryManager;

/**
 * 
 * @author Oracion
 * @author Manchou
 */
public class BlockBerryCrop extends BlockCrops {

	int berryIndex = 0;
	String berryName = "";
	
	static ArrayList<Integer> trees = new ArrayList<Integer>();
	public static ArrayList<BlockBerryLeaves> leaves = new ArrayList<BlockBerryLeaves>();
	public static ArrayList<BlockBerryLog> logs = new ArrayList<BlockBerryLog>();
	
	static
	{
		trees.add(3);
		trees.add(6);
		trees.add(7);
		trees.add(10);
		trees.add(18);
		trees.add(60);
	}
	
	public BlockBerryCrop() {
		super();
		this.setTickRandomly(true);
		disableStats();
		float var3 = 0.3F;
		this.setBlockBounds(0.5F - var3, -0.05F, 0.5F - var3, 0.5F + var3, 1F, 0.5F + var3);
	}

	public void setBerry(String berryName) {
		this.berryName = berryName;
	}
	
	public String getBerryName(){
		return berryName;
	}

	public void setBerryIndex(int berryId) {
		this.berryIndex = berryId;
	}

	/**
	 * Gets passed in the blockID of the block below and supposed to return true if its allowed to grow on the type of blockID passed in. Args: blockID
	 */
	protected boolean canThisPlantGrowOnThisBlockID(Block par1) {
		return par1 == Blocks.farmland;
	}

    @Override
    public void grow(World worldIn, BlockPos pos, IBlockState state)//TODO growing things here
    {
        int i = ((Integer)state.getValue(AGE)).intValue() + MathHelper.getRandomIntegerInRange(worldIn.rand, 2, 5);

        if (i > 7)
        {
            i = 7;
        }
        if(i==7)
        {
        	i = 0;
        	if(!trees.contains(berryIndex))
        	{
	        	Block fruit = BerryManager.berryFruits.get(berryIndex);
	        	worldIn.setBlockState(pos.up(), fruit.getDefaultState());
        	}
        	else
        	{
        		IBlockState leaf = null;
        		IBlockState log = null;
    			for(BlockBerryLeaves l: this.leaves)
    			{
    				if(leaf==null)
    				{
    					leaf = l.getStateForTree(berryName);
    				}
    			}
    			
    			for(BlockBerryLog l: this.logs)
    			{
    				if(log==null)
    				{
    					log = l.getStateForTree(berryName);
    				}
    			}
    			if(leaf!=null && log!=null)
    			{
    				growTree(worldIn, pos, log, leaf);
    				
    				return;
    			}
        	}
        }

        worldIn.setBlockState(pos, state.withProperty(AGE, Integer.valueOf(i)), 2);
    }
    
//    {
//		int metadata = world.getBlockMetadata(par2, par3, par4);
//		
//		if(!trees.contains(berryIndex))
//		{
//			if (metadata != 1) {
//				world.setBlockMetadataWithNotify(par2, par3, par4, 1, 2);
//			}
//			if (metadata == 1 && world.isAirBlock(par2, par3 + 1, par4)) {
//				world.setBlock(par2, par3 + 1, par4, BerryManager.berryFruits.get(berryIndex), 0, 3);
//			}
//		}
//		else
//		{
//			int woodMeta = -1;
//			int leavesMeta = -1;
//			Block leaves = null;
//			Block wood = null;
//			
//			for(BlockBerryLeaves l: this.leaves)
//			{
//				leavesMeta = l.getMetaFromName(berryName);
//				if(leavesMeta>=0)
//				{
//					leaves = l;
//					leavesMeta |= 8;
//					break;
//				}
//			}
//			
//			for(BlockBerryLog l: this.logs)
//			{
//				woodMeta = l.getMetaFromName(berryName);
//				if(woodMeta>=0)
//				{
//					wood = l;
//					break;
//				}
//			}
//			
//			if(woodMeta>=0&&leavesMeta>=0&&leaves!=null&&wood!=null)
//			{
//				growTree(world, par2, par3, par4, woodMeta, leavesMeta, wood, leaves);
//			}
//		}
//	}

	/**
	 * Gets the growth rate for the crop. Setup to encourage rows by halving growth rate if there is diagonals, crops on different sides that aren't opposing, and by adding growth for every crop next
	 * to this one (and for crop below this one). Args: x, y, z
	 */
	private float getGrowthRate(World par1World, int par2, int par3, int par4) {
		float var5 = 3.0F;

		return var5;
	}
	
    @Override
    /**
     * Called when a user uses the creative pick block button on this block
     *
     * @param target The full target the player is looking at
     * @return A ItemStack to add to the player's inventory, Null if nothing should be added.
     */
    public ItemStack getPickBlock(MovingObjectPosition target, World world, BlockPos pos, EntityPlayer player)
    {
    	return BerryManager.getBerryItem(berryName);
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune)
    {
        return BerryManager.getBerryItem(berryName).getItem();
    }
    
    @SideOnly(Side.CLIENT)
    public Item getItem(World worldIn, BlockPos pos)
    {
        return BerryManager.getBerryItem(berryName).getItem();
    }

    @Override
    /**
     * Get the damage value that this Block should drop
     */
    public int damageDropped(IBlockState state)
    {
        return berryIndex;
    }

    
	/**
	 * Returns the quantity of items to drop on block destruction.
	 */
	@Override
	public int quantityDropped(Random par1Random) {
		return 1;
	}

    
    private boolean isPalm()
    {
    	boolean ret = false;
    	ret = ret || berryIndex == 18;
    	return ret;
    }

    /**
     * Attempts to grow a sapling into a tree
     */
    public void growTree(World par1World, BlockPos pos, IBlockState wood, IBlockState leaves)
    {
        if (!TerrainGen.saplingGrowTree(par1World, par1World.rand, pos)) return;

        WorldGenBerries object = new WorldGenBerries();

        par1World.setBlockState(pos, wood);
        
        if(isPalm())
        {
        	object.generatePalmTree(par1World, par1World.rand, pos, wood, leaves);
        }
        else
        {
        	object.generateTree(par1World, par1World.rand, pos, wood, leaves);
        }
    }
    

}
