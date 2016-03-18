package pokecube.core.blocks.nests;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.ColorizerGrass;
import net.minecraft.world.World;
import pokecube.core.database.Database;
import pokecube.core.items.pokemobeggs.ItemPokemobEgg;

public class BlockNest extends Block implements ITileEntityProvider {

	public BlockNest() {
		super(Material.leaves);
		setLightOpacity(2);
	}

	@Override
	public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_) {
		return new TileEntityNest();
	}

	@Override
	public int getBlockColor() {
		double d = 0.5D;
		double d1 = 1.0D;
		return ColorizerGrass.getGrassColor(d, d1);
	}
	
    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state,
            EntityPlayer player, EnumFacing side, float f, float g, float t)
    {
    	
        TileEntity tile_entity = world.getTileEntity(pos);
        if(tile_entity instanceof TileEntityNest && player.capabilities.isCreativeMode && player.getHeldItemMainhand()!=null && player.getHeldItemMainhand().getItem() instanceof ItemPokemobEgg && !player.worldObj.isRemote)
        {
        	TileEntityNest nest = (TileEntityNest) tile_entity;
        	
        	nest.pokedexNb = ItemPokemobEgg.getNumber(player.getHeldItemMainhand());
        	player.addTextComponentMessage(new TextComponentString("Set to "+Database.getEntry(nest.pokedexNb)));
        	return true;
        }
        return false;
    }
}
