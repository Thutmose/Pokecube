package pokecube.adventures.handlers;

import static pokecube.core.PokecubeItems.register;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;
import pokecube.adventures.blocks.berries.BerryPlantManager;
import pokecube.adventures.blocks.berries.BlockBerryLeaves;
import pokecube.adventures.blocks.berries.BlockBerryLog;
import pokecube.adventures.blocks.berries.BlockBerryWood;
import pokecube.adventures.blocks.berries.ItemBlockMeta;
import pokecube.adventures.blocks.cloner.BlockCloner;
import pokecube.adventures.blocks.warppad.BlockWarpPad;
import pokecube.adventures.blocks.warppad.TileEntityWarpPad;
import pokecube.core.PokecubeItems;
import pokecube.core.mod_Pokecube;
import pokecube.core.items.berries.TileEntityBerryFruit;
import pokecube.core.moves.TreeRemover;

public class BlockHandler
{
    public static Block log0;
    public static Block log1;
    public static Block plank0;
    public static Block leaf0;
    public static Block leaf1;
    public static Block warppad;
    public static Block cloner;

    public static void registerBlocks()
    {
        addBerries();

        warppad = new BlockWarpPad().setUnlocalizedName("warppad");
        PokecubeItems.register(warppad, "warppad");
        warppad.setCreativeTab(mod_Pokecube.creativeTabPokecubeBlocks);
        GameRegistry.registerTileEntity(TileEntityWarpPad.class, "warppad");

        cloner = new BlockCloner().setUnlocalizedName("cloner");
        cloner.setCreativeTab(mod_Pokecube.creativeTabPokecubeBlocks);
        PokecubeItems.register(cloner, "cloner");

    }

    public static void addBerries()
    {
        BerryPlantManager.addBerry("cheri", 1);// Cures Paralysis
        BerryPlantManager.addBerry("chesto", 2);// Cures sleep
        BerryPlantManager.addBerry("pecha", 3);// Cures poison
        BerryPlantManager.addBerry("rawst", 4);// Cures burn
        BerryPlantManager.addBerry("aspear", 5);// Cures freeze
        BerryPlantManager.addBerry("leppa", 6);// Restores 10PP
        BerryPlantManager.addBerry("oran", 7);// Restores 10HP
        BerryPlantManager.addBerry("persim", 8);// Cures confusion
        BerryPlantManager.addBerry("lum", 9);// Cures any status ailment
        BerryPlantManager.addBerry("sitrus", 10);// Restores 1/4 HP
        BerryPlantManager.addBerry("nanab", 18);// Pokeblock ingredient
        BerryPlantManager.addBerry("pinap", 20);// Pokeblock ingredient
        BerryPlantManager.addBerry("cornn", 27);// Pokeblock ingredient
        BerryPlantManager.addBerry("enigma", 60);// Restores 1/4 of HP
        BerryPlantManager.addBerry("jaboca", 63);// 4th gen. Causes recoil
                                                 // damage on foe if holder is
                                                 // hit by a physical move
        BerryPlantManager.addBerry("rowap", 64);// 4th gen. Causes recoil damage
                                                // on foe if holder is hit by a
                                                // special move

        String[] names = { "pecha", "oran", "leppa", "sitrus" };
        BlockBerryLog.currentlyConstructing = 0;
        log0 = new BlockBerryLog(0, names).setHardness(2.0F).setStepSound(Block.soundTypeWood)
                .setUnlocalizedName("log0");
        leaf0 = new BlockBerryLeaves(0, names).setHardness(0.2F).setLightOpacity(1).setStepSound(Block.soundTypeGrass)
                .setUnlocalizedName("leaves0");
        plank0 = new BlockBerryWood(0, names).setHardness(2.0F).setResistance(5.0F).setStepSound(Block.soundTypeWood)
                .setUnlocalizedName("wood0");

        register(log0, ItemBlockMeta.class, "pokecube_log0");
        register(plank0, ItemBlockMeta.class, "pokecube_plank0");
        register(leaf0, ItemBlockMeta.class, "pokecube_leaf0");

        names = new String[] { "enigma", "nanab" };
        BlockBerryLog.currentlyConstructing = 4;
        log1 = new BlockBerryLog(4, names).setHardness(2.0F).setStepSound(Block.soundTypeWood)
                .setUnlocalizedName("log1");
        leaf1 = new BlockBerryLeaves(4, names).setHardness(0.2F).setLightOpacity(1).setStepSound(Block.soundTypeGrass)
                .setUnlocalizedName("leaves1");

        register(log1, ItemBlockMeta.class, "pokecube_log1");
        register(leaf1, ItemBlockMeta.class, "pokecube_leaf1");

        for (int i = 0; i < 4; i++)
            GameRegistry.addShapelessRecipe(new ItemStack(plank0, 4, i), new ItemStack(log0, 1, i));

        for (int i = 0; i < 2; i++)
            GameRegistry.addShapelessRecipe(new ItemStack(plank0, 4, i + 4), new ItemStack(log1, 1, i));

        OreDictionary.registerOre("logWood", new ItemStack(log0, 1, OreDictionary.WILDCARD_VALUE));
        OreDictionary.registerOre("logWood", new ItemStack(log1, 1, OreDictionary.WILDCARD_VALUE));

        OreDictionary.registerOre("plankWood", new ItemStack(plank0, 1, OreDictionary.WILDCARD_VALUE));

        OreDictionary.registerOre("treeLeaves", new ItemStack(leaf1, 1, OreDictionary.WILDCARD_VALUE));

        TreeRemover.woodTypes.add(log1);
        TreeRemover.woodTypes.add(log0);
        TreeRemover.plantTypes.add(leaf0);
        TreeRemover.plantTypes.add(leaf1);

        GameRegistry.registerTileEntity(TileEntityBerryFruit.class, "berry");
    }
}
