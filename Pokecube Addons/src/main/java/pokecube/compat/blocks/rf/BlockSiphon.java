package pokecube.compat.blocks.rf;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

public class BlockSiphon extends Block implements ITileEntityProvider
{

    public BlockSiphon()
    {
        super(Material.iron);
        this.setHardness(10);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int metadata)
    {
        return new TileEntitySiphon(world);
    }

    /** Called upon block activation (right click on the block.) */
    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
            EnumHand hand, ItemStack heldStack, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        TileEntitySiphon te = (TileEntitySiphon) worldIn.getTileEntity(pos);

        int stored = te.lastInput;
        if (!worldIn.isRemote) playerIn.addChatMessage(new TextComponentString(stored + " RF/t"));

        return false;
    }

}
