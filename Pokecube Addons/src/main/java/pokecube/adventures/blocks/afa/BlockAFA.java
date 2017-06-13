package pokecube.adventures.blocks.afa;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import pokecube.adventures.PokecubeAdv;
import thut.lib.CompatWrapper;

public final class BlockAFA extends Block implements ITileEntityProvider
{
    public static enum EnumType implements IStringSerializable
    {
        AFA("amplifier"), DAYCARE("daycare");

        final String name;

        private EnumType(String name)
        {
            this.name = name;
        }

        @Override
        public String getName()
        {
            return name;
        }
    }

    public static final PropertyEnum<EnumType> VARIANT = PropertyEnum.create("variant", EnumType.class);

    public BlockAFA()
    {
        super(Material.IRON);
        this.setLightOpacity(0);
        this.setHardness(10);
        this.setResistance(10);
        this.setDefaultState(this.blockState.getBaseState().withProperty(VARIANT, EnumType.AFA));
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, new IProperty[] { VARIANT });
    }

    /** Get the damage value that this Block should drop */
    @Override
    public int damageDropped(IBlockState state)
    {
        return state.getValue(VARIANT).ordinal();
    }

    /** Convert the BlockState into the correct metadata value */
    @Override
    public int getMetaFromState(IBlockState state)
    {
        return state.getValue(VARIANT).ordinal();
    }

    /** Convert the given metadata into a BlockState for this Block */
    @Override
    public IBlockState getStateFromMeta(int meta)
    {
        if (meta >= EnumType.values().length) meta = 0;
        return this.getDefaultState().withProperty(VARIANT, EnumType.values()[meta]);
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state)
    {
        dropItems(world, pos);
        super.breakBlock(world, pos, state);
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta)
    {
        if ((meta & 1) > 0) return new TileEntityDaycare();
        return new TileEntityAFA();
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
                        new ItemStack(item.getItem(), CompatWrapper.getStackSize(item), item.getItemDamage()));
                if (item.hasTagCompound())
                {
                    entity_item.getEntityItem().setTagCompound((NBTTagCompound) item.getTagCompound().copy());
                }
                float factor = 0.005F;
                entity_item.motionX = rand.nextGaussian() * factor;
                entity_item.motionY = rand.nextGaussian() * factor + 0.2F;
                entity_item.motionZ = rand.nextGaussian() * factor;
                world.spawnEntity(entity_item);
                CompatWrapper.setStackSize(item, 0);
            }
        }
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
        if (hand == EnumHand.MAIN_HAND)
            playerIn.openGui(PokecubeAdv.instance, PokecubeAdv.GUIAFA_ID, worldIn, pos.getX(), pos.getY(), pos.getZ());
        return true;
    }
}
