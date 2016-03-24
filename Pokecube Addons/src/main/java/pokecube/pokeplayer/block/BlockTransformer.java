package pokecube.pokeplayer.block;

import net.minecraft.block.BlockPressurePlate;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import pokecube.pokeplayer.tileentity.TileEntityTransformer;

public class BlockTransformer extends BlockPressurePlate implements ITileEntityProvider
{
    public BlockTransformer()
    {
        super(Material.iron, Sensitivity.MOBS);
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta)
    {
        return new TileEntityTransformer();
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side,
            float f, float g, float h)
    {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEntityTransformer)
        {
            ((TileEntityTransformer) tile).onInteract(player);
        }
        return true;
    }

    /** Called When an Entity Collided with the Block */
    @Override
    public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, IBlockState state, Entity entityIn)
    {
        TileEntity tile = worldIn.getTileEntity(pos);
        if (tile instanceof TileEntityTransformer && entityIn instanceof EntityPlayer)
        {
            ((TileEntityTransformer) tile).onStepped((EntityPlayer) entityIn);
        }
        super.onEntityCollidedWithBlock(worldIn, pos, state, entityIn);
    }

    @Override
    protected void updateState(World worldIn, BlockPos pos, IBlockState state, int oldRedstoneStrength)
    {
        TileEntity before = worldIn.getTileEntity(pos);
        NBTTagCompound tag = new NBTTagCompound();
        if (before != null)
        {
            before.writeToNBT(tag);
        }
        super.updateState(worldIn, pos, state, oldRedstoneStrength);
        TileEntity tile = worldIn.getTileEntity(pos);
        if (before != null && tile != null)
        {
            tile.readFromNBT(tag);
        }
    }
}
