package pokecube.adventures.blocks.siphon;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import thut.core.common.commands.CommandTools;

public class BlockSiphon extends Block implements ITileEntityProvider
{

    public BlockSiphon()
    {
        super(Material.IRON);
        this.setHardness(10);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int metadata)
    {
        return new TileEntitySiphon(world);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
            EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        if (!worldIn.isRemote && hand == EnumHand.MAIN_HAND)
        {
            TileEntitySiphon tile = (TileEntitySiphon) worldIn.getTileEntity(pos);
            ITextComponent message = CommandTools.makeTranslatedMessage("block.rfsiphon.info", "gold",
                    tile.currentOutput, tile.theoreticalOutput);
            playerIn.sendMessage(message);
        }
        return false;
    }
}
