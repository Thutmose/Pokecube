package pokecube.core.world.dimensions.secretpower;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome.SpawnListEntry;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkGenerator;

public class ChunkProviderSecretBase implements IChunkGenerator
{

    private final World worldObj;

    public ChunkProviderSecretBase(World world)
    {
        this.worldObj = world;
    }

    @Override
    public Chunk provideChunk(int x, int z)
    {
        // Void world
        return new Chunk(worldObj, x, z);
    }

    @Override
    public void populate(int x, int z)
    {
        // Void world
    }

    @Override
    public boolean generateStructures(Chunk chunkIn, int x, int z)
    {
        return false;
    }

    @Override
    public List<SpawnListEntry> getPossibleCreatures(EnumCreatureType creatureType, BlockPos pos)
    {
        return Lists.newArrayList();
    }

    @Override
    public BlockPos getStrongholdGen(World worldIn, String structureName, BlockPos position)
    {
        return null;
    }

    @Override
    public void recreateStructures(Chunk chunkIn, int x, int z)
    {
    }

}
