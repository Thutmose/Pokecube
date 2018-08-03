/**
 * 
 */
package pokecube.core.items.berries;

import java.util.Collection;

import com.google.common.base.Optional;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import pokecube.core.PokecubeItems;
import pokecube.core.blocks.berries.BerryGenManager.GenericGrower;
import pokecube.core.blocks.berries.TileEntityBerries;
import pokecube.core.handlers.ItemGenerator;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.items.UsableItemEffects;
import pokecube.core.items.UsableItemEffects.BerryUsable.BerryEffect;

/** @author Oracion
 * @author Manchou */
public class BerryManager
{
    public static final IProperty<String>    type          = new IProperty<String>()
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

    public static Block                      berryFruit;
    public static Block                      berryCrop;
    public static Block                      berryLeaf;
    /** Map of berry id -> block of crop */
    public static Int2ObjectArrayMap<Block>  berryCrops    = new Int2ObjectArrayMap<>();
    /** Map of berry id -> block of fruit */
    public static Int2ObjectArrayMap<Block>  berryFruits   = new Int2ObjectArrayMap<>();
    /** Map of berry id -> block of fruit */
    public static Int2ObjectArrayMap<Item>   berryItems    = new Int2ObjectArrayMap<>();
    /** Map of berry id -> name of berry */
    public static Int2ObjectArrayMap<String> berryNames    = new Int2ObjectArrayMap<>();
    /** Map of berry id -> flavours of berry, see {@link IMoveConstants.SPICY}
     * for the indecies of the array */
    public static Int2ObjectArrayMap<int[]>  berryFlavours = new Int2ObjectArrayMap<>();

    public static void addBerry(String name, int id, int spicy, int dry, int sweet, int bitter, int sour,
            BerryEffect effect)
    {
        berryNames.put(id, name);
        berryFlavours.put(id, new int[] { spicy, dry, sweet, bitter, sour });
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

    public static void initBerries()
    {
        OreDictionary.registerOre("berry", new ItemStack(PokecubeItems.nullberry, 1, OreDictionary.WILDCARD_VALUE));
        for (int id : berryNames.keySet())
        {
            String name = berryNames.get(id);
            ItemStack stack = getBerryItem(id);
            PokecubeItems.addSpecificItemStack(name + "berry", stack);
            OreDictionary.registerOre(name + "Berry", stack);
            PokecubeItems.addSpecificItemStack(name, stack);
            PokecubeItems.addToHoldables(name);
        }
    }

    public static ItemStack getBerryItem(int id)
    {
        if (!berryItems.containsKey(id)) return new ItemStack(PokecubeItems.nullberry);
        return new ItemStack(berryItems.get(id));
    }

    public static ItemStack getBerryItem(String name)
    {
        return PokecubeItems.getStack(name);
    }

    public static void registerTrees()
    {
        TileEntityBerries.trees.put(3, new GenericGrower(ItemGenerator.logs.get("pecha").getDefaultState()));
        TileEntityBerries.trees.put(6, new GenericGrower(ItemGenerator.logs.get("leppa").getDefaultState()));
        TileEntityBerries.trees.put(7, new GenericGrower(ItemGenerator.logs.get("oran").getDefaultState()));
        TileEntityBerries.trees.put(10, new GenericGrower(ItemGenerator.logs.get("sitrus").getDefaultState()));
        TileEntityBerries.trees.put(60, new GenericGrower(ItemGenerator.logs.get("enigma").getDefaultState()));
        TileEntityBerries.trees.put(18, new GenericGrower(ItemGenerator.logs.get("nanab").getDefaultState()));

        // EV Berries
        TileEntityBerries.trees.put(21, new GenericGrower(Blocks.LOG.getDefaultState()));
        TileEntityBerries.trees.put(22, new GenericGrower(Blocks.LOG.getDefaultState()));
        TileEntityBerries.trees.put(23, new GenericGrower(Blocks.LOG.getDefaultState()));
        TileEntityBerries.trees.put(24, new GenericGrower(Blocks.LOG.getDefaultState()));
        TileEntityBerries.trees.put(25, new GenericGrower(Blocks.LOG.getDefaultState()));
        TileEntityBerries.trees.put(26, new GenericGrower(Blocks.LOG.getDefaultState()));
    }
}
