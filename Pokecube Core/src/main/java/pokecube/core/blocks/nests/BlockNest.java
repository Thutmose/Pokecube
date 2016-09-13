package pokecube.core.blocks.nests;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import pokecube.core.blocks.berries.IMetaBlock;
import pokecube.core.database.Database;
import pokecube.core.items.pokemobeggs.ItemPokemobEgg;

public class BlockNest extends Block implements ITileEntityProvider, IMetaBlock
{
    public static ArrayList<String> types = new ArrayList<String>();

    static
    {
        types.add("next");
        types.add("secretPortal");
    }

    public PropertyInteger TYPE;

    public BlockNest()
    {
        super(Material.LEAVES);
        setLightOpacity(2);
        this.setDefaultState(this.blockState.getBaseState().withProperty(TYPE, Integer.valueOf(0)));
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta)
    {
        if (meta == 0) return new TileEntityNest();
        else if (meta == 1) return new TileEntityBasePortal();
        else return null;
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        if (TYPE == null)
        {
            TYPE = PropertyInteger.create("type", 0, types.size() - 1);
        }
        return new BlockStateContainer(this, new IProperty[] { TYPE });
    }

    @Override
    @Deprecated
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos)
    {
        if (pos.getY() > 0) return worldIn.getBlockState(pos.down()).getActualState(worldIn, pos.down());
        return state;
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
            EnumHand hand, ItemStack heldStack, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        TileEntity tile_entity = worldIn.getTileEntity(pos);
        if (tile_entity instanceof TileEntityNest && playerIn.capabilities.isCreativeMode
                && playerIn.getHeldItemMainhand() != null
                && playerIn.getHeldItemMainhand().getItem() instanceof ItemPokemobEgg
                && !playerIn.getEntityWorld().isRemote)
        {
            TileEntityNest nest = (TileEntityNest) tile_entity;

            nest.pokedexNb = ItemPokemobEgg.getNumber(playerIn.getHeldItemMainhand());
            playerIn.addChatComponentMessage(new TextComponentString("Set to " + Database.getEntry(nest.pokedexNb)));
            return true;
        }
        if (state.getValue(TYPE) == 1)
        {
            if (!worldIn.isRemote)
            {
                TileEntityBasePortal portal = (TileEntityBasePortal) tile_entity;
                portal.transferPlayer(playerIn);
            }
        }
        return true;
    }

    @Override
    public String getUnlocalizedName(ItemStack stack)
    {
        return super.getUnlocalizedName();
    }

    @Override
    /** Convert the BlockState into the correct metadata value */
    public int getMetaFromState(IBlockState state)
    {
        if (state.getBlock() == this) return state.getValue(TYPE).intValue();
        else return state.getBlock().getMetaFromState(state);
    }

    @Override
    /** Convert the given metadata into a BlockState for this Block */
    public IBlockState getStateFromMeta(int meta)
    {
        return this.getDefaultState().withProperty(TYPE, Integer.valueOf(meta));
    }

    @Override
    public boolean hasTileEntity(IBlockState state)
    {
        return state.getValue(TYPE).intValue() < 2;
    }
}
