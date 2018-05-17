package pokecube.core.blocks.berries;

import java.util.HashMap;

import com.google.common.collect.Maps;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TileEntityBerries extends TileEntity
{
    public static interface TreeGrower
    {
        void growTree(World world, BlockPos cropPos, int berryId);
    }

    public static enum Type
    {
        CROP, FRUIT, LOG, LEAF
    }

    public static HashMap<Integer, TreeGrower> trees      = Maps.newHashMap();

    protected Type                             type;

    private int                                berryId;
    protected int                              sleepTimer = 0;
    protected int                              stage      = 0;

    public TileEntityBerries()
    {
    }

    public TileEntityBerries(Type type)
    {
        this.type = type;
    }

    public int getBerryId()
    {
        return berryId;
    }

    @Override
    public void onLoad()
    {
        super.onLoad();
        switch (type)
        {
        case CROP:
            world.setTileEntity(getPos(), new TileEntityTickBerries(type));
            break;
        case FRUIT:
            break;
        case LEAF:
            world.setTileEntity(getPos(), new TileEntityTickBerries(type));
            break;
        case LOG:
            break;
        default:
            break;
        }
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

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt)
    {
        if (world.isRemote)
        {
            NBTTagCompound nbt = pkt.getNbtCompound();
            readFromNBT(nbt);
        }
    }

    /** Reads a tile entity from NBT. */
    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);
        berryId = nbt.getInteger("berry");
        stage = nbt.getInteger("stage");
        type = Type.valueOf(nbt.getString("type"));
    }

    public void setBerryId(int id)
    {
        berryId = id;
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate)
    {
        return oldState.getBlock() != newSate.getBlock();
    }

    /** Writes a tile entity to NBT.
     * 
     * @return */
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);
        nbt.setInteger("berry", berryId);
        nbt.setInteger("stage", stage);
        nbt.setString("type", type.toString());
        return nbt;
    }

    public void growCrop()
    {
    }
}
