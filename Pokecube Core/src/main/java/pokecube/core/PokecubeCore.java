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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Level;

import com.google.common.collect.Maps;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList.EntityEggInfo;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.profiler.ISnooperInfo;
import net.minecraft.stats.Achievement;
import net.minecraft.stats.AchievementList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraftforge.common.AchievementPage;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.LoadingCallback;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.Optional.Method;
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
import pokecube.core.achievements.AchievementCatch;
import pokecube.core.ai.utils.AISaveHandler;
import pokecube.core.blocks.berries.BerryGenManager;
import pokecube.core.blocks.pc.InventoryPC;
import pokecube.core.commands.Commands;
import pokecube.core.commands.GiftCommand;
import pokecube.core.commands.MakeCommand;
import pokecube.core.commands.RecallCommand;
import pokecube.core.commands.SecretBaseCommand;
import pokecube.core.commands.SettingsCommand;
import pokecube.core.commands.TMCommand;
import pokecube.core.database.Database;
import pokecube.core.database.Pokedex;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.stats.SpecialCaseRegister;
import pokecube.core.entity.pokemobs.EntityPokemob;
import pokecube.core.entity.pokemobs.EntityPokemobPart;
import pokecube.core.entity.professor.EntityProfessor;
import pokecube.core.events.PostPostInit;
import pokecube.core.events.handlers.EventsHandler;
import pokecube.core.events.handlers.PCEventsHandler;
import pokecube.core.events.handlers.SpawnHandler;
import pokecube.core.handlers.Config;
import pokecube.core.handlers.PokecubePlayerDataHandler.PokecubePlayerCustomData;
import pokecube.core.handlers.PokecubePlayerDataHandler.PokecubePlayerData;
import pokecube.core.handlers.PokecubePlayerDataHandler.PokecubePlayerStats;
import pokecube.core.handlers.PokedexInspector;
import pokecube.core.interfaces.IEntityProvider;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.pokecubes.EntityPokecube;
import pokecube.core.items.pokecubes.EntityPokecubeBase;
import pokecube.core.items.pokemobeggs.EntityPokemobEgg;
import pokecube.core.moves.MoveQueue.MoveQueuer;
import pokecube.core.moves.animations.EntityMoveUse;
import pokecube.core.moves.animations.MoveAnimationHelper;
import pokecube.core.moves.implementations.MovesAdder;
import pokecube.core.network.EntityProvider;
import pokecube.core.network.NetworkWrapper;
import pokecube.core.network.PokecubePacketHandler;
import pokecube.core.network.PokecubePacketHandler.StarterInfo;
import pokecube.core.utils.LogFormatter;
import pokecube.core.utils.PCSaveHandler;
import pokecube.core.utils.PokecubeSerializer;
import pokecube.core.utils.Tools;
import pokecube.core.world.dimensions.PokecubeDimensionManager;
import pokecube.core.world.gen.WorldGenFossils;
import pokecube.core.world.gen.WorldGenNests;
import pokecube.core.world.gen.WorldGenStartBuilding;
import pokecube.core.world.gen.village.buildings.ComponentPokeCentre;
import pokecube.core.world.gen.village.buildings.ComponentPokeMart;
import pokecube.core.world.gen.village.handlers.PokeCentreCreationHandler;
import pokecube.core.world.gen.village.handlers.PokeMartCreationHandler;
import pokecube.core.world.terrain.PokecubeTerrainChecker;
import thut.api.maths.Vector3;
import thut.core.common.handlers.PlayerDataHandler;

@Mod( // @formatter:off
        modid = PokecubeMod.ID, name = "Pokecube", version = PokecubeMod.VERSION, dependencies = "required-after:Forge@"
                + PokecubeMod.MINFORGEVERSION
                + PokecubeMod.DEPSTRING, acceptedMinecraftVersions = PokecubeMod.MCVERSIONS, acceptableRemoteVersions = PokecubeMod.MINVERSION, updateJSON = PokecubeMod.UPDATEURL, guiFactory = "pokecube.core.client.gui.config.ModGuiFactory") // @formatter:on
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

    public static MoveQueuer                moveQueues;

    public static int getMessageID()
    {
        messageId++;
        return messageId;
    }

    /** On client side, returns the instance of Minecraft. On server side
     * returns the instance of MinecraftServer.
     * 
     * @return */
    public static ISnooperInfo getMinecraftInstance()
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

    public static boolean isOnClientSide()
    {
        return FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT;
    }

    public static void registerSpawns()
    {
        int n = 0;
        List<PokedexEntry> spawns = new ArrayList<PokedexEntry>();
        Database.spawnables.clear();
        for (PokedexEntry dbe : Database.allFormes)
        {
            if (dbe.getSpawnData() != null)
            {
                dbe.getSpawnData().postInit();
                Database.spawnables.add(dbe);
            }
        }
        for (PokedexEntry dbe : Database.spawnables)
        {
            if (Pokedex.getInstance().getEntry(dbe.getPokedexNb()) != null && !spawns.contains(dbe))
            {
                spawns.add(dbe);
                n++;
            }
        }
        if (n != 1) PokecubeMod.log("Registered " + n + " Pokemob Spawns");
        else PokecubeMod.log("Registered " + n + " Pokemob Spawn");
    }

    public SpawnHandler        spawner;
    public String              newVersion;
    public String              newAlphaVersion;
    public Mod_Pokecube_Helper helper;
    private Config             config;
    IEntityProvider            provider;
    EventsHandler              events;

    public PokecubeCore()
    {
        new Tools();
        core = this;
    }

    @Override
    public Entity createPokemob(PokedexEntry entry, World world)
    {
        Entity e = createPokemob(entry.getPokedexNb(), world);
        if (e != null)
        {
            e = (Entity) ((IPokemob) e).setPokedexEntry(entry);
        }
        return e;
    }

    /** Creates a new instance of an entity in the world for the pokemob
     * specified by its pokedex number.
     * 
     * @param pokedexNb
     *            the pokedex number
     * @param world
     *            the {@link World} where to spawn
     * @return the {@link Entity} instance or null if a problem occurred */
    public Entity createPokemob(int pokedexNb, World world)
    {
        Entity entity = null;
        Class<?> clazz = null;
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
            return pokedexmap.get(Database.getEntry(new Integer(pokedexNb)));
        }
        catch (Exception e)
        {
            return null;
        }
    }

    @Override
    public IEntityProvider getEntityProvider()
    {
        if (provider == null) provider = new EntityProvider(null);
        return provider;
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
        for (PokedexEntry entry : Database.baseFormes.values())
        {
            if (entry.isStarter && !PokecubeMod.core.starters.contains(entry.getPokedexNb()))
            {
                PokecubeMod.core.starters.add(entry.getPokedexNb());
                Collections.sort(PokecubeMod.core.starters);
            }
            else if (!entry.isStarter)
            {
                for (int i = 0; i < PokecubeMod.core.starters.size(); i++)
                    if (PokecubeMod.core.starters.get(i) == entry.getPokedexNb()) PokecubeMod.core.starters.remove(i);
            }
        }
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

        if (entry != null) { return Pokedex.getInstance().getEntry(nb).getUnlocalizedName(); }

        return "" + nb;
    }

    @EventHandler
    private void init(FMLInitializationEvent evt)
    {
        System.out.println("mod_pokecube.init() " + FMLCommonHandler.instance().getEffectiveSide());
        new PokedexInspector();
        proxy.initClient();
        proxy.registerRenderInformation();
        moveQueues = new MoveQueuer();

        PlayerDataHandler.dataMap.add(PokecubePlayerData.class);
        PlayerDataHandler.dataMap.add(PokecubePlayerStats.class);
        PlayerDataHandler.dataMap.add(PokecubePlayerCustomData.class);

        EntityRegistry.registerModEntity(EntityPokemob.class, "genericMob", getUniqueEntityId(this), this, 80, 1, true);
        EntityRegistry.registerModEntity(EntityPokemobPart.class, "genericMobPart", getUniqueEntityId(this), this, 80,
                1, true);
        EntityRegistry.registerModEntity(EntityProfessor.class, "Professor", getUniqueEntityId(this), this, 80, 3,
                true);
        EntityRegistry.registerModEntity(EntityPokemobEgg.class, "pokemobEgg", getUniqueEntityId(this), this, 80, 3,
                false);
        EntityRegistry.registerModEntity(EntityPokecube.class, "cube", getUniqueEntityId(this), this, 80, 3, true);
        EntityRegistry.registerModEntity(EntityMoveUse.class, "moveuse", getUniqueEntityId(this), this, 80, 3, true);

        if (config.villagePokecenters)
            VillagerRegistry.instance().registerVillageCreationHandler(new PokeCentreCreationHandler());
        if (config.villagePokemarts)
            VillagerRegistry.instance().registerVillageCreationHandler(new PokeMartCreationHandler());
        try
        {
            if (config.villagePokecenters) MapGenStructureIO.registerStructureComponent(ComponentPokeCentre.class,
                    "poke_adventures:PokeCentreStructure");
            if (config.villagePokemarts) MapGenStructureIO.registerStructureComponent(ComponentPokeMart.class,
                    "poke_adventures:PokeMartStructure");
        }
        catch (Throwable e1)
        {
            System.out.println("Error registering Structures with Vanilla Minecraft");
        }
        if (config.doSpawnBuilding)
        {
            GameRegistry.registerWorldGenerator(new WorldGenStartBuilding(), 10);
        }
        // TODO figure out good spawn weights, Also config for these
        if (config.generateFossils) GameRegistry.registerWorldGenerator(new WorldGenFossils(), 10);
        if (config.nests) GameRegistry.registerWorldGenerator(new WorldGenNests(), 10);
        helper.initAllBlocks();
        proxy.registerKeyBindings();
        NetworkRegistry.INSTANCE.registerGuiHandler(this, proxy);
        helper.postInit();
        removeAllMobs();

        logger.setLevel(Level.ALL);

        try
        {
            File logfile = new File(".", "Pokecube.log");
            if ((logfile.exists() || logfile.createNewFile()) && logfile.canWrite() && logHandler == null)
            {
                logHandler = new FileHandler(logfile.getPath());
                logHandler.setFormatter(new LogFormatter());
                logger.addHandler(logHandler);
            }
        }
        catch (SecurityException | IOException e)
        {
            e.printStackTrace();
        }
    }

    @EventHandler
    private void postInit(FMLPostInitializationEvent evt)
    {
        PokecubeItems.init();
        Database.postInit();
        StarterInfo.processStarterInfo(config.defaultStarts);
        postInitPokemobs();
        helper.addVillagerTrades();
        SpecialCaseRegister.register();
        MinecraftForge.EVENT_BUS.post(new PostPostInit());
        MovesAdder.postInitMoves();
    }

    private void postInitPokemobs()
    {
        for (PokedexEntry p : Pokedex.getInstance().getRegisteredEntries())
        {
            p.setSound("mobs." + p.getName());
            p.getSoundEvent();
            p.updateMoves();
        }
        ResourceLocation sound = new ResourceLocation(PokecubeMod.ID + ":pokecube_caught");
        GameRegistry.register(EntityPokecubeBase.POKECUBESOUND = new SoundEvent(sound).setRegistryName(sound));
        sound = new ResourceLocation(PokecubeMod.ID + ":pokecenter");
        GameRegistry.register(new SoundEvent(sound).setRegistryName(sound));
        sound = new ResourceLocation(PokecubeMod.ID + ":pokecenterloop");
        GameRegistry.register(new SoundEvent(sound).setRegistryName(sound));
        System.out.println("Loaded " + Pokedex.getInstance().getEntries().size() + " Pokemon and "
                + Database.allFormes.size() + " Formes");
    }

    @EventHandler
    private void preInit(FMLPreInitializationEvent evt)
    {
        PokecubeTerrainChecker.init();
        MoveAnimationHelper.Instance();
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
        events = new EventsHandler();
        ForgeChunkManager.setForcedChunkLoadingCallback(this, new LoadingCallback()
        {
            @Override
            public void ticketsLoaded(List<Ticket> tickets, World world)
            {
                PokecubeSerializer.getInstance().reloadChunk(tickets, world);
            }

        });

        packetPipeline = new NetworkWrapper(ID);

        // Init Packets
        PokecubePacketHandler.init();
        helper.addItems();
        if (get1stPokemob == null)
        {
            System.out.println("REGISTERING ACHIEVEMENT");
            get1stPokemob = (new AchievementCatch(null, -3, -3, PokecubeItems.getItem("pokedex"), null));
            get1stPokemob.registerStat();
            AchievementList.ACHIEVEMENTS.add(get1stPokemob);
            achievementPageCatch = new AchievementPage("Pokecube Captures");
            AchievementPage.registerAchievementPage(achievementPageCatch);
            achievementPageHatch = new AchievementPage("Pokecube Hatchs");
            AchievementPage.registerAchievementPage(achievementPageHatch);
            achievementPageKill = new AchievementPage("Pokecube Kills");
            AchievementPage.registerAchievementPage(achievementPageKill);
        }
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
                if (fileIn != null) try
                {
                    fileIn.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
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
                    br.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }

        getProxy().preInit(evt);
        PokecubeDimensionManager.getInstance();

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
    @Override
    public void registerPokemon(boolean createEgg, Object mod, int pokedexNb)
    {
        registerPokemon(createEgg, mod, Database.getEntry(pokedexNb));
    }

    @Override
    public void registerPokemon(boolean createEgg, Object mod, PokedexEntry entry)
    {
        Class<?> c = genericMobClasses.get(entry);
        if (c == null)
        {
            if (loader == null)
            {
                loader = new ByteClassLoader(Launch.classLoader);
            }
            try
            {
                c = loader.generatePokemobClass(entry);
                registerPokemonByClass(c, createEgg, mod, entry);
            }
            catch (ClassNotFoundException e)
            {
                System.err.println("Error Making Class for  " + entry);
                e.printStackTrace();
            }
        }
        else
        {
            registerPokemonByClass(c, createEgg, mod, entry);
        }
    }

    @Override
    public void registerPokemon(boolean createEgg, Object mod, String name)
    {
        registerPokemon(createEgg, mod, Database.getEntry(name));
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
    public void registerPokemonByClass(Class clazz, boolean createEgg, Object mod, PokedexEntry entry)
    {
        if (pokedexmap == null)
        {
            pokedexmap = new HashMap();
        }
        String name = entry.getName();
        if (clazz != null)
        {
            try
            {
                // in case of double definition, the Manchou's implementation
                // will have the priority by default, or whatever is set in
                // config.
                if (!registered.get(entry.getPokedexNb()))
                {
                    EntityRegistry.registerModEntity(clazz, name, 25 + entry.getPokedexNb(), mod, 80, 3, true);

                    if (!pokemobEggs.containsKey(entry.getPokedexNb()))
                    {
                        pokemobEggs.put(new Integer(entry.getPokedexNb()),
                                new EntityEggInfo(entry.getName(), 0xE8E0A0, 0x78C848));
                    }
                    pokedexmap.put(entry, clazz);
                    for (PokedexEntry e : entry.forms.values())
                        pokedexmap.put(e, clazz);
                    registered.set(entry.getPokedexNb());
                    Pokedex.getInstance().registerPokemon(entry);
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
        Biome[] biomes;
        ArrayList<Biome> biomelist = new ArrayList<Biome>();
        for (ResourceLocation key : Biome.REGISTRY.getKeys())
        {
            Biome b = Biome.REGISTRY.getObject(key);
            if (b != null) biomelist.add(b);
        }
        biomes = biomelist.toArray(new Biome[0]);
        if (config.deactivateAnimals)
        {
            for (Biome biome : biomes)
            {
                List<?> spawns = biome.getSpawnableList(EnumCreatureType.CREATURE);
                spawns.clear();
                spawns = biome.getSpawnableList(EnumCreatureType.AMBIENT);
                spawns.clear();
                spawns = biome.getSpawnableList(EnumCreatureType.WATER_CREATURE);
                spawns.clear();
            }
        }
        if (config.deactivateMonsters)
        {
            for (Biome biome : biomes)
            {
                List<?> spawns = biome.getSpawnableList(EnumCreatureType.MONSTER);
                spawns.clear();
            }
        }
    }

    @Method(modid = "thut_wearables")
    @EventHandler
    public void preInitWearables(FMLPreInitializationEvent event)
    {
        MinecraftForge.EVENT_BUS.register(new pokecube.core.items.megastuff.WearablesCompat());
    }

    @EventHandler
    public void serverLoad(FMLServerStartingEvent event)
    {
        event.registerServerCommand(new Commands());
        event.registerServerCommand(new SettingsCommand());
        event.registerServerCommand(new MakeCommand());
        event.registerServerCommand(new GiftCommand());
        event.registerServerCommand(new TMCommand());
        event.registerServerCommand(new RecallCommand());
        event.registerServerCommand(new SecretBaseCommand());
        registerSpawns();
        try
        {
            PokecubeDimensionManager.getInstance().onServerStart(event);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void serverStop(FMLServerStoppingEvent event)
    {
        events.meteorprocessor.clear();
        BerryGenManager.berryLocations.clear();
        PokecubeDimensionManager.getInstance().onServerStop(event);
    }

    @Override
    public void setEntityProvider(IEntityProvider provider)
    {
        this.provider = provider;
    }

    @Override
    public void spawnParticle(World world, String par1Str, Vector3 location, Vector3 velocity, int... args)
    {
        getProxy().spawnParticle(world, par1Str, location, velocity, args);
    }

    /** Loads PC data when server starts
     * 
     * @param evt */
    @EventHandler
    public void WorldLoadEvent(FMLServerStartedEvent evt)
    {
        AISaveHandler.instance();
        for (Achievement a : AchievementList.ACHIEVEMENTS)
        {
            if (a == null) continue;
            try
            {
                String name = a.statId;
                if (name != null) achievements.put(name, a);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    /** clears PC when server stops
     * 
     * @param evt */
    @EventHandler
    public void WorldUnloadEvent(FMLServerStoppedEvent evt)
    {
        InventoryPC.clearPC();
        achievements.clear();
        WorldGenStartBuilding.building = false;
        if (PokecubeSerializer.instance != null) PokecubeSerializer.instance.clearInstance();
        AISaveHandler.clearInstance();
    }

    Map<String, Achievement> achievements = Maps.newHashMap();

    @Override
    public Achievement getAchievement(String desc)
    {
        return achievements.get(desc);
    }

}
