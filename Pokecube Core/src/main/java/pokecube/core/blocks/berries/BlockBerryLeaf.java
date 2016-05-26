package pokecube.core.blocks.berries;

import java.util.List;
import java.util.Random;

import com.google.common.collect.Lists;

import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockPlanks.EnumType;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.blocks.berries.TileEntityBerries.Type;
import pokecube.core.items.berries.BerryManager;

public class BlockBerryLeaf extends BlockLeaves implements ITileEntityProvider
{
    public BlockBerryLeaf()
    {
        super();
        // setCreativeTab(PokecubeMod.creativeTabPokecubeBerries);
        this.setDefaultState(this.blockState.getBaseState().withProperty(BerryManager.type, "cheri")
                .withProperty(CHECK_DECAY, Boolean.valueOf(true)).withProperty(DECAYABLE, Boolean.valueOf(true)));
    }

    @Override
    protected BlockState createBlockState()
    {
        return new BlockState(this, new IProperty[] { BerryManager.type, CHECK_DECAY, DECAYABLE });
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta)
    {
        return new TileEntityBerries(Type.LEAF);
    }

    @Override
    public EnumType getWoodType(int meta)
    {
        return EnumType.OAK;
    }

    @Override
    /** Get the Item that this Block should drop when harvested. */
    public Item getItemDropped(IBlockState state, Random rand, int fortune)
    {
        return null;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public EnumWorldBlockLayer getBlockLayer()
    {
        return EnumWorldBlockLayer.CUTOUT_MIPPED;
    }

    @Override
    public boolean isVisuallyOpaque()
    {
        return false;
    }

    @Override
    public List<ItemStack> onSheared(ItemStack item, IBlockAccess world, BlockPos pos, int fortune)
    {
        List<ItemStack> ret = Lists.newArrayList();
        TileEntityBerries tile = (TileEntityBerries) world.getTileEntity(pos);
        String berry = BerryManager.berryNames.get(tile.getBerryId());
        ItemStack stack = BerryManager.getBerryItem(berry);
        ret.add(stack);
        return ret;
    }

    @Override
    public java.util.List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune)
    {
        java.util.List<ItemStack> ret = new java.util.ArrayList<ItemStack>();
        Random rand = world instanceof World ? ((World) world).rand : new Random();
        int chance = this.getSaplingDropChance(state);
        TileEntityBerries tile = (TileEntityBerries) world.getTileEntity(pos);
        String berry = BerryManager.berryNames.get(tile.getBerryId());
        if (fortune > 0)
        {
            chance -= 2 << fortune;
            if (chance < 10) chance = 10;
        }

        if (rand.nextInt(chance) == 0) ret.add(BerryManager.getBerryItem(berry));

        chance = 200;
        if (fortune > 0)
        {
            chance -= 10 << fortune;
            if (chance < 40) chance = 40;
        }

        this.captureDrops(true);
        ret.addAll(this.captureDrops(false));
        return ret;
    }

    @Override
    /** Called when a user uses the creative pick block button on this block
     *
     * @param target
     *            The full target the player is looking at
     * @return A ItemStack to add to the player's inventory, Null if nothing
     *         should be added. */
    public ItemStack getPickBlock(MovingObjectPosition target, World world, BlockPos pos, EntityPlayer player)
    {
        TileEntityBerries tile = (TileEntityBerries) world.getTileEntity(pos);
        return BerryManager.getBerryItem(tile.getBerryId());
    }

    @Override
    /** Get the actual Block state of this Block at the given position. This
     * applies properties not visible in the metadata, such as fence
     * connections. */
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos)
    {
        return Blocks.leaves.getDefaultState();
    }

    @Override
    /** Convert the BlockState into the correct metadata value */
    public int getMetaFromState(IBlockState state)
    {
        int i = 0;

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

    @Override
    /** Convert the given metadata into a BlockState for this Block */
    public IBlockState getStateFromMeta(int meta)
    {
        return this.getDefaultState().withProperty(DECAYABLE, Boolean.valueOf((meta & 4) == 0))
                .withProperty(CHECK_DECAY, Boolean.valueOf((meta & 8) > 0));
    }

}
