package pokecube.core.blocks;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import pokecube.core.events.handlers.SpawnHandler;

/**
 *
 * @author Manchou
 *
 */
public class TileEntityRepel extends TileEntity
{
	public byte distance = 10;
	
    public TileEntityRepel() { }
    
    @Override
    public void validate() {
    	super.validate();
    	addForbiddenSpawningCoord();
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
    }

    /**
     * Writes a tile entity to NBT.
     */
    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);
        nbt.setByte("distance", distance);
    }
    
    public boolean addForbiddenSpawningCoord(){
    	return SpawnHandler.addForbiddenSpawningCoord(pos, worldObj.provider.getDimensionId(), distance);
    }
    
    public boolean removeForbiddenSpawningCoord(){
    	return SpawnHandler.removeForbiddenSpawningCoord(pos, worldObj.provider.getDimensionId());
    }
}
