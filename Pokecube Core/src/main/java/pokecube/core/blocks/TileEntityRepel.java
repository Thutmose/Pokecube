package pokecube.core.blocks;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import pokecube.core.events.handlers.SpawnHandler;

/**
 *
 * @author Manchou
 *
 */
public class TileEntityRepel extends TileEntity implements ITickable
{
	public byte distance = 10;
    boolean     enabled  = true;
	
    public TileEntityRepel() { }
    
    public boolean addForbiddenSpawningCoord(){
    	return SpawnHandler.addForbiddenSpawningCoord(pos, worldObj.provider.getDimension(), distance);
    }
    
    @Override
    public void invalidate() {
    	super.invalidate();
    	removeForbiddenSpawningCoord();
    }

    /**
     * Reads a tile entity from NBT.
     */
    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);
        distance = nbt.getByte("distance");
        enabled = nbt.getBoolean("enabled");
    }

    public boolean removeForbiddenSpawningCoord(){
    	return SpawnHandler.removeForbiddenSpawningCoord(pos, worldObj.provider.getDimension());
    }
    
    @Override
    public void update()
    {
        if (worldObj.isRemote) return;
        int power = worldObj.getStrongPower(getPos());
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
    public void validate() {
    	super.validate();
    	addForbiddenSpawningCoord();
    }

    /**
     * Writes a tile entity to NBT.
     */
    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);
        nbt.setByte("distance", distance);
        nbt.setBoolean("enabled", enabled);
    }
}
