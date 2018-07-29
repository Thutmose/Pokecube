package pokecube.core.blocks.berries;

import net.minecraft.block.BlockLog;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import pokecube.core.interfaces.PokecubeMod;

public class BlockBerryLog extends BlockLog
{
    static final class SwitchEnumAxis
    {
        static final int[] AXIS_LOOKUP = new int[BlockLog.EnumAxis.values().length];

        static
        {
            try
            {
                AXIS_LOOKUP[BlockLog.EnumAxis.X.ordinal()] = 1;
            }
            catch (NoSuchFieldError var3)
            {
                ;
            }

            try
            {
                AXIS_LOOKUP[BlockLog.EnumAxis.Z.ordinal()] = 2;
            }
            catch (NoSuchFieldError var2)
            {
                ;
            }

            try
            {
                AXIS_LOOKUP[BlockLog.EnumAxis.NONE.ordinal()] = 3;
            }
            catch (NoSuchFieldError var1)
            {
                ;
            }
        }
    }

    public final String name;

    /** @param logShift
     *            0 or 4, depending on first or second one.
     * @param names */
    public BlockBerryLog(String name)
    {
        super();
        IBlockState state = this.blockState.getBaseState();
        this.setDefaultState(state.withProperty(BlockLog.LOG_AXIS, EnumAxis.Y));
        this.name = name;
        this.setRegistryName(PokecubeMod.ID, name);
        this.setCreativeTab(PokecubeMod.creativeTabPokecubeBerries);
        this.setUnlocalizedName(this.getRegistryName().getResourcePath());
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, new IProperty[] { BlockLog.LOG_AXIS });
    }

    @Override
    public int getMetaFromState(IBlockState state)
    {
        byte b0 = 0;
        int i = b0;
        switch (SwitchEnumAxis.AXIS_LOOKUP[state.getValue(BlockLog.LOG_AXIS).ordinal()])
        {
        case 1:
            i |= 4;
            break;
        case 2:
            i |= 8;
            break;
        case 3:
            i |= 12;
        }

        return i;
    }

    public IBlockState getStateForTree(String berryName)
    {
        return getStateFromMeta(0).withProperty(BlockLog.LOG_AXIS, EnumAxis.Y);
    }

    /** Convert the given metadata into a BlockState for this Block */
    @Override
    public IBlockState getStateFromMeta(int meta)
    {
        IBlockState state = getDefaultState();
        switch (meta & 12)
        {
        case 0:
            state = state.withProperty(LOG_AXIS, BlockLog.EnumAxis.Y);
            break;
        case 4:
            state = state.withProperty(LOG_AXIS, BlockLog.EnumAxis.X);
            break;
        case 8:
            state = state.withProperty(LOG_AXIS, BlockLog.EnumAxis.Z);
            break;
        default:
            state = state.withProperty(LOG_AXIS, BlockLog.EnumAxis.NONE);
        }
        return state;
    }
}
