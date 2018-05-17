package pokecube.core.blocks.berries;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.terraingen.TerrainGen;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.berries.BerryManager;

public class TileEntityTickBerries extends TileEntityBerries implements ITickable
{

    public TileEntityTickBerries()
    {
    }

    public TileEntityTickBerries(Type type)
    {
        super(type);
    }

    private void doCropTick()
    {
        if (PokecubeMod.core.getConfig().cropGrowthTicks < 1) return;
        if (new Random().nextInt(PokecubeMod.core.getConfig().cropGrowthTicks) == 0
                && world.getLightFromNeighbors(pos.up()) >= 9)
        {
            growCrop();
        }
    }

    private void doLeafTick()
    {
        if (PokecubeMod.core.getConfig().leafBerryTicks < 1) return;
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
            else
            {
                if (!(world.getBlockState(pos.down()).getBlock() instanceof BlockBerryFruit))
                {
                    sleepTimer = 6000;
                }
                sleepTimer = 600;
            }
        }
    }

    public void growCrop()
    {
        BlockPos up = pos.up();
        if (!(world.getBlockState(up).getBlock().isAir(world.getBlockState(up), world, up)))
        {
            if (!(world.getBlockState(pos.up()).getBlock() instanceof BlockBerryFruit))
            {
                sleepTimer = 6000;
            }
            sleepTimer = 600;
            return;
        }
        stage++;
        if (stage > 7) stage = 7;
        if (stage == 7)
        {
            TreeGrower grower = null;
            if ((grower = trees.get(getBerryId())) != null)
            {
                if (!TerrainGen.saplingGrowTree(world, world.rand, pos)) return;
                grower.growTree(world, getPos(), getBerryId());
                return;
            }
            stage = 1;
            Block fruit = BerryManager.berryFruit;
            world.setBlockState(up, fruit.getDefaultState());
            TileEntityBerries tile = (TileEntityBerries) world.getTileEntity(up);
            tile.setBerryId(getBerryId());
        }
        world.setBlockState(pos, world.getBlockState(pos).withProperty(BlockCrops.AGE, Integer.valueOf(stage)), 2);
    }

    public void placeBerry()
    {
        Block fruit = BerryManager.berryFruit;
        world.setBlockState(pos.down(), fruit.getDefaultState());
        TileEntityBerries tile = (TileEntityBerries) world.getTileEntity(pos.down());
        if (tile != null) tile.setBerryId(getBerryId());
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate)
    {
        return oldState.getBlock() != newSate.getBlock();
    }

    @Override
    public void onLoad()
    {
        switch (type)
        {
        case CROP:
            break;
        case FRUIT:
            world.setTileEntity(getPos(), new TileEntityBerries(type));
            break;
        case LEAF:
            break;
        case LOG:
            world.setTileEntity(getPos(), new TileEntityBerries(type));
            break;
        default:
            break;
        }
    }

    @Override
    public void update()
    {
        if (world.isRemote) return;
        long time = System.nanoTime();
        tick:
        {
            if (sleepTimer-- > 0) break tick;
            switch (type)
            {
            case CROP:
                doCropTick();
                break;
            case FRUIT:
                sleepTimer = Integer.MAX_VALUE;
                break;
            case LEAF:
                doLeafTick();
                break;
            case LOG:
                sleepTimer = Integer.MAX_VALUE;
                break;
            default:
                break;
            }
        }
        if (PokecubeMod.debug)
        {
            double diff = (System.nanoTime() - time) / 1000d;
            if (diff > 40) PokecubeMod.log(diff + " " + type + " " + pos);
        }
    }
}
