package pokecube.core.world.terrain;

import static thut.api.terrain.BiomeType.LAKE;
import static thut.api.terrain.BiomeType.VILLAGE;
import static thut.api.terrain.TerrainSegment.GRIDSIZE;
import static thut.api.terrain.TerrainSegment.count;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.village.Village;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.BiomeDictionary.Type;
import pokecube.core.interfaces.PokecubeMod;
import thut.api.maths.Vector3;
import thut.api.terrain.BiomeDatabase;
import thut.api.terrain.BiomeType;
import thut.api.terrain.TerrainSegment;
import thut.api.terrain.TerrainSegment.ISubBiomeChecker;

public class PokecubeTerrainChecker implements ISubBiomeChecker
{
    public static BiomeType INSIDE = BiomeType.getBiome("inside", true);

    public static void init()
    {
        PokecubeTerrainChecker checker = new PokecubeTerrainChecker();
        TerrainSegment.defaultChecker = checker;
    }

    @Override
    public int getSubBiome(World world, Vector3 v, TerrainSegment segment, Chunk chunk, boolean caveAdjusted)
    {
        if (caveAdjusted)
        {
            if (world.provider.doesWaterVaporize() || chunk.canSeeSky(v.getPos())) return -1;
            boolean sky = false;
            Vector3 temp1 = Vector3.getNewVector();
            int x0 = segment.chunkX * 16, y0 = segment.chunkY * 16, z0 = segment.chunkZ * 16;
            int dx = ((v.intX() - x0) / GRIDSIZE) * GRIDSIZE;
            int dy = ((v.intY() - y0) / GRIDSIZE) * GRIDSIZE;
            int dz = ((v.intZ() - z0) / GRIDSIZE) * GRIDSIZE;
            int x1 = x0 + dx, y1 = y0 + dy, z1 = z0 + dz;
            outer:
            for (int i = x1; i < x1 + GRIDSIZE; i++)
                for (int j = y1; j < y1 + GRIDSIZE; j++)
                    for (int k = z1; k < z1 + GRIDSIZE; k++)
                    {
                        temp1.set(i, j, k);
                        if (segment.isInTerrainSegment(temp1.x, temp1.y, temp1.z))
                        {
                            double y = temp1.getMaxY(world);
                            sky = y <= temp1.y;
                        }
                        if (sky) break outer;
                    }
            if (sky) return -1;
            if (count(world, Blocks.WATER, v, 1) > 2) return BiomeType.CAVE_WATER.getType();
            else if (isCave(v, world)) return BiomeType.CAVE.getType();
            return INSIDE.getType();
        }
        else
        {
            int biome = 0;
            Biome b = v.getBiome(chunk, world.getBiomeProvider());
            biome = BiomeDatabase.getBiomeType(b);
            boolean notLake = BiomeDatabase.contains(b, Type.OCEAN) || BiomeDatabase.contains(b, Type.SWAMP)
                    || BiomeDatabase.contains(b, Type.RIVER) || BiomeDatabase.contains(b, Type.WATER)
                    || BiomeDatabase.contains(b, Type.BEACH);
            int water = v.blockCount2(world, Blocks.WATER, 3);
            if (water > 4)
            {
                if (!notLake)
                {
                    biome = LAKE.getType();
                }
                return biome;
            }
            boolean sky = chunk.canSeeSky(v.getPos());
            if (sky)
            {
                sky = v.findNextSolidBlock(world, Vector3.secondAxisNeg, 16) == null;
                if (sky) return BiomeType.SKY.getType();
            }

            if (world.villageCollectionObj != null)
            {
                Village village = world.villageCollectionObj.getNearestVillage(new BlockPos(
                        MathHelper.floor_double(v.x), MathHelper.floor_double(v.y), MathHelper.floor_double(v.z)), 2);
                if (village != null)
                {
                    biome = VILLAGE.getType();
                }
            }
            return biome;
        }
    }

    public boolean isCave(Vector3 v, World world)
    {
        return isCaveFloor(v, world) && isCaveCeiling(v, world);
    }

    public boolean isCaveFloor(Vector3 v, World world)
    {
        IBlockState state = v.getBlockState(world);
        Block b = state.getBlock();
        List<Block> cave = PokecubeMod.core.getConfig().getCaveBlocks();
        if (state.getMaterial().isSolid()) { return cave.contains(b); }
        Vector3 down = Vector3.getNextSurfacePoint(world, v, Vector3.secondAxisNeg, v.y);
        if (down == null) return false;
        b = down.getBlock(world);
        return cave.contains(b);
    }

    public boolean isCaveCeiling(Vector3 v, World world)
    {
        double y = v.getMaxY(world);
        if (y <= v.y) return false;
        IBlockState state = v.getBlockState(world);
        Block b = state.getBlock();
        if (state.getMaterial().isSolid()) { return PokecubeMod.core.getConfig().getCaveBlocks().contains(b); }
        Vector3 up = Vector3.getNextSurfacePoint(world, v, Vector3.secondAxis, 255 - v.y);
        if (up == null) return false;
        b = up.getBlock(world);
        return PokecubeMod.core.getConfig().getCaveBlocks().contains(b);
    }

    public boolean isInside(Vector3 v, World world)
    {
        return true;
    }

}
