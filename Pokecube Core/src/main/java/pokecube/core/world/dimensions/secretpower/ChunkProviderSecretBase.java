package pokecube.core.world.dimensions.secretpower;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome.SpawnListEntry;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.IChunkGenerator;

public class ChunkProviderSecretBase implements IChunkGenerator
{

    private final World world;

    public ChunkProviderSecretBase(World world)
    {
        this.world = world;
    }

    @Override
    public Chunk generateChunk(int x, int z)
    {
        // Void world
        return new Chunk(world, x, z);
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
    public BlockPos getNearestStructurePos(World worldIn, String structureName, BlockPos position, boolean p_180513_4_)
    {
        return null;
    }

    @Override
    public void recreateStructures(Chunk chunkIn, int x, int z)
    {
    }

    @Override
    public boolean isInsideStructure(World p_193414_1_, String p_193414_2_, BlockPos p_193414_3_)
    {
        return false;
    }

}
