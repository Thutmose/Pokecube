package pokecube.compat.blocks.rf;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class BlockSiphon extends Block implements ITileEntityProvider 
{

	public BlockSiphon() {
		super(Material.iron);
	}

	@Override
	public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_) {
		return new TileEntitySiphon(p_149915_1_);
	}

    /**
     * Called upon block activation (right click on the block.)
     */
    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumFacing side, float hitX, float hitY, float hitZ)
    {
    	TileEntitySiphon te = (TileEntitySiphon) worldIn.getTileEntity(pos);
    	
    	int stored = te.getEnergyStored(null);
    	if(!worldIn.isRemote)
    		playerIn.addChatMessage(new ChatComponentText(stored+" RF/t"));
    	
        return false;
    }
    
}
