package pokecube.core.blocks.nests;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.ColorizerGrass;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.database.Database;
import pokecube.core.items.pokemobeggs.ItemPokemobEgg;

public class BlockNest extends Block implements ITileEntityProvider
{

    public BlockNest()
    {
        super(Material.leaves);
        setLightOpacity(2);
    }

    @Override
    public boolean canRenderInLayer(EnumWorldBlockLayer layer)
    {
        return true;
    }

    @SideOnly(Side.CLIENT)
    public int colorMultiplier(IBlockAccess worldIn, BlockPos pos, int renderPass)
    {
        IBlockState state2 = worldIn.getBlockState(pos.down());
        if (state2 != null && state2.getBlock().getMaterial().isSolid() && state2.getBlock()
                .isNormalCube()) { return state2.getBlock().colorMultiplier(worldIn, pos, renderPass); }
        return 16777215;
    }

    @Override
    public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_)
    {
        return new TileEntityNest();
    }

    @Override
    /** Get the actual Block state of this Block at the given position. This
     * applies properties not visible in the metadata, such as fence
     * connections. */
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos)
    {
        IBlockState state2 = worldIn.getBlockState(pos.down());
        if (state2 != null && state2.getBlock().getMaterial().isSolid() && state2.getBlock().isNormalCube())
        {
            state2 = state2.getBlock().getActualState(state2, worldIn, pos);
            return state2;
        }
        return state;
    }

    @Override
    public int getBlockColor()
    {
        double d = 0.5D;
        double d1 = 1.0D;
        return ColorizerGrass.getGrassColor(d, d1);
    }

    @SideOnly(Side.CLIENT)
    public int getRenderColor(IBlockState state)
    {
        return 16777215;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side,
            float f, float g, float t)
    {

        TileEntity tile_entity = world.getTileEntity(pos);
        if (tile_entity instanceof TileEntityNest && player.capabilities.isCreativeMode && player.getHeldItem() != null
                && player.getHeldItem().getItem() instanceof ItemPokemobEgg && !player.worldObj.isRemote)
        {
            TileEntityNest nest = (TileEntityNest) tile_entity;

            nest.pokedexNb = ItemPokemobEgg.getNumber(player.getHeldItem());
            player.addChatComponentMessage(new ChatComponentText("Set to " + Database.getEntry(nest.pokedexNb)));
            return true;
        }
        return false;
    }
}
