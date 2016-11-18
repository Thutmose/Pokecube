package pokecube.adventures.blocks.warppad;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import pokecube.core.interfaces.PokecubeMod;
import thut.api.maths.Vector3;

public class BlockWarpPad extends Block implements ITileEntityProvider
{

    public BlockWarpPad()
    {
        super(Material.CLOTH);
        this.setHardness(10);
        this.setCreativeTab(PokecubeMod.creativeTabPokecube);
    }

    @Override
    public TileEntity createNewTileEntity(World world_, int meta)
    {
        return new TileEntityWarpPad();
    }

    /** Called when the block is placed in the world. */
    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY,
            float hitZ, int meta, EntityLivingBase placer, EnumHand hand)
    {
        Vector3 loc = Vector3.getNewVector().set(pos);
        TileEntity te = loc.getTileEntity(world);
        if (te != null && te instanceof TileEntityWarpPad)
        {
            TileEntityWarpPad pad = (TileEntityWarpPad) te;
            pad.setPlacer(placer);
        }
        return super.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, placer, hand);
    }

    @Override
    /** Called whenever an entity is walking on top of this block. Args: world,
     * x, y, z, entity */
    public void onEntityWalk(World world, BlockPos pos, Entity entity)
    {
        TileEntity te = world.getTileEntity(pos);
        if (te != null && te instanceof TileEntityWarpPad)
        {
            TileEntityWarpPad pad = (TileEntityWarpPad) world.getTileEntity(pos);
            if (!world.isRemote) pad.onStepped(entity);
        }
    }

}
