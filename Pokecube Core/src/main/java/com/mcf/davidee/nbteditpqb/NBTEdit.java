package com.mcf.davidee.nbteditpqb;

import java.io.File;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.layout.PatternLayout;

import com.mcf.davidee.nbteditpqb.forge.CommonProxy;
import com.mcf.davidee.nbteditpqb.nbt.NBTNodeSorter;
import com.mcf.davidee.nbteditpqb.nbt.NBTTree;
import com.mcf.davidee.nbteditpqb.nbt.NamedNBT;
import com.mcf.davidee.nbteditpqb.nbt.SaveStates;
import com.mcf.davidee.nbteditpqb.packets.PacketHandler;

import net.minecraft.command.ServerCommandManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

@Mod(modid = NBTEdit.MODID, name = NBTEdit.NAME, acceptedMinecraftVersions = "*", version = NBTEdit.VERSION, acceptableRemoteVersions = "*")
public class NBTEdit {
	public static final String MODID = "pceditmod";
	public static final String NAME = "In-game NBTEdit - Pokecube Edition";
	public static final String VERSION = "1.10.2-2.0.0";

	public static final NBTNodeSorter SORTER = new NBTNodeSorter();
	public static final PacketHandler NETWORK = new PacketHandler();

	public static Logger logger;

	public static NamedNBT clipboard = null;
	public static boolean opOnly = true;

	@Instance(MODID)
	private static NBTEdit instance;

	@SidedProxy(clientSide = "com.mcf.davidee.nbteditpqb.forge.ClientProxy", serverSide = "com.mcf.davidee.nbteditpqb.forge.CommonProxy")
	public static CommonProxy proxy;

	private SaveStates saves;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		Configuration config = new Configuration(event.getSuggestedConfigurationFile());
		config.load();
		opOnly = config.get("General", "opOnly", true, "true if only Ops can NBTEdit; false allows users in creative mode to NBTEdit").getBoolean(true);
		if (config.hasChanged()) {
			config.save();
		}

		logger = event.getModLog();
		org.apache.logging.log4j.core.Logger log = (org.apache.logging.log4j.core.Logger) logger;
		log.setAdditive(false); //Sets our logger to not show up in console.
		log.setLevel(Level.ALL);

		// Set up our file logging.
		PatternLayout layout = PatternLayout.createLayout("[%d{MM-dd HH:mm:ss}] [%level]: %msg%n", null, null, null, null);
		FileAppender appender = FileAppender.createAppender("logs/NBTEditpqb.log", "false", "false", "NBTEdit File Appender", "true", "false", "true", layout, null, "false", null, null);
		appender.start();
		log.addAppender(appender);

		ModMetadata m = event.getModMetadata();
		m.autogenerated = false;
		m.modId = MODID;
		m.version = VERSION;
		m.name = NAME;
		m.authorList.add("Davidee");

		m.credits = "Thanks to Mojang, Forge, and all your support.";
		m.description = "Allows you to edit NBT Tags in-game.\nPlease visit the URL above for help.";
		m.url = "http://www.minecraftforum.net/topic/1558668-151/";
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		logger.trace("NBTEdit Initalized");
		saves = new SaveStates(new File(new File(proxy.getMinecraftDirectory(),"saves"), "NBTEditpqb.dat"));
		//DISPATCHER.initialize();
		NETWORK.initialize();
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		proxy.registerInformation();
	}

	@EventHandler
	public void serverStarting(FMLServerStartingEvent event) {
		MinecraftServer server= event.getServer();
		ServerCommandManager serverCommandManager = (ServerCommandManager) server.getCommandManager();
		serverCommandManager.registerCommand(new CommandNBTEdit());
		logger.trace("Server Starting -- Added \"/pcedit\" command");
	}

	public static void log(Level l, String s) {
		logger.log(l, s);
	}

	public static void throwing(String cls, String mthd, Throwable thr) {
		logger.warn("class: " + cls + " method: " + mthd, thr);
	}

	static final String SEP = System.getProperty("line.separator");
	public static void logTag(NBTTagCompound tag) {
		NBTTree tree = new NBTTree(tag);
		String sb = "";
		for (String s : tree.toStrings()){
			sb += SEP + "\t\t\t"+ s;
		}
		NBTEdit.log(Level.TRACE, sb);
	}

	public static SaveStates getSaveStates(){
		return instance.saves;
	}
}
