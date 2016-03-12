package pokecube.core.world.gen;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.fml.common.IWorldGenerator;
import pokecube.core.PokecubeItems;
import pokecube.core.interfaces.PokecubeMod;

public class WorldGenNests implements IWorldGenerator {

	static boolean buildingTemple = false;

	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator,
			IChunkProvider chunkProvider) {
		if (!PokecubeMod.core.getConfig().nests)
			return;
		PokecubeMod.core.getConfig().nestsPerChunk = 1;
		if(random.nextDouble() < 0.9)
			return;
		
		
		switch (world.provider.getDimensionId()) {
		case -1:
			for(int i = 0; i<PokecubeMod.core.getConfig().nestsPerChunk; i++)
			generateNether(world, random, chunkX, chunkZ);
			break;
		case 0:
			for(int i = 0; i<PokecubeMod.core.getConfig().nestsPerChunk; i++)
				generateSurface(world, random, chunkX, chunkZ);
			break;
		case 1:
			generateEnd();
			break;
		}
	}

	public void generateEnd() {
		// we're not going to generate in the end yet
	}

	public void generateNether(World world, Random rand, int chunkX, int chunkZ) {
		int rX = rand.nextInt(20);
		int rZ = rand.nextInt(20);
		int rY = rand.nextInt(300);
		for (int i = 0; i < 16; i++) {
			for (int j = 1; j < 256; j++) {
				for (int k = 0; k < 16; k++) {
					int x = ((i + rX) % 16) + chunkX * 16;
					int y = ((j + rY) % 256);
					int z = ((k + rZ) % 16) + chunkZ * 16;
					BlockPos pos = new BlockPos(x, y, z);
					Block b = world.getBlockState(pos).getBlock();
					Block bUp = world.getBlockState(pos.up()).getBlock();
					boolean validUp = bUp.isAir(world, pos.up());
					validUp = validUp || (bUp.getMaterial().isReplaceable() && bUp.getMaterial() != Material.lava);
					if (!validUp) 
					{
						continue;
					}
					if(PokecubeMod.core.getConfig().getTerrain().contains(b))
					{
						world.setBlockState(pos, PokecubeItems.getBlock("pokemobNest").getDefaultState());
						return;
					}
				}
			}
		}

	}

	public void generateSurface(World world, Random rand, int chunkX, int chunkZ) {
		int rX = rand.nextInt(20);
		int rZ = rand.nextInt(20);
		int rY = rand.nextInt(300);
		for (int i = 0; i < 16; i++) {
			for (int j = 1; j < 256; j++) {
				for (int k = 0; k < 16; k++) {
					int x = ((i + rX) % 16) + chunkX * 16;
					int y = ((j + rY) % 256);
					int z = ((k + rZ) % 16) + chunkZ * 16;

					BlockPos pos = new BlockPos(x, y, z);
					Block b = world.getBlockState(pos).getBlock();
					Block bUp = world.getBlockState(pos.up()).getBlock();
					boolean validUp = bUp.isAir(world, pos.up());
					validUp = validUp || (bUp.getMaterial().isReplaceable() && bUp.getMaterial() != Material.lava);
					if (!validUp) 
					{
						continue;
					}
					if(PokecubeMod.core.getConfig().getTerrain().contains(b))
					{
						world.setBlockState(pos, PokecubeItems.getBlock("pokemobNest").getDefaultState());
						return;
					}
				}
			}
		}

	}
}