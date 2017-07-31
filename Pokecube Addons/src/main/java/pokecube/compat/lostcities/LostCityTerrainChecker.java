package pokecube.compat.lostcities;

import mcjty.lostcities.api.ILostChunkGenerator;
import mcjty.lostcities.api.ILostChunkInfo;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.world.gen.IChunkGenerator;
import pokecube.core.world.terrain.PokecubeTerrainChecker;
import thut.api.maths.Vector3;
import thut.api.terrain.BiomeType;
import thut.api.terrain.TerrainSegment;
import thut.api.terrain.TerrainSegment.ISubBiomeChecker;

public class LostCityTerrainChecker extends PokecubeTerrainChecker
{
    ISubBiomeChecker parent;

    public LostCityTerrainChecker(ISubBiomeChecker parent)
    {
        this.parent = parent;
    }

    @Override
    public int getSubBiome(World world, Vector3 v, TerrainSegment segment, Chunk chunk, boolean caveAdjusted)
    {
        check:
        if (caveAdjusted)
        {
            if (world.getChunkProvider() instanceof ChunkProviderServer)
            {
                IChunkGenerator generator = ((ChunkProviderServer) world.getChunkProvider()).chunkGenerator;
                if (generator instanceof ILostChunkGenerator)
                {
                    BlockPos pos = v.getPos();
                    ILostChunkGenerator lostGenerator = (ILostChunkGenerator) generator;
                    ILostChunkInfo info = lostGenerator.getChunkInfo(chunk.x, chunk.z);
                    String type = info.getBuildingType();
                    if (!info.isCity()) break check;
                    int streetLevel = lostGenerator.getRealHeight(info.getCityLevel());
                    int maxLevel = lostGenerator.getRealHeight(info.getNumFloors());
                    int minLevel = lostGenerator.getRealHeight(-info.getNumCellars());

                    // Adjust for streets which report funny levels.
                    if (maxLevel < streetLevel) maxLevel = streetLevel;
                    if (minLevel > streetLevel - 2) minLevel = streetLevel - 2;

                    int diff = pos.getY() - streetLevel;
                    // Give a leeway of 5 blocks above for roof structures..
                    boolean inStructure = pos.getY() >= minLevel && pos.getY() <= maxLevel + 5;
                    if (!inStructure) type = null;
                    // We only want streets to cover close to the ground, above
                    // that can be sky, below that can be cave.
                    else if (type == null && diff < 8 && diff > 0) type = "street";
                    if (type == null) break check;
                    return BiomeType.getBiome(type, true).getType();
                }
            }
        }
        return super.getSubBiome(world, v, segment, chunk, caveAdjusted);
    }

}
