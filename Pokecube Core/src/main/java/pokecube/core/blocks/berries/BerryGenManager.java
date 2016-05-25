package pokecube.core.blocks.berries;

import java.util.HashMap;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.block.Block;
import net.minecraft.block.BlockOldLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary;
import pokecube.core.blocks.berries.TileEntityBerries.TreeGrower;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.berries.BerryManager;
import thut.api.maths.Vector3;
import thut.api.terrain.BiomeDatabase;
import thut.api.terrain.BiomeType;
import thut.api.terrain.TerrainManager;
import thut.api.terrain.TerrainSegment;

public class BerryGenManager
{
    public static HashMap<Integer, List<ItemStack>> berryLocations = Maps.newHashMap();

    public static void placeBerryLeaf(World world, BlockPos pos, int berryId)
    {
        world.setBlockState(pos, BerryManager.berryLeaf.getDefaultState());
        TileEntityBerries tile = (TileEntityBerries) world.getTileEntity(pos);
        tile.setBerryId(berryId);
    }

    public static class GenericGrower implements TreeGrower
    {
        @Override
        public void growTree(World world, BlockPos pos, int berryId)
        {
            IBlockState wood = Blocks.log.getDefaultState();
            int l = world.rand.nextInt(1) + 6;
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
                                Block block = world.getBlockState(temp).getBlock();

                                if (!world.isAirBlock(temp) && !block.isLeaves(world, temp) && block != Blocks.grass
                                        && block != Blocks.dirt && !block.isWood(world, temp))
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
                    return;
                }
                else
                {
                    temp = pos.down();
                    Block soil = world.getBlockState(temp).getBlock();
                    boolean isSoil = true;// (soil != null &&
                                          // soil.canSustainPlant(par1World,
                                          // par3,
                                          // par4 - 1, par5, EnumFacing.UP,
                                          // (BlockSapling)Block.sapling));

                    if (isSoil && y < 256 - l - 1)
                    {
                        soil.onPlantGrow(world, temp, pos);
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

                                    if (Math.abs(k2) != i2 || Math.abs(i3) != i2
                                            || world.rand.nextInt(2) != 0 && k1 != 0)
                                    {
                                        temp = new BlockPos(j2, j1, l2);
                                        Block block = world.getBlockState(temp).getBlock();

                                        if (block == null || block.canBeReplacedByLeaves(world, temp))
                                        {
                                            if (world.isAirBlock(temp)) placeBerryLeaf(world, temp, berryId);
                                        }
                                    }
                                }
                            }
                        }

                        world.setBlockState(pos, wood);
                        for (j1 = 0; j1 < l; ++j1)
                        {
                            temp = new BlockPos(x, y + j1, z);
                            Block block = world.getBlockState(temp).getBlock();

                            if (block == null || block.isAir(world, temp) || block.isLeaves(world, temp))
                            {
                                world.setBlockState(temp, wood);
                            }
                        }
                    }
                }
            }
        }
    }

    public static class PalmGrower implements TreeGrower
    {
        @Override
        public void growTree(World world, BlockPos pos, int berryId)
        {
            int l = world.rand.nextInt(1) + 5;
            BlockPos temp;
            IBlockState wood = Blocks.log.getDefaultState().withProperty(BlockOldLog.VARIANT,
                    BlockPlanks.EnumType.JUNGLE);
            if (pos.getY() >= 1 && pos.getY() + l + 1 <= 256)
            {
                boolean stopped = false;
                // Trunk
                world.setBlockState(pos, wood);
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

                        if (world.isAirBlock(temp)) placeBerryLeaf(world, temp, berryId);
                        else if (i != 0)
                        {
                            stopped = true;
                            break;
                        }
                        if (d1 != d)
                        {
                            temp = new BlockPos(pos.getX(), pos.getY() + l - d, pos.getZ() + i - 1);
                            if (world.isAirBlock(temp)) placeBerryLeaf(world, temp, berryId);
                        }

                        d1 = d;
                    }
                    d1 = 0;
                    for (int i = 0; i <= l / 1.5; i++)
                    {
                        d = i / 3;
                        temp = new BlockPos(pos.getX(), pos.getY() + l - d, pos.getZ() - i);
                        if (world.isAirBlock(temp)) placeBerryLeaf(world, temp, berryId);
                        else if (i != 0)
                        {
                            stopped = true;
                            break;
                        }
                        if (d1 != d)
                        {
                            temp = new BlockPos(pos.getX(), pos.getY() + l - d, pos.getZ() - i + 1);
                            if (world.isAirBlock(temp)) placeBerryLeaf(world, temp, berryId);
                        }
                        d1 = d;
                    }
                    d1 = 0;
                    for (int i = 0; i <= l / 1.5; i++)
                    {
                        d = i / 3;
                        temp = new BlockPos(pos.getX() + i, pos.getY() + l - d, pos.getZ());
                        if (world.isAirBlock(temp)) placeBerryLeaf(world, temp, berryId);
                        else if (i != 0)
                        {
                            stopped = true;
                            break;
                        }
                        if (d1 != d)
                        {
                            temp = new BlockPos(pos.getX() + i - 1, pos.getY() + l - d, pos.getZ());
                            if (world.isAirBlock(temp)) placeBerryLeaf(world, temp, berryId);
                        }
                        d1 = d;
                    }
                    d1 = 0;
                    for (int i = 0; i <= l / 1.5; i++)
                    {
                        d = i / 3;
                        temp = new BlockPos(pos.getX() - i, pos.getY() + l - d, pos.getZ());

                        if (world.isAirBlock(temp)) placeBerryLeaf(world, temp, berryId);
                        else if (i != 0)
                        {
                            stopped = true;
                            break;
                        }
                        if (d1 != d)
                        {
                            temp = new BlockPos(pos.getX() - i + 1, pos.getY() + l - d, pos.getZ());
                            if (world.isAirBlock(temp)) placeBerryLeaf(world, temp, berryId);
                        }
                        d1 = d;
                    }
                }
            }
        }
    }

    public static ItemStack getRandomBerryForBiome(World world, BlockPos location)
    {
        TerrainSegment t = TerrainManager.getInstance().getTerrain(world, location);
        int i = t.getBiome(Vector3.getNewVector().set(location));
        if (berryLocations.isEmpty()) parseConfig();
        List<ItemStack> options = berryLocations.get(i);
        if (options == null || options.isEmpty()) options = berryLocations.get(BiomeType.ALL.getType());
        if (options != null && !options.isEmpty())
        {
            ItemStack ret = options.get(world.rand.nextInt(options.size())).copy();
            ret.stackSize = 1 + world.rand.nextInt(ret.stackSize + 10);
            return ret;
        }
        return null;
    }

    public static void parseConfig()
    {
        for (BiomeGenBase b : BiomeGenBase.getBiomeGenArray())
        {
            if (b != null)
            {
                for (String s : PokecubeMod.core.getConfig().berryLocations)
                {
                    String[] args = s.split(":");
                    String berryName = args[0];
                    String[] locations = args[1].split("\'");
                    for (String s1 : locations)
                    {
                        boolean valid = false;
                        if (s1.startsWith("S"))
                        {
                            valid = checkNormal(-1, b, s1.substring(1));
                        }
                        else if (s1.startsWith("T"))
                        {
                            valid = checkPerType(b, s1.substring(1));
                        }
                        if (valid)
                        {
                            addToList(b.biomeID, berryName);
                        }
                    }
                }
            }
        }
        for (ResourceLocation key : BiomeDatabase.biomeTypeRegistry.getKeys())
        {
            BiomeType type = BiomeDatabase.biomeTypeRegistry.getObject(key);
            for (String s : PokecubeMod.core.getConfig().berryLocations)
            {
                String[] args = s.split(":");
                String berryName = args[0];
                String[] locations = args[1].split("\'");
                for (String s1 : locations)
                {
                    boolean valid = false;
                    if (s1.startsWith("S"))
                    {
                        valid = checkNormal(type.getType(), null, s1.substring(1));
                    }
                    if (valid)
                    {
                        addToList(type.getType(), berryName);
                    }
                }
            }
        }
    }

    private static void addToList(int biomeId, String berryType)
    {
        List<ItemStack> stacks = berryLocations.get(biomeId);
        if (stacks == null)
        {
            stacks = Lists.newArrayList();
            berryLocations.put(biomeId, stacks);
        }
        ItemStack berry = BerryManager.getBerryItem(berryType);
        stacks.add(berry);
    }

    private static boolean checkNormal(int biomeid, BiomeGenBase b, String biome)
    {
        int type = -1;

        for (BiomeType b1 : BiomeType.values())
        {
            if (b1.name.replaceAll(" ", "").equalsIgnoreCase(biome)) type = b1.getType();
        }
        if (type == -1)
        {
            for (BiomeGenBase b1 : BiomeGenBase.getBiomeGenArray())
            {
                if (b1 != null) if (b1.biomeName.replaceAll(" ", "").equalsIgnoreCase(biome)) type = b1.biomeID;
            }
        }
        if (type == -1 && b != null)
        {
            for (BiomeDictionary.Type t : BiomeDictionary.Type.values())
            {
                if (t.toString().equalsIgnoreCase(biome)) { return BiomeDictionary.isBiomeOfType(b, t); }
            }
        }
        else
        {
            int tb = biomeid;
            int vb = b != null ? b.biomeID : -1;
            if (tb == type || vb == type) return true;
        }
        return false;
    }

    private static boolean checkPerType(BiomeGenBase b, String biome)
    {
        String[] args = biome.split(",");
        List<BiomeDictionary.Type> neededTypes = Lists.newArrayList();
        List<BiomeDictionary.Type> bannedTypes = Lists.newArrayList();
        for (String s : args)
        {
            String name = s.substring(1);
            if (s.startsWith("B"))
            {
                for (BiomeDictionary.Type t : BiomeDictionary.Type.values())
                {
                    if (t.toString().equalsIgnoreCase(name))
                    {
                        bannedTypes.add(t);
                    }
                }
            }
            else if (s.startsWith("W"))
            {
                for (BiomeDictionary.Type t : BiomeDictionary.Type.values())
                {
                    if (t.toString().equalsIgnoreCase(name))
                    {
                        neededTypes.add(t);
                    }
                }
            }
        }
        boolean correctType = true;
        boolean bannedType = false;
        for (BiomeDictionary.Type t : neededTypes)
        {
            correctType = correctType && BiomeDictionary.isBiomeOfType(b, t);
        }
        for (BiomeDictionary.Type t : bannedTypes)
        {
            bannedType = bannedType || BiomeDictionary.isBiomeOfType(b, t);
        }
        return correctType && !bannedType;
    }

}
