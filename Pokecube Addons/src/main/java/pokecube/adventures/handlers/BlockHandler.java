package pokecube.adventures.handlers;

import net.minecraft.block.Block;
import net.minecraftforge.fml.common.registry.GameRegistry;
import pokecube.adventures.blocks.cloner.BlockCloner;
import pokecube.adventures.blocks.cloner.TileEntityCloner;
import pokecube.adventures.blocks.warppad.BlockWarpPad;
import pokecube.adventures.blocks.warppad.TileEntityWarpPad;
import pokecube.core.PokecubeItems;
import pokecube.core.mod_Pokecube;

public class BlockHandler
{
    public static Block warppad;
    public static Block cloner;
    
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

    }
}
