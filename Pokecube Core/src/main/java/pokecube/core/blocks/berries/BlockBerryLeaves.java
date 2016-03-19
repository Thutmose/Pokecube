package pokecube.core.blocks.berries;

import java.util.List;
import java.util.Random;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;

import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockPlanks.EnumType;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.interfaces.PokecubeMod;

public class BlockBerryLeaves extends BlockLeaves implements IMetaBlock
{
    public static final PropertyEnum<BlockBerryWood.EnumType> VARIANT4 = PropertyEnum
            .create("variant", BlockBerryWood.EnumType.class, new Predicate<BlockBerryWood.EnumType>()
            {
                @Override
                public boolean apply(BlockBerryWood.EnumType type)
                {
                    return type.getMetadata() >= 4;
                }
            });
    public static final PropertyEnum<BlockBerryWood.EnumType> VARIANT0 = PropertyEnum
            .create("variant", BlockBerryWood.EnumType.class, new Predicate<BlockBerryWood.EnumType>()
            {
                @Override
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
        setCreativeTab(PokecubeMod.creativeTabPokecubeBerries);
        this.setDefaultState(this.blockState.getBaseState()
                .withProperty(shift == 0 ? VARIANT0 : VARIANT4, BlockBerryWood.EnumType.byMetadata(shift))
                .withProperty(CHECK_DECAY, Boolean.valueOf(true)).withProperty(DECAYABLE, Boolean.valueOf(true)));
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        IProperty<?> prop = (BlockBerryLog.currentlyConstructing == 0 ? VARIANT0 : VARIANT4);
        return new BlockStateContainer(this, new IProperty[] { prop, CHECK_DECAY, DECAYABLE });
    }

    @Override
    protected ItemStack createStackedBlock(IBlockState state)
    {
        return new ItemStack(Item.getItemFromBlock(this), 1,
                state.getValue(shift == 0 ? VARIANT0 : VARIANT4).getMetadata() - 4);
    }

    @Override
    /** Get the damage value that this Block should drop */
    public int damageDropped(IBlockState state)
    {
        return state.getValue(shift == 0 ? VARIANT0 : VARIANT4).getMetadata();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer()
    {
        return BlockRenderLayer.CUTOUT_MIPPED;
    }

    @Override
    /** Convert the BlockState into the correct metadata value */
    public int getMetaFromState(IBlockState state)
    {
        byte b0 = 0;
        int i = b0
                | state.getValue((shift == 0 ? VARIANT0 : VARIANT4)).getMetadata() - shift;

        if (!state.getValue(DECAYABLE).booleanValue())
        {
            i |= 4;
        }

        if (state.getValue(CHECK_DECAY).booleanValue())
        {
            i |= 8;
        }

        return i;
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

    @Override
    /** Convert the given metadata into a BlockState for this Block */
    public IBlockState getStateFromMeta(int meta)
    {
        return this.getDefaultState().withProperty((shift == 0 ? VARIANT0 : VARIANT4), this.getWoodType2(meta))
                .withProperty(DECAYABLE, Boolean.valueOf((meta & 4) == 0))
                .withProperty(CHECK_DECAY, Boolean.valueOf((meta & 8) > 0));
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
    public EnumType getWoodType(int meta)
    {
        return null;
    }

    public BlockBerryWood.EnumType getWoodType2(int meta)
    {
        return BlockBerryWood.EnumType.byMetadata((meta & 3) + shift);
    }

    @Override
    public boolean isOpaqueCube(IBlockState state)
    {
        return false;
    }

    @Override
    public List<ItemStack> onSheared(ItemStack item, net.minecraft.world.IBlockAccess world, BlockPos pos, int fortune)
    {
        IBlockState state = world.getBlockState(pos);
        return Lists.newArrayList(new ItemStack(this, 1,
                state.getValue((shift == 0 ? VARIANT0 : VARIANT4)).getMetadata() - shift));
    }

    @Override
    /** Returns the quantity of items to drop on block destruction. */
    public int quantityDropped(Random par1Random)
    {
        return 0;
    }

    /** How many world ticks before ticking */
    @Override
    public int tickRate(World p_149738_1_)
    {
        return 20;
    }

    @Override
    /** Ticks the block if it's been scheduled */
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand)
    {
        if (!worldIn.isRemote)
        {
            boolean air = worldIn.isAirBlock(pos.down());

            boolean canGrow = (state.getValue(DECAYABLE)) && air && Math.random() > 0.9;
            if (canGrow)
            {
                BlockBerryFruit fruit = (BlockBerryFruit) state
                        .getValue(shift == 0 ? VARIANT0 : VARIANT4).getBerryFruit();

                if (fruit != null)
                {
                    worldIn.setBlockState(pos.down(), fruit.getDefaultState());
                }
            }
        }
        super.updateTick(worldIn, pos, state, rand);
    }

}
