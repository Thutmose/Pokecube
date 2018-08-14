package pokecube.core.world.terrain;

import static thut.api.terrain.BiomeType.LAKE;
import static thut.api.terrain.BiomeType.VILLAGE;
import static thut.api.terrain.TerrainSegment.GRIDSIZE;

import java.util.Map;

import com.google.common.base.Predicate;
import com.google.common.collect.Maps;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.village.Village;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.common.BiomeDictionary.Type;
import pokecube.core.PokecubeCore;
import pokecube.core.interfaces.PokecubeMod;
import thut.api.maths.Vector3;
import thut.api.terrain.BiomeDatabase;
import thut.api.terrain.BiomeType;
import thut.api.terrain.TerrainSegment;
import thut.api.terrain.TerrainSegment.ISubBiomeChecker;

public class PokecubeTerrainChecker implements ISubBiomeChecker
{
    public static BiomeType INSIDE = BiomeType.getBiome("inside", true);

    public static boolean isCave(IBlockState state)
    {
        if (state.getMaterial() == Material.AIR) return false;
        for (Predicate<IBlockState> predicate : PokecubeMod.core.getConfig().getCaveBlocks())
        {
            if (predicate.apply(state)) return true;
        }
        return false;
    }

    public static boolean isSurface(IBlockState state)
    {
        if (state.getMaterial() == Material.AIR) return false;
        for (Predicate<IBlockState> predicate : PokecubeMod.core.getConfig().getSurfaceBlocks())
        {
            if (predicate.apply(state)) return true;
        }
        return false;
    }

    public static boolean isRock(IBlockState state)
    {
        if (state.getMaterial() == Material.AIR) return false;
        for (Predicate<IBlockState> predicate : PokecubeMod.core.getConfig().getRocks())
        {
            if (predicate.apply(state)) return true;
        }
        return false;
    }

    public static boolean isTerrain(IBlockState state)
    {
        if (state.getMaterial() == Material.AIR) return false;
        for (Predicate<IBlockState> predicate : PokecubeMod.core.getConfig().getTerrain())
        {
            if (predicate.apply(state)) return true;
        }
        return false;
    }

    public static boolean isDirt(IBlockState state)
    {
        if (state.getMaterial() == Material.AIR) return false;
        for (Predicate<IBlockState> predicate : PokecubeMod.core.getConfig().getDirtTypes())
        {
            if (predicate.apply(state)) return true;
        }
        return false;
    }

    public static boolean isWood(IBlockState state)
    {
        if (state.getMaterial() == Material.AIR) return false;
        for (Predicate<IBlockState> predicate : PokecubeMod.core.getConfig().getWoodTypes())
        {
            if (predicate.apply(state)) return true;
        }
        return false;
    }

    public static boolean isPlant(IBlockState state)
    {
        if (state.getMaterial() == Material.AIR) return false;
        for (Predicate<IBlockState> predicate : PokecubeMod.core.getConfig().getPlantTypes())
        {
            if (predicate.apply(state)) return true;
        }
        return false;
    }

    public static boolean isFruit(IBlockState state)
    {
        if (state.getMaterial() == Material.AIR) return false;
        for (Predicate<IBlockState> predicate : PokecubeMod.core.getConfig().getFruitTypes())
        {
            if (predicate.apply(state)) return true;
        }
        return false;
    }

    public static boolean isIndustrial(IBlockState state)
    {
        if (state.getMaterial() == Material.AIR) return false;
        for (Predicate<IBlockState> predicate : PokecubeMod.core.getConfig().getIndustrial())
        {
            if (predicate.apply(state)) return true;
        }
        return false;
    }

    public static void init()
    {
        PokecubeTerrainChecker checker = new PokecubeTerrainChecker();
        TerrainSegment.defaultChecker = checker;
    }

    public static Map<String, String> structureSubbiomeMap = Maps.newHashMap();

    @Override
    public int getSubBiome(World world, Vector3 v, TerrainSegment segment, Chunk chunk, boolean caveAdjusted)
    {
        if (caveAdjusted)
        {
            if (world instanceof WorldServer)
            {
                WorldServer worldS = (WorldServer) world;
                IChunkGenerator generator = worldS.getChunkProvider().chunkGenerator;
                if (generator != null) for (String key : structureSubbiomeMap.keySet())
                {
                    if (generator.isInsideStructure(worldS, key, v.getPos()))
                    {
                        String mapping = structureSubbiomeMap.get(key);
                        BiomeType biome = BiomeType.getBiome(mapping, true);
                        return biome.getType();
                    }
                }
            }
            if (world.provider.doesWaterVaporize() || chunk.canSeeSky(v.getPos())
                    || !PokecubeCore.core.getConfig().autoDetectSubbiomes)
                return -1;
            boolean sky = false;
            Vector3 temp1 = Vector3.getNewVector();
            int x0 = segment.chunkX * 16, y0 = segment.chunkY * 16, z0 = segment.chunkZ * 16;
            int dx = ((v.intX() - x0) / GRIDSIZE) * GRIDSIZE;
            int dy = ((v.intY() - y0) / GRIDSIZE) * GRIDSIZE;
            int dz = ((v.intZ() - z0) / GRIDSIZE) * GRIDSIZE;
            int x1 = x0 + dx, y1 = y0 + dy, z1 = z0 + dz;
            int industrial = 0;
            int water = 0;
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
                        IBlockState state;
                        if (isIndustrial(state = temp1.getBlockState(world))) industrial++;
                        if (industrial > 2) return BiomeType.INDUSTRIAL.getType();
                        if (state.getMaterial() == Material.WATER) water++;
                        if (sky) break outer;
                    }
            if (sky) return -1;
            if (water > 4)
            {
                return BiomeType.CAVE_WATER.getType();
            }
            else if (isCave(v, world)) return BiomeType.CAVE.getType();

            return INSIDE.getType();
        }
        int biome = -1;
        Biome b = v.getBiome(chunk, world.getBiomeProvider());
        if (!PokecubeCore.core.getConfig().autoDetectSubbiomes) return biome;
        boolean notLake = BiomeDatabase.contains(b, Type.OCEAN) || BiomeDatabase.contains(b, Type.SWAMP)
                || BiomeDatabase.contains(b, Type.RIVER) || BiomeDatabase.contains(b, Type.WATER)
                || BiomeDatabase.contains(b, Type.BEACH);
        int industrial = 0;
        int water = 0;
        Vector3 temp1 = Vector3.getNewVector();
        int x0 = segment.chunkX * 16, y0 = segment.chunkY * 16, z0 = segment.chunkZ * 16;
        int dx = ((v.intX() - x0) / GRIDSIZE) * GRIDSIZE;
        int dy = ((v.intY() - y0) / GRIDSIZE) * GRIDSIZE;
        int dz = ((v.intZ() - z0) / GRIDSIZE) * GRIDSIZE;
        int x1 = x0 + dx, y1 = y0 + dy, z1 = z0 + dz;
        for (int i = x1; i < x1 + GRIDSIZE; i++)
            for (int j = y1; j < y1 + GRIDSIZE; j++)
                for (int k = z1; k < z1 + GRIDSIZE; k++)
                {
                    IBlockState state;
                    if (isIndustrial(state = temp1.set(i, j, k).getBlockState(world))) industrial++;
                    if (industrial > 2) return BiomeType.INDUSTRIAL.getType();
                    if (state.getMaterial() == Material.WATER) water++;
                }
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
        if (world.villageCollection != null)
        {
            Village village = world.villageCollection.getNearestVillage(
                    new BlockPos(MathHelper.floor(v.x), MathHelper.floor(v.y), MathHelper.floor(v.z)), 2);
            if (village != null)
            {
                biome = VILLAGE.getType();
            }
        }
        return biome;
    }

    public boolean isCave(Vector3 v, World world)
    {
        return isCaveFloor(v, world) && isCaveCeiling(v, world);
    }

    public boolean isCaveFloor(Vector3 v, World world)
    {
        IBlockState state = v.getBlockState(world);
        if (state.getMaterial().isSolid()) { return isCave(state); }
        Vector3 down = Vector3.getNextSurfacePoint(world, v, Vector3.secondAxisNeg, v.y);
        if (down == null) return false;
        return isCave(down.getBlockState(world));
    }

    public boolean isCaveCeiling(Vector3 v, World world)
    {
        double y = v.getMaxY(world);
        if (y <= v.y) return false;
        IBlockState state = v.getBlockState(world);
        if (state.getMaterial().isSolid()) { return isCave(state); }
        Vector3 up = Vector3.getNextSurfacePoint(world, v, Vector3.secondAxis, 255 - v.y);
        if (up == null) return false;
        state = up.getBlockState(world);
        return isCave(state);
    }

    public boolean isInside(Vector3 v, World world)
    {
        return true;
    }

}
