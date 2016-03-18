package pokecube.core.blocks.berries;

import java.util.List;

import com.google.common.base.Predicate;

import net.minecraft.block.BlockLog;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.blocks.berries.BlockBerryWood.EnumType;
import pokecube.core.interfaces.PokecubeMod;

public class BlockBerryLog extends BlockLog implements IMetaBlock
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
	public static final PropertyEnum<BlockBerryWood.EnumType>	VARIANT4				= PropertyEnum.create("variant",
			BlockBerryWood.EnumType.class, new Predicate<BlockBerryWood.EnumType>()
			{
				@Override
                public boolean apply(BlockBerryWood.EnumType type)
				{
					return type.getMetadata() >= 4;
				}
			});
	public static final PropertyEnum<BlockBerryWood.EnumType>	VARIANT0				= PropertyEnum.create("variant",
			BlockBerryWood.EnumType.class, new Predicate<BlockBerryWood.EnumType>()
			{
				@Override
                public boolean apply(BlockBerryWood.EnumType type)
				{
					return type.getMetadata() < 4;
				}
			});
	public static int					currentlyConstructing	= 0;
	public final String[]				woodType;

	public final int					shift;

	/** @param logShift
	 *            0 or 4, depending on first or second one.
	 * @param names */
	public BlockBerryLog(int logShift, String[] names)
	{
		super();
		woodType = names;
		BlockBerryCrop.logs.add(this);
		shift = logShift;

		IBlockState state = this.blockState.getBaseState();
		EnumType type = EnumType.byMetadata(logShift);
		PropertyEnum<EnumType> property = shift == 0 ? VARIANT0 : VARIANT4;
		state = state.withProperty(property, type);
		this.setDefaultState(state.withProperty(LOG_AXIS, BlockLog.EnumAxis.Y));
		setCreativeTab(PokecubeMod.creativeTabPokecubeBerries);
	}

	@Override
    protected BlockStateContainer createBlockState()
	{
		return new BlockStateContainer(this, new IProperty[] { currentlyConstructing == 0 ? VARIANT0 : VARIANT4, LOG_AXIS });
	}

	@Override
    protected ItemStack createStackedBlock(IBlockState state)
	{
		return new ItemStack(Item.getItemFromBlock(this), 1,
				state.getValue(shift == 0 ? VARIANT0 : VARIANT4).getMetadata() - shift);
	}

	/** Get the damage value that this Block should drop */
	@Override
    public int damageDropped(IBlockState state)
	{
		return state.getValue(shift == 0 ? VARIANT0 : VARIANT4).getMetadata() - shift;
	}

	@Override
    public int getMetaFromState(IBlockState state)
	{
		byte b0 = 0;
		int i = b0 | state.getValue(shift == 0 ? VARIANT0 : VARIANT4).getMetadata() - shift;

		switch (SwitchEnumAxis.AXIS_LOOKUP[state.getValue(LOG_AXIS).ordinal()])
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
		BlockBerryWood.EnumType type = BlockBerryWood.EnumType.valueOf(berryName.toUpperCase());
		
		if(type!=null)
		{
			int num = type.getMetadata() - shift;
			if(num >= 0 && num < 4)
			{
				return getStateFromMeta(num+shift).withProperty(LOG_AXIS, BlockLog.EnumAxis.Y);
			}
		}
		return null;
	}
	
    /**
     * Convert the given metadata into a BlockState for this Block
     */
    @Override
    public IBlockState getStateFromMeta(int meta)
    {
    	IBlockState state = super.getStateFromMeta(meta);
    	IProperty<EnumType> property = (shift == 0 ? VARIANT0 : VARIANT4);
        return state.withProperty(property, EnumType.byMetadata((meta & 3) + shift));
    }

	@Override
    @SideOnly(Side.CLIENT)

	/** returns a list of blocks with the same ID, but different meta (eg: wood
	 * returns 4 blocks) */
	public void getSubBlocks(Item par1, CreativeTabs par2CreativeTabs, List<ItemStack> par3List)
	{
		for (int i = 0; i < woodType.length; i++)
		{
			par3List.add(new ItemStack(par1, 1, i));
		}
	}

	@Override
	public String getUnlocalizedName(ItemStack stack)
	{
		return "tile." + woodType[stack.getItemDamage() % woodType.length] + "Log";
	}
}
