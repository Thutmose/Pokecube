package pokecube.core.blocks;

import java.util.UUID;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public class TileEntityOwnable extends TileEntity
{
    public UUID placer;

    public TileEntityOwnable()
    {
    }

    public void setPlacer(Entity placer)
    {
        this.placer = placer.getUniqueID();
    }

    public boolean canEdit(Entity editor)
    {
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
    public void writeToNBT(NBTTagCompound tagCompound)
    {
        super.writeToNBT(tagCompound);
        if (placer != null)
        {
            tagCompound.setBoolean("owned", true);
            tagCompound.setLong("uuidMost", placer.getMostSignificantBits());
            tagCompound.setLong("uuidLeast", placer.getLeastSignificantBits());
        }
    }
}
