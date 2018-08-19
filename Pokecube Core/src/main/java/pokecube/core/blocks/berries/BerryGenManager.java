package pokecube.core.blocks.berries;

import java.io.File;
import java.io.FileReader;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.xml.namespace.QName;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.block.Block;
import net.minecraft.block.BlockOldLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import pokecube.core.PokecubeItems;
import pokecube.core.blocks.berries.TileEntityBerries.TreeGrower;
import pokecube.core.database.Database;
import pokecube.core.database.Database.EnumDatabase;
import pokecube.core.database.PokedexEntryLoader;
import pokecube.core.database.PokedexEntryLoader.SpawnRule;
import pokecube.core.database.SpawnBiomeMatcher;
import pokecube.core.database.SpawnBiomeMatcher.SpawnCheck;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.berries.BerryManager;
import thut.api.maths.Vector3;

public class BerryGenManager
{
    public static class GenericGrower implements TreeGrower
    {
        final IBlockState wood;

        public GenericGrower(IBlockState trunk)
        {
            if (trunk == null)
            {
                wood = Blocks.LOG.getDefaultState();
            }
            else
            {
                wood = trunk;
            }
        }

        @Override
        public void growTree(World world, BlockPos pos, int berryId)
        {
            int l = world.rand.nextInt(1) + 6;
            boolean flag = true;
            BlockPos temp;
            int y = pos.getY();
            int z = pos.getZ();
            int x = pos.getX();
            if (y >= 1 && y + l + 1 <= world.provider.getActualHeight())
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
                            if (i1 >= 0 && i1 < world.provider.getActualHeight())
                            {
                                temp = new BlockPos(l1, i1, j1);
                                Block block = world.getBlockState(temp).getBlock();

                                if (!world.isAirBlock(temp) && !block.isLeaves(world.getBlockState(temp), world, temp)
                                        && block != Blocks.GRASS && block != Blocks.DIRT && !block.isWood(world, temp))
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
                if (!flag) { return; }
                temp = pos.down();
                Block soil = world.getBlockState(temp).getBlock();
                boolean isSoil = true;// (soil != null &&
                                      // soil.canSustainPlant(par1World,
                                      // par3,
                                      // par4 - 1, par5, EnumFacing.UP,
                                      // (BlockSapling)Block.sapling));

                if (isSoil && y < world.provider.getActualHeight() - l - 1)
                {
                    soil.onPlantGrow(world.getBlockState(temp), world, temp, pos);
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

                                if (Math.abs(k2) != i2 || Math.abs(i3) != i2 || world.rand.nextInt(2) != 0 && k1 != 0)
                                {
                                    temp = new BlockPos(j2, j1, l2);
                                    Block block = world.getBlockState(temp).getBlock();

                                    if (block == null
                                            || block.canBeReplacedByLeaves(world.getBlockState(temp), world, temp))
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

                        if (block == null || block.isAir(world.getBlockState(temp), world, temp)
                                || block.isLeaves(world.getBlockState(temp), world, temp))
                        {
                            world.setBlockState(temp, wood);
                        }
                    }
                }
            }
        }
    }

    public static class PalmGrower implements TreeGrower
    {
        final IBlockState wood;

        public PalmGrower(IBlockState trunk)
        {
            if (trunk == null)
            {
                wood = Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.JUNGLE);
            }
            else
            {
                wood = trunk;
            }
        }

        @Override
        public void growTree(World world, BlockPos pos, int berryId)
        {
            int l = world.rand.nextInt(1) + 5;
            BlockPos temp;
            if (pos.getY() >= 1 && pos.getY() + l + 1 <= world.provider.getActualHeight())
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

    public static Map<SpawnBiomeMatcher, List<ItemStack>> berryLocations = Maps.newHashMap();
    private static List<SpawnBiomeMatcher>                matchers       = Lists.newArrayList();

    public static ItemStack getRandomBerryForBiome(World world, BlockPos location)
    {
        if (berryLocations.isEmpty()) parseConfig();
        SpawnBiomeMatcher toMatch = null;
        SpawnCheck checker = new SpawnCheck(Vector3.getNewVector().set(location), world);
        /** Shuffle list, then re-sort it. This allows the values of the same
         * priority to be randomized, but then still respect priority order for
         * specific ones. */
        Collections.shuffle(matchers);
        matchers.sort(COMPARE);
        for (SpawnBiomeMatcher matcher : matchers)
        {
            if (matcher.matches(checker))
            {
                toMatch = matcher;
                break;
            }
        }
        if (toMatch == null) return ItemStack.EMPTY;
        List<ItemStack> options = berryLocations.get(toMatch);
        if (options == null || options.isEmpty()) return ItemStack.EMPTY;
        ItemStack ret = options.get(world.rand.nextInt(options.size())).copy();
        int size = 1 + world.rand.nextInt(ret.getCount() + 5);
        ret.setCount(size);
        return ret;
    }

    public static void parseConfig()
    {
        berryLocations.clear();
        matchers.clear();
        if (list == null) loadConfig();
        if (list != null)
        {
            for (BerrySpawn rule : list.locations)
            {
                for (SpawnRule spawn : rule.spawn)
                {
                    SpawnBiomeMatcher matcher = new SpawnBiomeMatcher(spawn);
                    List<ItemStack> berries = Lists.newArrayList();
                    for (String s : rule.berry.split(","))
                    {
                        ItemStack berry = BerryManager.getBerryItem(s.trim());
                        if (!berry.isEmpty())
                        {
                            berries.add(berry);
                        }
                    }
                    if (!berries.isEmpty())
                    {
                        matchers.add(matcher);
                        berryLocations.put(matcher, berries);
                    }
                }
            }
        }
        if (berryLocations.isEmpty() && PokecubeMod.core.getConfig().autoAddNullBerries)
        {
            SpawnBiomeMatcher matcher = SpawnBiomeMatcher.ALLMATCHER;
            matcher.reset();
            List<ItemStack> berries = Lists.newArrayList();
            berries.add(new ItemStack(PokecubeItems.nullberry));
            matchers.add(matcher);
            berryLocations.put(matcher, berries);
        }
        if (!matchers.isEmpty())
        {
            matchers.sort(COMPARE);
        }
    }

    public static void placeBerryLeaf(World world, BlockPos pos, int berryId)
    {
        world.setBlockState(pos, BerryManager.berryLeaf.getDefaultState());
        TileEntityBerries tile = (TileEntityBerries) world.getTileEntity(pos);
        tile.setBerryId(berryId);
    }

    private static void loadConfig()
    {
        list = new BerryGenList();
        for (String s : Database.configDatabases.get(EnumDatabase.BERRIES.ordinal()))
        {
            if (s.isEmpty()) continue;
            File file = new File(Database.CONFIGLOC + "berries" + File.separator + s);
            if (!file.exists()) continue;
            try
            {
                BerryGenList loaded;
                FileReader reader = new FileReader(file);
                loaded = PokedexEntryLoader.gson.fromJson(reader, BerryGenList.class);
                reader.close();
                list.locations.addAll(loaded.locations);
            }
            catch (Exception e)
            {
                PokecubeMod.log(Level.WARNING, "Error loading Berries Spawn Database " + s, e);
            }
        }
    }

    private static BerryGenList                        list;
    private static final QName                         prior   = new QName("priority");
    private static final Comparator<SpawnBiomeMatcher> COMPARE = new Comparator<SpawnBiomeMatcher>()
                                                               {
                                                                   @Override
                                                                   public int compare(SpawnBiomeMatcher o1,
                                                                           SpawnBiomeMatcher o2)
                                                                   {
                                                                       Integer p1 = 50;
                                                                       Integer p2 = 50;
                                                                       if (o1.spawnRule.values.containsKey(prior))
                                                                       {
                                                                           p1 = Integer.parseInt(
                                                                                   o1.spawnRule.values.get(prior));
                                                                       }
                                                                       if (o2.spawnRule.values.containsKey(prior))
                                                                       {
                                                                           p2 = Integer.parseInt(
                                                                                   o2.spawnRule.values.get(prior));
                                                                       }
                                                                       return p1.compareTo(p2);
                                                                   }
                                                               };

    private static class BerryGenList
    {
        List<BerrySpawn> locations = Lists.newArrayList();
    }

    private static class BerrySpawn
    {
        public List<SpawnRule> spawn;
        public String          berry;
    }

}
