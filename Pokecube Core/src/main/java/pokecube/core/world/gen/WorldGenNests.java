package pokecube.core.world.gen;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;
import pokecube.core.PokecubeItems;
import pokecube.core.events.handlers.SpawnHandler;
import pokecube.core.interfaces.PokecubeMod;

public class WorldGenNests implements IWorldGenerator
{

    static boolean buildingTemple = false;

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator,
            IChunkProvider chunkProvider)
    {
        if (!PokecubeMod.core.getConfig().nests) return;
        if (!SpawnHandler.canSpawnInWorld(world)) return;
        PokecubeMod.core.getConfig().nestsPerChunk = 1;
        if (random.nextDouble() < 0.9) return;

        switch (world.provider.getDimension())
        {
        case -1:
            for (int i = 0; i < PokecubeMod.core.getConfig().nestsPerChunk; i++)
                generateNether(world, random, chunkX, chunkZ);
            break;
        case 0:
            for (int i = 0; i < PokecubeMod.core.getConfig().nestsPerChunk; i++)
                generateSurface(world, random, chunkX, chunkZ);
            break;
        case 1:
            generateEnd();
            break;
        }
    }

    public void generateEnd()
    {
        // we're not going to generate in the end yet
    }

    public void generateNether(World world, Random rand, int chunkX, int chunkZ)
    {
        int rX = rand.nextInt(20);
        int rZ = rand.nextInt(20);
        int rY = rand.nextInt(world.provider.getActualHeight());
        for (int i = 0; i < 16; i++)
        {
            for (int j = 1; j < world.provider.getActualHeight(); j++)
            {
                for (int k = 0; k < 16; k++)
                {
                    int x = ((i + rX) % 16) + chunkX * 16;
                    int y = ((j + rY) % world.provider.getActualHeight());
                    int z = ((k + rZ) % 16) + chunkZ * 16;
                    BlockPos pos = new BlockPos(x, y, z);
                    IBlockState state = world.getBlockState(pos);
                    IBlockState stateUp = world.getBlockState(pos.up());
                    Block b = state.getBlock();
                    Block bUp = stateUp.getBlock();
                    boolean validUp = bUp.isAir(state, world, pos.up());
                    validUp = validUp
                            || (stateUp.getMaterial().isReplaceable() && stateUp.getMaterial() != Material.LAVA);
                    if (!validUp)
                    {
                        continue;
                    }
                    if (PokecubeMod.core.getConfig().getTerrain().contains(b))
                    {
                        world.setBlockState(pos, PokecubeItems.getBlock("pokemobNest").getDefaultState());
                        return;
                    }
                }
            }
        }

    }

    public void generateSurface(World world, Random rand, int chunkX, int chunkZ)
    {
        int rX = rand.nextInt(20);
        int rZ = rand.nextInt(20);
        int rY = rand.nextInt(world.provider.getActualHeight());
        for (int i = 0; i < 16; i++)
        {
            for (int j = 1; j < world.provider.getActualHeight(); j++)
            {
                for (int k = 0; k < 16; k++)
                {
                    int x = ((i + rX) % 16) + chunkX * 16;
                    int y = ((j + rY) % world.provider.getActualHeight());
                    int z = ((k + rZ) % 16) + chunkZ * 16;

                    BlockPos pos = new BlockPos(x, y, z);
                    IBlockState state = world.getBlockState(pos);
                    IBlockState stateUp = world.getBlockState(pos.up());
                    Block b = state.getBlock();
                    Block bUp = stateUp.getBlock();
                    boolean validUp = bUp.isAir(state, world, pos.up());
                    validUp = validUp
                            || (stateUp.getMaterial().isReplaceable() && stateUp.getMaterial() != Material.LAVA);
                    if (!validUp)
                    {
                        continue;
                    }
                    if (PokecubeMod.core.getConfig().getTerrain().contains(b))
                    {
                        world.setBlockState(pos, PokecubeItems.getBlock("pokemobNest").getDefaultState());
                        return;
                    }
                }
            }
        }

    }
}