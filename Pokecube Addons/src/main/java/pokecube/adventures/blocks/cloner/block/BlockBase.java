package pokecube.adventures.blocks.cloner.block;

import java.util.Random;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thut.core.common.blocks.BlockRotatable;
import thut.lib.CompatWrapper;

public abstract class BlockBase extends BlockRotatable implements ITileEntityProvider
{

    public BlockBase()
    {
        super(Material.IRON);
        this.setLightOpacity(0);
        this.setHardness(10);
        this.setResistance(10);
        this.setLightLevel(1f);
    }

    /** Used to determine ambient occlusion and culling when rebuilding chunks
     * for render */
    @Override
    public boolean isOpaqueCube(IBlockState state)
    {
        return false;
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, new IProperty[] { FACING });
    }

    /** Convert the BlockState into the correct metadata value */
    @Override
    public int getMetaFromState(IBlockState state)
    {
        int meta = 0;
        int direction;
        switch (state.getValue(FACING))
        {
        case NORTH:
            direction = 0 * 4;
            break;
        case EAST:
            direction = 1 * 4;
            break;
        case SOUTH:
            direction = 2 * 4;
            break;
        case WEST:
            direction = 3 * 4;
            break;
        default:
            direction = 0;
        }
        meta |= direction;
        return meta;
    }

    /** Convert the given metadata into a BlockState for this Block */
    @Override
    public IBlockState getStateFromMeta(int meta)
    {
        int direction = meta / 4;
        EnumFacing dir = EnumFacing.NORTH;
        switch (direction)
        {
        case 0:
            dir = EnumFacing.NORTH;
            break;
        case 1:
            dir = EnumFacing.EAST;
            break;
        case 2:
            dir = EnumFacing.SOUTH;
            break;
        case 3:
            dir = EnumFacing.WEST;
            break;
        }
        return this.getDefaultState().withProperty(FACING, dir);
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state)
    {
        dropItems(world, pos);
        super.breakBlock(world, pos, state);
    }

    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY,
            float hitZ, int meta, EntityLivingBase placer)
    {
        return this.getStateFromMeta(meta).withProperty(FACING, placer.getHorizontalFacing().getOpposite());
    }

    @Override
    public abstract TileEntity createNewTileEntity(World worldIn, int meta);

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

    @SideOnly(Side.CLIENT)
    @Override
    public BlockRenderLayer getBlockLayer()
    {
        return BlockRenderLayer.TRANSLUCENT;
    }

}
