package pokecube.core.blocks.pc;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import pokecube.core.blocks.TileEntityOwnable;
import pokecube.core.handlers.Config;
import pokecube.core.interfaces.PokecubeMod;
import thut.core.common.blocks.BlockRotatable;

public class BlockPC extends BlockRotatable implements ITileEntityProvider
{
    public final boolean top;

    public BlockPC(boolean top)
    {
        super(Material.GLASS);
        this.top = top;
        this.setLightOpacity(0);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
        this.setHardness(500);
        this.setResistance(100);
        this.setLightLevel(1f);
        this.setRegistryName(PokecubeMod.ID, "pc_" + (top ? "top" : "base"));
        this.setCreativeTab(PokecubeMod.creativeTabPokecubeBlocks);
        this.setUnlocalizedName(getRegistryName().getResourcePath());
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, new IProperty[] { FACING });
    }

    @Override
    public TileEntity createNewTileEntity(World var1, int var2)
    {
        return new TileEntityPC();
    }

    @Override
    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        return super.getExtendedState(state, world, pos);
    }

    @Override
    /** Convert the BlockState into the correct metadata value */
    public int getMetaFromState(IBlockState state)
    {
        int ret = state.getValue(FACING).getIndex();
        return ret;
    }

    @Override
    /** Convert the given metadata into a BlockState for this Block */
    public IBlockState getStateFromMeta(int meta)
    {
        EnumFacing enumfacing = EnumFacing.getFront(meta & 7);

        if (enumfacing.getAxis() == EnumFacing.Axis.Y)
        {
            enumfacing = EnumFacing.NORTH;
        }

        return this.getDefaultState().withProperty(FACING, enumfacing);
    }

    @Override
    public boolean isFullCube(IBlockState state)
    {
        return false;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state)
    {
        return false;
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
            EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        this.setLightLevel(1f);
        if (!top) { return false; }
        IBlockState down = worldIn.getBlockState(pos.down());
        Block idDown = down.getBlock();
        if (!(idDown instanceof BlockPC) || ((BlockPC) idDown).top) return false;
        InventoryPC inventoryPC = InventoryPC.getPC(playerIn.getUniqueID());
        if (inventoryPC != null)
        {
            if (worldIn.isRemote) { return true; }
            playerIn.openGui(PokecubeMod.core, Config.GUIPC_ID, worldIn, pos.getX(), pos.getY(), pos.getZ());
            return true;
        }
        return true;
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY,
            float hitZ, int meta, EntityLivingBase placer, EnumHand hand)
    {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEntityOwnable)
        {
            TileEntityOwnable tile = (TileEntityOwnable) te;
            tile.setPlacer(placer);
        }
        return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
    }
}