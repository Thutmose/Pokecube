package pokecube.core.blocks.repel;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
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
        return SpawnHandler.addForbiddenSpawningCoord(pos, world.provider.getDimension(), distance);
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

    public boolean removeForbiddenSpawningCoord()
    {
        return SpawnHandler.removeForbiddenSpawningCoord(pos, world.provider.getDimension());
    }

    @Override
    public void update()
    {
        if (world.isRemote) return;
        int power = world.getStrongPower(getPos());
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

    /** Overriden in a sign to provide the text. */
    @Override
    public SPacketUpdateTileEntity getUpdatePacket()
    {
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        if (world.isRemote) return new SPacketUpdateTileEntity(this.getPos(), 3, nbttagcompound);
        this.writeToNBT(nbttagcompound);
        return new SPacketUpdateTileEntity(this.getPos(), 3, nbttagcompound);
    }

    @Override
    public NBTTagCompound getUpdateTag()
    {
        NBTTagCompound nbt = new NBTTagCompound();
        return writeToNBT(nbt);
    }

    /** Called when you receive a TileEntityData packet for the location this
     * TileEntity is currently in. On the client, the NetworkManager will always
     * be the remote server. On the server, it will be whomever is responsible
     * for sending the packet.
     *
     * @param net
     *            The NetworkManager the packet originated from
     * @param pkt
     *            The data packet */
    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt)
    {
        if (world.isRemote)
        {
            NBTTagCompound nbt = pkt.getNbtCompound();
            readFromNBT(nbt);
        }
    }
}
