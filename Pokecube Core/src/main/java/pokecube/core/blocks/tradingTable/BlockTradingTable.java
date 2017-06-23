package pokecube.core.blocks.tradingTable;

import java.util.Random;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
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
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.pokecubes.PokecubeManager;
import thut.core.common.blocks.BlockRotatable;
import thut.lib.CompatWrapper;

public class BlockTradingTable extends BlockRotatable implements ITileEntityProvider
{
    public static final PropertyBool TMC = PropertyBool.create("tmc");

    public BlockTradingTable()
    {
        super(Material.CLOTH);
        // this.setBlockBounds(0, 0, 0, 1, 0.75f, 1);
        this.setCreativeTab(PokecubeMod.creativeTabPokecube);
        this.setDefaultState(
                this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(TMC, false));
        this.setHardness(100);
        this.setResistance(100);
        this.setLightOpacity(0);
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state)
    {
        dropItems(worldIn, pos);
        super.breakBlock(worldIn, pos, state);
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, new IProperty[] { FACING, TMC });
    }

    @Override
    public TileEntity createNewTileEntity(World var1, int var2)
    {
        return new TileEntityTradingTable();
    }

    private void dropItems(World world, BlockPos pos)
    {
        Random rand = new Random();
        TileEntity tile_entity = world.getTileEntity(pos);

        if (!(tile_entity instanceof IInventory)) { return; }

        if (tile_entity instanceof TileEntityTradingTable)
        {
            TileEntityTradingTable table = (TileEntityTradingTable) tile_entity;
            if (table.player1 != null) table.player1.closeScreen();
            if (table.player2 != null) table.player2.closeScreen();
        }
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
                        new ItemStack(item.getItem(), CompatWrapper.getStackSize(item), item.getItemDamage()));
                if (item.hasTagCompound())
                {
                    entity_item.getItem().setTagCompound((NBTTagCompound) item.getTagCompound().copy());
                }
                if (PokecubeManager.isFilled(item))
                {
                    ItemTossEvent toss = new ItemTossEvent(entity_item, PokecubeMod.getFakePlayer());
                    MinecraftForge.EVENT_BUS.post(toss);
                    boolean toPC = toss.isCanceled();
                    if (toPC)
                    {
                        continue;
                    }
                }
                float factor = 0.5F;
                entity_item.motionX = rand.nextGaussian() * factor;
                entity_item.motionY = rand.nextGaussian() * factor + 0.2F;
                entity_item.motionZ = rand.nextGaussian() * factor;
                world.spawnEntity(entity_item);
                CompatWrapper.setStackSize(item, 0);
            }
        }
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
        if ((state.getValue(TMC))) ret += 8;
        return ret;
    }

    @Override
    /** Convert the given metadata into a BlockState for this Block */
    public IBlockState getStateFromMeta(int meta)
    {
        EnumFacing enumfacing = EnumFacing.getFront(meta);

        boolean tmc = (meta & 8) > 0;
        if (enumfacing.getAxis() == EnumFacing.Axis.Y)
        {
            enumfacing = EnumFacing.NORTH;
        }
        return this.getDefaultState().withProperty(FACING, enumfacing).withProperty(TMC, tmc);
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

    // 1.11
    public boolean isVisuallyOpaque(IBlockState state)
    {
        return false;
    }

    // 1.10
    public boolean isVisuallyOpaque()
    {
        return false;
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
        TileEntityTradingTable table = (TileEntityTradingTable) worldIn.getTileEntity(pos);
        table.openGUI(playerIn);
        return true;
    }

    @Override
    public int damageDropped(IBlockState state)
    {
        return state.getValue(TMC) ? 8 : 0;
    }

    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state)
    {
    }

    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ,
            int meta, EntityLivingBase placer)
    {
        return this.getStateFromMeta(meta).withProperty(FACING, placer.getHorizontalFacing().getOpposite());
    }
}
