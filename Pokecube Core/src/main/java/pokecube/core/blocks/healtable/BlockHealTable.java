package pokecube.core.blocks.healtable;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
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
import pokecube.core.blocks.TileEntityOwnable;
import pokecube.core.handlers.Config;
import pokecube.core.utils.PokecubeSerializer;
import thut.api.maths.Vector3;

public class BlockHealTable extends Block implements ITileEntityProvider
{
    public static final PropertyBool      FIXED  = PropertyBool.create("fixed");
    public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);

    public BlockHealTable()
    {
        super(Material.CLOTH);
        setHardness(20);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(FIXED,
                Boolean.FALSE));
        this.setCreativeTab(CreativeTabs.REDSTONE);
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state)
    {
        dropItems(world, pos);
        Vector3 v = Vector3.getNewVector();
        if (!world.isRemote && !((Boolean) state.getValue(BlockHealTable.FIXED)))
        {
            PokecubeSerializer.getInstance().removeChunks(world, v.set(pos));
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

            if (item != null && item.stackSize > 0)
            {
                float rx = rand.nextFloat() * 0.6F + 0.1F;
                float ry = rand.nextFloat() * 0.6F + 0.1F;
                float rz = rand.nextFloat() * 0.6F + 0.1F;
                EntityItem entity_item = new EntityItem(world, pos.getX() + rx, pos.getY() + ry, pos.getZ() + rz,
                        new ItemStack(item.getItem(), item.stackSize, item.getItemDamage()));

                if (item.hasTagCompound())
                {
                    entity_item.getEntityItem().setTagCompound((NBTTagCompound) item.getTagCompound().copy());
                }

                float factor = 0.5F;
                entity_item.motionX = rand.nextGaussian() * factor;
                entity_item.motionY = rand.nextGaussian() * factor + 0.2F;
                entity_item.motionZ = rand.nextGaussian() * factor;
                world.spawnEntityInWorld(entity_item);
                item.stackSize = 0;
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

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
            EnumHand hand, ItemStack heldStack, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        TileEntity tile_entity = worldIn.getTileEntity(pos);

        if (tile_entity == null || playerIn.isSneaking())
        {
            if (playerIn.capabilities.isCreativeMode && !worldIn.isRemote)
            {
                state = state.cycleProperty(FIXED);
                playerIn.addChatMessage(new TextComponentString(
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
    public IBlockState onBlockPlaced(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ,
            int meta, EntityLivingBase placer)
    {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEntityOwnable)
        {
            TileEntityOwnable tile = (TileEntityOwnable) te;
            tile.setPlacer(placer);
        }
        return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite())
                .withProperty(FIXED, ((meta & 8) > 0));
    }
}