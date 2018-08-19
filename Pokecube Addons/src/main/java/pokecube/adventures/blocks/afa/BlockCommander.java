package pokecube.adventures.blocks.afa;

import java.util.UUID;
import java.util.logging.Level;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import pokecube.adventures.PokecubeAdv;
import pokecube.core.interfaces.PokecubeMod;
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
        else if (!playerIn.isSneaking())
        {
            playerIn.openGui(PokecubeAdv.instance, PokecubeAdv.GUICOMMANDER_ID, worldIn, pos.getX(), pos.getY(),
                    pos.getZ());
        }
        return false;
    }

    /** Called when a neighboring block was changed and marks that this state
     * should perform any checks during a neighbor change. Cases may include
     * when redstone power is updated, cactus blocks popping off due to a
     * neighboring solid block, etc. */
    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos)
    {
        int power = worldIn.isBlockIndirectlyGettingPowered(pos);
        TileEntity tile = worldIn.getTileEntity(pos);
        if (tile == null || !(tile instanceof TileEntityCommander)) return;
        TileEntityCommander commander = (TileEntityCommander) tile;
        // Trigger on rising signal
        if (power > 0 && commander.power == 0)
        {
            try
            {
                commander.initCommand();
                commander.sendCommand();
            }
            catch (Exception e)
            {
                if (PokecubeMod.debug) PokecubeMod.log(Level.WARNING, "Invalid Commander Block use at " + pos, e);
                worldIn.playSound(null, pos, SoundEvents.BLOCK_NOTE_BASEDRUM, SoundCategory.BLOCKS, 1, 1);
            }
        }
        commander.power = power;
    }
}
