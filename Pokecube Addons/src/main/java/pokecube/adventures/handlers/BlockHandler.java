package pokecube.adventures.handlers;

import net.minecraft.block.Block;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.blocks.afa.BlockAFA;
import pokecube.adventures.blocks.afa.TileEntityAFA;
import pokecube.adventures.blocks.afa.TileEntityCommander;
import pokecube.adventures.blocks.afa.TileEntityDaycare;
import pokecube.adventures.blocks.cloner.block.BlockCloner;
import pokecube.adventures.blocks.cloner.tileentity.TileEntityCloner;
import pokecube.adventures.blocks.cloner.tileentity.TileEntityGeneExtractor;
import pokecube.adventures.blocks.cloner.tileentity.TileEntitySplicer;
import pokecube.adventures.blocks.siphon.BlockSiphon;
import pokecube.adventures.blocks.siphon.TileEntitySiphon;
import pokecube.adventures.blocks.warppad.BlockWarpPad;
import pokecube.adventures.blocks.warppad.TileEntityWarpPad;
import pokecube.core.PokecubeItems;
import pokecube.core.interfaces.PokecubeMod;
import thut.lib.CompatWrapper;

public class BlockHandler
{
    public static Block warppad = new BlockWarpPad().setUnlocalizedName("warppad").setRegistryName(PokecubeAdv.ID,
            "warppad");
    public static Block cloner  = new BlockCloner().setUnlocalizedName("cloner").setRegistryName(PokecubeAdv.ID,
            "cloner");
    public static Block afa     = new BlockAFA().setUnlocalizedName("afa").setRegistryName(PokecubeAdv.ID, "afa");
    public static Block siphon  = new BlockSiphon().setUnlocalizedName("pokesiphon").setRegistryName(PokecubeAdv.ID,
            "pokesiphon");

    public static void registerTiles(Object registry)
    {
        CompatWrapper.registerTileEntity(TileEntityWarpPad.class, PokecubeAdv.ID + ":warppad");
        CompatWrapper.registerTileEntity(TileEntityCloner.class, PokecubeAdv.ID + ":cloner");
        CompatWrapper.registerTileEntity(TileEntitySplicer.class, PokecubeAdv.ID + ":splicer");
        CompatWrapper.registerTileEntity(TileEntityGeneExtractor.class, PokecubeAdv.ID + ":extractor");
        CompatWrapper.registerTileEntity(TileEntityAFA.class, PokecubeAdv.ID + ":afa");
        CompatWrapper.registerTileEntity(TileEntityDaycare.class, PokecubeAdv.ID + ":daycare");
        CompatWrapper.registerTileEntity(TileEntityCommander.class, PokecubeAdv.ID + ":commander");
        CompatWrapper.registerTileEntity(TileEntitySiphon.class, PokecubeAdv.ID + ":pokesiphon");
    }

    public static void registerBlocks(Object registry)
    {

        PokecubeItems.register(warppad, registry);
        warppad.setCreativeTab(PokecubeMod.creativeTabPokecubeBlocks);

        cloner.setCreativeTab(PokecubeMod.creativeTabPokecubeBlocks);
        PokecubeItems.register(cloner, registry);

        afa.setCreativeTab(PokecubeMod.creativeTabPokecubeBlocks);
        PokecubeItems.register(afa, registry);

        siphon.setCreativeTab(PokecubeMod.creativeTabPokecubeBlocks);
        PokecubeItems.register(siphon, registry);
    }
}
