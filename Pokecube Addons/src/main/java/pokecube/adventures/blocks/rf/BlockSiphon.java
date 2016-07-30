package pokecube.adventures.blocks.rf;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.World;
import pokecube.core.commands.CommandTools;

public class BlockSiphon extends Block implements ITileEntityProvider 
{

	public BlockSiphon() {
		super(Material.iron);
		this.setHardness(10);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {
		return new TileEntitySiphon(world);
	}

    /**
     * Called upon block activation (right click on the block.)
     */
    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumFacing side, float hitX, float hitY, float hitZ)
    {
    	if(!worldIn.isRemote)
    	{
            TileEntitySiphon tile = (TileEntitySiphon) worldIn.getTileEntity(pos);
            int input = tile.getInput();
            int stored = tile.lastInput;
            IChatComponent message = CommandTools.makeTranslatedMessage("block.rfsiphon.info", "", input, stored);
            playerIn.addChatMessage(message);
    	}
    	
        return false;
    }
    
}
