package pokecube.adventures;

import java.io.File;

import net.minecraft.init.Items;
import net.minecraft.stats.Achievement;
import net.minecraftforge.common.AchievementPage;
import net.minecraftforge.common.MinecraftForge;
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
import pokecube.adventures.achievements.AchievementDefeatLeader;
import pokecube.adventures.achievements.AchievementDefeatTrainer;
import pokecube.adventures.achievements.AchievementGetBadge;
import pokecube.adventures.comands.Config;
import pokecube.adventures.comands.GeneralCommands;
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
import pokecube.adventures.items.ItemBadge;
import pokecube.adventures.items.bags.InventoryBag;
import pokecube.adventures.legends.LegendaryConditions;
import pokecube.adventures.network.PacketPokeAdv;
import pokecube.adventures.network.PacketPokeAdv.MessageClient;
import pokecube.adventures.network.PacketPokeAdv.MessageClient.MessageHandlerClient;
import pokecube.adventures.network.PacketPokeAdv.MessageServer;
import pokecube.adventures.network.PacketPokeAdv.MessageServer.MessageHandlerServer;
import pokecube.adventures.utils.DBLoader;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.events.PostPostInit;
import pokecube.core.interfaces.PokecubeMod;
import thut.lib.CompatWrapper;

@Mod( // @formatter:off
        modid = PokecubeAdv.ID, name = "Pokecube Adventures", version = PokecubeAdv.version, dependencies = PokecubeAdv.DEPSTRING, guiFactory = "pokecube.adventures.client.gui.config.ModGuiFactory", updateJSON = PokecubeAdv.UPDATEURL, acceptedMinecraftVersions = PokecubeAdv.MCVERSIONS) // @formatter:on
public class PokecubeAdv
{
    public static final String ID                 = "pokecube_adventures";
    public static final String version            = "@VERSION";
    public final static String MCVERSIONS         = "@MCVERSION";
    public final static String DEPSTRING          = "required-after:pokecube@@POKECUBEVERSION";

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
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent e)
    {
    }

    @SubscribeEvent
    public void postPostInit(PostPostInit e)
    {
        conf.postInit();
        if (conf.legendaryConditions) new LegendaryConditions();
        RecipeHandler.register();
        DBLoader.load();
        registerAchieves();
    }
    
    private void registerAchieves()
    {
        int x = -3;
        int y = -2;
        Achievement beatTrainer = new AchievementDefeatTrainer("pokeadv.defeat.trainer", "pokeadv.defeat.trainer", x,
                y++, Items.IRON_SWORD, null);
        beatTrainer.registerStat();
        Achievement beatLeader = new AchievementDefeatLeader("pokeadv.defeat.leader", "pokeadv.defeat.leader", x, y++,
                Items.DIAMOND_SWORD, null);
        beatLeader.registerStat();
        AchievementPage.getAchievementPage(0).getAchievements().add(beatLeader);
        AchievementPage.getAchievementPage(0).getAchievements().add(beatTrainer);
        for (String s : ItemBadge.variants)
        {
            Achievement badge = new AchievementGetBadge("pokeadv." + s, "achievement.pokeadv.get." + s, x, y++,
                    PokecubeItems.getStack(s), beatLeader);
            badge.registerStat();
            AchievementPage.getAchievementPage(0).getAchievements().add(badge);
        }
    }

    
    @EventHandler
    public void preInit(FMLPreInitializationEvent e)
    {
        conf = new Config(PokecubeMod.core.getPokecubeConfig(e).getConfigFile());
        tesla = Loader.isModLoaded("tesla");
        DBLoader.preInit(e);
        setTrainerConfig(e);
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new ItemHandler());
        proxy.preinit();
        RecipeHandler.preInit();
    }

    @EventHandler
    public void registerItems(FMLPreInitializationEvent e)
    {
        ItemHandler.registerItems(e);
    }
    @EventHandler
    public void registerBlocks(FMLPreInitializationEvent e)
    {
        BlockHandler.registerBlocks(e);
    }
    @EventHandler
    public void registerTiles(FMLPreInitializationEvent e)
    {
        BlockHandler.registerTiles(e);
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent event)
    {
        event.registerServerCommand(new GeneralCommands());
        TypeTrainer.initSpawns();
    }

    @EventHandler
    public void serverEnding(FMLServerStoppedEvent evt)
    {
        TrainerSpawnHandler.trainers.clear();
        InventoryBag.clearInventory();
    }

}
