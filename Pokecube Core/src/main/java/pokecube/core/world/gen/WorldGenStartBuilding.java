package pokecube.core.world.gen;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemDoor;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.fml.common.IWorldGenerator;
import pokecube.core.PokecubeItems;
import pokecube.core.interfaces.PokecubeMod;
import thut.api.maths.Vector3;

public class WorldGenStartBuilding implements IWorldGenerator
{

    public static boolean building = false;

    public static void fillWithBlocks(World world, Vector3 centre, Vector3 lower, Vector3 upper, Block block, int meta)
    {
        fillWithBlocks(world, centre, lower, upper, block.getStateFromMeta(meta));
    }

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

        // Hollow it out
        fillWithBlocks(world, centre, temp1.set(0, 0, 0), temp2.set(8, 8, 8), Blocks.air, 0);

        // Roof
        fillWithBlocks(world, centre, temp1.set(0, 6, 0), temp2.set(8, 6, 8), Blocks.stained_hardened_clay, 14);
        fillWithBlocks(world, centre, temp1.set(1, 6, 1), temp2.set(7, 7, 7), Blocks.stained_hardened_clay, 14);
        fillWithBlocks(world, centre, temp1.set(2, 6, 2), temp2.set(6, 8, 6), Blocks.stained_hardened_clay, 14);
        fillWithBlocks(world, centre, temp1.set(1, 6, 1), temp2.set(7, 6, 7), Blocks.planks, 1);

        // Floor
        fillWithBlocks(world, centre, temp1.set(0, 0, 0), temp2.set(8, 2, 8), Blocks.cobblestone, 0);
        fillWithBlocks(world, centre, temp1.set(1, 2, 1), temp2.set(7, 2, 7), Blocks.planks, 1);

        // Walls
        fillWithBlocks(world, centre, temp1.set(8, 3, 0), temp2.set(8, 5, 8), Blocks.planks, 1);// RIGHT
                                                                                                // WALL
        fillWithBlocks(world, centre, temp1.set(0, 3, 8), temp2.set(8, 5, 8), Blocks.planks, 1);// LEFT
                                                                                                // WALL
        fillWithBlocks(world, centre, temp1.set(0, 3, 0), temp2.set(0, 5, 8), Blocks.planks, 1);// BACK
                                                                                                // WALL
        fillWithBlocks(world, centre, temp1.set(0, 3, 0), temp2.set(8, 5, 0), Blocks.planks, 1);// FRONT
                                                                                                // WALL
        // CORNERS
        fillWithBlocks(world, centre, temp1.set(0, 3, 0), temp2.set(0, 5, 0), Blocks.log, 1);
        fillWithBlocks(world, centre, temp1.set(0, 3, 8), temp2.set(0, 5, 8), Blocks.log, 1);
        fillWithBlocks(world, centre, temp1.set(8, 3, 8), temp2.set(8, 5, 8), Blocks.log, 1);
        fillWithBlocks(world, centre, temp1.set(8, 3, 0), temp2.set(8, 5, 0), Blocks.log, 1);

        // Windows
        fillWithBlocks(world, centre, temp1.set(2, 4, 0), temp2.set(2, 4, 0), Blocks.glass_pane, 0);// front
        fillWithBlocks(world, centre, temp1.set(6, 4, 0), temp2.set(6, 4, 0), Blocks.glass_pane, 0);// front
        fillWithBlocks(world, centre, temp1.set(8, 4, 2), temp2.set(8, 4, 3), Blocks.glass_pane, 0);// left
        fillWithBlocks(world, centre, temp1.set(8, 4, 5), temp2.set(8, 4, 6), Blocks.glass_pane, 0);// left
        fillWithBlocks(world, centre, temp1.set(0, 4, 2), temp2.set(0, 4, 3), Blocks.glass_pane, 0);// right
        fillWithBlocks(world, centre, temp1.set(0, 4, 5), temp2.set(0, 4, 6), Blocks.glass_pane, 0);// right

        // carpet
        fillWithBlocks(world, centre, temp1.set(3, 3, 2), temp2.set(5, 3, 2), Blocks.carpet, 0);// white
        fillWithBlocks(world, centre, temp1.set(3, 3, 3), temp2.set(5, 3, 3), Blocks.carpet, 15);// black
        fillWithBlocks(world, centre, temp1.set(3, 3, 4), temp2.set(5, 3, 4), Blocks.carpet, 14);// red
        temp1.set(centre).addTo(4, 3, 3).setBlock(world, Blocks.carpet.getStateFromMeta(7));// grey

        // Ceiling Light
        temp1.set(centre).addTo(4, 6, 3).setBlock(world, Blocks.lit_redstone_lamp.getDefaultState());// lamp
        temp1.set(centre).addTo(4, 7, 3).setBlock(world, Blocks.redstone_block.getDefaultState());// redstone
                                                                                                  // to
                                                                                                  // power
                                                                                                  // lamp

        fillWithBlocks(world, centre, temp1.set(3, 3, 5), temp2.set(5, 3, 5), Blocks.double_stone_slab, 0);// front
        temp1.set(centre).addTo(6, 3, 6).setBlock(world, Blocks.double_stone_slab.getDefaultState());// side
        temp1.set(centre).addTo(2, 3, 6).setBlock(world, Blocks.double_stone_slab.getDefaultState());// side
        fillWithBlocks(world, centre, temp1.set(2, 2, 6), temp2.set(6, 2, 7), Blocks.double_stone_slab, 0);// floor

        // accessories
        temp1.set(centre).addTo(6, 3, 5).setBlock(world, PokecubeItems.getBlock("pc").getDefaultState());// PC
                                                                                                         // Base
        temp1.set(centre).addTo(6, 4, 5).setBlock(world, PokecubeItems.getBlock("pc").getStateFromMeta(8));// PC
                                                                                                           // Top
        temp1.set(centre).addTo(2, 3, 5).setBlock(world, PokecubeItems.getBlock("tradingtable").getDefaultState());// trading
                                                                                                                   // table
        temp1.set(centre).addTo(1, 3, 1).setBlock(world, PokecubeItems.getBlock("pokecube_table").getDefaultState());// poke
                                                                                                                     // table
        temp1.set(centre).addTo(4, 3, 7).setBlock(world, PokecubeItems.pokecenter.getDefaultState());// healing
                                                                                                     // table
        temp1.set(centre).addTo(4, 2, 7).setBlock(world, Blocks.redstone_torch.getDefaultState());// table
                                                                                                  // power

        ItemDoor.placeDoor(world, temp1.set(centre).addTo(4, 3, 0).getPos(), EnumFacing.SOUTH, Blocks.oak_door);
    }

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator,
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
            world.setBlockState(pos, Blocks.stone_stairs.getStateFromMeta(2), 7);
        }
        else while (j > 1)
        {
            pos = new BlockPos(chunkX * 16 + 4, j, chunkZ * 16 - 1);
            if (world.isAirBlock(pos))
            {
                world.setBlockState(pos, Blocks.ladder.getStateFromMeta(2));
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
        Block id = Blocks.air;
        BlockPos pos = new BlockPos(x, y, z);
        while ((id.isAir(world, pos) || id == Blocks.snow || id.isLeaves(world, pos) || id.isWood(world, pos)) && y > 1)
        {
            y--;
            pos = new BlockPos(x, y, z);
            id = world.getBlockState(pos).getBlock();
        }

        return y;
    }

}
