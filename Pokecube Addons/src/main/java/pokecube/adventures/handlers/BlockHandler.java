package pokecube.adventures.handlers;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import pokecube.adventures.blocks.afa.BlockAFA;
import pokecube.adventures.blocks.afa.ItemBlockAFA;
import pokecube.adventures.blocks.afa.TileEntityAFA;
import pokecube.adventures.blocks.afa.TileEntityDaycare;
import pokecube.adventures.blocks.cloner.BlockCloner;
import pokecube.adventures.blocks.cloner.ItemBlockCloner;
import pokecube.adventures.blocks.cloner.TileEntityCloner;
import pokecube.adventures.blocks.siphon.BlockSiphon;
import pokecube.adventures.blocks.siphon.TileEntitySiphon;
import pokecube.adventures.blocks.warppad.BlockWarpPad;
import pokecube.adventures.blocks.warppad.TileEntityWarpPad;
import pokecube.core.PokecubeItems;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.utils.CompatWrapper;

public class BlockHandler
{
    public static Block warppad;
    public static Block cloner;
    public static Block afa;
    public static Block siphon;

    public static void registerBlocks()
    {
        warppad = new BlockWarpPad().setUnlocalizedName("warppad");
        PokecubeItems.register(warppad, "warppad");
        warppad.setCreativeTab(PokecubeMod.creativeTabPokecubeBlocks);
        CompatWrapper.registerTileEntity(TileEntityWarpPad.class, "warppad");

        cloner = new BlockCloner().setUnlocalizedName("cloner");
        cloner.setCreativeTab(PokecubeMod.creativeTabPokecubeBlocks);
        PokecubeItems.register(cloner, ItemBlockCloner.class, "cloner");
        CompatWrapper.registerTileEntity(TileEntityCloner.class, "cloner");
        PokecubeItems.addSpecificItemStack("cloner", new ItemStack(cloner, 1, 1));
        PokecubeItems.addSpecificItemStack("reanimator", new ItemStack(cloner, 1, 0));

        afa = new BlockAFA().setUnlocalizedName("afa");
        afa.setCreativeTab(PokecubeMod.creativeTabPokecubeBlocks);
        PokecubeItems.register(afa, ItemBlockAFA.class, "afa");
        CompatWrapper.registerTileEntity(TileEntityAFA.class, "afa");
        CompatWrapper.registerTileEntity(TileEntityDaycare.class, "daycare");
        PokecubeItems.addSpecificItemStack("daycare", new ItemStack(afa, 1, 1));
        PokecubeItems.addSpecificItemStack("afa", new ItemStack(afa, 1, 0));

        siphon = new BlockSiphon().setUnlocalizedName("pokesiphon");
        siphon.setCreativeTab(PokecubeMod.creativeTabPokecubeBlocks);
        PokecubeItems.register(siphon, "pokesiphon");
        CompatWrapper.registerTileEntity(TileEntitySiphon.class, "pokesiphon");
    }
}
