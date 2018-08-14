package pokecube.core.blocks.healtable;

import java.util.Random;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import pokecube.core.PokecubeCore;
import pokecube.core.handlers.Config;
import pokecube.core.utils.PokecubeSerializer;
import thut.core.common.blocks.BlockRotatable;
import thut.lib.CompatWrapper;

public class BlockHealTable extends BlockRotatable implements ITileEntityProvider
{
    public static final PropertyBool      FIXED  = PropertyBool.create("fixed");

    public BlockHealTable()
    {
        super(Material.CLOTH);
        setHardness(1000);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(FIXED,
                Boolean.FALSE));
        this.setCreativeTab(CreativeTabs.REDSTONE);
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state)
    {
        dropItems(world, pos);
        if (!world.isRemote)
        {
            PokecubeSerializer.getInstance().removeChunks(world, pos);
        }
        super.breakBlock(world, pos, state);
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, new IProperty[] { FIXED, FACING });
    }

    @Override
    public TileEntity createNewTileEntity(World var1, int var2)
    {
        return new TileHealTable();
    }

    private void dropItems(World world, BlockPos pos)
    {
        Random rand = new Random();
        TileEntity tile_entity = world.getTileEntity(pos);

        if (!(tile_entity instanceof IInventory)) { return; }

        IInventory inventory = (IInventory) tile_entity;

        for (int i = 0; i < inventory.getSizeInventory(); i++)
        {
            ItemStack item = inventory.getStackInSlot(i);
            if (CompatWrapper.isValid(item))
            {
                float rx = rand.nextFloat() * 0.6F + 0.1F;
                float ry = rand.nextFloat() * 0.6F + 0.1F;
                float rz = rand.nextFloat() * 0.6F + 0.1F;
                EntityItem entity_item = new EntityItem(world, pos.getX() + rx, pos.getY() + ry, pos.getZ() + rz,
                        new ItemStack(item.getItem(), item.getCount(), item.getItemDamage()));
                if (item.hasTagCompound())
                {
                    entity_item.getItem().setTagCompound((NBTTagCompound) item.getTagCompound().copy());
                }
                float factor = 0.005F;
                entity_item.motionX = rand.nextGaussian() * factor;
                entity_item.motionY = rand.nextGaussian() * factor + 0.2F;
                entity_item.motionZ = rand.nextGaussian() * factor;
                world.spawnEntity(entity_item);
                item.setCount(0);
            }
        }
    }

    /** Convert the BlockState into the correct metadata value */
    @Override
    public int getMetaFromState(IBlockState state)
    {
        int ret = state.getValue(FACING).getIndex();
        if ((state.getValue(FIXED))) ret += 8;
        return ret;
    }

    @Override
    public IBlockState getStateFromMeta(int meta)
    {
        EnumFacing enumfacing = EnumFacing.getFront(meta & 7);
        boolean top = (meta & 8) > 0;
        if (enumfacing.getAxis() == EnumFacing.Axis.Y)
        {
            enumfacing = EnumFacing.NORTH;
        }
        return this.getDefaultState().withProperty(FACING, enumfacing).withProperty(FIXED, Boolean.valueOf(top));
    }

    // 1.11
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
            EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        return onBlockActivated(worldIn, pos, state, playerIn, hand, playerIn.getHeldItem(hand), side, hitX, hitY,
                hitZ);
    }

    // 1.10
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
            EnumHand hand, ItemStack heldStack, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        TileEntity tile_entity = worldIn.getTileEntity(pos);

        if (tile_entity == null || playerIn.isSneaking())
        {
            if (playerIn.capabilities.isCreativeMode && !worldIn.isRemote && hand == EnumHand.MAIN_HAND)
            {
                state = state.cycleProperty(FIXED);
                playerIn.sendMessage(new TextComponentString(
                        "Set Block to " + (state.getValue(BlockHealTable.FIXED) ? "Breakable" : "Unbreakable")));
                worldIn.setBlockState(pos, state);
            }
            return false;
        }
        playerIn.openGui(PokecubeCore.instance, Config.GUIPOKECENTER_ID, worldIn, pos.getX(), pos.getY(), pos.getZ());
        return true;
    }

    @Override
    /** Called when a block is placed using its ItemBlock. Args: World, X, Y, Z,
     * side, hitX, hitY, hitZ, block metadata */
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ,
            int meta, EntityLivingBase placer)
    {
        if (!world.isRemote) PokecubeSerializer.getInstance().addChunks(world, pos, placer);
        return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite())
                .withProperty(FIXED, ((meta & 8) > 0));
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
}