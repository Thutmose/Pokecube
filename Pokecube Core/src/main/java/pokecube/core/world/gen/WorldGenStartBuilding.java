package pokecube.core.world.gen;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCarpet;
import net.minecraft.block.BlockColored;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.BlockOldLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemDoor;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkGenerator;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.fml.common.IWorldGenerator;
import pokecube.core.PokecubeItems;
import pokecube.core.blocks.pc.BlockPC;
import pokecube.core.interfaces.PokecubeMod;
import thut.api.maths.Vector3;

public class WorldGenStartBuilding implements IWorldGenerator
{

    public static boolean building = false;

    public static void fillWithBlocks(World world, Vector3 centre, Vector3 lower, Vector3 upper, IBlockState state)
    {
        int x, y, z;
        Vector3 temp = Vector3.getNewVector();
        for (x = lower.intX(); x <= upper.x; x++)
            for (y = lower.intY(); y <= upper.y; y++)
                for (z = lower.intZ(); z <= upper.z; z++)
                {
                    temp.set(centre).addTo(x, y, z).setBlock(world, state);
                }
    }

    public static void makePokecenter(Vector3 centre, World world)
    {
        Vector3 temp1 = Vector3.getNewVector();
        Vector3 temp2 = Vector3.getNewVector();

        IBlockState spruce = Blocks.PLANKS.getDefaultState().withProperty(BlockPlanks.VARIANT,
                BlockPlanks.EnumType.SPRUCE);
        IBlockState clay = Blocks.STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockColored.COLOR,
                EnumDyeColor.RED);
        IBlockState glass = Blocks.GLASS_PANE.getDefaultState();
        IBlockState logs = Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.SPRUCE);
        IBlockState redCarpet = Blocks.CARPET.getDefaultState().withProperty(BlockCarpet.COLOR, EnumDyeColor.RED);
        IBlockState whiteCarpet = Blocks.CARPET.getDefaultState().withProperty(BlockCarpet.COLOR, EnumDyeColor.WHITE);
        IBlockState blackCarpet = Blocks.CARPET.getDefaultState().withProperty(BlockCarpet.COLOR, EnumDyeColor.BLACK);
        IBlockState greyCarpet = Blocks.CARPET.getDefaultState().withProperty(BlockCarpet.COLOR, EnumDyeColor.GRAY);
        // Hollow it out
        fillWithBlocks(world, centre, temp1.set(0, 0, 0), temp2.set(8, 8, 8), Blocks.AIR.getDefaultState());

        // Roof
        fillWithBlocks(world, centre, temp1.set(0, 6, 0), temp2.set(8, 6, 8), clay);
        fillWithBlocks(world, centre, temp1.set(1, 6, 1), temp2.set(7, 7, 7), clay);
        fillWithBlocks(world, centre, temp1.set(2, 6, 2), temp2.set(6, 8, 6), clay);
        fillWithBlocks(world, centre, temp1.set(1, 6, 1), temp2.set(7, 6, 7), spruce);

        // Floor
        fillWithBlocks(world, centre, temp1.set(0, 0, 0), temp2.set(8, 2, 8), Blocks.COBBLESTONE.getDefaultState());
        fillWithBlocks(world, centre, temp1.set(1, 2, 1), temp2.set(7, 2, 7), spruce);

        // Walls
        fillWithBlocks(world, centre, temp1.set(8, 3, 0), temp2.set(8, 5, 8), spruce);// RIGHT
        fillWithBlocks(world, centre, temp1.set(0, 3, 8), temp2.set(8, 5, 8), spruce);// LEFT
        fillWithBlocks(world, centre, temp1.set(0, 3, 0), temp2.set(0, 5, 8), spruce);// BACK
        fillWithBlocks(world, centre, temp1.set(0, 3, 0), temp2.set(8, 5, 0), spruce);// FRONT
        // CORNERS
        fillWithBlocks(world, centre, temp1.set(0, 3, 0), temp2.set(0, 5, 0), logs);
        fillWithBlocks(world, centre, temp1.set(0, 3, 8), temp2.set(0, 5, 8), logs);
        fillWithBlocks(world, centre, temp1.set(8, 3, 8), temp2.set(8, 5, 8), logs);
        fillWithBlocks(world, centre, temp1.set(8, 3, 0), temp2.set(8, 5, 0), logs);

        // Windows
        fillWithBlocks(world, centre, temp1.set(2, 4, 0), temp2.set(2, 4, 0), glass);// front
        fillWithBlocks(world, centre, temp1.set(6, 4, 0), temp2.set(6, 4, 0), glass);// front
        fillWithBlocks(world, centre, temp1.set(8, 4, 2), temp2.set(8, 4, 3), glass);// left
        fillWithBlocks(world, centre, temp1.set(8, 4, 5), temp2.set(8, 4, 6), glass);// left
        fillWithBlocks(world, centre, temp1.set(0, 4, 2), temp2.set(0, 4, 3), glass);// right
        fillWithBlocks(world, centre, temp1.set(0, 4, 5), temp2.set(0, 4, 6), glass);// right

        // carpet
        fillWithBlocks(world, centre, temp1.set(3, 3, 2), temp2.set(5, 3, 2), whiteCarpet);// white
        fillWithBlocks(world, centre, temp1.set(3, 3, 3), temp2.set(5, 3, 3), blackCarpet);// black
        fillWithBlocks(world, centre, temp1.set(3, 3, 4), temp2.set(5, 3, 4), redCarpet);// red
        temp1.set(centre).addTo(4, 3, 3).setBlock(world, greyCarpet);// grey

        // Ceiling Light
        temp1.set(centre).addTo(4, 6, 3).setBlock(world, Blocks.LIT_REDSTONE_LAMP.getDefaultState());// lamp
        temp1.set(centre).addTo(4, 7, 3).setBlock(world, Blocks.REDSTONE_BLOCK.getDefaultState());// redstone
                                                                                                  // to
                                                                                                  // power
                                                                                                  // lamp

        fillWithBlocks(world, centre, temp1.set(3, 3, 5), temp2.set(5, 3, 5),
                Blocks.DOUBLE_STONE_SLAB.getDefaultState());// front
        temp1.set(centre).addTo(6, 3, 6).setBlock(world, Blocks.DOUBLE_STONE_SLAB.getDefaultState());// side
        temp1.set(centre).addTo(2, 3, 6).setBlock(world, Blocks.DOUBLE_STONE_SLAB.getDefaultState());// side
        fillWithBlocks(world, centre, temp1.set(2, 2, 6), temp2.set(6, 2, 7),
                Blocks.DOUBLE_STONE_SLAB.getDefaultState());// floor

        // accessories
        temp1.set(centre).addTo(6, 3, 5).setBlock(world, PokecubeItems.getBlock("pc").getDefaultState());// PC
                                                                                                         // Base
        temp1.set(centre).addTo(6, 4, 5).setBlock(world,
                PokecubeItems.getBlock("pc").getDefaultState().withProperty(BlockPC.TOP, true));// PC
                                                                                                // Top
        temp1.set(centre).addTo(2, 3, 5).setBlock(world, PokecubeItems.getBlock("tradingtable").getDefaultState());// trading
                                                                                                                   // table
        temp1.set(centre).addTo(1, 3, 1).setBlock(world, PokecubeItems.getBlock("pokecube_table").getDefaultState());// poke
                                                                                                                     // table
        temp1.set(centre).addTo(4, 3, 7).setBlock(world, PokecubeItems.pokecenter.getDefaultState());// healing
                                                                                                     // table
        temp1.set(centre).addTo(4, 2, 7).setBlock(world, Blocks.REDSTONE_TORCH.getDefaultState());// table
                                                                                                  // power

        ItemDoor.placeDoor(world, temp1.set(centre).addTo(4, 3, 0).getPos(), EnumFacing.SOUTH, Blocks.OAK_DOOR, false);
    }

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator,
            IChunkProvider chunkProvider)
    {
        if (building || !PokecubeMod.core.getConfig().doSpawnBuilding) return;

        if (world.provider.getDimension() != 0) return;

        int x = world.getSpawnPoint().getX() / 16;
        int z = world.getSpawnPoint().getZ() / 16;

        if (x != chunkX || z != chunkZ) return;
        System.out.println("spawn building " + chunkX + " " + chunkZ);
        building = true;
        int y = getAverageHeight(world, chunkX * 16, chunkZ * 16) - 1;

        Vector3 centre = Vector3.getNewVector().set(chunkX * 16, y - 1, chunkZ * 16);

        makePokecenter(centre, world);

        for (int i = 0; i < 9; i++)
            for (int j = 3; j <= 6; j++)
            {
                BlockPos pos = new BlockPos(chunkX * 16 + i, y - 1 + j, chunkZ * 16 - 1);
                world.setBlockToAir(pos);
            }
        int j = y + 1;
        BlockPos pos = new BlockPos(chunkX * 16 + 4, j, chunkZ * 16 - 1);
        if (world.isAirBlock(pos) && !world.isAirBlock(pos.down()))
        {
            world.setBlockState(pos,
                    Blocks.STONE_STAIRS.getDefaultState().withProperty(BlockHorizontal.FACING, EnumFacing.SOUTH), 7);
        }
        else while (j > 1)
        {
            pos = new BlockPos(chunkX * 16 + 4, j, chunkZ * 16 - 1);
            if (world.isAirBlock(pos))
            {
                world.setBlockState(pos,
                        Blocks.LADDER.getDefaultState().withProperty(BlockHorizontal.FACING, EnumFacing.SOUTH));
            }
            else
            {
                break;
            }
            j--;
        }
        pos = new BlockPos(chunkX * 16 + 4, centre.y + 3, chunkZ * 16 + 4);
        world.provider.setSpawnPoint(pos);
    }

    int getAverageHeight(World world, int x, int z)
    {
        int y = 0;
        int minY = 255;
        for (int i = 0; i < 9; i++)
            for (int j = 0; j < 9; j++)
            {
                if (getMaxY(world, x, z) < minY) minY = getMaxY(world, x + i, z + j);
            }

        for (int i = 0; i < 9; i++)
            for (int j = 0; j < 9; j++)
                y += getMaxY(world, x + i, z + j);
        y /= 81;
        if ((minY < y - 5) && !(minY < y - 10)) y = minY;
        return y;
    }

    int getMaxY(World world, int x, int z)
    {
        int y = 255;
        Block id = Blocks.AIR;
        BlockPos pos = new BlockPos(x, y, z);
        while ((id.isAir(id.getDefaultState(), world, pos) || id == Blocks.SNOW
                || id.isLeaves(id.getDefaultState(), world, pos) || id.isWood(world, pos)) && y > 1)
        {
            y--;
            pos = new BlockPos(x, y, z);
            id = world.getBlockState(pos).getBlock();
        }

        return y;
    }

}
