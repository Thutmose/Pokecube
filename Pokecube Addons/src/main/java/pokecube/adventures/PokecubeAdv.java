package pokecube.adventures;

import java.io.File;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.adventures.advancements.Triggers;
import pokecube.adventures.comands.BattleCommand;
import pokecube.adventures.comands.Config;
import pokecube.adventures.comands.GeneralCommands;
import pokecube.adventures.entity.helper.capabilities.CapabilityHasPokemobs;
import pokecube.adventures.entity.helper.capabilities.CapabilityHasPokemobs.DefaultPokemobs;
import pokecube.adventures.entity.helper.capabilities.CapabilityHasPokemobs.IHasPokemobs;
import pokecube.adventures.entity.helper.capabilities.CapabilityHasRewards;
import pokecube.adventures.entity.helper.capabilities.CapabilityHasRewards.DefaultRewards;
import pokecube.adventures.entity.helper.capabilities.CapabilityHasRewards.IHasRewards;
import pokecube.adventures.entity.helper.capabilities.CapabilityNPCAIStates;
import pokecube.adventures.entity.helper.capabilities.CapabilityNPCAIStates.DefaultAIStates;
import pokecube.adventures.entity.helper.capabilities.CapabilityNPCAIStates.IHasNPCAIStates;
import pokecube.adventures.entity.helper.capabilities.CapabilityNPCMessages;
import pokecube.adventures.entity.helper.capabilities.CapabilityNPCMessages.DefaultMessager;
import pokecube.adventures.entity.helper.capabilities.CapabilityNPCMessages.IHasMessages;
import pokecube.adventures.entity.trainers.EntityLeader;
import pokecube.adventures.entity.trainers.EntityPokemartSeller;
import pokecube.adventures.entity.trainers.EntityTrainer;
import pokecube.adventures.entity.trainers.TypeTrainer;
import pokecube.adventures.events.PAEventsHandler;
import pokecube.adventures.handlers.BlockHandler;
import pokecube.adventures.handlers.ItemHandler;
import pokecube.adventures.handlers.RecipeHandler;
import pokecube.adventures.handlers.TrainerSpawnHandler;
import pokecube.adventures.items.EntityTarget;
import pokecube.adventures.items.bags.InventoryBag;
import pokecube.adventures.legends.LegendaryConditions;
import pokecube.adventures.network.PacketPokeAdv;
import pokecube.adventures.network.PacketPokeAdv.MessageClient;
import pokecube.adventures.network.PacketPokeAdv.MessageClient.MessageHandlerClient;
import pokecube.adventures.network.PacketPokeAdv.MessageServer;
import pokecube.adventures.network.PacketPokeAdv.MessageServer.MessageHandlerServer;
import pokecube.adventures.network.packets.PacketTrainer;
import pokecube.adventures.utils.DBLoader;
import pokecube.core.PokecubeCore;
import pokecube.core.events.PostPostInit;
import pokecube.core.interfaces.PokecubeMod;
import thut.core.common.commands.CommandConfig;
import thut.lib.CompatWrapper;

@Mod( // @formatter:off
        modid = PokecubeAdv.ID, name = "Pokecube Adventures", version = PokecubeAdv.version, dependencies = PokecubeAdv.DEPSTRING, acceptableRemoteVersions = PokecubeAdv.MINVERSION, guiFactory = "pokecube.adventures.client.gui.config.ModGuiFactory", updateJSON = PokecubeAdv.UPDATEURL, acceptedMinecraftVersions = PokecubeAdv.MCVERSIONS) // @formatter:on
public class PokecubeAdv
{
    public static final String ID                 = "pokecube_adventures";
    public static final String version            = "@VERSION";
    public final static String MCVERSIONS         = "@MCVERSION";
    public final static String MINVERSION         = "@MINVERSION";
    public final static String DEPSTRING          = "required-after:pokecube@@POKECUBEVERSION;"
            + "after:thut_wearables;" + "after:thutessentials;" + "after:waila;" + "after:advancedrocketry;"
            + "after:thut_bling;" + "after:theoneprobe;" + "after:tesla;" + "after:lostcities;" + "after:ruins;"
            + "after:ftbl;" + "after:journeymap;" + "after:reccomplex;" + "after:minefactoryreloaded";

    public final static String UPDATEURL          = "https://gist.githubusercontent.com/Thutmose/4d7320c36696cd39b336/raw/revival.json";
    public static final String TRAINERTEXTUREPATH = ID + ":textures/trainer/";

    public static String       CUSTOMTRAINERFILE;

    public static int          GUITRAINER_ID      = 2;
    public static int          GUIBAG_ID          = 3;
    public static int          GUICLONER_ID       = 4;
    public static int          GUIBIOMESETTER_ID  = 5;
    public static int          GUIAFA_ID          = 6;
    public static int          GUISPLICER_ID      = 7;
    public static int          GUIEXTRACTOR_ID    = 8;

    public static boolean      tesla              = false;

    @SidedProxy(clientSide = "pokecube.adventures.client.ClientProxy", serverSide = "pokecube.adventures.CommonProxy")
    public static CommonProxy  proxy;

    @Instance(ID)
    public static PokecubeAdv  instance;

    public static Config       conf;

    public static void setTrainerConfig(FMLPreInitializationEvent evt)
    {
        File file = evt.getSuggestedConfigurationFile();
        String seperator = System.getProperty("file.separator");

        String folder = file.getAbsolutePath();
        String name = file.getName();
        folder = folder.replace(name, "pokecube" + seperator + "trainers" + seperator + "trainers.xml");

        CUSTOMTRAINERFILE = folder;

        return;
    }

    public PokecubeAdv()
    {
        MinecraftForge.EVENT_BUS.register(this);
        Triggers.init();
    }

    @EventHandler
    public void load(FMLInitializationEvent evt)
    {
        proxy.initClient();
        PacketPokeAdv.init();
        PokecubeMod.packetPipeline.registerMessage(MessageHandlerClient.class, MessageClient.class,
                PokecubeCore.getMessageID(), Side.CLIENT);
        PokecubeMod.packetPipeline.registerMessage(MessageHandlerServer.class, MessageServer.class,
                PokecubeCore.getMessageID(), Side.SERVER);

        NetworkRegistry.INSTANCE.registerGuiHandler(this, proxy);

        CompatWrapper.registerModEntity(EntityTarget.class, "targetParticles", 0, this, 16, 3, true);
        CompatWrapper.registerModEntity(EntityTrainer.class, "trainer", 1, this, 80, 3, true);
        CompatWrapper.registerModEntity(EntityLeader.class, "leader", 2, this, 80, 3, true);
        CompatWrapper.registerModEntity(EntityPokemartSeller.class, "trainermerchant", 4, this, 80, 3, true);
        PAEventsHandler events = new PAEventsHandler();
        MinecraftForge.EVENT_BUS.register(events);
        new TrainerSpawnHandler();
        ItemHandler.handleLoot();
        RecipeHandler.register(evt);
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent e)
    {
        PacketTrainer.register();
        proxy.postinit();
    }

    @SubscribeEvent
    public void postPostInit(PostPostInit e)
    {
        conf.postInit();
        if (conf.legendaryConditions) new LegendaryConditions();
        RecipeHandler.addClonerRecipes();
        DBLoader.load();
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent e)
    {
        conf = new Config(PokecubeMod.core.getPokecubeConfig(e).getConfigFile());
        tesla = Loader.isModLoaded("tesla");
        DBLoader.preInit(e);
        setTrainerConfig(e);
        MinecraftForge.EVENT_BUS.register(new ItemHandler());
        proxy.preinit();
        RecipeHandler.preInit();

        CapabilityManager.INSTANCE.register(IHasPokemobs.class,
                CapabilityHasPokemobs.storage = new CapabilityHasPokemobs.Storage(), DefaultPokemobs::new);
        CapabilityManager.INSTANCE.register(IHasNPCAIStates.class,
                CapabilityNPCAIStates.storage = new CapabilityNPCAIStates.Storage(), DefaultAIStates::new);
        CapabilityManager.INSTANCE.register(IHasMessages.class,
                CapabilityNPCMessages.storage = new CapabilityNPCMessages.Storage(), DefaultMessager::new);
        CapabilityManager.INSTANCE.register(IHasRewards.class,
                CapabilityHasRewards.storage = new CapabilityHasRewards.Storage(), DefaultRewards::new);
    }

    @SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> evt)
    {
        ItemHandler.registerItems(evt.getRegistry());
        proxy.initItemModels();
    }

    @SubscribeEvent
    public void registerBlocks(RegistryEvent.Register<Block> evt)
    {
        BlockHandler.registerBlocks(evt.getRegistry());
        proxy.initBlockModels();
    }

    @SubscribeEvent
    public void registerTiles(RegistryEvent.Register<Block> evt)
    {
        BlockHandler.registerTiles(evt.getRegistry());
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent event)
    {
        event.registerServerCommand(new GeneralCommands());
        event.registerServerCommand(new BattleCommand());
        event.registerServerCommand(new CommandConfig("pokeadvsettings", Config.instance));
        TypeTrainer.initSpawns();
    }

    @EventHandler
    public void serverEnding(FMLServerStoppedEvent evt)
    {
        TrainerSpawnHandler.trainers.clear();
        InventoryBag.clearInventory();
    }

}
