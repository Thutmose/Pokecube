package pokecube.core.blocks.berries;

import java.util.HashMap;
import java.util.Random;

import com.google.common.collect.Maps;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import pokecube.core.items.berries.BerryManager;

public class TileEntityBerries extends TileEntity implements ITickable
{
    public static interface TreeGrower
    {
        void growTree(World world, BlockPos cropPos, int berryId);
    }

    public static enum Type
    {
        CROP, FRUIT, LOG, LEAF
    }

    public static HashMap<Integer, TreeGrower> trees = Maps.newHashMap();

    private Type                               type;

    private int                                berryId;
    private int                                stage = 0;

    public TileEntityBerries()
    {
    }

    public TileEntityBerries(Type type)
    {
        this.type = type;
    }

    private void doCropTick()
    {
        if (worldObj.getLightFromNeighbors(pos.up()) >= 9)
        {
            int i = worldObj.getBlockState(pos).getValue(BlockCrops.AGE).intValue();

            if (i <= 7)
            {
                if (new Random().nextInt(2500) == 0)
                {
                    growCrop();
                }
            }
        }
    }

    private void doLeafTick()
    {
        if (worldObj.getLightFromNeighbors(pos.down()) >= 9)
        {
            if (new Random().nextInt(5000) == 0
                    && worldObj.getBlockState(pos.down()).getBlock().isAir(worldObj.getBlockState(pos.down()),worldObj, pos.down()))
            {
                placeBerry();
            }
        }
    }

    public int getBerryId()
    {
        return berryId;
    }

    /** Overriden in a sign to provide the text. */
    @Override
    public SPacketUpdateTileEntity getUpdatePacket()
    {
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        if (worldObj.isRemote) return new SPacketUpdateTileEntity(this.getPos(), 3, nbttagcompound);
        this.writeToNBT(nbttagcompound);
        return new SPacketUpdateTileEntity(this.getPos(), 3, nbttagcompound);
    }

    public void growCrop()
    {
        stage++;
        if (stage > 7) stage = 7;
        if (stage == 7 && worldObj.getBlockState(pos.up()).getBlock().isAir(worldObj.getBlockState(pos.up()),worldObj, pos.up()))
        {
            TreeGrower grower = null;
            if ((grower = trees.get(berryId)) != null)
            {
                grower.growTree(worldObj, getPos(), berryId);
                return;
            }
            else
            {
                stage = 1;
                Block fruit = BerryManager.berryFruit;
                worldObj.setBlockState(pos.up(), fruit.getDefaultState());
                TileEntityBerries tile = (TileEntityBerries) worldObj.getTileEntity(pos.up());
                tile.setBerryId(berryId);
            }
        }
        worldObj.setBlockState(pos, worldObj.getBlockState(pos).withProperty(BlockCrops.AGE, Integer.valueOf(stage)),
                2);
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt)
    {
        if (worldObj.isRemote)
        {
            NBTTagCompound nbt = pkt.getNbtCompound();
            readFromNBT(nbt);
        }
    }

    public void placeBerry()
    {
        Block fruit = BerryManager.berryFruit;
        worldObj.setBlockState(pos.down(), fruit.getDefaultState());
        TileEntityBerries tile = (TileEntityBerries) worldObj.getTileEntity(pos.down());
        if (tile != null) tile.setBerryId(berryId);
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

    @Override
    public void update()
    {
        if (worldObj.isRemote) return;
        switch (type)
        {
        case CROP:
            doCropTick();
            break;
        case FRUIT:

            break;
        case LEAF:
            doLeafTick();
            break;
        case LOG:

            break;

        default:
            break;
        }
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
}
