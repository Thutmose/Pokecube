/**
 *
 */
package pokecube.core;

import static pokecube.core.PokecubeItems.removeFromHoldables;
import static pokecube.core.handlers.Config.GUICHOOSEFIRSTPOKEMOB_ID;
import static pokecube.core.handlers.Config.GUIDISPLAYPOKECUBEINFO_ID;
import static pokecube.core.handlers.Config.GUIDISPLAYTELEPORTINFO_ID;
import static pokecube.core.handlers.Config.GUIPC_ID;
import static pokecube.core.handlers.Config.GUIPOKECENTER_ID;
import static pokecube.core.handlers.Config.GUIPOKEDEX_ID;
import static pokecube.core.handlers.Config.GUIPOKEMOBSPAWNER_ID;
import static pokecube.core.handlers.Config.GUIPOKEMOB_ID;
import static pokecube.core.handlers.Config.GUITMTABLE_ID;
import static pokecube.core.handlers.Config.GUITRADINGTABLE_ID;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.common.base.Predicate;
import com.google.common.collect.Sets;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraftforge.registries.GameData;
import net.minecraftforge.registries.IForgeRegistry;
import pokecube.core.handlers.Config;
import pokecube.core.handlers.ItemHandler;
import pokecube.core.handlers.RecipeHandler;
import pokecube.core.interfaces.PokecubeMod;

/** @author Manchou */
public class Mod_Pokecube_Helper
{

    public static final String   CATEGORY_ADVANCED = "advanced";

    public static HashSet<Block> allBlocks         = new HashSet<Block>();

    private static void addToList(List<Predicate<IBlockState>> list, String... conts)
    {
        if (conts == null) return;
        if (conts.length < 1) return;
        for (String s : conts)
        {
            Predicate<IBlockState> b = PokecubeItems.getState(s);
            if (b != null)
            {
                list.add(b);
            }
        }

    }

    static void initLists()
    {
        Config config = PokecubeCore.core.getConfig();
        if (!config.autoPopulateLists) return;

        Set<String> detectedLeaves = Sets.newHashSet();
        Set<String> detectedWood = Sets.newHashSet();
        for (Block block : allBlocks)
        {
            try
            {
                if (block.isWood(null, null))
                {
                    detectedWood.add(block.getRegistryName().toString());
                }
                if (block.isLeaves(block.getDefaultState(), null, null))
                {
                    detectedLeaves.add(block.getRegistryName().toString());
                }
            }
            catch (Exception e)
            {
                PokecubeMod.log("Error checking if " + block + " is wood or leaves");
            }
        }
        for (String s : config.blocksLeaves)
            detectedLeaves.add(s);
        for (String s : config.blocksWood)
            detectedWood.add(s);
        config.blocksLeaves = detectedLeaves.toArray(new String[0]);
        config.blocksWood = detectedWood.toArray(new String[0]);
        config.setSettings();
    }

    public Mod_Pokecube_Helper()
    {
        ItemHandler.initBerries();
    }

    public void itemRegistry(IForgeRegistry<Item> iForgeRegistry)
    {
        ItemHandler.registerItems(iForgeRegistry);
    }

    public void blockRegistry(IForgeRegistry<Block> iForgeRegistry)
    {
        ItemHandler.registerBlocks(iForgeRegistry);
    }

    public void tileRegistry(IForgeRegistry<Block> iForgeRegistry)
    {
        ItemHandler.registerTiles(iForgeRegistry);
    }

    public void initAllBlocks()
    {
        allBlocks.clear();
        Iterator<Block> iter = GameData.getWrapper(Block.class).iterator();
        while (iter.hasNext())
        {
            Block b = iter.next();
            if (b != null) allBlocks.add(b);
        }
        initLists();
    }

    public void registerRecipes(Object event)
    {
        RecipeHandler.initRecipes(event);
    }

    public void postInit()
    {
        // Gui
        GUICHOOSEFIRSTPOKEMOB_ID = 11;
        GUIDISPLAYPOKECUBEINFO_ID = 12;
        GUIPOKECENTER_ID = 13;
        GUIPOKEDEX_ID = 14;
        GUIPOKEMOBSPAWNER_ID = 15;
        GUITRADINGTABLE_ID = 16;
        GUIPC_ID = 17;
        GUIDISPLAYTELEPORTINFO_ID = 18;
        GUIPOKEMOB_ID = 19;
        GUITMTABLE_ID = 20;

        Config config = PokecubeCore.core.getConfig();

        addToList(config.getTerrain(), config.blocksStones);
        addToList(config.getTerrain(), config.blocksTerrain);
        addToList(config.getRocks(), config.blocksStones);
        addToList(config.getCaveBlocks(), config.blocksStones);
        addToList(config.getSurfaceBlocks(), config.blocksGround);
        addToList(config.getTerrain(), config.blocksGround);
        addToList(config.getSurfaceBlocks(), config.blocksStones);
        addToList(config.getRocks(), config.blocksOre);
        addToList(config.getDirtTypes(), config.blocksGround);
        addToList(config.getPlantTypes(), config.blocksPlants);
        addToList(config.getIndustrial(), config.blocksStones);
        addToList(config.getWoodTypes(), config.blocksWood);
        addToList(config.getPlantTypes(), config.blocksLeaves);

        removeFromHoldables("tm");
    }
}
