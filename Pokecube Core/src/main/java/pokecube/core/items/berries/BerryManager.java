/**
 * 
 */
package pokecube.core.items.berries;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import pokecube.core.PokecubeItems;
import pokecube.core.blocks.berries.BerryGenManager.GenericGrower;
import pokecube.core.blocks.berries.BerryGenManager.PalmGrower;
import pokecube.core.blocks.berries.BlockBerryLog;
import pokecube.core.blocks.berries.BlockBerryWood;
import pokecube.core.blocks.berries.TileEntityBerries;
import pokecube.core.handlers.ItemHandler;
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
        TileEntityBerries.trees.put(3, new GenericGrower(ItemHandler.log0.getDefaultState()
                .withProperty(BlockBerryLog.VARIANT0, BlockBerryWood.EnumType.PECHA)));
        TileEntityBerries.trees.put(6, new GenericGrower(ItemHandler.log0.getDefaultState()
                .withProperty(BlockBerryLog.VARIANT0, BlockBerryWood.EnumType.LEPPA)));
        TileEntityBerries.trees.put(7, new GenericGrower(ItemHandler.log0.getDefaultState()
                .withProperty(BlockBerryLog.VARIANT0, BlockBerryWood.EnumType.ORAN)));
        TileEntityBerries.trees.put(10, new GenericGrower(ItemHandler.log0.getDefaultState()
                .withProperty(BlockBerryLog.VARIANT0, BlockBerryWood.EnumType.SITRUS)));
        TileEntityBerries.trees.put(60, new GenericGrower(ItemHandler.log1.getDefaultState()
                .withProperty(BlockBerryLog.VARIANT4, BlockBerryWood.EnumType.ENIGMA)));
        TileEntityBerries.trees.put(18, new PalmGrower(ItemHandler.log1.getDefaultState()
                .withProperty(BlockBerryLog.VARIANT4, BlockBerryWood.EnumType.NANAB)));
    }
}
