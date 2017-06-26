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
import java.util.List;

import com.google.common.base.Predicate;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.registry.IForgeRegistry;
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
        String[] STONES = { "minecraft:stone variant=stone", "minecraft:stone variant=granite",
                "minecraft:stone variant=diorite", "minecraft:stone variant=andesite", "minecraft:netherrack",
                "minecraft:sandstone type=sandstone", "minecraft:red_sandstone type=red_sandstone" };
        String[] ORE = { "minecraft:.*_ore" };
        String[] GROUND = { "minecraft:sand", "minecraft:gravel", "minecraft:stained_hardened_clay",
                "minecraft:hardened_clay", "minecraft:dirt", "minecraft:grass" };
        String[] PLANTS = { "minecraft:double_plant", "minecraft:red_flower", "minecraft:yellow_flower",
                "minecraft:tallgrass", "minecraft:deadbush", "pokecube:berryfruit" };
        String[] INDUSTRIAL = { "minecraft:redstone_block", "minecraft:furnace", "minecraft:lit_furnace",
                "minecraft:piston", "minecraft:sticky_piston", "minecraft:dispenser", "minecraft:dropper",
                "minecraft:hopper", "minecraft:anvil" };

        addToList(config.getTerrain(), STONES);
        addToList(config.getRocks(), STONES);
        addToList(config.getCaveBlocks(), STONES);
        addToList(config.getSurfaceBlocks(), GROUND);
        addToList(config.getTerrain(), GROUND);
        addToList(config.getSurfaceBlocks(), STONES);
        addToList(config.getRocks(), ORE);
        addToList(config.getDirtTypes(), GROUND);
        addToList(config.getPlantTypes(), PLANTS);
        addToList(config.getIndustrial(), INDUSTRIAL);

        for (Block block : allBlocks)
        {
            try
            {
                if (block.isWood(null, null))
                {
                    addToList(config.getWoodTypes(), block.getRegistryName().toString());
                }
                if (block.isLeaves(block.getDefaultState(), null, null))
                {
                    addToList(config.getPlantTypes(), block.getRegistryName().toString());
                }
            }
            catch (Exception e)
            {
                PokecubeMod.log("Error checking if " + block + " is wood or leaves");
            }
        }
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

    public void addVillagerTrades()
    {
        // TODO decide if I want to add these.
    }

    public void initAllBlocks()
    {
        allBlocks.clear();
        for (int i = 0; i < 4096; i++)
        {
            if (Block.getBlockById(i) != null) allBlocks.add(Block.getBlockById(i));
        }
        initLists();
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
        RecipeHandler.initRecipes();
        addToList(PokecubeMod.core.getConfig().getCaveBlocks(), PokecubeMod.core.getConfig().blockListCaveFloor);
        addToList(PokecubeMod.core.getConfig().getSurfaceBlocks(), PokecubeMod.core.getConfig().blockListSurface);
        addToList(PokecubeMod.core.getConfig().getRocks(), PokecubeMod.core.getConfig().blockListRocks);
        addToList(PokecubeMod.core.getConfig().getWoodTypes(), PokecubeMod.core.getConfig().blockListTreeBlocks);
        addToList(PokecubeMod.core.getConfig().getPlantTypes(),
                PokecubeMod.core.getConfig().blockListHarvestablePlants);
        addToList(PokecubeMod.core.getConfig().getTerrain(), PokecubeMod.core.getConfig().blockListMiscTerrain);
        addToList(PokecubeMod.core.getConfig().getIndustrial(),
                PokecubeCore.core.getConfig().blockListIndustrialBlocks);
        removeFromHoldables("tm");
    }
}
