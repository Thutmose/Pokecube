package pokecube.core.blocks.repel;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import pokecube.core.items.ItemPokedex;
import pokecube.core.items.berries.ItemBerry;

/** @author Manchou */
public class BlockRepel extends Block implements ITileEntityProvider
{
    public BlockRepel()
    {
        this(Material.CLOTH);
    }

    public BlockRepel(Material material)
    {
        super(material);
    }

    @Override
    public TileEntity createNewTileEntity(World var1, int var2)
    {
        TileEntityRepel tile = new TileEntityRepel();
        return tile;
    }

    /** Is this block (a) opaque and (b) a full 1m cube? This determines whether
     * or not to render the shared face of two adjacent blocks and also whether
     * the player can attach torches, redstone wire, etc to this block. */
    @Override
    public boolean isOpaqueCube(IBlockState state)
    {
        return true;
    }

    // 1.11
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
            EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        return onBlockActivated(worldIn, pos, state, playerIn, hand, playerIn.getHeldItem(hand), side, hitX, hitY,
                hitZ);
    }

    // 1.10
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
            EnumHand hand, ItemStack heldStack, EnumFacing side, float hitX, float hitY, float hitZ)
    {

        TileEntity te = worldIn.getTileEntity(pos);
        if (te instanceof TileEntityRepel && heldStack != null && heldStack.getItem() instanceof ItemBerry)
        {
            TileEntityRepel repel = (TileEntityRepel) te;
            repel.invalidate();
            repel.distance = (byte) Math.max(5, heldStack.getItemDamage());
            repel.validate();
            playerIn.addChatMessage(new TextComponentString("" + repel.distance));
        }
        if (te instanceof TileEntityRepel && heldStack != null && heldStack.getItem() instanceof ItemPokedex
                && !worldIn.isRemote)
        {
            TileEntityRepel repel = (TileEntityRepel) te;
            playerIn.addChatMessage(new TextComponentString("" + repel.distance));
        }

        return true;
    }

    /** Returns the quantity of items to drop on block destruction. */
    @Override
    public int quantityDropped(Random par1Random)
    {
        return 1;
    }

    /** Called when a neighboring block was changed and marks that this state
     * should perform any checks during a neighbor change. Cases may include
     * when redstone power is updated, cactus blocks popping off due to a
     * neighboring solid block, etc. */
    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn)
    {
        int power = worldIn.isBlockIndirectlyGettingPowered(pos);
        TileEntity tile = worldIn.getTileEntity(pos);
        if (tile == null || !(tile instanceof TileEntityRepel)) return;
        TileEntityRepel repel = (TileEntityRepel) tile;
        boolean toggled;
        if (power != 0)
        {
            repel.enabled = false;
            toggled = repel.removeForbiddenSpawningCoord();
        }
        else
        {
            repel.enabled = true;
            toggled = repel.addForbiddenSpawningCoord();
        }
        System.out.println(repel.enabled + " " + toggled);
    }
}
