package pokecube.core.handlers;

import static pokecube.core.PokecubeItems.addSpecificItemStack;
import static pokecube.core.PokecubeItems.addToHoldables;
import static pokecube.core.PokecubeItems.berries;
import static pokecube.core.PokecubeItems.berryJuice;
import static pokecube.core.PokecubeItems.luckyEgg;
import static pokecube.core.PokecubeItems.megaring;
import static pokecube.core.PokecubeItems.megastone;
import static pokecube.core.PokecubeItems.pc;
import static pokecube.core.PokecubeItems.pokedex;
import static pokecube.core.PokecubeItems.pokemobEgg;
import static pokecube.core.PokecubeItems.register;
import static pokecube.core.PokecubeItems.registerItemTexture;
import static pokecube.core.PokecubeItems.repelBlock;
import static pokecube.core.PokecubeItems.revive;
import static pokecube.core.PokecubeItems.tableBlock;
import static pokecube.core.PokecubeItems.tradingtable;
import static pokecube.core.interfaces.PokecubeMod.creativeTabPokecube;
import static pokecube.core.interfaces.PokecubeMod.creativeTabPokecubeBerries;
import static pokecube.core.interfaces.PokecubeMod.creativeTabPokecubeBlocks;
import static pokecube.core.interfaces.PokecubeMod.creativeTabPokecubes;

import java.util.Locale;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDispenser;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.registry.IForgeRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.oredict.OreDictionary;
import pokecube.core.Mod_Pokecube_Helper;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.blocks.berries.BlockBerryCrop;
import pokecube.core.blocks.berries.BlockBerryFruit;
import pokecube.core.blocks.berries.BlockBerryLeaf;
import pokecube.core.blocks.berries.BlockBerryLog;
import pokecube.core.blocks.berries.BlockBerryWood;
import pokecube.core.blocks.berries.ItemBlockMeta;
import pokecube.core.blocks.berries.TileEntityBerries;
import pokecube.core.blocks.nests.BlockNest;
import pokecube.core.blocks.nests.TileEntityBasePortal;
import pokecube.core.blocks.nests.TileEntityNest;
import pokecube.core.blocks.pc.ItemBlockPC;
import pokecube.core.blocks.pokecubeTable.TileEntityPokecubeTable;
import pokecube.core.blocks.repel.TileEntityRepel;
import pokecube.core.blocks.tradingTable.ItemBlockTradingTable;
import pokecube.core.blocks.tradingTable.TileEntityTradingTable;
import pokecube.core.events.onload.RegisterPokecubes;
import pokecube.core.interfaces.IPokecube.PokecubeBehavior;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.ItemHeldItems;
import pokecube.core.items.ItemPokemobUseableFood;
import pokecube.core.items.berries.BerryManager;
import pokecube.core.items.megastuff.ItemMegawearable;
import pokecube.core.items.pokecubes.DispenserBehaviorPokecube;
import pokecube.core.items.pokecubes.Pokecube;
import pokecube.core.items.vitamins.ItemVitamin;
import thut.lib.CompatWrapper;

public class ItemHandler extends Mod_Pokecube_Helper
{
    static final String[] names1 = { "pecha", "oran", "leppa", "sitrus" };
    static final String[] names  = new String[] { "enigma", "nanab" };
    static
    {
        BlockBerryLog.currentlyConstructing = 0;
    }
    public static Block log0   = new BlockBerryLog(0, names1).setHardness(2.0F).setUnlocalizedName("log0")
            .setRegistryName(PokecubeMod.ID, "log0");
    public static Block plank0 = new BlockBerryWood(0, names1).setHardness(2.0F).setResistance(5.0F)
            .setUnlocalizedName("wood0").setRegistryName(PokecubeMod.ID, "wood0");
    static
    {
        BlockBerryLog.currentlyConstructing = 4;
    }
    public static Block log1 = new BlockBerryLog(4, names).setHardness(2.0F).setUnlocalizedName("log1")
            .setRegistryName(PokecubeMod.ID, "log1");

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
        register(log0, registry);// ItemBlockMeta.class
        register(plank0, registry);// ItemBlockMeta.class
        register(log1, registry);// ItemBlockMeta.class
        BerryManager.registerTrees();
    }

    private static void addBerryTiles(Object registry)
    {
        CompatWrapper.registerTileEntity(TileEntityBerries.class, "pokecube:berries");
    }

    private static void addBerryItems(Object registry)
    {
        berries.setRegistryName(PokecubeMod.ID, "berry");
        register(berries, registry);
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
        {
            registerItemTexture(PokecubeItems.berries, 0, new ModelResourceLocation("pokecube:berry", "inventory"));
        }
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
            if (!HeldItemHandler.fossilVariants.contains(s.toLowerCase(Locale.ENGLISH)))
                HeldItemHandler.fossilVariants.add(s.toLowerCase(Locale.ENGLISH));
        }
        PokecubeItems.register(PokecubeItems.fossil, registry);
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
        register(nest, registry);// ItemBlockMeta.class

        tradingtable.setCreativeTab(PokecubeMod.creativeTabPokecubeBlocks);
        tradingtable.setRegistryName(PokecubeMod.ID, "tradingtable");
        register(tradingtable, registry);// ItemBlockTradingTable.class
        PokecubeItems.addSpecificItemStack("tmtable", new ItemStack(tradingtable, 1, 8));

        pc.setCreativeTab(PokecubeMod.creativeTabPokecubeBlocks);
        pc.setRegistryName(PokecubeMod.ID, "pc");
        register(pc, registry);// ItemBlockPC.class
        PokecubeItems.addSpecificItemStack("pctop", new ItemStack(pc, 1, 8));
        PokecubeItems.addSpecificItemStack("pcbase", new ItemStack(pc, 1, 0));
    }

    private static void registerItemBlocks(Object registry)
    {
        ItemBlock item = new ItemBlockPC(pc);
        item.setRegistryName(PokecubeMod.ID, "pc");
        register(item, registry);

        item = new ItemBlockTradingTable(tradingtable);
        item.setRegistryName(PokecubeMod.ID, "tradingtable");
        register(item, registry);

        item = new ItemBlockMeta(PokecubeItems.nest);
        item.setRegistryName(PokecubeMod.ID, "pokemobnest");
        register(item, registry);
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
        {
            for (int i = 0; i < BlockNest.types.size(); i++)
                registerItemTexture(item, i, new ModelResourceLocation("minecraft:stone", "inventory"));
        }

        item = new ItemBlockMeta(log0);
        item.setRegistryName(PokecubeMod.ID, "log0");
        register(item, registry);

        item = new ItemBlockMeta(plank0);
        item.setRegistryName(PokecubeMod.ID, "wood0");
        register(item, registry);

        item = new ItemBlockMeta(log1);
        item.setRegistryName(PokecubeMod.ID, "log1");
        register(item, registry);

        item = new ItemBlock(tableBlock);
        item.setRegistryName(PokecubeMod.ID, "pokecube_table");
        register(item, registry);

        item = new ItemBlock(repelBlock);
        item.setRegistryName(PokecubeMod.ID, "repel");
        register(item, registry);
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
            registerItemTexture(item, 0, new ModelResourceLocation("pokecube:repel", "inventory"));

        item = new ItemBlock(PokecubeItems.pokecenter);
        item.setRegistryName(PokecubeMod.ID, "pokecenter");
        register(item, registry);
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
        {
            registerItemTexture(item, 0, new ModelResourceLocation("pokecube:pokecenter", "inventory"));
            registerItemTexture(item, 1, new ModelResourceLocation("pokecube:pokecenter", "inventory"));
        }

        item = new ItemBlock(PokecubeItems.fossilStone);
        item.setRegistryName(PokecubeItems.fossilStone.getRegistryName());
        register(item, registry);
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
            registerItemTexture(Item.getItemFromBlock(PokecubeItems.fossilStone), 0,
                    new ModelResourceLocation("pokecube:fossilstone", "inventory"));
    }

    private static void addMiscTiles(Object registry)
    {
        CompatWrapper.registerTileEntity(TileEntityPokecubeTable.class, "pokecube:pokecube_table");
        CompatWrapper.registerTileEntity(pokecube.core.blocks.healtable.TileHealTable.class, "pokecube:pokecenter");
        CompatWrapper.registerTileEntity(TileEntityNest.class, "pokecube:pokemobnest");
        CompatWrapper.registerTileEntity(TileEntityBasePortal.class, "pokecube:pokecubebaseportal");
        CompatWrapper.registerTileEntity(pokecube.core.blocks.pc.TileEntityPC.class, "pokecube:pc");
        CompatWrapper.registerTileEntity(TileEntityTradingTable.class, "pokecube:tradingtable");
        CompatWrapper.registerTileEntity(TileEntityRepel.class, "pokecube:repel");
    }

    private static void addMiscItems(Object registry)
    {
        luckyEgg.setRegistryName(PokecubeMod.ID, "luckyegg");
        register(luckyEgg, registry);
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
            registerItemTexture(luckyEgg, 0, new ModelResourceLocation("egg", "inventory"));
        addToHoldables("luckyegg");

        register(pokemobEgg.setRegistryName(PokecubeMod.ID, "pokemobegg"), registry);
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
            registerItemTexture(pokemobEgg, 0, new ModelResourceLocation("pokecube:pokemobegg", "inventory"));
        OreDictionary.registerOre("egg", new ItemStack(pokemobEgg, 1, OreDictionary.WILDCARD_VALUE));
        BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(pokemobEgg, new DispenserBehaviorPokecube());

        pokedex.setCreativeTab(creativeTabPokecube);
        register(pokedex.setRegistryName(PokecubeMod.ID, "pokedex"), registry);
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) for (int i = 0; i < 10; i++)
            registerItemTexture(pokedex, i, new ModelResourceLocation("pokecube:pokedex", "inventory"));

        megaring.setCreativeTab(creativeTabPokecube);
        register(megaring.setRegistryName(PokecubeMod.ID, "megaring"), registry);
        for (String s : ItemMegawearable.wearables.keySet())
        {
            ItemStack stack = new ItemStack(PokecubeItems.megaring);
            stack.setTagCompound(new NBTTagCompound());
            stack.getTagCompound().setString("type", s);
            PokecubeItems.addSpecificItemStack(s, stack);
        }

        for (String s : PokecubeCore.core.getConfig().customMegaStones)
        {
            if (!HeldItemHandler.megaVariants.contains(s.toLowerCase(Locale.ENGLISH)))
                HeldItemHandler.megaVariants.add(s.toLowerCase(Locale.ENGLISH));
        }

        megastone.setCreativeTab(creativeTabPokecube);
        register(megastone.setRegistryName(PokecubeMod.ID, "megastone"), registry);
        for (int n = 0; n < HeldItemHandler.megaVariants.size(); n++)
        {
            String s = HeldItemHandler.megaVariants.get(n);
            ItemStack stack = new ItemStack(PokecubeItems.megastone);
            stack.setTagCompound(new NBTTagCompound());
            stack.getTagCompound().setString("pokemon", s);
            PokecubeItems.addSpecificItemStack(s, stack);
            if (n > 1) PokecubeItems.addToHoldables(s);
        }

        revive.setCreativeTab(creativeTabPokecube);
        register(revive.setRegistryName(PokecubeMod.ID, "revive"), registry);
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
            registerItemTexture(revive, 0, new ModelResourceLocation("pokecube:revive", "inventory"));
        addToHoldables("revive");

        Item tm = PokecubeItems.tm;
        tm.setCreativeTab(creativeTabPokecube);
        register(tm.setRegistryName(PokecubeMod.ID, "tm"), registry);
        addSpecificItemStack("rarecandy", new ItemStack(tm, 1, 20));
        addSpecificItemStack("emerald_shard", new ItemStack(tm, 1, 19));
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
        addStones(iForgeRegistry);
        addBerryItems(iForgeRegistry);
        addVitamins(iForgeRegistry);
        addFossilItems(iForgeRegistry);
        addMiscItems(iForgeRegistry);
        registerItemBlocks(iForgeRegistry);
    }

    private static void addPokecubes(Object registry)
    {
        RegisterPokecubes event = new RegisterPokecubes();
        MinecraftForge.EVENT_BUS.post(event);

        // Register any cube types from event.
        for (Integer i : event.cubePrefixes.keySet())
        {
            String name = event.cubePrefixes.get(i);
            Pokecube cube = new Pokecube();
            cube.setUnlocalizedName(name + "cube").setCreativeTab(creativeTabPokecubes);
            register(cube.setRegistryName(PokecubeMod.ID, name + "cube"), registry);
            if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
                registerItemTexture(cube, 0, new ModelResourceLocation("pokecube:" + name + "cube", "inventory"));
            PokecubeItems.addCube(i, new Object[] { cube });
        }
        // Register any cube behaviours from event.
        for (Integer i : event.behaviors.keySet())
        {
            PokecubeBehavior.addCubeBehavior(i, event.behaviors.get(i));
        }

        Pokecube pokeseal = new Pokecube();
        pokeseal.setUnlocalizedName("pokeseal").setCreativeTab(creativeTabPokecubes);
        register(pokeseal.setRegistryName(PokecubeMod.ID, "pokeseal"), registry);
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
            registerItemTexture(pokeseal, 0, new ModelResourceLocation("pokecube:pokeseal", "inventory"));
        PokecubeItems.addCube(-2, new Object[] { pokeseal });

    }

    private static void addStones(Object registry)
    {
        for (String s : PokecubeCore.core.getConfig().customHeldItems)
        {
            if (!ItemHeldItems.variants.contains(s.toLowerCase(Locale.ENGLISH)))
                ItemHeldItems.variants.add(s.toLowerCase(Locale.ENGLISH));
        }

        PokecubeItems.register(PokecubeItems.held, registry);
        for (String s : ItemHeldItems.variants)
        {
            ItemStack stack = new ItemStack(PokecubeItems.held);
            stack.setTagCompound(new NBTTagCompound());
            stack.getTagCompound().setString("type", s);
            PokecubeItems.addSpecificItemStack(s, stack);
            PokecubeItems.addToEvos(s);
            PokecubeItems.addToHoldables(s);
        }

        berryJuice = (new ItemPokemobUseableFood(6, 0.6f, false)).setUnlocalizedName("berryjuice");
        berryJuice.setCreativeTab(creativeTabPokecube);
        register(berryJuice.setRegistryName(PokecubeMod.ID, "berryjuice"), registry);
        PokecubeItems.addToHoldables("berryjuice");
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
            registerItemTexture(berryJuice, 0, new ModelResourceLocation("pokecube:berryjuice", "inventory"));
    }

    private static void addVitamins(Object registry)
    {
        Item vitamins = (new ItemVitamin()).setUnlocalizedName("vitamins");
        vitamins.setCreativeTab(creativeTabPokecube);
        register(vitamins.setRegistryName(PokecubeMod.ID, "vitamins"), registry);

        for (String s : ItemVitamin.vitamins)
        {
            ItemStack stack = new ItemStack(vitamins);
            stack.setTagCompound(new NBTTagCompound());
            stack.getTagCompound().setString("vitamin", s);
            PokecubeItems.addSpecificItemStack(s, stack);
            PokecubeItems.addToHoldables(s);
        }
    }
}
