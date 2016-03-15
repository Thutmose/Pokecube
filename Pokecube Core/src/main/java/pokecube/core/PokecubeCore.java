package pokecube.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList.EntityEggInfo;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.monster.EntityCaveSpider;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.monster.EntitySilverfish;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityMooshroom;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.passive.EntityRabbit;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.profiler.IPlayerUsage;
import net.minecraft.stats.Achievement;
import net.minecraft.stats.AchievementList;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraftforge.common.AchievementPage;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.LoadingCallback;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.VillagerRegistry;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.core.ai.thread.PokemobAIThread;
import pokecube.core.ai.utils.AISaveHandler;
import pokecube.core.blocks.pc.InventoryPC;
import pokecube.core.commands.Commands;
import pokecube.core.commands.GiftCommand;
import pokecube.core.commands.MakeCommand;
import pokecube.core.commands.SettingsCommand;
import pokecube.core.commands.TMCommand;
import pokecube.core.database.Database;
import pokecube.core.database.Pokedex;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.stats.SpecialCaseRegister;
import pokecube.core.entity.pokemobs.EntityPokemob;
import pokecube.core.entity.professor.EntityProfessor;
import pokecube.core.events.PostPostInit;
import pokecube.core.events.handlers.EventsHandler;
import pokecube.core.events.handlers.PCEventsHandler;
import pokecube.core.events.handlers.SpawnHandler;
import pokecube.core.handlers.Config;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.pokecubes.EntityPokecube;
import pokecube.core.items.pokemobeggs.EntityPokemobEgg;
import pokecube.core.moves.MovesAdder;
import pokecube.core.moves.animations.MoveAnimationHelper;
import pokecube.core.network.PCPacketHandler;
import pokecube.core.network.PokecubePacketHandler;
import pokecube.core.network.PokecubePacketHandler.PokecubeClientPacket;
import pokecube.core.network.PokecubePacketHandler.PokecubeClientPacket.PokecubeMessageHandlerClient;
import pokecube.core.network.PokecubePacketHandler.PokecubeServerPacket;
import pokecube.core.network.PokecubePacketHandler.PokecubeServerPacket.PokecubeMessageHandlerServer;
import pokecube.core.network.PokecubePacketHandler.StarterInfo;
import pokecube.core.network.pokemobs.PokemobPacketHandler.MessageClient;
import pokecube.core.network.pokemobs.PokemobPacketHandler.MessageClient.MessageHandlerClient;
import pokecube.core.network.pokemobs.PokemobPacketHandler.MessageServer;
import pokecube.core.network.pokemobs.PokemobPacketHandler.MessageServer.MessageHandlerServer;
import pokecube.core.utils.PCSaveHandler;
import pokecube.core.utils.PokecubeSerializer;
import pokecube.core.utils.Tools;
import pokecube.core.world.gen.WorldGenBerries;
import pokecube.core.world.gen.WorldGenFossils;
import pokecube.core.world.gen.WorldGenNests;
import pokecube.core.world.gen.WorldGenStartBuilding;
import pokecube.core.world.gen.village.buildings.ComponentPokeCentre;
import pokecube.core.world.gen.village.buildings.ComponentPokeMart;
import pokecube.core.world.gen.village.handlers.PokeCentreCreationHandler;
import pokecube.core.world.gen.village.handlers.PokeMartCreationHandler;
import pokecube.core.world.terrain.PokecubeTerrainChecker;
import thut.api.maths.Vector3;

@Mod( // @formatter:off
        modid = PokecubeMod.ID, 
        name = "Pokecube", 
        version = PokecubeMod.VERSION, 
        dependencies = "required-after:Forge@"+ PokecubeMod.MINFORGEVERSION + PokecubeMod.DEPSTRING, 
        acceptedMinecraftVersions = PokecubeMod.MCVERSIONS, 
        updateJSON = PokecubeMod.UPDATEURL,
        guiFactory = "pokecube.core.client.gui.config.ModGuiFactory"
    )// @formatter:on
public class PokecubeCore extends PokecubeMod
{
    @SidedProxy(clientSide = "pokecube.core.client.ClientProxyPokecube", serverSide = "pokecube.core.CommonProxyPokecube")
    public static CommonProxyPokecube       proxy;

    @Instance(ID)
    public static PokecubeCore              instance;

    static boolean                          server          = false;

    static boolean                          checked         = false;
    private static HashMap<Object, Integer> highestEntityId = new HashMap<Object, Integer>();

    private static int                      messageId       = 0;

    public static int getMessageID()
    {
        messageId++;
        return messageId;
    }

    /** On client side, returns the instance of Minecraft. On server side
     * returns the instance of MinecraftServer.
     * 
     * @return */
    public static IPlayerUsage getMinecraftInstance()
    {
        return getProxy().getMinecraftInstance();
    }

    /** On client side, if the param is null returns the Player. If the param is
     * not null, returns the requested player.
     * 
     * @param playerName
     * @return the {@link EntityPlayer} wanted */
    public static EntityPlayer getPlayer(String playerName)
    {
        return getProxy().getPlayer(playerName);
    }

    /** Should be useless on final install. But needed in Eclipse.
     * 
     * @return the Proxy depending on the SimpleComponent */
    public static CommonProxyPokecube getProxy()
    {
        return proxy;
    }

    public static int getUniqueEntityId(Object mod)
    {
        if (highestEntityId.get(mod) == null)
        {
            highestEntityId.put(mod, 0);
            return 0;
        }

        int id = highestEntityId.get(mod) + 1;
        highestEntityId.put(mod, id);
        return id;
    }

    /** Should not be used. Prefer FML methods.
     * 
     * @return an instance of the World */
    public static World getWorld()
    {
        return getProxy().getWorld();
    }

    @SuppressWarnings({ "rawtypes", "unused" })
    public static boolean isOnClientSide()
    {
        if (!checked)
        {
            checked = true;
            try
            {
                Class c = Class.forName("net.minecraft.server.dedicated.DedicatedServer");
                server = true;
            }
            catch (ClassNotFoundException e)
            {
            }
        }

        if (server) return false;
        return FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT;
    }

    public static void registerSpawns()
    {
        int n = 0;
        List<PokedexEntry> spawns = new ArrayList<PokedexEntry>();
        SpawnHandler.spawns.clear();
        for (PokedexEntry dbe : Database.data.values())
        {
            if (dbe.getSpawnData() != null)
            {
                dbe.getSpawnData().postInit();
            }
        }

        for (PokedexEntry dbe : Database.spawnables)
        {
            if (Pokedex.getInstance().getEntry(dbe.getPokedexNb()) != null && !spawns.contains(dbe))
            {
                spawns.add(dbe);
                SpawnHandler.addSpawn(dbe);
                n++;
            }
        }

        if (n != 1) System.out.println("Registered " + n + " Pokemob Spawns");
        else System.out.println("Registered " + n + " Pokemob Spawn");
    }

    public SpawnHandler        spawner;

    public String              newVersion;

    public String              newAlphaVersion;

    public Mod_Pokecube_Helper helper;

    private Config             config;

    public PokecubeCore()
    {
        new Tools();
        core = this;
    }

    /** Creates a new instance of an entity in the world for the pokemob
     * specified by its pokedex number.
     * 
     * @param pokedexNb
     *            the pokedex number
     * @param world
     *            the {@link World} where to spawn
     * @return the {@link Entity} instance or null if a problem occurred */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public Entity createEntityByPokedexNb(int pokedexNb, World world)
    {
        Entity entity = null;
        Class clazz = null;

        if (!registered.get(pokedexNb)) return null;

        try
        {
            clazz = getEntityClassFromPokedexNumber(pokedexNb);

            if (clazz != null)
            {
                entity = (Entity) clazz.getConstructor(new Class[] { World.class }).newInstance(new Object[] { world });
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        if (entity == null)
        {
            System.err.println("Problem with entity with pokedexNb: " + pokedexNb);
            System.err.println(clazz + " ");
        }

        return entity;
    }

    @Override
    public Config getConfig()
    {
        return config;
    }

    /** Returns the class of the {@link EntityLiving} for the given pokedexNb.
     * If no Pokemob has been registered for this pokedex number, it returns
     * <code>null</code>.
     * 
     * @param pokedexNb
     *            the pokedex number
     * @return the {@link Class} of the pokemob */
    @SuppressWarnings("rawtypes")
    @Override
    public Class getEntityClassFromPokedexNumber(int pokedexNb)
    {
        try
        {
            return pokedexmap.get(new Integer(pokedexNb));
        }
        catch (Exception e)
        {
            return null;
        }
    }

    @Override
    public Configuration getPokecubeConfig(FMLPreInitializationEvent evt)
    {
        File file = evt.getSuggestedConfigurationFile();
        String seperator = System.getProperty("file.separator");
        String folder = file.getAbsolutePath();
        String name = file.getName();
        folder = folder.replace(name, "pokecube" + seperator + name);

        file = new File(folder);
        return new Configuration(file);
    }

    @Override
    public Integer[] getStarters()
    {
        return starters.toArray(new Integer[0]);
    }

    /** Returns the translated Pokemob name of the pokemob with the specify
     * pokedex number.
     *
     * @param nb
     *            the pokedex number
     * @return the {@link String} name */
    @Override
    public String getTranslatedPokenameFromPokedexNumber(int nb)
    {
        PokedexEntry entry = Pokedex.getInstance().getEntry(nb);

        if (entry != null) { return Pokedex.getInstance().getEntry(nb).getTranslatedName(); }

        return "" + nb;
    }

    @EventHandler
    private void init(FMLInitializationEvent evt)
    {
        System.out.println("mod_pokecube.init() " + FMLCommonHandler.instance().getEffectiveSide());
        proxy.registerRenderInformation();
        EntityRegistry.registerModEntity(EntityPokemob.class, "pokecube:genericMob", getUniqueEntityId(this), this, 80,
                1, true);
        EntityRegistry.registerModEntity(EntityProfessor.class, "pokecube:Professor", getUniqueEntityId(this), this, 80,
                3, true);
        EntityRegistry.registerModEntity(EntityPokemobEgg.class, "pokecube:pokemobEgg", getUniqueEntityId(this), this,
                80, 3, false);
        EntityRegistry.registerModEntity(EntityPokecube.class, "pokecube:cube", getUniqueEntityId(this), this, 80, 3,
                true);

        if (!Loader.isModLoaded("reccomplex"))
        {
            VillagerRegistry.instance().registerVillageCreationHandler(new PokeCentreCreationHandler());
            VillagerRegistry.instance().registerVillageCreationHandler(new PokeMartCreationHandler());
            PokecubePacketHandler.giveHealer = false;
            try
            {
                MapGenStructureIO.registerStructureComponent(ComponentPokeCentre.class,
                        "poke_adventures:PokeCentreStructure");
                MapGenStructureIO.registerStructureComponent(ComponentPokeMart.class,
                        "poke_adventures:PokeMartStructure");
            }
            catch (Throwable e1)
            {
                System.out.println(
                        "Error registering Structures with Vanilla Minecraft: this is expected in versions earlier than 1.6.4");
            }
        }
        else
        {

        }
        GameRegistry.registerWorldGenerator(new WorldGenStartBuilding(), 10);
        // TODO figure out good spawn weights, Also config for these
        GameRegistry.registerWorldGenerator(new WorldGenBerries(), 10);
        GameRegistry.registerWorldGenerator(new WorldGenFossils(), 10);
        GameRegistry.registerWorldGenerator(new WorldGenNests(), 10);
        helper.initAllBlocks();
        proxy.registerKeyBindings();
        NetworkRegistry.INSTANCE.registerGuiHandler(this, proxy);
        helper.postInit();
    }

    @EventHandler
    private void postInit(FMLPostInitializationEvent evt)
    {
        removeAllMobs();
        PokecubeItems.init();
        Database.postInit();
        StarterInfo.processStarterInfo(config.defaultStarts);
        postInitPokemobs();
        helper.addVillagerTrades();
        SpecialCaseRegister.register();
        MoveAnimationHelper.Instance();
        MinecraftForge.EVENT_BUS.post(new PostPostInit());
        MovesAdder.postInitMoves();
    }

    private void postInitPokemobs()
    {
        registerSpawns();
        SpawnHandler.sortSpawnables();
        int n = 0;
        for (Integer i : Pokedex.getInstance().getEntries())
        {
            PokedexEntry p = Pokedex.getInstance().getEntry(i);
            if (p.getPokedexNb() < 722)
            {
                p.setSound(ID + ":mobs." + p.getName());
                n++;
            }
            else
            {
                p.setSound(p.getModId() + ":mobs." + p.getName());
            }
            p.updateMoves();
            // Refreshes the forme's modIds
            p.setModId(p.getModId());
        }
        System.out.println("Loaded " + n + " Pokemob sounds, " + Pokedex.getInstance().getEntries().size()
                + " Pokemon and " + Database.allFormes.size() + " Formes");
    }

    @EventHandler
    private void preInit(FMLPreInitializationEvent evt)
    {
        PokecubeTerrainChecker.init();
        config = new Config(getPokecubeConfig(evt).getConfigFile());

        helper = new Mod_Pokecube_Helper();
        // used to register the moves from the spreadsheets
        Database.init(evt);

        System.out.println("Registering Moves");
        MovesAdder.registerMoves();

        spawner = new SpawnHandler();
        if (!config.defaultMobs.equals(""))
        {
            System.out.println("Changing Default Mobs to " + config.defaultMobs);
            defaultMod = config.defaultMobs;
        }

        config.save();
        config.initDefaultStarts();
        EventsHandler evts = new EventsHandler();
        MinecraftForge.EVENT_BUS.register(evts);
        ForgeChunkManager.setForcedChunkLoadingCallback(this, new LoadingCallback()
        {
            @Override
            public void ticketsLoaded(List<Ticket> tickets, World world)
            {
                PokecubeSerializer.getInstance().loadData();
                PokecubeSerializer.getInstance().reloadChunk(tickets, world);
            }

        });

        packetPipeline = NetworkRegistry.INSTANCE.newSimpleChannel(ID);

        // General Pokecube Packets
        packetPipeline.registerMessage(PokecubeMessageHandlerClient.class, PokecubeClientPacket.class, getMessageID(),
                Side.CLIENT);
        packetPipeline.registerMessage(PokecubeMessageHandlerServer.class, PokecubeServerPacket.class, getMessageID(),
                Side.SERVER);

        // Packets for Pokemobs
        PokecubeMod.packetPipeline.registerMessage(MessageHandlerClient.class, MessageClient.class,
                PokecubeCore.getMessageID(), Side.CLIENT);
        PokecubeMod.packetPipeline.registerMessage(MessageHandlerServer.class, MessageServer.class,
                PokecubeCore.getMessageID(), Side.SERVER);

        // Packets for PCs and Trading
        PokecubeMod.packetPipeline.registerMessage(PCPacketHandler.MessageClient.MessageHandlerClient.class,
                PCPacketHandler.MessageClient.class, PokecubeCore.getMessageID(), Side.CLIENT);
        PokecubeMod.packetPipeline.registerMessage(PCPacketHandler.MessageServer.MessageHandlerServer.class,
                PCPacketHandler.MessageServer.class, PokecubeCore.getMessageID(), Side.SERVER);

        helper.addItems();
        Reader fileIn = null;
        BufferedReader br;

        String giftLoc = GIFTURL;
        giftLocations.add(giftLoc);

        for (String location : giftLocations)
        {
            try
            {
                URL url = new URL(location);
                URLConnection con = url.openConnection();
                con.setConnectTimeout(1000);
                con.setReadTimeout(1000);
                InputStream in = con.getInputStream();
                fileIn = new InputStreamReader(in);
            }
            catch (Exception e1)
            {
                fileIn = null;
                e1.printStackTrace();
            }
            if (fileIn != null)
            {
                br = new BufferedReader(fileIn);
                try
                {
                    String line;
                    while ((line = br.readLine()) != null)
                    {
                        if (line.isEmpty()) break;

                        String code = line.split("`")[0];
                        String gift = line.split("`")[1];
                        gifts.put(code, gift);
                    }
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }

        getProxy().preInit(evt);

        PCSaveHandler save = new PCSaveHandler();
        MinecraftForge.EVENT_BUS.register(save);
        PCEventsHandler events = new PCEventsHandler();
        MinecraftForge.EVENT_BUS.register(events);
    }

    /** Registers a Pokemob into the Pokedex. Have a look to the file called
     * <code>"HelpEntityJava.png"</code> provided with the SDK.
     *
     * @param createEgg
     *            whether an egg should be created for this species (is a base
     *            non legendary pokemob)
     * @param mod
     *            the instance of your mod
     * @param pokedexnb
     *            the pokedex number */
    @SuppressWarnings("rawtypes")
    @Override
    public void registerPokemon(boolean createEgg, Object mod, int pokedexNb)
    {
        Class c = genericMobClasses.get(pokedexNb);
        if (c == null)
        {
            if (loader == null)
            {
                loader = new ByteClassLoader(Launch.classLoader);
            }
            try
            {
                c = loader.generatePokemobClass(pokedexNb);
                registerPokemonByClass(c, createEgg, mod, pokedexNb);
            }
            catch (ClassNotFoundException e)
            {
                System.err.println("Error Making Class for  " + Database.getEntry(pokedexNb));
                e.printStackTrace();
            }
        }
        else
        {
            registerPokemonByClass(c, createEgg, mod, pokedexNb);
        }

        return;
    }

    @Override
    public void registerPokemon(boolean createEgg, Object mod, String name)
    {
        registerPokemon(createEgg, mod, Database.getEntry(name).getPokedexNb());
    }

    /** Registers a Pokemob into the Pokedex. Have a look to the file called
     * <code>"HelpEntityJava.png"</code> provided with the SDK.
     *
     * @param clazz
     *            the {@link Entity} class, must extends {@link EntityPokemob}
     * @param createEgg
     *            whether an egg should be created for this species (is a base
     *            non legendary pokemob)
     * @param mod
     *            the instance of your mod
     * @param pokedexEntry
     *            the {@link PokedexEntry} */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void registerPokemonByClass(Class clazz, boolean createEgg, Object mod, int pokedexNb)
    {
        if (pokedexmap == null || pokemobAchievements == null)
        {
            pokedexmap = new HashMap();
            pokemobAchievements = new HashMap<Integer, Achievement>();
        }

        if (get1stPokemob == null)
        {
            System.out.println("REGISTERING ACHIEVEMENT");
            get1stPokemob = (new AchievementCatch(0, "get1stPokemob", -3, -3, PokecubeItems.getItem("pokedex"), null));
            get1stPokemob.registerStat();
            AchievementList.achievementList.add(get1stPokemob);
            pokemobAchievements.put(new Integer(0), get1stPokemob);
            achievementPagePokecube = new AchievementPage("Pokecube", get1stPokemob);
            AchievementPage.registerAchievementPage(achievementPagePokecube);
        }

        PokedexEntry pokedexEntry = Database.getEntry(pokedexNb);
        Mod annotation = mod.getClass().getAnnotation(Mod.class);
        String modId = ID;
        if (annotation != null) modId = annotation.modid();
        if (pokedexEntry.getModId() == null)
        {
            pokedexEntry.setModId(modId);
        }

        String name = pokedexEntry.getName();
        Achievement achievement = pokemobAchievements.get(pokedexNb);
        if (clazz != null)
        {
            PokedexEntry previousEntry = Pokedex.getInstance().getEntry(pokedexEntry.getPokedexNb());
            try
            {
                // in case of double definition, the Manchou's implementation
                // will have the priority by default, or whatever is set in
                // config.
                if (!registered.get(pokedexNb))
                {
                    EntityRegistry.registerModEntity(clazz, name, 25 + pokedexNb, mod, 80, 3, true);

                    if (!pokemobEggs.containsKey(pokedexNb))
                    {
                        pokemobEggs.put(new Integer(pokedexNb),
                                new EntityEggInfo(pokedexNb + 7000, 0xE8E0A0, 0x78C848));
                    }
                    pokedexmap.put(new Integer(pokedexNb), clazz);
                    registered.set(pokedexNb);

                    if (previousEntry != null)
                    {
                        Database.getEntry(pokedexNb).setModId(modId);
                    }
                    Pokedex.getInstance().registerPokemon(pokedexEntry);

                    if (achievement == null)
                    {
                        int x = -2 + (pokedexNb / 16) * 2;
                        int y = -2 + (pokedexNb % 16) - 1;
                        try
                        {
                            if (PokecubeItems.getEmptyCube(0) == null) System.err.println("cube is null");
                            achievement = (new AchievementCatch(pokedexNb, name, x, y, PokecubeItems.getEmptyCube(0),
                                    get1stPokemob));
                            achievement.registerStat();
                            achievementPagePokecube.getAchievements().add(achievement);
                            pokemobAchievements.put(pokedexNb, achievement);
                        }
                        catch (Throwable e)
                        {
                            System.err.println("An achievement could not be added.");
                            e.printStackTrace();
                        }
                    }
                    else
                    {
                        System.err.println("Double Registration " + pokedexEntry + " Default set to version from "
                                + pokedexEntry.getModId());
                    }
                }
            }
            catch (Throwable e)
            {
                e.printStackTrace();
            }
        }
    }

    private void removeAllMobs()
    {
        BiomeGenBase[] biomes;
        ArrayList<BiomeGenBase> biomelist = new ArrayList<BiomeGenBase>();
        for (BiomeGenBase b : BiomeGenBase.getBiomeGenArray())
            if (b != null) biomelist.add(b);
        biomes = biomelist.toArray(new BiomeGenBase[0]);

        if (config.deactivateAnimals)
        {
            EntityRegistry.removeSpawn(EntityRabbit.class, EnumCreatureType.CREATURE, biomes);
            EntityRegistry.removeSpawn(EntityChicken.class, EnumCreatureType.CREATURE, biomes);
            EntityRegistry.removeSpawn(EntityCow.class, EnumCreatureType.CREATURE, biomes);
            EntityRegistry.removeSpawn(EntityPig.class, EnumCreatureType.CREATURE, biomes);
            EntityRegistry.removeSpawn(EntitySheep.class, EnumCreatureType.CREATURE, biomes);
            EntityRegistry.removeSpawn(EntityOcelot.class, EnumCreatureType.MONSTER, biomes);
            EntityRegistry.removeSpawn(EntityWolf.class, EnumCreatureType.CREATURE, biomes);
            EntityRegistry.removeSpawn(EntityRabbit.class, EnumCreatureType.CREATURE, biomes);
            EntityRegistry.removeSpawn(EntitySquid.class, EnumCreatureType.WATER_CREATURE, biomes);
            EntityRegistry.removeSpawn(EntityBat.class, EnumCreatureType.AMBIENT, biomes);
            EntityRegistry.removeSpawn(EntityHorse.class, EnumCreatureType.CREATURE, biomes);
            EntityRegistry.removeSpawn(EntityMooshroom.class, EnumCreatureType.CREATURE, biomes);
        }
        if (config.deactivateMonsters)
        {
            EntityRegistry.removeSpawn(EntityBlaze.class, EnumCreatureType.MONSTER, biomes);
            EntityRegistry.removeSpawn(EntityCreeper.class, EnumCreatureType.MONSTER, biomes);
            EntityRegistry.removeSpawn(EntityEnderman.class, EnumCreatureType.MONSTER, biomes);
            EntityRegistry.removeSpawn(EntitySilverfish.class, EnumCreatureType.MONSTER, biomes);
            EntityRegistry.removeSpawn(EntitySkeleton.class, EnumCreatureType.MONSTER, biomes);
            EntityRegistry.removeSpawn(EntityWitch.class, EnumCreatureType.MONSTER, biomes);
            EntityRegistry.removeSpawn(EntitySpider.class, EnumCreatureType.MONSTER, biomes);
            EntityRegistry.removeSpawn(EntityCaveSpider.class, EnumCreatureType.MONSTER, biomes);
            EntityRegistry.removeSpawn(EntityZombie.class, EnumCreatureType.MONSTER, biomes);
            EntityRegistry.removeSpawn(EntityPigZombie.class, EnumCreatureType.MONSTER, biomes);
            EntityRegistry.removeSpawn(EntityDragon.class, EnumCreatureType.MONSTER, biomes);
            EntityRegistry.removeSpawn(EntitySlime.class, EnumCreatureType.MONSTER, biomes);
            EntityRegistry.removeSpawn(EntityMagmaCube.class, EnumCreatureType.MONSTER, biomes);
            EntityRegistry.removeSpawn(EntityGhast.class, EnumCreatureType.MONSTER, biomes);
        }
    }

    @EventHandler
    public void serverLoad(FMLServerStartingEvent event)
    {
        event.registerServerCommand(new Commands());
        event.registerServerCommand(new SettingsCommand());
        event.registerServerCommand(new MakeCommand());
        event.registerServerCommand(new GiftCommand());
        event.registerServerCommand(new TMCommand());
    }

    @EventHandler
    public void serverStop(FMLServerStoppingEvent event)
    {
        PokemobAIThread.clear();
    }

    @Override
    public void spawnParticle(String par1Str, Vector3 location, Vector3 velocity)
    {
        getProxy().spawnParticle(par1Str, location, velocity);
    }

    /** Loads PC data when server starts
     * 
     * @param evt */
    @EventHandler
    public void WorldLoadEvent(FMLServerStartedEvent evt)
    {
        PCSaveHandler.getInstance().loadPC();
        AISaveHandler.instance();
    }

    /** clears PC when server stops
     * 
     * @param evt */
    @EventHandler
    public void WorldUnloadEvent(FMLServerStoppedEvent evt)
    {
        InventoryPC.clearPC();
        WorldGenStartBuilding.building = false;
        if (PokecubeSerializer.instance != null) PokecubeSerializer.instance.clearInstance();
        AISaveHandler.clearInstance();
    }

}
