package pokecube.core.blocks;

import java.util.UUID;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import thut.api.block.IOwnableTE;

public class TileEntityOwnable extends TileEntity implements IOwnableTE
{
    public UUID placer;

    public TileEntityOwnable()
    {
    }

    @Override
    public boolean canEdit(Entity editor)
    {
        if (editor instanceof EntityPlayer && ((EntityPlayer) editor).capabilities.isCreativeMode) return true;
        if (placer == null || placer.compareTo(editor.getUniqueID()) != 0) return false;
        return true;
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound)
    {
        super.readFromNBT(tagCompound);
        if (tagCompound.getBoolean("owned"))
        {
            placer = new UUID(tagCompound.getLong("uuidMost"), tagCompound.getLong("uuidLeast"));
        }
    }

    @Override
    public void setPlacer(Entity placer)
    {
        this.placer = placer.getUniqueID();
    }

    /** Called from Chunk.setBlockIDWithMetadata and Chunk.fillChunk, determines
     * if this tile entity should be re-created when the ID, or Metadata
     * changes. Use with caution as this will leave straggler TileEntities, or
     * create conflicts with other TileEntities if not used properly.
     *
     * @param world
     *            Current world
     * @param pos
     *            Tile's world position
     * @param oldState
     *            The old ID of the block
     * @param newState
     *            The new ID of the block (May be the same)
     * @return true forcing the invalidation of the existing TE, false not to
     *         invalidate the existing TE */
    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate)
    {
        return oldState.getBlock() != newSate.getBlock();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound)
    {
        super.writeToNBT(tagCompound);
        if (placer != null)
        {
            tagCompound.setBoolean("owned", true);
            tagCompound.setLong("uuidMost", placer.getMostSignificantBits());
            tagCompound.setLong("uuidLeast", placer.getLeastSignificantBits());
        }
        return tagCompound;
    }

    public boolean shouldBreak()
    {
        return true;
    }
}
