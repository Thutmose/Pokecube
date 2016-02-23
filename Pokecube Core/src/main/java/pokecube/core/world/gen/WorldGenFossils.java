package pokecube.core.world.gen;

import java.util.Random;

import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraftforge.fml.common.IWorldGenerator;
import pokecube.core.PokecubeItems;

public class WorldGenFossils implements IWorldGenerator
{

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator,
            IChunkProvider chunkProvider)
    {
        if (!world.provider.isSurfaceWorld()) return;

        int fossilchance = random.nextInt(5) + 2;
        for (int i = fossilchance; i > 0; i--)
        {
            int randPosX = chunkX * 16 + random.nextInt(16);
            int randPosY = random.nextInt(40) + 5;
            int randPosZ = chunkZ * 16 + random.nextInt(16);
            BiomeGenBase bgb = world.getBiomeGenForCoords(new BlockPos(randPosX, 0, randPosZ));
            if (bgb == BiomeGenBase.desertHills || bgb == BiomeGenBase.desert || bgb == BiomeGenBase.jungle
                    || bgb == BiomeGenBase.jungleHills || bgb == BiomeGenBase.ocean)
            {
                System.out.println("test " + new BlockPos(randPosX, randPosY, randPosZ));
                System.out.println(new BlockPos(randPosX, randPosY, randPosZ));
                (new WorldGenMinable(PokecubeItems.getBlock("fossilstone").getDefaultState(), random.nextInt(3) + 3))
                        .generate(world, random, new BlockPos(randPosX, randPosY, randPosZ));
            }
        }
    }

}
