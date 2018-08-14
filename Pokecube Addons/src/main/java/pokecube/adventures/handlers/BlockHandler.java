package pokecube.adventures.handlers;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.blocks.afa.BlockAFA;
import pokecube.adventures.blocks.afa.BlockCommander;
import pokecube.adventures.blocks.afa.BlockDaycare;
import pokecube.adventures.blocks.afa.TileEntityAFA;
import pokecube.adventures.blocks.afa.TileEntityCommander;
import pokecube.adventures.blocks.afa.TileEntityDaycare;
import pokecube.adventures.blocks.cloner.block.BlockExtractor;
import pokecube.adventures.blocks.cloner.block.BlockReanimator;
import pokecube.adventures.blocks.cloner.block.BlockSplicer;
import pokecube.adventures.blocks.cloner.tileentity.TileEntityCloner;
import pokecube.adventures.blocks.cloner.tileentity.TileEntityGeneExtractor;
import pokecube.adventures.blocks.cloner.tileentity.TileEntitySplicer;
import pokecube.adventures.blocks.siphon.BlockSiphon;
import pokecube.adventures.blocks.siphon.TileEntitySiphon;
import pokecube.adventures.blocks.warppad.BlockWarpPad;
import pokecube.adventures.blocks.warppad.TileEntityWarpPad;
import pokecube.core.PokecubeItems;
import pokecube.core.interfaces.PokecubeMod;

public class BlockHandler
{
    public static List<Block> blocks = Lists.newArrayList();

    public static void registerTiles(Object registry)
    {
        GameRegistry.registerTileEntity(TileEntityWarpPad.class, new ResourceLocation(PokecubeAdv.ID + ":warppad"));
        GameRegistry.registerTileEntity(TileEntityCloner.class, new ResourceLocation(PokecubeAdv.ID + ":cloner"));
        GameRegistry.registerTileEntity(TileEntitySplicer.class, new ResourceLocation(PokecubeAdv.ID + ":splicer"));
        GameRegistry.registerTileEntity(TileEntityGeneExtractor.class,
                new ResourceLocation(PokecubeAdv.ID + ":extractor"));
        GameRegistry.registerTileEntity(TileEntityAFA.class, new ResourceLocation(PokecubeAdv.ID + ":afa"));
        GameRegistry.registerTileEntity(TileEntityDaycare.class, new ResourceLocation(PokecubeAdv.ID + ":daycare"));
        GameRegistry.registerTileEntity(TileEntityCommander.class, new ResourceLocation(PokecubeAdv.ID + ":commander"));
        GameRegistry.registerTileEntity(TileEntitySiphon.class, new ResourceLocation(PokecubeAdv.ID + ":pokesiphon"));
    }

    public static void registerBlocks(Object registry)
    {
        Block block = new BlockWarpPad();
        block.setUnlocalizedName("warppad").setRegistryName(PokecubeAdv.ID, "warppad");
        blocks.add(block);
        block.setCreativeTab(PokecubeMod.creativeTabPokecubeBlocks);
        PokecubeItems.register(block, registry);

        block = new BlockSiphon();
        block.setUnlocalizedName("pokesiphon").setRegistryName(PokecubeAdv.ID, "pokesiphon");
        blocks.add(block);
        block.setCreativeTab(PokecubeMod.creativeTabPokecubeBlocks);
        PokecubeItems.register(block, registry);

        block = new BlockAFA();
        block.setUnlocalizedName("afa").setRegistryName(PokecubeAdv.ID, "afa");
        blocks.add(block);
        block.setCreativeTab(PokecubeMod.creativeTabPokecubeBlocks);
        PokecubeItems.register(block, registry);

        block = new BlockDaycare();
        block.setUnlocalizedName("daycare").setRegistryName(PokecubeAdv.ID, "daycare");
        blocks.add(block);
        block.setCreativeTab(PokecubeMod.creativeTabPokecubeBlocks);
        PokecubeItems.register(block, registry);

        block = new BlockCommander();
        block.setUnlocalizedName("pokecommand").setRegistryName(PokecubeAdv.ID, "pokecommand");
        blocks.add(block);
        block.setCreativeTab(PokecubeMod.creativeTabPokecubeBlocks);
        PokecubeItems.register(block, registry);

        block = new BlockReanimator();
        block.setUnlocalizedName("reanimator").setRegistryName(PokecubeAdv.ID, "reanimator");
        blocks.add(block);
        block.setCreativeTab(PokecubeMod.creativeTabPokecubeBlocks);
        PokecubeItems.register(block, registry);

        block = new BlockSplicer();
        block.setUnlocalizedName("splicer").setRegistryName(PokecubeAdv.ID, "splicer");
        blocks.add(block);
        block.setCreativeTab(PokecubeMod.creativeTabPokecubeBlocks);
        PokecubeItems.register(block, registry);

        block = new BlockExtractor();
        block.setUnlocalizedName("extractor").setRegistryName(PokecubeAdv.ID, "extractor");
        blocks.add(block);
        block.setCreativeTab(PokecubeMod.creativeTabPokecubeBlocks);
        PokecubeItems.register(block, registry);

    }
}
