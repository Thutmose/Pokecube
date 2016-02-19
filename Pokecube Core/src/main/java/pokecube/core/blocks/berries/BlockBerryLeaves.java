package pokecube.core.blocks.berries;

import java.util.List;
import java.util.Random;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;

import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockPlanks.EnumType;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.PokecubeCore;

public class BlockBerryLeaves extends BlockLeaves implements IMetaBlock
{
    public static final PropertyEnum<BlockBerryWood.EnumType> VARIANT4 = PropertyEnum
            .create("variant", BlockBerryWood.EnumType.class, new Predicate<BlockBerryWood.EnumType>()
            {
                public boolean apply(BlockBerryWood.EnumType type)
                {
                    return type.getMetadata() >= 4;
                }
            });
    public static final PropertyEnum<BlockBerryWood.EnumType> VARIANT0 = PropertyEnum
            .create("variant", BlockBerryWood.EnumType.class, new Predicate<BlockBerryWood.EnumType>()
            {
                public boolean apply(BlockBerryWood.EnumType type)
                {
                    return type.getMetadata() < 4;
                }
            });

    public final String[] LEAF_TYPES;

    public final int shift;

    public BlockBerryLeaves(int logShift, String[] names)
    {
        super();
        LEAF_TYPES = names;
        shift = logShift;
        BlockBerryCrop.leaves.add(this);
        setCreativeTab(PokecubeCore.creativeTabPokecubeBerries);
        this.setDefaultState(this.blockState.getBaseState()
                .withProperty(shift == 0 ? VARIANT0 : VARIANT4, BlockBerryWood.EnumType.byMetadata(shift))
                .withProperty(CHECK_DECAY, Boolean.valueOf(true)).withProperty(DECAYABLE, Boolean.valueOf(true)));
    }

    /** How many world ticks before ticking */
    public int tickRate(World p_149738_1_)
    {
        return 20;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public EnumWorldBlockLayer getBlockLayer()
    {
        return EnumWorldBlockLayer.CUTOUT_MIPPED;
    }

    @Override
    /** Ticks the block if it's been scheduled */
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand)
    {
        if (!worldIn.isRemote)
        {
            boolean air = worldIn.isAirBlock(pos.down());

            boolean canGrow = ((Boolean) state.getValue(DECAYABLE)) && air && Math.random() > 0.9;
            if (canGrow)
            {
                BlockBerryFruit fruit = (BlockBerryFruit) ((BlockBerryWood.EnumType) state
                        .getValue(shift == 0 ? VARIANT0 : VARIANT4)).getBerryFruit();

                if (fruit != null)
                {
                    worldIn.setBlockState(pos.down(), fruit.getDefaultState());
                }
            }
        }
        super.updateTick(worldIn, pos, state, rand);
    }

    @Override
    /** Returns the quantity of items to drop on block destruction. */
    public int quantityDropped(Random par1Random)
    {
        return 0;
    }

    @Override
    @SideOnly(Side.CLIENT)

    /** returns a list of blocks with the same ID, but different meta (eg: wood
     * returns 4 blocks) */
    public void getSubBlocks(Item itemIn, CreativeTabs par2CreativeTabs, List<ItemStack> list)
    {
        if (shift == 0)
        {
            list.add(new ItemStack(itemIn, 1, BlockBerryWood.EnumType.PECHA.getMetadata()));
            list.add(new ItemStack(itemIn, 1, BlockBerryWood.EnumType.LEPPA.getMetadata()));
            list.add(new ItemStack(itemIn, 1, BlockBerryWood.EnumType.ORAN.getMetadata()));
            list.add(new ItemStack(itemIn, 1, BlockBerryWood.EnumType.SITRUS.getMetadata()));
        }
        else
        {
            list.add(new ItemStack(itemIn, 1, BlockBerryWood.EnumType.ENIGMA.getMetadata() - shift));
            list.add(new ItemStack(itemIn, 1, BlockBerryWood.EnumType.NANAB.getMetadata() - shift));
        }
    }

    @Override
    public String getUnlocalizedName(ItemStack stack)
    {
        return "tile." + getWoodType2(stack.getMetadata()).getUnlocalizedName() + "Leaves";
    }

    @Override
    /** Get the damage value that this Block should drop */
    public int damageDropped(IBlockState state)
    {
        return ((BlockBerryWood.EnumType) state.getValue(shift == 0 ? VARIANT0 : VARIANT4)).getMetadata();
    }

    @Override
    public int getDamageValue(World worldIn, BlockPos pos)
    {
        IBlockState iblockstate = worldIn.getBlockState(pos);
        return iblockstate.getBlock().getMetaFromState(iblockstate) & 3;
    }

    @Override
    protected ItemStack createStackedBlock(IBlockState state)
    {
        return new ItemStack(Item.getItemFromBlock(this), 1,
                ((BlockBerryWood.EnumType) state.getValue(shift == 0 ? VARIANT0 : VARIANT4)).getMetadata() - 4);
    }

    @Override
    /** Convert the given metadata into a BlockState for this Block */
    public IBlockState getStateFromMeta(int meta)
    {
        return this.getDefaultState().withProperty((shift == 0 ? VARIANT0 : VARIANT4), this.getWoodType2(meta))
                .withProperty(DECAYABLE, Boolean.valueOf((meta & 4) == 0))
                .withProperty(CHECK_DECAY, Boolean.valueOf((meta & 8) > 0));
    }

    @Override
    /** Convert the BlockState into the correct metadata value */
    public int getMetaFromState(IBlockState state)
    {
        byte b0 = 0;
        int i = b0
                | ((BlockBerryWood.EnumType) state.getValue((shift == 0 ? VARIANT0 : VARIANT4))).getMetadata() - shift;

        if (!((Boolean) state.getValue(DECAYABLE)).booleanValue())
        {
            i |= 4;
        }

        if (((Boolean) state.getValue(CHECK_DECAY)).booleanValue())
        {
            i |= 8;
        }

        return i;
    }

    public BlockBerryWood.EnumType getWoodType2(int meta)
    {
        return BlockBerryWood.EnumType.byMetadata((meta & 3) + shift);
    }

    @Override
    protected BlockState createBlockState()
    {
        IProperty<?> prop = (BlockBerryLog.currentlyConstructing == 0 ? VARIANT0 : VARIANT4);
        return new BlockState(this, new IProperty[] { prop, CHECK_DECAY, DECAYABLE });
    }

    @Override
    public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, TileEntity te)
    {

        super.harvestBlock(worldIn, player, pos, state, te);

    }

    @Override
    public List<ItemStack> onSheared(ItemStack item, net.minecraft.world.IBlockAccess world, BlockPos pos, int fortune)
    {
        IBlockState state = world.getBlockState(pos);
        return Lists.newArrayList(new ItemStack(this, 1,
                ((BlockBerryWood.EnumType) state.getValue((shift == 0 ? VARIANT0 : VARIANT4))).getMetadata() - shift));
    }

    @Override
    public EnumType getWoodType(int meta)
    {
        return null;
    }

    @Override
    public boolean isOpaqueCube()
    {
        return false;
    }

    public IBlockState getStateForTree(String berryName)
    {
        BlockBerryWood.EnumType type = BlockBerryWood.EnumType.valueOf(berryName.toUpperCase());
        if (type != null)
        {
            int num = type.getMetadata() - shift;
            if (num >= 0 && num < 4) { return getStateFromMeta(num + shift).withProperty(CHECK_DECAY, Boolean.FALSE)
                    .withProperty(DECAYABLE, Boolean.TRUE); }
        }
        return null;
    }

}
