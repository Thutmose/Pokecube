package pokecube.core.blocks.berries;

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
    public static enum Type
    {
        CROP, FRUIT, LOG, LEAF
    }

    private Type type;

    private int  berryId;
    private int  stage = 0;

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
                if (worldObj.rand.nextInt((int) (50.0F) + 1) == 0)
                {
                    growCrop();
                }
            }
        }
    }

    public void growCrop()
    {
        stage++;
        if (stage > 7) stage = 7;
        IBlockState state = worldObj.getBlockState(pos.up());
        if (stage == 7 && state.getBlock().isAir(state, worldObj, pos.up()))
        {
            stage = 1;
            Block fruit = BerryManager.berryFruit;
            worldObj.setBlockState(pos.up(), fruit.getDefaultState());
            TileEntityBerries tile = (TileEntityBerries) worldObj.getTileEntity(pos.up());
            tile.setBerryId(berryId);
        }
        worldObj.setBlockState(pos, worldObj.getBlockState(pos).withProperty(BlockCrops.AGE, Integer.valueOf(stage)),
                2);
    }

    public void growTree()
    {

    }

    public void placeBerry()
    {

    }

    private void doLeafTick()
    {

    }

    public boolean isTree()
    {
        return false;
    }

    public int getBerryId()
    {
        return berryId;
    }

    public void setBerryId(int id)
    {
        berryId = id;
        System.out.println("id set to " + id);
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

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate)
    {
        return oldState.getBlock() != newSate.getBlock();
    }

    /** Writes a tile entity to NBT. 
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

    /** Overriden in a sign to provide the text. */
    @Override
    public SPacketUpdateTileEntity getUpdatePacket()
    {
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        if (worldObj.isRemote) return new SPacketUpdateTileEntity(this.getPos(), 3, nbttagcompound);
        this.writeToNBT(nbttagcompound);
        return new SPacketUpdateTileEntity(this.getPos(), 3, nbttagcompound);
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
}
