package pokecube.adventures.handlers;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
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
import pokecube.core.PokecubeCore;
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
        warppad.setCreativeTab(PokecubeCore.creativeTabPokecubeBlocks);
        GameRegistry.registerTileEntity(TileEntityWarpPad.class, "warppad");

        cloner = new BlockCloner().setUnlocalizedName("cloner");
        cloner.setCreativeTab(PokecubeCore.creativeTabPokecubeBlocks);
        PokecubeItems.register(cloner, "cloner");
        GameRegistry.registerTileEntity(TileEntityCloner.class, "cloner");

        afa = new BlockAFA().setUnlocalizedName("afa");
        afa.setCreativeTab(PokecubeCore.creativeTabPokecubeBlocks);
        PokecubeItems.register(afa, "afa");
        GameRegistry.registerTileEntity(TileEntityAFA.class, "afa");

        LegendaryConditions.spawner1 = new BlockLegendSpawner();
        LegendaryConditions.spawner1.setUnlocalizedName("legendSpawner");
        PokecubeItems.register(LegendaryConditions.spawner1, ItemBlockMeta.class, "legendSpawner");
        LegendaryConditions.spawner1.setCreativeTab(PokecubeMod.creativeTabPokecubeBlocks);
        LegendaryConditions.registerPreInit();
        PokecubeItems.addSpecificItemStack("registeelspawner", new ItemStack(LegendaryConditions.spawner1, 1, 0));
        PokecubeItems.addSpecificItemStack("regicespawner", new ItemStack(LegendaryConditions.spawner1, 1, 1));
        PokecubeItems.addSpecificItemStack("regirockspawner", new ItemStack(LegendaryConditions.spawner1, 1, 2));
        PokecubeItems.addSpecificItemStack("celebispawner", new ItemStack(LegendaryConditions.spawner1, 1, 3));
        PokecubeItems.addSpecificItemStack("hoohspawner", new ItemStack(LegendaryConditions.spawner1, 1, 4));
        PokecubeItems.addSpecificItemStack("kyogrespawner", new ItemStack(LegendaryConditions.spawner1, 1, 5));
        PokecubeItems.addSpecificItemStack("groudonspawner", new ItemStack(LegendaryConditions.spawner1, 1, 6));

    }
}
