package pokecube.adventures;

import java.io.File;

import net.minecraftforge.common.MinecraftForge;
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
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.adventures.comands.Config;
import pokecube.adventures.comands.GeneralCommands;
import pokecube.adventures.comands.TeamCommands;
import pokecube.adventures.entity.trainers.EntityLeader;
import pokecube.adventures.entity.trainers.EntityTrainer;
import pokecube.adventures.entity.villager.EntityTrader;
import pokecube.adventures.events.PAEventsHandler;
import pokecube.adventures.events.TeamEventsHandler;
import pokecube.adventures.handlers.BlockHandler;
import pokecube.adventures.handlers.ItemHandler;
import pokecube.adventures.handlers.RecipeHandler;
import pokecube.adventures.handlers.TeamManager;
import pokecube.adventures.handlers.TrainerSpawnHandler;
import pokecube.adventures.items.EntityTarget;
import pokecube.adventures.network.PacketPokeAdv.MessageClient;
import pokecube.adventures.network.PacketPokeAdv.MessageClient.MessageHandlerClient;
import pokecube.adventures.network.PacketPokeAdv.MessageServer;
import pokecube.adventures.network.PacketPokeAdv.MessageServer.MessageHandlerServer;
import pokecube.adventures.utils.DBLoader;
import pokecube.core.PokecubeCore;
import pokecube.core.events.PostPostInit;
import pokecube.core.events.SpawnEvent;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.network.PokecubePacketHandler;

@Mod( // @formatter:off
    modid = PokecubeAdv.ID, 
    name = "Pokecube Adventures", 
    version = PokecubeAdv.version, 
    dependencies = PokecubeAdv.DEPSTRING, 
    guiFactory = "pokecube.adventures.client.gui.config.ModGuiFactory", 
    updateJSON = PokecubeAdv.UPDATEURL, 
    acceptedMinecraftVersions = PokecubeAdv.MCVERSIONS
    )// @formatter:on
public class PokecubeAdv
{
    public static final String ID                 = "pokecube_adventures";
    public static final String version            = "@VERSION";
    public final static String MCVERSIONS         = "@MCVERSION";
    public final static String DEPSTRING    = "required-after:pokecube@@POKECUBEVERSION";

    public final static String UPDATEURL          = "https://raw.githubusercontent.com/Thutmose/Pokecube/master/Pokecube%20Addons/versions.json";
    public static final String TRAINERTEXTUREPATH = ID + ":textures/trainer/";

    public static String       CUSTOMTRAINERFILE;

    public static int          GUITRAINER_ID      = 2;
    public static int          GUIBAG_ID          = 3;
    public static int          GUICLONER_ID       = 4;
    public static int          GUIBIOMESETTER_ID  = 5;
    public static int          GUIAFA_ID          = 6;

    @SidedProxy(clientSide = "pokecube.adventures.client.ClientProxy", serverSide = "pokecube.adventures.CommonProxy")
    public static CommonProxy  proxy;

    @Instance(ID)
    public static PokecubeAdv  instance;

    public static Config       conf;

    @EventHandler
    public void preInit(FMLPreInitializationEvent e)
    {
        conf = new Config(PokecubeMod.core.getPokecubeConfig(e).getConfigFile());
        BlockHandler.registerBlocks();
        ItemHandler.registerItems();
        setTrainerConfig(e);
        MinecraftForge.EVENT_BUS.register(this);
        proxy.preinit();
    }

    @EventHandler
    public void load(FMLInitializationEvent evt)
    {
        proxy.initClient();

        ItemHandler.postInitItems();

        PokecubeMod.packetPipeline.registerMessage(MessageHandlerClient.class, MessageClient.class,
                PokecubeCore.getMessageID(), Side.CLIENT);
        PokecubeMod.packetPipeline.registerMessage(MessageHandlerServer.class, MessageServer.class,
                PokecubeCore.getMessageID(), Side.SERVER);

        NetworkRegistry.INSTANCE.registerGuiHandler(this, proxy);

        // VillageHandlerCubeSalesman.init();

        EntityRegistry.registerModEntity(EntityTarget.class, "targetParticles", 0, this, 16, 3, true);

        EntityRegistry.registerModEntity(EntityTrainer.class, "pokecube:trainer", 1, this, 80, 3, true);
        EntityRegistry.registerModEntity(EntityLeader.class, "pokecube:leader", 2, this, 80, 3, true);
        EntityRegistry.registerModEntity(EntityTrader.class, "pokecube:trader", 3, this, 80, 3, true);

        PAEventsHandler events = new PAEventsHandler();
        TeamEventsHandler teams = new TeamEventsHandler();
        MinecraftForge.EVENT_BUS.register(teams);
        MinecraftForge.EVENT_BUS.register(events);
        new TrainerSpawnHandler();
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent e)
    {
        PokecubePacketHandler.giveHealer = false;
    }

    @SubscribeEvent
    public void postPostInit(PostPostInit e)
    {
        conf.postInit();
        ItemHandler.initBadges();
        RecipeHandler.register();
        DBLoader.load();
        LegendaryConditions.registerSpecialConditions();
    }

    @EventHandler
    public void serverLoad(FMLServerStartingEvent event)
    {
        event.registerServerCommand(new GeneralCommands());
        event.registerServerCommand(new TeamCommands());
    }

    @EventHandler
    public void WorldUnloadEvent(FMLServerStoppedEvent evt)
    {
        TrainerSpawnHandler.trainers.clear();
        TeamManager.clearInstance();
    }

    @SubscribeEvent
    public void pokemobSpawnCheck(SpawnEvent.Pre evt)
    {
        int id = evt.world.provider.getDimensionId();
        for (int i : conf.dimensionBlackList)
        {
            if (i == id)
            {
                evt.setCanceled(true);
                return;
            }
        }
    }

    public static void setTrainerConfig(FMLPreInitializationEvent evt)
    {
        File file = evt.getSuggestedConfigurationFile();
        String seperator = System.getProperty("file.separator");

        String folder = file.getAbsolutePath();
        String name = file.getName();
        folder = folder.replace(name, "pokecube" + seperator + "trainers" + seperator + "trainers.csv");

        CUSTOMTRAINERFILE = folder;

        return;
    }

}
