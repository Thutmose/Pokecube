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
import pokecube.core.world.gen.WorldGenBerries;

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
    public void grow(World worldIn, BlockPos pos, IBlockState state)
    {
        int i = ((Integer)state.getValue(AGE)).intValue() + MathHelper.getRandomIntegerInRange(worldIn.rand, 2, 5);

        if (i > 7)
        {
            i = 7;
        }
        if(i==7)
        {
        	i = 1;
        	if(!trees.contains(berryIndex))
        	{
	        	Block fruit = BerryManager.berryFruits.get(berryIndex);
	        	worldIn.setBlockState(pos.up(), fruit.getDefaultState());
        	}
        	else
        	{
        		IBlockState leaf = null;
        		IBlockState log = null;
    			for(BlockBerryLeaves l: BlockBerryCrop.leaves)
    			{
    				if(leaf==null)
    				{
    					leaf = l.getStateForTree(berryName);
    				}
    			}
    			
    			for(BlockBerryLog l: BlockBerryCrop.logs)
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
    
    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand)
    {
        super.updateTick(worldIn, pos, state, rand);

        if (worldIn.getLightFromNeighbors(pos.up()) >= 9)
        {
            int i = ((Integer)state.getValue(AGE)).intValue();

            if (i < 7)
            {
                float f = getGrowthChance(this, worldIn, pos);

                if (rand.nextInt((int)(25.0F / f) + 1) == 0)
                {
                    grow(worldIn, pos, state);
                }
            }
        }
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
    
    @Override
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
