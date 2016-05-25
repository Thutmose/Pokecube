/**
 * 
 */
package pokecube.core.items.berries;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import pokecube.core.PokecubeItems;
import pokecube.core.blocks.berries.TileEntityBerries;
import pokecube.core.blocks.berries.TileEntityBerries.TreeGrower;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;

/** @author Oracion
 * @author Manchou */
public class BerryManager implements IMoveConstants
{
    public static final IProperty<String> type          = new IProperty<String>()
                                                        {
                                                            @Override
                                                            public String getName()
                                                            {
                                                                return "type";
                                                            }

                                                            @Override
                                                            public Collection<String> getAllowedValues()
                                                            {
                                                                return BerryManager.berryNames.values();
                                                            }

                                                            @Override
                                                            public Class<String> getValueClass()
                                                            {
                                                                return String.class;
                                                            }

                                                            @Override
                                                            public String getName(String value)
                                                            {
                                                                return value;
                                                            }
                                                        };

    public static Block                   berryFruit;
    public static Block                   berryCrop;
    public static Block                   berryLog;
    public static Block                   berryLeaf;
    /** Map of berry id -> block of crop */
    public static Map<Integer, Block>     berryCrops    = new HashMap<Integer, Block>();
    /** Map of berry id -> block of fruit */
    public static Map<Integer, Block>     berryFruits   = new HashMap<Integer, Block>();
    /** Map of berry id -> name of berry */
    public static Map<Integer, String>    berryNames    = new HashMap<Integer, String>();
    /** Map of berry id -> flavours of berry, see {@link IMoveConstants.SPICY}
     * for the indecies of the array */
    public static Map<Integer, int[]>     berryFlavours = new HashMap<Integer, int[]>();

    public static void addBerry(String name, int id, int spicy, int dry, int sweet, int bitter, int sour)
    {
        berryNames.put(id, name);
        berryFlavours.put(id, new int[] { spicy, dry, sweet, bitter, sour });
        PokecubeItems.addSpecificItemStack(name + "berry", new ItemStack(PokecubeItems.berries, 1, id));
        PokecubeItems.addSpecificItemStack(name, new ItemStack(PokecubeItems.berries, 1, id));
        PokecubeItems.addToHoldables(name);
    }

    public static boolean berryEffect(IPokemob pokemob, ItemStack berry)
    {

        byte status = pokemob.getStatus();
        if (!berryNames.containsKey(berry.getItemDamage())) return false;

        String berryName = berryNames.get(berry.getItemDamage());

        if (status == STATUS_PAR && berryName.equalsIgnoreCase("cheri"))
        {
            pokemob.healStatus();
            return true;
        }
        if (status == STATUS_SLP && berryName.equalsIgnoreCase("chesto"))
        {
            pokemob.healStatus();
            return true;
        }
        if ((status == STATUS_PSN || status == STATUS_PSN2) && berryName.equalsIgnoreCase("pecha"))
        {
            pokemob.healStatus();
            return true;
        }
        if (status == STATUS_BRN && berryName.equalsIgnoreCase("rawst"))
        {
            pokemob.healStatus();
            return true;
        }
        if (status == STATUS_FRZ && berryName.equalsIgnoreCase("aspear"))
        {
            pokemob.healStatus();
            return true;
        }
        if (status != STATUS_NON && berryName.equalsIgnoreCase("lum"))
        {
            pokemob.healStatus();
            return true;
        }
        EntityLivingBase entity = (EntityLivingBase) pokemob;
        float HP = entity.getHealth();
        float HPmax = entity.getMaxHealth();
        if (HP < HPmax / 3f)
        {
            if (berryName.equalsIgnoreCase("oran"))
            {
                entity.heal(10);
                return true;
            }
            else if (berryName.equalsIgnoreCase("sitrus"))
            {
                entity.heal(HPmax / 4f);
                return true;
            }
            else if (berryName.equalsIgnoreCase("enigma"))
            {
                entity.heal(HPmax / 4f);
                return true;
            }
        }

        return false;
    }

    public static Block getBerryCrop(String name)
    {
        for (Block berryCrop : berryCrops.values())
        {
            if (berryCrop.getUnlocalizedName().toLowerCase().contains(name.toLowerCase())) return berryCrop;
        }
        return null;
    }
    
    public static ItemStack getRandomBerryForBiome()
    {
        return null;
    }

    public static ItemStack getBerryItem(int id)
    {
        return getBerryItem(berryNames.get(id));
    }

    public static ItemStack getBerryItem(String name)
    {
        return PokecubeItems.getStack(name);
    }
    
    public static void registerTrees()
    {
        TileEntityBerries.trees.put(3, new GenericGrower());
        TileEntityBerries.trees.put(6, new GenericGrower());
        TileEntityBerries.trees.put(7, new GenericGrower());
        TileEntityBerries.trees.put(10, new GenericGrower());
        TileEntityBerries.trees.put(60, new GenericGrower());
        TileEntityBerries.trees.put(18, new PalmGrower());
    }
    
    public static void placeBerryLeaf(World world, BlockPos pos, int berryId)
    {
        world.setBlockState(pos, berryLeaf.getDefaultState());
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
                                          // soil.canSustainPlant(par1World, par3,
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

                                    if (Math.abs(k2) != i2 || Math.abs(i3) != i2 || world.rand.nextInt(2) != 0 && k1 != 0)
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
            IBlockState wood = Blocks.log.getDefaultState();
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
}
