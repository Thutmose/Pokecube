package pokecube.core.blocks.repel;

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
import net.minecraft.util.text.TextComponentTranslation;
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

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
            EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        ItemStack heldStack = playerIn.getHeldItem(hand);
        TileEntity te = worldIn.getTileEntity(pos);
        if (te instanceof TileEntityRepel && heldStack != null && heldStack.getItem() instanceof ItemBerry)
        {
            ItemBerry berry = (ItemBerry) heldStack.getItem();
            TileEntityRepel repel = (TileEntityRepel) te;
            repel.invalidate();
            repel.distance = (byte) Math.max(5, berry.index);
            repel.validate();
            if (!worldIn.isRemote)
                playerIn.sendMessage(new TextComponentTranslation("repel.info.setrange", repel.distance));
        }
        if (te instanceof TileEntityRepel && heldStack != null && heldStack.getItem() instanceof ItemPokedex
                && !worldIn.isRemote)
        {
            TileEntityRepel repel = (TileEntityRepel) te;
            playerIn.sendMessage(new TextComponentTranslation("repel.info.getrange", repel.distance));
        }
        return true;
    }

    /** Here we toggle the repel on/off */
    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos)
    {
        int power = worldIn.isBlockIndirectlyGettingPowered(pos);
        TileEntity tile = worldIn.getTileEntity(pos);
        if (tile == null || !(tile instanceof TileEntityRepel)) return;
        TileEntityRepel repel = (TileEntityRepel) tile;
        if (power != 0)
        {
            repel.enabled = false;
            repel.removeForbiddenSpawningCoord();
        }
        else
        {
            repel.enabled = true;
            repel.addForbiddenSpawningCoord();
        }
    }
}
