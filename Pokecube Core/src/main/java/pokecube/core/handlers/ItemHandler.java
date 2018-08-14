package pokecube.core.handlers;

import static pokecube.core.PokecubeItems.addSpecificItemStack;
import static pokecube.core.PokecubeItems.addToHoldables;
import static pokecube.core.PokecubeItems.berryJuice;
import static pokecube.core.PokecubeItems.fossilStone;
import static pokecube.core.PokecubeItems.luckyEgg;
import static pokecube.core.PokecubeItems.pc_base;
import static pokecube.core.PokecubeItems.pc_top;
import static pokecube.core.PokecubeItems.pokecenter;
import static pokecube.core.PokecubeItems.pokedex;
import static pokecube.core.PokecubeItems.pokemobEgg;
import static pokecube.core.PokecubeItems.pokewatch;
import static pokecube.core.PokecubeItems.register;
import static pokecube.core.PokecubeItems.registerItemTexture;
import static pokecube.core.PokecubeItems.repelBlock;
import static pokecube.core.PokecubeItems.revive;
import static pokecube.core.PokecubeItems.tableBlock;
import static pokecube.core.PokecubeItems.tm_machine;
import static pokecube.core.PokecubeItems.trading_table;
import static pokecube.core.interfaces.PokecubeMod.creativeTabPokecube;
import static pokecube.core.interfaces.PokecubeMod.creativeTabPokecubeBerries;
import static pokecube.core.interfaces.PokecubeMod.creativeTabPokecubeBlocks;
import static pokecube.core.interfaces.PokecubeMod.creativeTabPokecubes;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import com.google.common.collect.Lists;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDispenser;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.registries.IForgeRegistry;
import pokecube.core.Mod_Pokecube_Helper;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.blocks.ItemBlockGeneric;
import pokecube.core.blocks.berries.BlockBerryCrop;
import pokecube.core.blocks.berries.BlockBerryFruit;
import pokecube.core.blocks.berries.BlockBerryLeaf;
import pokecube.core.blocks.berries.ItemBlockMeta;
import pokecube.core.blocks.berries.TileEntityBerries;
import pokecube.core.blocks.berries.TileEntityTickBerries;
import pokecube.core.blocks.nests.BlockNest;
import pokecube.core.blocks.nests.TileEntityBasePortal;
import pokecube.core.blocks.nests.TileEntityNest;
import pokecube.core.blocks.pokecubeTable.TileEntityPokecubeTable;
import pokecube.core.blocks.repel.TileEntityRepel;
import pokecube.core.blocks.tradingTable.TileEntityTMMachine;
import pokecube.core.blocks.tradingTable.TileEntityTradingTable;
import pokecube.core.events.onload.RegisterPokecubes;
import pokecube.core.interfaces.IPokecube.PokecubeBehavior;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.berries.BerryManager;
import pokecube.core.items.pokecubes.DispenserBehaviorPokecube;
import pokecube.core.items.pokecubes.Pokecube;
import pokecube.core.items.vitamins.ItemCandy;
import pokecube.core.items.vitamins.ItemVitamin;

public class ItemHandler extends Mod_Pokecube_Helper
{
    public static void registerItemBlock(Block block, Object registry)
    {
        ItemBlock item = new ItemBlockGeneric(block);
        register(item, registry);
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
            registerItemTexture(item, 0, new ModelResourceLocation(item.getRegistryName().toString(), "inventory"));
    }

    private static void addBerryBlocks(Object registry)
    {
        BerryManager.berryCrop = new BlockBerryCrop().setRegistryName(PokecubeMod.ID, "berrycrop")
                .setUnlocalizedName("berrycrop");
        register(BerryManager.berryCrop, registry);
        BerryManager.berryFruit = new BlockBerryFruit().setRegistryName(PokecubeMod.ID, "berryfruit")
                .setUnlocalizedName("berryfruit");
        register(BerryManager.berryFruit, registry);
        BerryManager.berryLeaf = new BlockBerryLeaf().setRegistryName(PokecubeMod.ID, "berryleaf")
                .setUnlocalizedName("berryleaf");
        register(BerryManager.berryLeaf, registry);
        ItemGenerator.makeWoodBlocks(registry);
        BerryManager.registerTrees();
    }

    private static void addBerryTiles(Object registry)
    {
        GameRegistry.registerTileEntity(TileEntityBerries.class, new ResourceLocation("pokecube:berries"));
        GameRegistry.registerTileEntity(TileEntityTickBerries.class, new ResourceLocation("pokecube:berries_ticks"));
    }

    private static void addBerryItems(Object registry)
    {
        List<Integer> list = Lists.newArrayList(BerryManager.berryItems.keySet());
        Collections.sort(list);
        for (Integer i : list)
        {
            register(BerryManager.berryItems.get(i), registry);
            if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
            {
                registerItemTexture(BerryManager.berryItems.get(i), 0, new ModelResourceLocation(
                        "pokecube:" + "berry_" + BerryManager.berryNames.get(i), "inventory"));
            }
        }
        // Register the berries.
        BerryManager.initBerries();
    }

    private static void addFossilBlocks(Object registry)
    {
        PokecubeItems.register(PokecubeItems.fossilStone, registry);
        PokecubeItems.fossilStone.setCreativeTab(PokecubeMod.creativeTabPokecubeBlocks);
    }

    private static void addFossilItems(Object registry)
    {
        for (String s : PokecubeCore.core.getConfig().customFossils)
        {
            if (!ItemGenerator.fossilVariants.contains(s.toLowerCase(Locale.ENGLISH)))
                ItemGenerator.fossilVariants.add(s.toLowerCase(Locale.ENGLISH));
        }
        ItemGenerator.makeFossils(registry);
    }

    private static void addMiscBlocks(Object registry)
    {
        repelBlock.setUnlocalizedName("repel").setRegistryName(PokecubeMod.ID, "repel");
        repelBlock.setCreativeTab(creativeTabPokecubeBerries);
        register(repelBlock, registry);

        tableBlock.setUnlocalizedName("pokecube_table").setRegistryName(PokecubeMod.ID, "pokecube_table");
        tableBlock.setCreativeTab(creativeTabPokecubeBlocks);
        register(tableBlock, registry);
        PokecubeItems.pokecenter.setRegistryName(PokecubeMod.ID, "pokecenter");
        register(PokecubeItems.pokecenter, registry);

        Block nest = PokecubeItems.nest;
        nest.setRegistryName(PokecubeMod.ID, "pokemobnest");
        register(nest, registry);

        register(trading_table, registry);
        register(tm_machine, registry);

        register(pc_top, registry);
        register(pc_base, registry);
    }

    private static void registerItemBlocks(Object registry)
    {
        registerItemBlock(pc_top, registry);
        registerItemBlock(pc_base, registry);
        registerItemBlock(trading_table, registry);
        registerItemBlock(tm_machine, registry);
        registerItemBlock(repelBlock, registry);
        registerItemBlock(fossilStone, registry);
        registerItemBlock(pokecenter, registry);

        ItemBlock item = new ItemBlockMeta(PokecubeItems.nest);
        item.setRegistryName(PokecubeMod.ID, "pokemobnest");
        register(item, registry);
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
        {
            for (int i = 0; i < BlockNest.types.size(); i++)
                registerItemTexture(item, i, new ModelResourceLocation("minecraft:stone", "inventory"));
        }

        ItemGenerator.makeWoodItems(registry);

        item = new ItemBlock(tableBlock);
        item.setRegistryName(PokecubeMod.ID, "pokecube_table");
        register(item, registry);
    }

    private static void addMiscTiles(Object registry)
    {
        GameRegistry.registerTileEntity(TileEntityPokecubeTable.class, new ResourceLocation("pokecube:pokecube_table"));
        GameRegistry.registerTileEntity(pokecube.core.blocks.healtable.TileHealTable.class,
                new ResourceLocation("pokecube:pokecenter"));
        GameRegistry.registerTileEntity(TileEntityNest.class, new ResourceLocation("pokecube:pokemobnest"));
        GameRegistry.registerTileEntity(TileEntityBasePortal.class,
                new ResourceLocation("pokecube:pokecubebaseportal"));
        GameRegistry.registerTileEntity(pokecube.core.blocks.pc.TileEntityPC.class,
                new ResourceLocation("pokecube:pc"));
        GameRegistry.registerTileEntity(TileEntityTradingTable.class, new ResourceLocation("pokecube:tradingtable"));
        GameRegistry.registerTileEntity(TileEntityTMMachine.class, new ResourceLocation("pokecube:tm_machine"));
        GameRegistry.registerTileEntity(TileEntityRepel.class, new ResourceLocation("pokecube:repel"));
    }

    private static void addMiscItems(Object registry)
    {
        luckyEgg.setRegistryName(PokecubeMod.ID, "luckyegg");
        register(luckyEgg, registry);
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
            registerItemTexture(luckyEgg, 0, new ModelResourceLocation("egg", "inventory"));
        addToHoldables("luckyegg");

        register(pokemobEgg, registry);
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
            registerItemTexture(pokemobEgg, 0, new ModelResourceLocation("pokecube:pokemobegg", "inventory"));
        OreDictionary.registerOre("egg", new ItemStack(pokemobEgg, 1, OreDictionary.WILDCARD_VALUE));
        BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(pokemobEgg, new DispenserBehaviorPokecube());

        pokedex.setCreativeTab(creativeTabPokecube);
        register(pokedex, registry);
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
        {
            registerItemTexture(pokedex, 0,
                    new ModelResourceLocation(pokedex.getRegistryName().toString(), "inventory"));
        }
        pokewatch.setCreativeTab(creativeTabPokecube);
        register(pokewatch, registry);
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
        {
            registerItemTexture(pokewatch, 0,
                    new ModelResourceLocation(pokewatch.getRegistryName().toString(), "inventory"));
        }

        for (String s : PokecubeCore.core.getConfig().customHeldItems)
        {
            if (!ItemGenerator.variants.contains(s.toLowerCase(Locale.ENGLISH)))
                ItemGenerator.variants.add(s.toLowerCase(Locale.ENGLISH));
        }
        ItemGenerator.makeHeldItems(registry);
        ItemGenerator.makeTMs(registry);
        ItemGenerator.makeMegaWearables(registry);

        Item shard = new Item();
        shard.setCreativeTab(creativeTabPokecube);
        shard.setRegistryName(PokecubeMod.ID, "emerald_shard").setUnlocalizedName("emerald_shard");
        register(shard, registry);
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
        {
            registerItemTexture(shard, 0, new ModelResourceLocation(shard.getRegistryName().toString(), "inventory"));
        }

        ItemCandy candy = new ItemCandy();
        candy.setCreativeTab(creativeTabPokecube);
        candy.setRegistryName(PokecubeMod.ID, "candy").setUnlocalizedName("candy");
        register(candy, registry);
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
        {
            registerItemTexture(candy, 0, new ModelResourceLocation(candy.getRegistryName().toString(), "inventory"));
        }

        revive.setCreativeTab(creativeTabPokecube);
        register(revive.setRegistryName(PokecubeMod.ID, "revive"), registry);
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
            registerItemTexture(revive, 0, new ModelResourceLocation("pokecube:revive", "inventory"));
        addSpecificItemStack("revive", new ItemStack(revive));
        addToHoldables("revive");
    }

    public static void registerBlocks(IForgeRegistry<Block> iForgeRegistry)
    {
        addMiscBlocks(iForgeRegistry);
        addFossilBlocks(iForgeRegistry);
        addBerryBlocks(iForgeRegistry);
    }

    public static void registerTiles(IForgeRegistry<Block> iForgeRegistry)
    {
        addMiscTiles(iForgeRegistry);
        addBerryTiles(iForgeRegistry);
    }

    public static void registerItems(IForgeRegistry<Item> iForgeRegistry)
    {
        addPokecubes(iForgeRegistry);
        addBerryItems(iForgeRegistry);
        addStones(iForgeRegistry);
        addVitamins(iForgeRegistry);
        addFossilItems(iForgeRegistry);
        addMiscItems(iForgeRegistry);
        registerItemBlocks(iForgeRegistry);
    }

    private static void addPokecubes(Object registry)
    {
        RegisterPokecubes event = new RegisterPokecubes();
        MinecraftForge.EVENT_BUS.post(event);

        // Register any cube behaviours and cubes from event.
        for (PokecubeBehavior i : event.behaviors)
        {
            PokecubeBehavior.addCubeBehavior(i);
            String name = i.getRegistryName().getResourcePath();
            Pokecube cube = new Pokecube();
            cube.setUnlocalizedName(name + "cube").setCreativeTab(creativeTabPokecubes);
            register(cube.setRegistryName(PokecubeMod.ID, name + "cube"), registry);
            if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
                registerItemTexture(cube, 0, new ModelResourceLocation("pokecube:" + name + "cube", "inventory"));
            PokecubeItems.addCube(i.getRegistryName(), new Object[] { cube });
        }

        Pokecube pokeseal = new Pokecube();
        pokeseal.setUnlocalizedName("pokeseal").setCreativeTab(creativeTabPokecubes);
        PokecubeBehavior.POKESEAL = new ResourceLocation("pokecube:seal");
        register(pokeseal.setRegistryName(PokecubeMod.ID, "pokeseal"), registry);
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
            registerItemTexture(pokeseal, 0, new ModelResourceLocation("pokecube:pokeseal", "inventory"));
        PokecubeItems.addCube(PokecubeBehavior.POKESEAL, new Object[] { pokeseal });

    }

    private static void addStones(Object registry)
    {
        berryJuice = (new ItemFood(6, 0.6f, false)).setUnlocalizedName("berryjuice");
        berryJuice.setCreativeTab(creativeTabPokecube);
        register(berryJuice.setRegistryName(PokecubeMod.ID, "berryjuice"), registry);
        PokecubeItems.addToHoldables("berryjuice");
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
            registerItemTexture(berryJuice, 0, new ModelResourceLocation("pokecube:berryjuice", "inventory"));
    }

    private static void addVitamins(Object registry)
    {
        for (int i = 0; i < ItemVitamin.vitamins.size(); i++)
        {
            ItemVitamin vitamin = new ItemVitamin(ItemVitamin.vitamins.get(i));
            register(vitamin, registry);
            ItemStack stack = new ItemStack(vitamin, 1, 0);
            PokecubeItems.addSpecificItemStack(vitamin.type, stack);
            PokecubeItems.addToHoldables(stack);
            if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) registerItemTexture(vitamin, 0,
                    new ModelResourceLocation(vitamin.getRegistryName().toString(), "inventory"));
        }
    }
}
