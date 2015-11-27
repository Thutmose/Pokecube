package pokecube.core.blocks.berries;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.mod_Pokecube;
import pokecube.core.items.berries.BerryManager;

public class BlockBerryWood extends Block implements IMetaBlock
{
	public static final PropertyEnum VARIANT = PropertyEnum.create("variant", EnumType.class);

	public BlockBerryWood(int par1, String[] names)
	{
		super(Material.wood);
		this.setDefaultState(this.blockState.getBaseState().withProperty(VARIANT, EnumType.PECHA));
		woodType = names;
		setCreativeTab(mod_Pokecube.creativeTabPokecubeBerries);
	}

	/** The type of tree this block came from. */
	public final String[] woodType;

	@Override
	public String getUnlocalizedName(ItemStack stack)
	{
		return "tile." + EnumType.byMetadata(stack.getItemDamage()) + "Wood";
	}

	/** Get the damage value that this Block should drop */
	public int damageDropped(IBlockState state)
	{
		return ((EnumType) state.getValue(VARIANT)).getMetadata();
	}

	/** returns a list of blocks with the same ID, but different meta (eg: wood
	 * returns 4 blocks) */
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Item itemIn, CreativeTabs tab, List list)
	{
		EnumType[] aenumtype = EnumType.values();
		int i = aenumtype.length;

		for (int j = 0; j < i; ++j)
		{
			EnumType enumtype = aenumtype[j];
			list.add(new ItemStack(itemIn, 1, enumtype.getMetadata()));
		}
	}

	/** Convert the given metadata into a BlockState for this Block */
	public IBlockState getStateFromMeta(int meta)
	{
		return this.getDefaultState().withProperty(VARIANT, EnumType.byMetadata(meta));
	}

	/** Convert the BlockState into the correct metadata value */
	public int getMetaFromState(IBlockState state)
	{
		return ((EnumType) state.getValue(VARIANT)).getMetadata();
	}

	protected BlockState createBlockState()
	{
		return new BlockState(this, new IProperty[] { VARIANT });
	}

	public static enum EnumType implements IStringSerializable
	{
		PECHA(0, 3, "pecha"), ORAN(1, 7, "oran"), LEPPA(2, 6, "leppa"), SITRUS(3, 10, "sitrus"), ENIGMA(4, 60, "enigma"), NANAB(5, 18,
				"nanab");

		private static final EnumType[] META_LOOKUP = new EnumType[values().length];
		private final int meta;
		private final int berryIndex;
		private final String name;
		private final String unlocalizedName;

		private EnumType(int meta, int berryIndex, String name)
		{
			this(meta, berryIndex, name, name);
		}

		private EnumType(int meta, int berryIndex, String name, String unlocalizedName)
		{
			this.meta = meta;
			this.berryIndex = berryIndex;
			this.name = name;
			this.unlocalizedName = unlocalizedName;
		}

		public int getMetadata()
		{
			return this.meta;
		}

		public Block getBerryFruit()
		{
			return BerryManager.berryFruits.get(berryIndex);
		}
		
		public String toString()
		{
			return this.name;
		}

		public static EnumType byMetadata(int meta)
		{
			if (meta < 0 || meta >= META_LOOKUP.length)
			{
				meta = 0;
			}

			return META_LOOKUP[meta];
		}

		public String getName()
		{
			return this.name;
		}

		public String getUnlocalizedName()
		{
			return this.unlocalizedName;
		}

		static
		{
			EnumType[] var0 = values();
			int var1 = var0.length;

			for (int var2 = 0; var2 < var1; ++var2)
			{
				EnumType var3 = var0[var2];
				META_LOOKUP[var3.getMetadata()] = var3;
			}
		}
	}
}
