package pokecube.core.blocks.repel;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import pokecube.core.events.handlers.SpawnHandler;

/** @author Manchou */
public class TileEntityRepel extends TileEntity implements ITickable
{
    public byte distance = 10;
    boolean     enabled  = true;

    public TileEntityRepel()
    {
    }

    public boolean addForbiddenSpawningCoord()
    {
        if (getWorld().isRemote) return false;
        return SpawnHandler.addForbiddenSpawningCoord(pos, getWorld().provider.getDimension(), distance);
    }

    @Override
    public void invalidate()
    {
        super.invalidate();
        removeForbiddenSpawningCoord();
    }

    /** Reads a tile entity from NBT. */
    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);
        distance = nbt.getByte("distance");
        enabled = nbt.getBoolean("enabled");
    }

    @Override
    public void onLoad()
    {
        removeForbiddenSpawningCoord();
        if (enabled) addForbiddenSpawningCoord();
    }

    public boolean removeForbiddenSpawningCoord()
    {
        if (getWorld().isRemote) return false;
        return SpawnHandler.removeForbiddenSpawningCoord(pos, getWorld().provider.getDimension());
    }

    @Override
    public void update()
    {
        if (getWorld().isRemote) return;
        int power = getWorld().getStrongPower(getPos());
        if (power != 0 && enabled)
        {
            enabled = false;
            removeForbiddenSpawningCoord();
        }
        else if (power == 0 && !enabled)
        {
            enabled = true;
            addForbiddenSpawningCoord();
        }
    }

    @Override
    public void validate()
    {
        super.validate();
        addForbiddenSpawningCoord();
        enabled = true;
    }

    /** Writes a tile entity to NBT.
     * 
     * @return */
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);
        nbt.setByte("distance", distance);
        nbt.setBoolean("enabled", enabled);
        return nbt;
    }
}
