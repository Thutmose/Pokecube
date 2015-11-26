package pokecube.adventures;

import java.io.File;

import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.VillagerRegistry;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.adventures.blocks.berries.WorldGenBerries;
import pokecube.adventures.entity.trainers.EntityLeader;
import pokecube.adventures.entity.trainers.EntityTrainer;
import pokecube.adventures.entity.villager.EntityTrader;
import pokecube.adventures.events.PAEventsHandler;
import pokecube.adventures.events.TeamEventsHandler;
import pokecube.adventures.handlers.BlockHandler;
import pokecube.adventures.handlers.ConfigHandler;
import pokecube.adventures.handlers.GeneralCommands;
import pokecube.adventures.handlers.ItemHandler;
import pokecube.adventures.handlers.RecipeHandler;
import pokecube.adventures.handlers.TeamCommands;
import pokecube.adventures.handlers.TrainerSpawnHandler;
import pokecube.adventures.items.*;
import pokecube.adventures.network.PacketPokeAdv.MessageClient;
import pokecube.adventures.network.PacketPokeAdv.MessageClient.MessageHandlerClient;
import pokecube.adventures.network.PacketPokeAdv.MessageServer;
import pokecube.adventures.network.PacketPokeAdv.MessageServer.MessageHandlerServer;
import pokecube.adventures.utils.DBLoader;
import pokecube.core.mod_Pokecube;
import pokecube.core.events.PostPostInit;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.network.PokecubePacketHandler;

@Mod(modid = PokecubeAdv.ID, name = "Pokecube Adventures", version = PokecubeAdv.version, dependencies = "required-after:pokecube")
public class PokecubeAdv
{
	public static final String	ID		= "pokecube_adventures";
	public static final String	version	= "0.1.0";

	public static final String TRAINERTEXTUREPATH = ID + ":textures/trainer/";

	// public static SimpleNetworkWrapper wrapper;

	public static String CUSTOMTRAINERFILE;

	public static int	GUITRAINER_ID		= 2;
	public static int	GUIBAG_ID			= 3;
	public static int	GUICLONER_ID		= 4;

	@SidedProxy(clientSide = "pokecube.adventures.client.ClientProxy", serverSide = "pokecube.adventures.CommonProxy")
	public static CommonProxy proxy;

	@Instance(ID)
	public static PokecubeAdv instance;

	@EventHandler
	public void preInit(FMLPreInitializationEvent e)
	{
		BlockHandler.registerBlocks();
		ItemHandler.registerItems();
		doMetastuff();
		Configuration config = PokecubeMod.core.getPokecubeConfig(e);
		ConfigHandler.load(config);
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
				mod_Pokecube.getMessageID(), Side.CLIENT);
		PokecubeMod.packetPipeline.registerMessage(MessageHandlerServer.class, MessageServer.class,
				mod_Pokecube.getMessageID(), Side.SERVER);

		NetworkRegistry.INSTANCE.registerGuiHandler(this, proxy);

//		VillageHandlerCubeSalesman.init();

        EntityRegistry.registerModEntity(EntityTarget.class, "targetParticles", 0, this, 16, 3, true);

		EntityRegistry.registerModEntity(EntityTrainer.class, "pokecube:trainer", 1, this, 80, 3, true);
		EntityRegistry.registerModEntity(EntityLeader.class, "pokecube:leader", 2, this, 80, 3, true);
		EntityRegistry.registerModEntity(EntityTrader.class, "pokecube:trader", 3, this, 80, 3, true);

		GameRegistry.registerWorldGenerator(new WorldGenBerries(), 10);// TODO find number

		PAEventsHandler events = new PAEventsHandler();
		TeamEventsHandler teams = new TeamEventsHandler();
		MinecraftForge.EVENT_BUS.register(teams);
		MinecraftForge.EVENT_BUS.register(events);
		FMLCommonHandler.instance().bus().register(events);
		new TrainerSpawnHandler();
		RecipeHandler.register();
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent e)
	{
		PokecubePacketHandler.giveHealer = false;
	}

	@SubscribeEvent
	public void postPostInit(PostPostInit e)
	{
		DBLoader.load();
		ConfigHandler.parseBiomes();
	}

	@EventHandler
	public void serverLoad(FMLServerStartingEvent event)
	{
		event.registerServerCommand(new GeneralCommands());
		event.registerServerCommand(new TeamCommands());
	}

	private void doMetastuff()
	{
		ModMetadata meta = FMLCommonHandler.instance().findContainerFor(this).getMetadata();

		meta.parent = PokecubeMod.ID;
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
