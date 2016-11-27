package pokecube.core.world.gen;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkGenerator;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraftforge.fml.common.IWorldGenerator;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.world.gen.template.PokecubeTemplate;
import pokecube.core.world.gen.template.PokecubeTemplates;
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
        PokecubeTemplates.serverInit(world.getMinecraftServer());
        PokecubeTemplate template = PokecubeTemplates.getTemplate(PokecubeTemplates.POKECENTER);
        Mirror mirror = Mirror.NONE;
        EnumFacing dir = EnumFacing.HORIZONTALS[world.rand.nextInt(4)];
        Rotation rotation = Rotation.NONE;
        if (dir == EnumFacing.NORTH) rotation = Rotation.CLOCKWISE_180;
        if (dir == EnumFacing.EAST) rotation = Rotation.CLOCKWISE_90;
        if (dir == EnumFacing.WEST) rotation = Rotation.COUNTERCLOCKWISE_90;
        PlacementSettings placementsettings = (new PlacementSettings()).setMirror(mirror).setRotation(rotation)
                .setIgnoreEntities(false).setChunk(null).setReplacedBlock(null).setIgnoreStructureBlock(true);
        template.addBlocksToWorldChunk(world, centre.getPos().up(), placementsettings);
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
        building = true;
        int y = getAverageHeight(world, chunkX * 16, chunkZ * 16) - 1;
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
        Vector3 centre = Vector3.getNewVector().set(chunkX * 16, y - 1, chunkZ * 16);
        makePokecenter(centre, world);
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
