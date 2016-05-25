package pokecube.adventures.blocks.rf;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockSiphon extends Block implements ITileEntityProvider 
{

	public BlockSiphon() {
		super(Material.IRON);
		this.setHardness(10);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {
		return new TileEntitySiphon(world);
	}
    
}
