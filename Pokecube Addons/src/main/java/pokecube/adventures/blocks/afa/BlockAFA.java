package pokecube.adventures.blocks.afa;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import pokecube.adventures.PokecubeAdv;

public final class BlockAFA extends BlockBase
{
    public BlockAFA()
    {
        super();
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta)
    {
        return new TileEntityAFA();
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
            EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        if (hand == EnumHand.MAIN_HAND)
            playerIn.openGui(PokecubeAdv.instance, PokecubeAdv.GUIAFA_ID, worldIn, pos.getX(), pos.getY(), pos.getZ());
        return true;
    }
}
