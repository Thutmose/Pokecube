package pokecube.adventures.handlers;

import net.minecraft.block.Block;
import net.minecraftforge.fml.common.registry.GameRegistry;
import pokecube.adventures.LegendaryConditions;
import pokecube.adventures.blocks.afa.BlockAFA;
import pokecube.adventures.blocks.afa.TileEntityAFA;
import pokecube.adventures.blocks.cloner.BlockCloner;
import pokecube.adventures.blocks.cloner.TileEntityCloner;
import pokecube.adventures.blocks.legendary.BlockLegendSpawner;
import pokecube.adventures.blocks.warppad.BlockWarpPad;
import pokecube.adventures.blocks.warppad.TileEntityWarpPad;
import pokecube.core.PokecubeItems;
import pokecube.core.mod_Pokecube;
import pokecube.core.blocks.berries.ItemBlockMeta;
import pokecube.core.interfaces.PokecubeMod;

public class BlockHandler
{
    public static Block warppad;
    public static Block cloner;
    public static Block afa;

    public static void registerBlocks()
    {
        warppad = new BlockWarpPad().setUnlocalizedName("warppad");
        PokecubeItems.register(warppad, "warppad");
        warppad.setCreativeTab(mod_Pokecube.creativeTabPokecubeBlocks);
        GameRegistry.registerTileEntity(TileEntityWarpPad.class, "warppad");

        cloner = new BlockCloner().setUnlocalizedName("cloner");
        cloner.setCreativeTab(mod_Pokecube.creativeTabPokecubeBlocks);
        PokecubeItems.register(cloner, "cloner");
        GameRegistry.registerTileEntity(TileEntityCloner.class, "cloner");

        afa = new BlockAFA().setUnlocalizedName("afa");
        afa.setCreativeTab(mod_Pokecube.creativeTabPokecubeBlocks);
        PokecubeItems.register(afa, "afa");
        GameRegistry.registerTileEntity(TileEntityAFA.class, "afa");

        LegendaryConditions.spawner1 = new BlockLegendSpawner();
        LegendaryConditions.spawner1.setUnlocalizedName("legendSpawner");
        PokecubeItems.register(LegendaryConditions.spawner1, ItemBlockMeta.class, "legendSpawner");
        LegendaryConditions.spawner1.setCreativeTab(PokecubeMod.creativeTabPokecubeBlocks);
        LegendaryConditions.registerPreInit();

    }
}
