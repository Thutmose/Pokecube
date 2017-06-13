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
import net.minecraftforge.event.terraingen.TerrainGen;
import pokecube.core.interfaces.PokecubeMod;
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
        if (new Random().nextInt(PokecubeMod.core.getConfig().cropGrowthTicks) == 0
                && world.getLightFromNeighbors(pos.up()) >= 9)
        {
            growCrop();
        }
    }

    private void doLeafTick()
    {
        if (new Random().nextInt(PokecubeMod.core.getConfig().leafBerryTicks) == 0
                && world.getLightFromNeighbors(pos.down()) >= 9)
        {
            if (world.getBlockState(pos).getBlock().isAir(world.getBlockState(pos), world, pos))
            {
                world.setBlockToAir(pos);
            }
            else if (world.getBlockState(pos.down()).getBlock().isAir(world.getBlockState(pos.down()), world,
                    pos.down()))
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

    public void growCrop()
    {
        stage++;
        if (stage > 7) stage = 7;
        BlockPos up = pos.up();
        if (stage == 7 && world.getBlockState(up).getBlock().isAir(world.getBlockState(up), world, up))
        {
            TreeGrower grower = null;
            if ((grower = trees.get(berryId)) != null)
            {
                if (!TerrainGen.saplingGrowTree(world, world.rand, pos)) return;
                grower.growTree(world, getPos(), berryId);
                return;
            }
            stage = 1;
            Block fruit = BerryManager.berryFruit;
            world.setBlockState(up, fruit.getDefaultState());
            TileEntityBerries tile = (TileEntityBerries) world.getTileEntity(up);
            tile.setBerryId(berryId);
        }
        world.setBlockState(pos, world.getBlockState(pos).withProperty(BlockCrops.AGE, Integer.valueOf(stage)),
                2);
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

    public void placeBerry()
    {
        Block fruit = BerryManager.berryFruit;
        world.setBlockState(pos.down(), fruit.getDefaultState());
        TileEntityBerries tile = (TileEntityBerries) world.getTileEntity(pos.down());
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
        if (world.isRemote) return;
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
