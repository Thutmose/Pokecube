/**
 * 
 */
package pokecube.core.items.berries;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Optional;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import pokecube.core.PokecubeItems;
import pokecube.core.blocks.berries.BerryGenManager.GenericGrower;
import pokecube.core.blocks.berries.BerryGenManager.PalmGrower;
import pokecube.core.blocks.berries.BlockBerryLog;
import pokecube.core.blocks.berries.BlockBerryWood;
import pokecube.core.blocks.berries.TileEntityBerries;
import pokecube.core.handlers.ItemHandler;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.items.UsableItemEffects;
import pokecube.core.items.UsableItemEffects.BerryUsable.BerryEffect;

/** @author Oracion
 * @author Manchou */
public class BerryManager
{
    public static final IProperty<String> type          = new IProperty<String>()
                                                        {
                                                            @Override
                                                            public Collection<String> getAllowedValues()
                                                            {
                                                                return BerryManager.berryNames.values();
                                                            }

                                                            @Override
                                                            public String getName()
                                                            {
                                                                return "type";
                                                            }

                                                            @Override
                                                            public String getName(String value)
                                                            {
                                                                return value;
                                                            }

                                                            @Override
                                                            public Class<String> getValueClass()
                                                            {
                                                                return String.class;
                                                            }

                                                            @Override
                                                            public Optional<String> parseValue(String value)
                                                            {
                                                                return Optional.<String> fromNullable(value);
                                                            }
                                                        };

    public static Block                   berryFruit;
    public static Block                   berryCrop;
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

    public static void addBerry(String name, int id, int spicy, int dry, int sweet, int bitter, int sour,
            BerryEffect effect)
    {
        berryNames.put(id, name);
        berryFlavours.put(id, new int[] { spicy, dry, sweet, bitter, sour });
        PokecubeItems.addSpecificItemStack(name + "berry", new ItemStack(PokecubeItems.berries, 1, id));
        PokecubeItems.addSpecificItemStack(name, new ItemStack(PokecubeItems.berries, 1, id));
        PokecubeItems.addToHoldables(name);
        if (effect != null)
        {
            UsableItemEffects.BerryUsable.effects.put(id, effect);
        }
    }

    public static void addBerry(String name, int id, int spicy, int dry, int sweet, int bitter, int sour)
    {
        addBerry(name, id, spicy, dry, sweet, bitter, sour, null);
    }

    public static Block getBerryCrop(String name)
    {
        for (Block berryCrop : berryCrops.values())
        {
            if (berryCrop.getUnlocalizedName().toLowerCase(java.util.Locale.ENGLISH)
                    .contains(name.toLowerCase(java.util.Locale.ENGLISH)))
                return berryCrop;
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
        TileEntityBerries.trees.put(7, new GenericGrower(
                ItemHandler.log0.getDefaultState().withProperty(BlockBerryLog.VARIANT0, BlockBerryWood.EnumType.ORAN)));
        TileEntityBerries.trees.put(10, new GenericGrower(ItemHandler.log0.getDefaultState()
                .withProperty(BlockBerryLog.VARIANT0, BlockBerryWood.EnumType.SITRUS)));
        TileEntityBerries.trees.put(60, new GenericGrower(ItemHandler.log1.getDefaultState()
                .withProperty(BlockBerryLog.VARIANT4, BlockBerryWood.EnumType.ENIGMA)));
        TileEntityBerries.trees.put(18, new PalmGrower(ItemHandler.log1.getDefaultState()
                .withProperty(BlockBerryLog.VARIANT4, BlockBerryWood.EnumType.NANAB)));

        // EV Berries
        TileEntityBerries.trees.put(21, new GenericGrower(Blocks.LOG.getDefaultState()));
        TileEntityBerries.trees.put(22, new GenericGrower(Blocks.LOG.getDefaultState()));
        TileEntityBerries.trees.put(23, new GenericGrower(Blocks.LOG.getDefaultState()));
        TileEntityBerries.trees.put(24, new GenericGrower(Blocks.LOG.getDefaultState()));
        TileEntityBerries.trees.put(25, new GenericGrower(Blocks.LOG.getDefaultState()));
        TileEntityBerries.trees.put(26, new GenericGrower(Blocks.LOG.getDefaultState()));
    }
}
