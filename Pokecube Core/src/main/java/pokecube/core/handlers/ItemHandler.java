package pokecube.core.handlers;

import static pokecube.core.PokecubeItems.addSpecificItemStack;
import static pokecube.core.PokecubeItems.addToEvos;
import static pokecube.core.PokecubeItems.addToHoldables;
import static pokecube.core.PokecubeItems.berries;
import static pokecube.core.PokecubeItems.berryJuice;
import static pokecube.core.PokecubeItems.dawnstone;
import static pokecube.core.PokecubeItems.duskstone;
import static pokecube.core.PokecubeItems.everstone;
import static pokecube.core.PokecubeItems.firestone;
import static pokecube.core.PokecubeItems.kingsrock;
import static pokecube.core.PokecubeItems.leafstone;
import static pokecube.core.PokecubeItems.luckyEgg;
import static pokecube.core.PokecubeItems.megaring;
import static pokecube.core.PokecubeItems.megastone;
import static pokecube.core.PokecubeItems.moonstone;
import static pokecube.core.PokecubeItems.ovalstone;
import static pokecube.core.PokecubeItems.pc;
import static pokecube.core.PokecubeItems.pokedex;
import static pokecube.core.PokecubeItems.pokemobEgg;
import static pokecube.core.PokecubeItems.register;
import static pokecube.core.PokecubeItems.registerItemTexture;
import static pokecube.core.PokecubeItems.repelBlock;
import static pokecube.core.PokecubeItems.revive;
import static pokecube.core.PokecubeItems.shinystone;
import static pokecube.core.PokecubeItems.sunstone;
import static pokecube.core.PokecubeItems.tableBlock;
import static pokecube.core.PokecubeItems.thunderstone;
import static pokecube.core.PokecubeItems.tradingtable;
import static pokecube.core.PokecubeItems.waterstone;
import static pokecube.core.interfaces.PokecubeMod.creativeTabPokecube;
import static pokecube.core.interfaces.PokecubeMod.creativeTabPokecubeBerries;
import static pokecube.core.interfaces.PokecubeMod.creativeTabPokecubeBlocks;
import static pokecube.core.interfaces.PokecubeMod.creativeTabPokecubes;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.oredict.OreDictionary;
import pokecube.core.Mod_Pokecube_Helper;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.blocks.BlockRepel;
import pokecube.core.blocks.TileEntityRepel;
import pokecube.core.blocks.berries.BerryPlantManager;
import pokecube.core.blocks.berries.BlockBerryLeaves;
import pokecube.core.blocks.berries.BlockBerryLog;
import pokecube.core.blocks.berries.BlockBerryWood;
import pokecube.core.blocks.berries.ItemBlockMeta;
import pokecube.core.blocks.fossil.BlockFossilStone;
import pokecube.core.blocks.healtable.BlockHealTable;
import pokecube.core.blocks.nests.BlockNest;
import pokecube.core.blocks.nests.TileEntityNest;
import pokecube.core.blocks.pc.BlockPC;
import pokecube.core.blocks.pc.ItemBlockPC;
import pokecube.core.blocks.pokecubeTable.BlockPokecubeTable;
import pokecube.core.blocks.pokecubeTable.TileEntityPokecubeTable;
import pokecube.core.blocks.tradingTable.BlockTradingTable;
import pokecube.core.blocks.tradingTable.TileEntityTradingTable;
import pokecube.core.database.stats.CaptureStats;
import pokecube.core.events.CaptureEvent.Post;
import pokecube.core.events.CaptureEvent.Pre;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokecube.PokecubeBehavior;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.ItemLuckyEgg;
import pokecube.core.items.ItemPokedex;
import pokecube.core.items.ItemPokemobUseableFood;
import pokecube.core.items.ItemTM;
import pokecube.core.items.berries.BerryManager;
import pokecube.core.items.berries.ItemBerry;
import pokecube.core.items.megastuff.ItemMegaring;
import pokecube.core.items.megastuff.ItemMegastone;
import pokecube.core.items.pokecubes.EntityPokecube;
import pokecube.core.items.pokecubes.Pokecube;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.items.pokemobeggs.ItemPokemobEgg;
import pokecube.core.items.revive.ItemRevive;
import pokecube.core.items.vitamins.VitaminManager;
import pokecube.core.moves.TreeRemover;
import pokecube.core.utils.Tools;
import thut.api.maths.Vector3;

public class ItemHandler extends Mod_Pokecube_Helper
{
    public static Block log0;
    public static Block log1;
    public static Block plank0;
    public static Block leaf0;
    public static Block leaf1;

    private static void addBerries()
    {
        berries = new ItemBerry().setCreativeTab(PokecubeMod.creativeTabPokecubeBerries).setUnlocalizedName("berry");
        register(berries, "berry");
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
        {
            registerItemTexture(PokecubeItems.berries, 0, new ModelResourceLocation("pokecube:berry", "inventory"));
        }
        BerryManager.addBerry("cheri", 1, 10, 0, 0, 0, 0);// Cures Paralysis
        BerryManager.addBerry("chesto", 2, 0, 10, 0, 0, 0);// Cures sleep
        BerryManager.addBerry("pecha", 3, 0, 0, 10, 0, 0);// Cures poison
        BerryManager.addBerry("rawst", 4, 0, 0, 0, 10, 0);// Cures burn
        BerryManager.addBerry("aspear", 5, 0, 0, 0, 0, 10);// Cures freeze
        BerryManager.addBerry("leppa", 6, 10, 0, 10, 10, 10);// Restores 10PP
        BerryManager.addBerry("oran", 7, 10, 10, 10, 10, 10);// Restores 10HP
        BerryManager.addBerry("persim", 8, 10, 10, 10, 0, 10);// Cures confusion
        BerryManager.addBerry("lum", 9, 10, 10, 10, 10, 0);// Cures any status
                                                           // ailment
        BerryManager.addBerry("sitrus", 10, 0, 10, 10, 10, 10);// Restores 1/4
                                                               // HP
        BerryManager.addBerry("nanab", 18, 0, 0, 10, 10, 0);// Pokeblock
                                                            // ingredient
        BerryManager.addBerry("pinap", 20, 10, 0, 0, 0, 10);// Pokeblock
                                                            // ingredient
        BerryManager.addBerry("cornn", 27, 0, 20, 10, 0, 0);// Pokeblock
                                                            // ingredient
        BerryManager.addBerry("enigma", 60, 40, 10, 0, 0, 0);// Restores 1/4 of
                                                             // HP
        BerryManager.addBerry("jaboca", 63, 0, 0, 0, 40, 10);// 4th gen. Causes
                                                             // recoil damage on
                                                             // foe if holder is
                                                             // hit by a
                                                             // physical move
        BerryManager.addBerry("rowap", 64, 10, 0, 0, 0, 40);// 4th gen. Causes
                                                            // recoil damage on
                                                            // foe if holder is
                                                            // hit by a special
                                                            // move

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
        log0 = new BlockBerryLog(0, names).setHardness(2.0F)//.setStepSound(Block.soundTypeWood)
                .setUnlocalizedName("log0");
        leaf0 = new BlockBerryLeaves(0, names).setHardness(0.2F).setLightOpacity(1)//.setStepSound(Block.soundTypeGrass)
                .setUnlocalizedName("leaves0");
        plank0 = new BlockBerryWood(0, names).setHardness(2.0F).setResistance(5.0F)//.setStepSound(Block.soundTypeWood)
                .setUnlocalizedName("wood0");

        register(log0, ItemBlockMeta.class, "pokecube_log0");
        register(plank0, ItemBlockMeta.class, "pokecube_plank0");
        register(leaf0, ItemBlockMeta.class, "pokecube_leaf0");

        names = new String[] { "enigma", "nanab" };
        BlockBerryLog.currentlyConstructing = 4;
        log1 = new BlockBerryLog(4, names).setHardness(2.0F)//.setStepSound(Block.soundTypeWood)
                .setUnlocalizedName("log1");
        leaf1 = new BlockBerryLeaves(4, names).setHardness(0.2F).setLightOpacity(1)//.setStepSound(Block.soundTypeGrass)
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

    }

    private static void addFossils()
    {
        Block fossilStone = (new BlockFossilStone()).setHardness(3F).setResistance(4F)
                .setUnlocalizedName("fossilstone");
        PokecubeItems.register(fossilStone, "fossilstone");
        fossilStone.setCreativeTab(PokecubeMod.creativeTabPokecubeBlocks);
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
            registerItemTexture(Item.getItemFromBlock(fossilStone), 0,
                    new ModelResourceLocation("pokecube:fossilstone", "inventory"));

        Item fossilLileep = (new Item().setUnlocalizedName("LileepFossil"));
        PokecubeItems.register(fossilLileep, "fossilLileep");
        fossilLileep.setCreativeTab(PokecubeMod.creativeTabPokecube);
        PokecubeItems.registerFossil(PokecubeItems.getStack("fossilLileep"), "lileep");
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
        {
            PokecubeItems.registerItemTexture(fossilLileep, 0,
                    new ModelResourceLocation("pokecube:fossilLileep", "inventory"));
        }

        Item fossilAnorith = (new Item().setUnlocalizedName("AnorithFossil"));
        PokecubeItems.register(fossilAnorith, "fossilAnorith");
        fossilAnorith.setCreativeTab(PokecubeMod.creativeTabPokecube);
        PokecubeItems.registerFossil(PokecubeItems.getStack("fossilAnorith"), "anorith");
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
        {
            PokecubeItems.registerItemTexture(fossilAnorith, 0,
                    new ModelResourceLocation("pokecube:fossilAnorith", "inventory"));
        }

        Item fossilCranidos = (new Item().setUnlocalizedName("CranidosFossil"));
        PokecubeItems.register(fossilCranidos, "fossilCranidos");
        fossilCranidos.setCreativeTab(PokecubeMod.creativeTabPokecube);
        PokecubeItems.registerFossil(PokecubeItems.getStack("fossilCranidos"), "cranidos");
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
        {
            PokecubeItems.registerItemTexture(fossilCranidos, 0,
                    new ModelResourceLocation("pokecube:fossilCranidos", "inventory"));
        }

        Item fossilShieldon = (new Item().setUnlocalizedName("ShieldonFossil"));
        PokecubeItems.register(fossilShieldon, "fossilShieldon");
        fossilShieldon.setCreativeTab(PokecubeMod.creativeTabPokecube);
        PokecubeItems.registerFossil(PokecubeItems.getStack("fossilShieldon"), "shieldon");
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
        {
            PokecubeItems.registerItemTexture(fossilShieldon, 0,
                    new ModelResourceLocation("pokecube:fossilShieldon", "inventory"));
        }

        Item dome = (new Item().setUnlocalizedName("DomeFossil"));
        PokecubeItems.register(dome, "fossilDome");
        dome.setCreativeTab(PokecubeMod.creativeTabPokecube);
        PokecubeItems.registerFossil(PokecubeItems.getStack("fossilDome"), "kabuto");
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
        {
            PokecubeItems.registerItemTexture(dome, 0, new ModelResourceLocation("pokecube:fossilDome", "inventory"));
        }

        Item helix = (new Item().setUnlocalizedName("HelixFossil"));
        PokecubeItems.register(helix, "fossilHelix");
        helix.setCreativeTab(PokecubeMod.creativeTabPokecube);
        PokecubeItems.registerFossil(PokecubeItems.getStack("fossilHelix"), "omanyte");
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
        {
            PokecubeItems.registerItemTexture(helix, 0, new ModelResourceLocation("pokecube:fossilHelix", "inventory"));
        }
    }

    public static void addItems(Mod_Pokecube_Helper helper)
    {
        addPokecubes();
        addStones();
        addBerries();
        addVitamins();
        addFossils();

        GameRegistry.registerTileEntity(TileEntityRepel.class, "repel");
        repelBlock = new BlockRepel();
        repelBlock.setUnlocalizedName("repel");
        repelBlock.setCreativeTab(creativeTabPokecubeBerries);
        register(repelBlock, ItemBlock.class, "repel");
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
            registerItemTexture(Item.getItemFromBlock(repelBlock), 0,
                    new ModelResourceLocation("pokecube:repel", "inventory"));

        GameRegistry.registerTileEntity(TileEntityPokecubeTable.class, "pokecube:pokecube_table");
        tableBlock = new BlockPokecubeTable();
        tableBlock.setUnlocalizedName("pokecube_table");
        tableBlock.setCreativeTab(creativeTabPokecubeBlocks);
        register(tableBlock, ItemBlock.class, "pokecube_table");

        luckyEgg = new ItemLuckyEgg().setUnlocalizedName("luckyEgg").setCreativeTab(creativeTabPokecube);
        register(luckyEgg, "luckyEgg");
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
            registerItemTexture(luckyEgg, 0, new ModelResourceLocation("egg", "inventory"));
        addToHoldables("luckyEgg");

        BlockHealTable pokecenter = (BlockHealTable) (new BlockHealTable()).setUnlocalizedName("pokecenter");
        pokecenter.setCreativeTab(creativeTabPokecubeBlocks);

        register(pokecenter, ItemBlock.class, "pokecenter");
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
        {
            registerItemTexture(Item.getItemFromBlock(pokecenter), 0,
                    new ModelResourceLocation("pokecube:pokecenter", "inventory"));
            registerItemTexture(Item.getItemFromBlock(pokecenter), 1,
                    new ModelResourceLocation("pokecube:pokecenter", "inventory"));
        }

        Block nest = new BlockNest().setCreativeTab(PokecubeMod.creativeTabPokecubeBlocks)
                .setUnlocalizedName("pokemobNest");
        PokecubeItems.register(nest, ItemBlock.class, "pokemobNest");
        GameRegistry.registerTileEntity(TileEntityNest.class, "pokemobNest");
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
        {
            registerItemTexture(Item.getItemFromBlock(nest), 0,
                    new ModelResourceLocation("minecraft:stone", "inventory"));
        }

        GameRegistry.registerTileEntity(pokecube.core.blocks.healtable.TileHealTable.class, "pokecenter");
        PokecubeItems.pokecenter = pokecenter;

        pokemobEgg = new ItemPokemobEgg().setUnlocalizedName("pokemobEgg");
        register(pokemobEgg, "pokemobEgg");
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
            registerItemTexture(pokemobEgg, 0, new ModelResourceLocation("pokecube:pokemobEgg", "inventory"));
        OreDictionary.registerOre("egg", new ItemStack(pokemobEgg, 1, OreDictionary.WILDCARD_VALUE));
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
        {
            registerItemTexture(pokemobEgg, 0, new ModelResourceLocation("pokecube:pokemobEgg", "inventory"));
        }
        pokedex = (new ItemPokedex()).setUnlocalizedName("pokedex");
        pokedex.setCreativeTab(creativeTabPokecube);
        register(pokedex, "pokedex");
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) for (int i = 0; i < 10; i++)
            registerItemTexture(pokedex, i, new ModelResourceLocation("pokecube:pokedex", "inventory"));

        megaring = (new ItemMegaring()).setUnlocalizedName("megaring");
        megaring.setCreativeTab(creativeTabPokecube);
        register(megaring, "megaring");
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
            registerItemTexture(megaring, 0, new ModelResourceLocation("pokecube:megaring", "inventory"));

        megastone = (new ItemMegastone()).setUnlocalizedName("megastone");
        megastone.setCreativeTab(creativeTabPokecube);
        register(megastone, "megastone");
        addToHoldables("megastone");

        revive = (new ItemRevive()).setUnlocalizedName("revive");
        revive.setCreativeTab(creativeTabPokecube);
        register(revive, "revive");
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
            registerItemTexture(revive, 0, new ModelResourceLocation("pokecube:revive", "inventory"));
        addToHoldables("revive");

        tradingtable = (new BlockTradingTable()).setUnlocalizedName("tradingtable");
        tradingtable.setCreativeTab(PokecubeMod.creativeTabPokecubeBlocks);
        GameRegistry.registerTileEntity(TileEntityTradingTable.class, "tradingtable");
        PokecubeItems.register(tradingtable, "tradingtable");

        pc = (new BlockPC()).setUnlocalizedName("pc");
        GameRegistry.registerTileEntity(pokecube.core.blocks.pc.TileEntityPC.class, "pc");
        pc.setCreativeTab(PokecubeMod.creativeTabPokecubeBlocks);
        PokecubeItems.register(pc, ItemBlockPC.class, "pc");
        PokecubeItems.addSpecificItemStack("pctop", new ItemStack(pc, 1, 8));
        PokecubeItems.addSpecificItemStack("pcbase", new ItemStack(pc, 1, 0));

        Item tm = (new ItemTM()).setUnlocalizedName("tm");
        tm.setCreativeTab(creativeTabPokecube);
        register(tm, "tm");
        addSpecificItemStack("rarecandy", new ItemStack(tm, 1, 20));
        addSpecificItemStack("emerald_shard", new ItemStack(tm, 1, 19));
    }

    private static void addPokecubes()
    {
        Pokecube pokecube = new Pokecube();
        pokecube.setUnlocalizedName("pokecube").setCreativeTab(creativeTabPokecubes);
        register(pokecube);
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
            registerItemTexture(pokecube, 0, new ModelResourceLocation("pokecube:pokecube", "inventory"));

        Pokecube greatcube = new Pokecube();
        greatcube.setUnlocalizedName("greatcube").setCreativeTab(creativeTabPokecubes);
        register(greatcube);
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
            registerItemTexture(greatcube, 0, new ModelResourceLocation("pokecube:greatcube", "inventory"));

        Pokecube ultracube = new Pokecube();
        ultracube.setUnlocalizedName("ultracube").setCreativeTab(creativeTabPokecubes);
        register(ultracube);
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
            registerItemTexture(ultracube, 0, new ModelResourceLocation("pokecube:ultracube", "inventory"));

        Pokecube mastercube = new Pokecube();
        mastercube.setUnlocalizedName("mastercube").setCreativeTab(creativeTabPokecubes);
        register(mastercube);
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
            registerItemTexture(mastercube, 0, new ModelResourceLocation("pokecube:mastercube", "inventory"));

        Pokecube snagcube = new Pokecube();
        snagcube.setUnlocalizedName("snagcube").setCreativeTab(creativeTabPokecubes);
        register(snagcube);
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
            registerItemTexture(snagcube, 0, new ModelResourceLocation("pokecube:snagcube", "inventory"));

        Pokecube duskcube = new Pokecube();
        duskcube.setUnlocalizedName("duskcube").setCreativeTab(creativeTabPokecubes);
        register(duskcube);
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
            registerItemTexture(duskcube, 0, new ModelResourceLocation("pokecube:duskcube", "inventory"));

        Pokecube quickcube = new Pokecube();
        quickcube.setUnlocalizedName("quickcube").setCreativeTab(creativeTabPokecubes);
        register(quickcube);
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
            registerItemTexture(quickcube, 0, new ModelResourceLocation("pokecube:quickcube", "inventory"));

        Pokecube timercube = new Pokecube();
        timercube.setUnlocalizedName("timercube").setCreativeTab(creativeTabPokecubes);
        register(timercube);
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
            registerItemTexture(timercube, 0, new ModelResourceLocation("pokecube:timercube", "inventory"));

        Pokecube netcube = new Pokecube();
        netcube.setUnlocalizedName("netcube").setCreativeTab(creativeTabPokecubes);
        register(netcube);
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
            registerItemTexture(netcube, 0, new ModelResourceLocation("pokecube:netcube", "inventory"));

        Pokecube nestcube = new Pokecube();
        nestcube.setUnlocalizedName("nestcube").setCreativeTab(creativeTabPokecubes);
        register(nestcube);
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
            registerItemTexture(nestcube, 0, new ModelResourceLocation("pokecube:nestcube", "inventory"));

        Pokecube divecube = new Pokecube();
        divecube.setUnlocalizedName("divecube").setCreativeTab(creativeTabPokecubes);
        register(divecube);
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
            registerItemTexture(divecube, 0, new ModelResourceLocation("pokecube:divecube", "inventory"));

        Pokecube repeatcube = new Pokecube();
        repeatcube.setUnlocalizedName("repeatcube").setCreativeTab(creativeTabPokecubes);
        register(repeatcube);
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
            registerItemTexture(repeatcube, 0, new ModelResourceLocation("pokecube:repeatcube", "inventory"));

        Pokecube premiercube = new Pokecube();
        premiercube.setUnlocalizedName("premiercube").setCreativeTab(creativeTabPokecubes);
        register(premiercube);
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
            registerItemTexture(premiercube, 0, new ModelResourceLocation("pokecube:premiercube", "inventory"));

        Pokecube cherishcube = new Pokecube();
        cherishcube.setUnlocalizedName("cherishcube").setCreativeTab(creativeTabPokecubes);
        register(cherishcube);
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
            registerItemTexture(cherishcube, 0, new ModelResourceLocation("pokecube:cherishcube", "inventory"));

        Pokecube pokeseal = new Pokecube();
        pokeseal.setUnlocalizedName("pokeseal").setCreativeTab(creativeTabPokecubes);
        register(pokeseal);
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
            registerItemTexture(pokeseal, 0, new ModelResourceLocation("pokecube:pokeseal", "inventory"));

        PokecubeItems.addCube(0, new Object[] { pokecube });
        PokecubeItems.addCube(1, new Object[] { greatcube });
        PokecubeItems.addCube(2, new Object[] { ultracube });
        PokecubeItems.addCube(3, new Object[] { mastercube });
        PokecubeItems.addCube(99, new Object[] { snagcube });
        PokecubeItems.addCube(5, new Object[] { duskcube });
        PokecubeItems.addCube(6, new Object[] { quickcube });
        PokecubeItems.addCube(7, new Object[] { timercube });
        PokecubeItems.addCube(8, new Object[] { netcube });
        PokecubeItems.addCube(9, new Object[] { nestcube });
        PokecubeItems.addCube(10, new Object[] { divecube });
        PokecubeItems.addCube(11, new Object[] { repeatcube });
        PokecubeItems.addCube(12, new Object[] { premiercube });
        PokecubeItems.addCube(13, new Object[] { cherishcube });
        PokecubeItems.addCube(-2, new Object[] { pokeseal });

        PokecubeBehavior snag = new PokecubeBehavior()
        {

            @Override
            public void onPostCapture(Post evt)
            {
                IPokemob mob = evt.caught;
                mob.setPokemonOwnerByName("");
                mob.setPokemonAIState(IMoveConstants.TAMED, false);
                ((Entity) mob).entityDropItem(PokecubeManager.pokemobToItem(mob), 0.0F);
            }

            @Override
            public void onPreCapture(Pre evt)
            {
                if (evt.caught.isShadow())
                {
                    EntityPokecube cube = (EntityPokecube) evt.pokecube;

                    IPokemob mob = (IPokemob) PokecubeCore.instance.createEntityByPokedexNb(evt.caught.getPokedexNb(),
                            cube.worldObj);
                    Vector3 v = Vector3.getNewVector();
                    cube.tilt = Tools.computeCatchRate(mob, 1);
                    cube.time = cube.tilt * 20;
                    evt.caught.setPokecubeId(PokecubeItems.getCubeId(evt.filledCube));
                    cube.setEntityItemStack(PokecubeManager.pokemobToItem(evt.caught));
                    PokecubeManager.setTilt(cube.getEntityItem(), cube.tilt);
                    v.set(evt.caught).moveEntity(cube);
                    ((Entity) evt.caught).setDead();
                    cube.setVelocity(0, 0.1, 0);
                    cube.worldObj.spawnEntityInWorld(cube.copy());
                    evt.pokecube.setDead();
                    System.out.println(cube.tilt);
                }
                evt.setCanceled(true);
            }
        };

        PokecubeBehavior repeat = new PokecubeBehavior()
        {
            @Override
            public void onPostCapture(Post evt)
            {

            }

            @Override
            public void onPreCapture(Pre evt)
            {
                EntityPokecube cube = (EntityPokecube) evt.pokecube;

                IPokemob mob = (IPokemob) PokecubeCore.instance.createEntityByPokedexNb(evt.caught.getPokedexNb(),
                        cube.worldObj);
                Vector3 v = Vector3.getNewVector();
                Entity thrower = cube.shootingEntity;
                int has = CaptureStats.getTotalNumberOfPokemobCaughtBy(thrower.getUniqueID().toString(),
                        mob.getPokedexEntry());
                // TODO make this also check achievments, to check if pokemon
                // has been traded to or hatched.
                double rate = has > 0 ? 3 : 1;
                cube.tilt = Tools.computeCatchRate(mob, rate);
                cube.time = cube.tilt * 4;
                evt.caught.setPokecubeId(PokecubeItems.getCubeId(evt.filledCube));
                cube.setEntityItemStack(PokecubeManager.pokemobToItem(evt.caught));
                PokecubeManager.setTilt(cube.getEntityItem(), cube.tilt);
                v.set(evt.caught).moveEntity(cube);
                ((Entity) evt.caught).setDead();
                cube.setVelocity(0, 0.1, 0);
                cube.worldObj.spawnEntityInWorld(cube.copy());
                evt.setCanceled(true);
                evt.pokecube.setDead();
            }

        };

        PokecubeBehavior.addCubeBehavior(99, snag);
        PokecubeBehavior.addCubeBehavior(11, repeat);

    }

    private static void addStones()
    {
        waterstone = (new Item()).setUnlocalizedName("waterstone");
        waterstone.setCreativeTab(creativeTabPokecube);
        register(waterstone, "waterstone");
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
            registerItemTexture(waterstone, 0, new ModelResourceLocation("pokecube:waterstone", "inventory"));

        firestone = (new Item()).setUnlocalizedName("firestone");
        firestone.setCreativeTab(creativeTabPokecube);
        register(firestone, "firestone");
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
            registerItemTexture(firestone, 0, new ModelResourceLocation("pokecube:firestone", "inventory"));

        leafstone = (new Item()).setUnlocalizedName("leafstone");
        leafstone.setCreativeTab(creativeTabPokecube);
        register(leafstone, "leafstone");
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
            registerItemTexture(leafstone, 0, new ModelResourceLocation("pokecube:leafstone", "inventory"));

        thunderstone = (new Item());
        thunderstone.setUnlocalizedName("thunderstone");
        register(thunderstone, "thunderstone");
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
            registerItemTexture(thunderstone, 0, new ModelResourceLocation("pokecube:thunderstone", "inventory"));

        thunderstone.setCreativeTab(creativeTabPokecube);
        moonstone = (new Item()).setUnlocalizedName("moonstone");
        moonstone.setCreativeTab(creativeTabPokecube);
        register(moonstone, "moonstone");
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
            registerItemTexture(moonstone, 0, new ModelResourceLocation("pokecube:moonstone", "inventory"));

        sunstone = (new Item()).setUnlocalizedName("sunstone");
        sunstone.setCreativeTab(creativeTabPokecube);
        register(sunstone, "sunstone");
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
            registerItemTexture(sunstone, 0, new ModelResourceLocation("pokecube:sunstone", "inventory"));

        shinystone = (new Item()).setUnlocalizedName("shinystone");
        shinystone.setCreativeTab(creativeTabPokecube);
        register(shinystone, "shinystone");
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
            registerItemTexture(shinystone, 0, new ModelResourceLocation("pokecube:shinystone", "inventory"));

        ovalstone = (new Item()).setUnlocalizedName("ovalstone");
        ovalstone.setCreativeTab(creativeTabPokecube);
        register(ovalstone, "ovalstone");
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
            registerItemTexture(ovalstone, 0, new ModelResourceLocation("pokecube:ovalstone", "inventory"));

        everstone = (new Item()).setUnlocalizedName("everstone");
        everstone.setCreativeTab(creativeTabPokecube);
        register(everstone, "everstone");
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
            registerItemTexture(everstone, 0, new ModelResourceLocation("pokecube:everstone", "inventory"));

        duskstone = (new Item()).setUnlocalizedName("duskstone");
        duskstone.setCreativeTab(creativeTabPokecube);
        register(duskstone, "duskstone");
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
            registerItemTexture(duskstone, 0, new ModelResourceLocation("pokecube:duskstone", "inventory"));

        dawnstone = (new Item()).setUnlocalizedName("dawnstone");
        dawnstone.setCreativeTab(creativeTabPokecube);
        register(dawnstone, "dawnstone");
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
            registerItemTexture(dawnstone, 0, new ModelResourceLocation("pokecube:dawnstone", "inventory"));

        kingsrock = (new Item()).setUnlocalizedName("kingsrock");
        kingsrock.setCreativeTab(creativeTabPokecube);
        register(kingsrock, "kingsrock");
        addToHoldables("kingsrock");
        addToEvos("kingsrock");
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
            registerItemTexture(kingsrock, 0, new ModelResourceLocation("pokecube:kingsrock", "inventory"));

        berryJuice = (new ItemPokemobUseableFood(6, 0.6f, false)).setUnlocalizedName("berryjuice");
        berryJuice.setCreativeTab(creativeTabPokecube);
        register(berryJuice, "berryjuice");
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
            registerItemTexture(berryJuice, 0, new ModelResourceLocation("pokecube:berryjuice", "inventory"));
    }

    private static void addVitamins()
    {
        VitaminManager.addVitamin("carbos", 1);
        VitaminManager.addVitamin("zinc", 2);
        VitaminManager.addVitamin("protein", 3);
        VitaminManager.addVitamin("calcium", 4);
        VitaminManager.addVitamin("hpup", 5);
        VitaminManager.addVitamin("iron", 6);

    }
}
