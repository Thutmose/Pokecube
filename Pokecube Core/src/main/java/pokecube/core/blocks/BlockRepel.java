package pokecube.core.blocks;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import pokecube.core.items.ItemPokedex;
import pokecube.core.items.berries.ItemBerry;

/**
 * @author Manchou
 */
public class BlockRepel extends Block implements ITileEntityProvider
{
    public BlockRepel()
    {
        this(Material.cloth);
    }

    public BlockRepel(Material material)
    {
        super(material);
    }
    
	@Override
	public TileEntity createNewTileEntity(World var1, int var2) {
        TileEntityRepel tile = new TileEntityRepel();
		return tile;
	}
	
    /**
     * Is this block (a) opaque and (b) a full 1m cube?  This determines whether or not to render the shared face of two
     * adjacent blocks and also whether the player can attach torches, redstone wire, etc to this block.
     */
    @Override
    public boolean isOpaqueCube()
    {
        return true;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state,
            EntityPlayer player, EnumFacing side, float f, float g, float t)
    {
    	
    	TileEntity te = world.getTileEntity(pos);
    	if(te instanceof TileEntityRepel && player.getHeldItemMainhand()!=null && player.getHeldItemMainhand().getItem() instanceof ItemBerry)
    	{
    		TileEntityRepel repel = (TileEntityRepel) te;
    		repel.invalidate();
    		repel.distance = (byte) Math.max(5, player.getHeldItemMainhand().getItemDamage());
    		repel.validate();
    	}
    	if(te instanceof TileEntityRepel && player.getHeldItemMainhand() != null && player.getHeldItemMainhand().getItem() instanceof ItemPokedex && !world.isRemote)
    	{
    		TileEntityRepel repel = (TileEntityRepel) te;
    		player.addChatMessage(new TextComponentString(""+repel.distance));
    	}
    	
    	return true;
    }
    /**
     * Returns the quantity of items to drop on block destruction.
     */
    @Override
    public int quantityDropped(Random par1Random)
    {
        return 1;
    }
}
