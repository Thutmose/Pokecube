package pokecube.core.world.gen;

import java.util.Random;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraftforge.fml.common.IWorldGenerator;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.events.handlers.SpawnHandler;
import thut.api.maths.Vector3;
import thut.api.terrain.BiomeDatabase;

public class WorldGenFossils implements IWorldGenerator
{

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator,
            IChunkProvider chunkProvider)
    {
        if (!world.provider.isSurfaceWorld() || !PokecubeCore.core.getConfig().generateFossils) return;
        if (!SpawnHandler.canSpawnInWorld(world)) return;

        int fossilchance = random.nextInt(5) + 2;
        for (int i = fossilchance; i > 0; i--)
        {
            int randPosX = chunkX * 16 + random.nextInt(16);
            int randPosY = random.nextInt(40) + 5;
            int randPosZ = chunkZ * 16 + random.nextInt(16);
            Vector3 v = Vector3.getNewVector().set(randPosX, 0, randPosZ);
            Biome bgb = v.getBiome(world);
            if (bgb == BiomeDatabase.getBiome("desertHills") || bgb == BiomeDatabase.getBiome("desert")
                    || bgb == BiomeDatabase.getBiome("jungle") || bgb == BiomeDatabase.getBiome("jungleHills")
                    || bgb == BiomeDatabase.getBiome("ocean"))
            {
                (new WorldGenMinable(PokecubeItems.getBlock("fossilstone").getDefaultState(), random.nextInt(3) + 3))
                        .generate(world, random, new BlockPos(randPosX, randPosY, randPosZ));
            }
        }
    }

}
