package pokecube.adventures.blocks.afa;

import java.util.UUID;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import pokecube.core.items.pokecubes.PokecubeManager;
import thut.core.common.commands.CommandTools;

public final class BlockCommander extends BlockBase
{
    // TODO add some methods here to respond to redstone changes to send
    // commands to TE

    public BlockCommander()
    {
        super();
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta)
    {
        return new TileEntityCommander();
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
            EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        UUID id = PokecubeManager.getUUID(playerIn.getHeldItem(hand));
        if (id != null)
        {
            TileEntity tile = worldIn.getTileEntity(pos);
            if (tile instanceof TileEntityCommander)
            {
                ((TileEntityCommander) tile).setPokeID(id);
                if (!worldIn.isRemote)
                {
                    CommandTools.sendMessage(playerIn, "UUID Set to: " + id);
                }
                return true;
            }
        }
        return false;
    }
}
