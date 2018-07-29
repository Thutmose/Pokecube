package pokecube.adventures.blocks.cloner.block;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.blocks.cloner.tileentity.TileEntityGeneExtractor;

public class BlockExtractor extends BlockBase
{
    public BlockExtractor()
    {
        super();
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta)
    {
        return new TileEntityGeneExtractor();
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
            EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        playerIn.openGui(PokecubeAdv.instance, PokecubeAdv.GUIEXTRACTOR_ID, worldIn, pos.getX(), pos.getY(),
                pos.getZ());
        return true;
    }
}
