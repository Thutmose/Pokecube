package pokecube.core.world.terrain;

import static thut.api.terrain.TerrainSegment.GRIDSIZE;
import static thut.api.terrain.TerrainSegment.count;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import pokecube.core.interfaces.PokecubeMod;
import thut.api.maths.Vector3;
import thut.api.terrain.BiomeType;
import thut.api.terrain.TerrainSegment;
import thut.api.terrain.TerrainSegment.ISubBiomeChecker;

public class PokecubeTerrainChecker implements ISubBiomeChecker
{
    public static BiomeType INSIDE = BiomeType.getBiome("inside", true);

    public static void init()
    {
        TerrainSegment.biomeCheckers.add(new PokecubeTerrainChecker());
    }

    @Override
    public int getSubBiome(World world, Vector3 v, TerrainSegment segment, Chunk chunk, boolean caveAdjusted)
    {
        if (caveAdjusted)
        {
            if (world.provider.doesWaterVaporize()) return -1;
            boolean sky = false;
            Vector3 temp1 = Vector3.getNewVector();
            outer:
            for (int i = 0; i < GRIDSIZE; i++)
                for (int j = 0; j < GRIDSIZE; j++)
                    for (int k = 0; k < GRIDSIZE; k++)
                    {
                        temp1.set(v).addTo(i, j, k);
                        if (segment.isInTerrainSegment(temp1.x, temp1.y, temp1.z))
                            sky = sky || temp1.isOnSurfaceIgnoringWater(chunk, world);
                        if (sky) break outer;
                    }
            if (sky) return -1;
            if (count(world, Blocks.water, v, 1) > 2) return BiomeType.CAVE_WATER.getType();
            else if (isCaveFloor(v, world)) return BiomeType.CAVE.getType();
            return INSIDE.getType();
        }
        return -1;
    }

    public boolean isCaveFloor(Vector3 v, World world)
    {
        Block b = v.getBlock(world);
        if (!b.getMaterial().isSolid()) { return PokecubeMod.core.getConfig().getCaveBlocks().contains(b); }

        Vector3 top = Vector3.getNextSurfacePoint(world, v, Vector3.secondAxisNeg, v.y);
        if (top == null) return false;
        b = top.getBlock(world);
        return PokecubeMod.core.getConfig().getCaveBlocks().contains(b);
    }

}
