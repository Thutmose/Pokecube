package pokecube.core.world.gen;

import static thut.api.terrain.BiomeDatabase.contains;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.fml.common.IWorldGenerator;
import pokecube.core.items.berries.BerryManager;
import thut.api.maths.Vector3;

/** @author Oracion
 * @author Manchou */
public class WorldGenBerries implements IWorldGenerator
{

    Block grassBlock = Blocks.grass;

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator,
            IChunkProvider chunkProvider)
    {
        switch (world.provider.getDimensionId())
        {
        case -1:
            generateNether(world, random, chunkX * 16, chunkZ * 16);
            break;
        case 0:
            generateSurface(world, random, chunkX * 16, chunkZ * 16);
            break;
        case 1:
            generateEnd(world, random, chunkX * 16, chunkZ * 16);
            break;
        }
    }

    public void generateNether(World world, Random rand, int chunkX, int chunkZ)
    {
        // RAWST BERRY//
        if (rand.nextInt(9) == 0)
        {
            int randPosX = chunkX + rand.nextInt(15) + 1;
            int randPosZ = chunkZ + rand.nextInt(15) + 1;
            BlockPos pos = world.getTopSolidOrLiquidBlock(new BlockPos(randPosX, 0, randPosZ));

            if (world.getBiomeGenForCoords(pos) == (BiomeGenBase.hell))
            {
                if (world.getBlockState(pos).getBlock() == Blocks.netherrack)
                {
                    placeCropAt(world, pos.up(), "rawst");
                }
            }
        }
    }

    public void generateSurface(World world, Random rand, int chunkX, int chunkZ)
    {
        // ASPEAR BERRY//
        if (rand.nextInt(8) == 0)
        {
            int randPosX = chunkX + rand.nextInt(15) + 1;
            int randPosZ = chunkZ + rand.nextInt(15) + 1;
            BlockPos pos = world.getTopSolidOrLiquidBlock(new BlockPos(randPosX, 0, randPosZ));
            Vector3 v = Vector3.getNewVector().set(pos);
            BiomeGenBase b = v.getBiome(world);

            if (contains(b, Type.COLD))
            {
                if (world.getBlockState(pos.down()).getBlock() == grassBlock)
                {
                    placeCropAt(world, pos, "aspear");
                    placeCropAt(world, pos.east(), "aspear");
                    placeCropAt(world, pos.north(), "aspear");
                }
            }
        }

        // CHERI BERRY//
        if (rand.nextInt(8) == 0)
        {
            int randPosX = chunkX + rand.nextInt(15) + 1;
            int randPosZ = chunkZ + rand.nextInt(15) + 1;
            BlockPos pos = world.getTopSolidOrLiquidBlock(new BlockPos(randPosX, 0, randPosZ));
            Vector3 v = Vector3.getNewVector().set(pos);
            BiomeGenBase b = v.getBiome(world);

            if (contains(b, Type.PLAINS) && !contains(b, Type.SAVANNA))
            {
                if (world.getBlockState(pos.down()).getBlock() == grassBlock)
                {
                    placeCropAt(world, pos, "cheri");
                    placeCropAt(world, pos.east(), "cheri");
                    placeCropAt(world, pos.north(), "cheri");
                }
            }
        }

        // CHESTO BERRY//
        if (rand.nextInt(15) + 1 == 0)
        {
            int randPosX = chunkX + rand.nextInt(15) + 1;
            int randPosZ = chunkZ + rand.nextInt(15) + 1;
            BlockPos pos = world.getTopSolidOrLiquidBlock(new BlockPos(randPosX, 0, randPosZ));
            Vector3 v = Vector3.getNewVector().set(pos);
            BiomeGenBase b = v.getBiome(world);

            if (contains(b, Type.FOREST) && !contains(b, Type.CONIFEROUS))
            {
                if (world.getBlockState(pos.down()).getBlock() == grassBlock)
                {
                    placeCropAt(world, pos, "chesto");
                    placeCropAt(world, pos.east(), "chesto");
                    placeCropAt(world, pos.north(), "chesto");
                    placeCropAt(world, pos.west(), "chesto");
                }
            }
        }

        // CORNN BERRY//
        if (rand.nextInt(4) == 0)
        {
            int randPosX = chunkX + rand.nextInt(15) + 1;
            int randPosZ = chunkZ + rand.nextInt(15) + 1;
            BlockPos pos = world.getTopSolidOrLiquidBlock(new BlockPos(randPosX, 0, randPosZ));
            Vector3 v = Vector3.getNewVector().set(pos);
            BiomeGenBase b = v.getBiome(world);

            if (contains(b, Type.SWAMP))
            {
                if (world.getBlockState(pos.down()).getBlock() == grassBlock)
                {
                    placeCropAt(world, pos, "cornn");
                    placeCropAt(world, pos.east(), "cornn");
                }
            }
        }

        // ORAN BERRY//
        if (rand.nextInt(8) == 0)
        {
            int randPosX = chunkX + rand.nextInt(15) + 1;
            int randPosZ = chunkZ + rand.nextInt(15) + 1;
            BlockPos pos = world.getTopSolidOrLiquidBlock(new BlockPos(randPosX, 0, randPosZ));
            Vector3 v = Vector3.getNewVector().set(pos);
            BiomeGenBase b = v.getBiome(world);

            if (contains(b, Type.FOREST) && contains(b, Type.HILLS) && !contains(b, Type.CONIFEROUS))
            {
                if (world.getBlockState(pos.down()).getBlock() == grassBlock)
                {
                    placeCropAt(world, pos, "oran");
                }
            }
        }

        // LEPPA BERRY//
        if (rand.nextInt(4) == 0)
        {
            int randPosX = chunkX + rand.nextInt(15) + 1;
            int randPosZ = chunkZ + rand.nextInt(15) + 1;
            BlockPos pos = world.getTopSolidOrLiquidBlock(new BlockPos(randPosX, 0, randPosZ));
            Vector3 v = Vector3.getNewVector().set(pos);
            BiomeGenBase b = v.getBiome(world);

            if (contains(b, Type.PLAINS) && !contains(b, Type.SAVANNA))
            {
                if (world.getBlockState(pos.down()).getBlock() == grassBlock)
                {
                    placeCropAt(world, pos, "leppa");
                }
            }
        }

        // LUM BERRY//
        if (rand.nextInt(4) == 0)
        {
            int randPosX = chunkX + rand.nextInt(15) + 1;
            int randPosZ = chunkZ + rand.nextInt(15) + 1;
            BlockPos pos = world.getTopSolidOrLiquidBlock(new BlockPos(randPosX, 0, randPosZ));
            Vector3 v = Vector3.getNewVector().set(pos);
            BiomeGenBase b = v.getBiome(world);

            if (contains(b, Type.JUNGLE) && !contains(b, Type.HILLS))
            {
                if (world.getBlockState(pos.down()).getBlock() == grassBlock)
                {
                    placeCropAt(world, pos, "lum");
                }
            }
        }

        // PECHA BERRY//
        if (rand.nextInt(8) == 0)
        {
            int randPosX = chunkX + rand.nextInt(15) + 1;
            int randPosZ = chunkZ + rand.nextInt(15) + 1;
            BlockPos pos = world.getTopSolidOrLiquidBlock(new BlockPos(randPosX, 0, randPosZ));
            Vector3 v = Vector3.getNewVector().set(pos);
            BiomeGenBase b = v.getBiome(world);

            if (contains(b, Type.FOREST) && !contains(b, Type.CONIFEROUS))
            {
                if (world.getBlockState(pos.down()).getBlock() == grassBlock)
                {
                    placeCropAt(world, pos, "pecha");
                }
            }
        }

        // RAWST BERRY//
        if (rand.nextInt(4) == 0)
        {
            int randPosX = chunkX + rand.nextInt(15) + 1;
            int randPosZ = chunkZ + rand.nextInt(15) + 1;
            BlockPos pos = world.getTopSolidOrLiquidBlock(new BlockPos(randPosX, 0, randPosZ));
            Vector3 v = Vector3.getNewVector().set(pos);
            BiomeGenBase b = v.getBiome(world);

            if (contains(b, Type.MOUNTAIN) && contains(b, Type.HILLS))
            {
                if (world.getBlockState(pos.down()).getBlock() == grassBlock)
                {
                    placeCropAt(world, pos, "rawst");
                    placeCropAt(world, pos.east(), "rawst");
                }
            }
        }

        // ROWAP BERRY//
        if (rand.nextInt(4) == 0)
        {
            int randPosX = chunkX + rand.nextInt(15) + 1;
            int randPosZ = chunkZ + rand.nextInt(15) + 1;
            BlockPos pos = world.getTopSolidOrLiquidBlock(new BlockPos(randPosX, 0, randPosZ));
            Vector3 v = Vector3.getNewVector().set(pos);
            BiomeGenBase b = v.getBiome(world);

            if (contains(b, Type.FOREST) && contains(b, Type.CONIFEROUS))
            {
                if (world.getBlockState(pos.down()).getBlock() == grassBlock)
                {
                    placeCropAt(world, pos, "rowap");
                    placeCropAt(world, pos.east(), "rowap");
                    placeCropAt(world, pos.north(), "rowap");
                }
            }
        }

        // NANAB BERRY//
        if (rand.nextInt(8) == 0)
        {
            int randPosX = chunkX + rand.nextInt(15) + 1;
            int randPosZ = chunkZ + rand.nextInt(15) + 1;
            BlockPos pos = world.getTopSolidOrLiquidBlock(new BlockPos(randPosX, 0, randPosZ));
            Vector3 v = Vector3.getNewVector().set(pos);
            BiomeGenBase b = v.getBiome(world);

            if (contains(b, Type.JUNGLE) && !contains(b, Type.HILLS))
            {
                if (world.getBlockState(pos.down()).getBlock() == grassBlock)
                {
                    placeCropAt(world, pos, "nanab");
                }
            }
        }

        // PINAP BERRY//
        if (rand.nextInt(4) == 0)
        {
            int randPosX = chunkX + rand.nextInt(15) + 1;
            int randPosZ = chunkZ + rand.nextInt(15) + 1;
            BlockPos pos = world.getTopSolidOrLiquidBlock(new BlockPos(randPosX, 0, randPosZ));
            Vector3 v = Vector3.getNewVector().set(pos);
            BiomeGenBase b = v.getBiome(world);

            if (contains(b, Type.JUNGLE) && !contains(b, Type.HILLS))
            {
                if (world.getBlockState(pos.down()).getBlock() == grassBlock)
                {
                    placeCropAt(world, pos, "pinap");
                    placeCropAt(world, pos.east(), "pinap");
                    placeCropAt(world, pos.north(), "pinap");
                }
            }
        }

        // PERSIM BERRY//
        if (rand.nextInt(8) == 0)
        {
            int randPosX = chunkX + rand.nextInt(15) + 1;
            int randPosZ = chunkZ + rand.nextInt(15) + 1;
            BlockPos pos = world.getTopSolidOrLiquidBlock(new BlockPos(randPosX, 0, randPosZ));
            Vector3 v = Vector3.getNewVector().set(pos);
            BiomeGenBase b = v.getBiome(world);

            if (contains(b, Type.SWAMP))
            {
                if (world.getBlockState(pos.down()).getBlock() == grassBlock)
                {
                    placeCropAt(world, pos, "persim");
                    placeCropAt(world, pos.east(), "persim");
                }
            }
        }

        // JABOCA BERRY//
        if (rand.nextInt(2) == 0)
        {
            int randPosX = chunkX + rand.nextInt(15) + 1;
            int randPosZ = chunkZ + rand.nextInt(15) + 1;
            BlockPos pos = world.getTopSolidOrLiquidBlock(new BlockPos(randPosX, 0, randPosZ));
            Vector3 v = Vector3.getNewVector().set(pos);
            BiomeGenBase b = v.getBiome(world);

            if (contains(b, Type.MOUNTAIN) && contains(b, Type.HILLS))
            {
                if (world.getBlockState(pos.down()).getBlock() == grassBlock)
                {
                    placeCropAt(world, pos.east(), "jaboca");
                }
            }
        }

        // SITRUS BERRY//
        if (rand.nextInt(19) == 0)
        {
            int randPosX = chunkX + rand.nextInt(15) + 1;
            int randPosZ = chunkZ + rand.nextInt(15) + 1;
            BlockPos pos = world.getTopSolidOrLiquidBlock(new BlockPos(randPosX, 0, randPosZ));
            Vector3 v = Vector3.getNewVector().set(pos);
            BiomeGenBase b = v.getBiome(world);

            if (contains(b, Type.JUNGLE) && contains(b, Type.HILLS))
            {
                if (world.getBlockState(pos.down()).getBlock() == grassBlock)
                {
                    placeCropAt(world, pos, "sitrus");
                }
            }
        }

    }

    public void generateEnd(World world, Random rand, int chunkX, int chunkZ)
    {
        // ENIGMA BERRY//
        if (rand.nextInt(9) == 0)
        {
            int randPosX = chunkX + rand.nextInt(15) + 1;
            int randPosZ = chunkZ + rand.nextInt(15) + 1;
            BlockPos pos = world.getTopSolidOrLiquidBlock(new BlockPos(randPosX, 0, randPosZ));
            if (world.getBiomeGenForCoords(pos) == (BiomeGenBase.sky))
            {
                if (world.getBlockState(pos.down()).getBlock() == Blocks.end_stone)
                {
                    placeCropAt(world, pos, "enigma");
                }
            }
        }
    }

    private void placeCropAt(World world, BlockPos pos, String berryName)
    {
        if (BerryManager.getBerryCrop(berryName) != null)
        {
            world.setBlockState(pos, BerryManager.getBerryCrop(berryName).getDefaultState());
            world.setBlockState(pos.down(), Blocks.farmland.getDefaultState());
        }
        else
        {
            System.err.println("Null Berry " + berryName);
        }
    }

    public boolean generateTree(World par1World, Random par2Random, BlockPos pos, IBlockState wood, IBlockState leaves)
    {
        int l = par2Random.nextInt(1) + 6;
        boolean flag = true;
        BlockPos temp;
        int y = pos.getY();
        int z = pos.getZ();
        int x = pos.getX();
        if (y >= 1 && y + l + 1 <= 256)
        {
            int i1;
            byte b0;
            int j1;
            int k1;

            for (i1 = y; i1 <= y + 1 + l; ++i1)
            {
                b0 = 1;

                if (i1 == y)
                {
                    b0 = 0;
                }

                if (i1 >= y + 1 + l - 2)
                {
                    b0 = 2;
                }

                for (int l1 = x - b0; l1 <= x + b0 && flag; ++l1)
                {
                    for (j1 = z - b0; j1 <= z + b0 && flag; ++j1)
                    {
                        if (i1 >= 0 && i1 < 256)
                        {
                            temp = new BlockPos(l1, i1, j1);
                            Block block = par1World.getBlockState(temp).getBlock();

                            if (!par1World.isAirBlock(temp) && !block.isLeaves(par1World, temp) && block != Blocks.grass
                                    && block != Blocks.dirt && !block.isWood(par1World, temp))
                            {
                                flag = false;
                            }
                        }
                        else
                        {
                            flag = false;
                        }
                    }
                }
            }
            flag = true;
            if (!flag)
            {
                return false;
            }
            else
            {
                temp = pos.down();
                Block soil = par1World.getBlockState(temp).getBlock();
                boolean isSoil = true;// (soil != null &&
                                      // soil.canSustainPlant(par1World, par3,
                                      // par4 - 1, par5, EnumFacing.UP,
                                      // (BlockSapling)Block.sapling));

                if (isSoil && y < 256 - l - 1)
                {
                    soil.onPlantGrow(par1World, temp, pos);
                    b0 = 3;
                    byte b1 = 0;
                    int i2;
                    int j2;
                    int k2;

                    for (j1 = y - b0 + l; j1 <= y + l; ++j1)
                    {
                        k1 = j1 - (y + l);
                        i2 = b1 + 1 - k1 / 2;

                        for (j2 = x - i2; j2 <= x + i2; ++j2)
                        {
                            k2 = j2 - x;

                            for (int l2 = z - i2; l2 <= z + i2; ++l2)
                            {
                                int i3 = l2 - z;

                                if (Math.abs(k2) != i2 || Math.abs(i3) != i2 || par2Random.nextInt(2) != 0 && k1 != 0)
                                {
                                    temp = new BlockPos(j2, j1, l2);
                                    Block block = par1World.getBlockState(temp).getBlock();

                                    if (block == null || block.canBeReplacedByLeaves(par1World, temp))
                                    {
                                        par1World.setBlockState(temp, leaves);
                                    }
                                }
                            }
                        }
                    }

                    for (j1 = 0; j1 < l; ++j1)
                    {
                        temp = new BlockPos(x, y + j1, z);
                        Block block = par1World.getBlockState(temp).getBlock();

                        if (block == null || block.isAir(par1World, temp) || block.isLeaves(par1World, temp))
                        {
                            par1World.setBlockState(temp, wood);
                        }
                    }
                }

                return true;
            }
        }
        else
        {
            return false;
        }
    }

    public boolean generatePalmTree(World world, Random par2Random, BlockPos pos, IBlockState wood, IBlockState leaves)
    {
        int l = par2Random.nextInt(1) + 5;
        BlockPos temp;
        if (pos.getY() >= 1 && pos.getY() + l + 1 <= 256)
        {
            boolean stopped = false;
            // Trunk
            for (int i = 1; i < l; i++)
            {
                if (world.isAirBlock(temp = pos.up(i))) world.setBlockState(temp, wood);
                else
                {
                    stopped = true;
                    break;
                }
            }

            if (!stopped)
            {
                int d = 0;
                int d1 = 0;
                for (int i = 0; i <= l / 1.5; i++)
                {
                    d = i / 3;
                    temp = new BlockPos(pos.getX(), pos.getY() + l - d, pos.getZ() + i);

                    if (world.isAirBlock(temp)) world.setBlockState(temp, leaves);
                    else if (i != 0)
                    {
                        stopped = true;
                        break;
                    }
                    if (d1 != d)
                    {
                        temp = new BlockPos(pos.getX(), pos.getY() + l - d, pos.getZ() + i - 1);
                        if (world.isAirBlock(temp)) world.setBlockState(temp, leaves);
                    }

                    d1 = d;
                }
                d1 = 0;
                for (int i = 0; i <= l / 1.5; i++)
                {
                    d = i / 3;
                    temp = new BlockPos(pos.getX(), pos.getY() + l - d, pos.getZ() - i);
                    if (world.isAirBlock(temp)) world.setBlockState(temp, leaves);
                    else if (i != 0)
                    {
                        stopped = true;
                        break;
                    }
                    if (d1 != d)
                    {
                        temp = new BlockPos(pos.getX(), pos.getY() + l - d, pos.getZ() - i + 1);
                        if (world.isAirBlock(temp)) world.setBlockState(temp, leaves);
                    }
                    d1 = d;
                }
                d1 = 0;
                for (int i = 0; i <= l / 1.5; i++)
                {
                    d = i / 3;
                    temp = new BlockPos(pos.getX() + i, pos.getY() + l - d, pos.getZ());
                    if (world.isAirBlock(temp)) world.setBlockState(temp, leaves);
                    else if (i != 0)
                    {
                        stopped = true;
                        break;
                    }
                    if (d1 != d)
                    {
                        temp = new BlockPos(pos.getX() + i - 1, pos.getY() + l - d, pos.getZ());
                        if (world.isAirBlock(temp)) world.setBlockState(temp, leaves);
                    }
                    d1 = d;
                }
                d1 = 0;
                for (int i = 0; i <= l / 1.5; i++)
                {
                    d = i / 3;
                    temp = new BlockPos(pos.getX() - i, pos.getY() + l - d, pos.getZ());

                    if (world.isAirBlock(temp)) world.setBlockState(temp, leaves);
                    else if (i != 0)
                    {
                        stopped = true;
                        break;
                    }
                    if (d1 != d)
                    {
                        temp = new BlockPos(pos.getX() - i + 1, pos.getY() + l - d, pos.getZ());
                        if (world.isAirBlock(temp)) world.setBlockState(temp, leaves);
                    }
                    d1 = d;
                }
            }

            return true;

        }
        return false;
    }

}