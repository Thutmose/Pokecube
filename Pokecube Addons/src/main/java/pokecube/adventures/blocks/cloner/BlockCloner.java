package pokecube.adventures.blocks.cloner;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import pokecube.adventures.PokecubeAdv;

public class BlockCloner extends Block implements ITileEntityProvider
{

    public BlockCloner()
    {
        super(Material.iron);
        this.setLightOpacity(0);
        this.setHardness(10);
        this.setResistance(10);
        this.setLightLevel(1f);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side,
            float hitX, float hitY, float hitZ)
    {
        player.openGui(PokecubeAdv.instance, PokecubeAdv.GUICLONER_ID, world, pos.getX(), pos.getY(), pos.getZ());
        return true;
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta)
    {
        return new TileEntityCloner();
    }

}
