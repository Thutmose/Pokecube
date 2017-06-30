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
import java.util.logging.FileHandler;
import java.util.logging.Level;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.profiler.ISnooperInfo;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.LoadingCallback;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.RegistryEvent;
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
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.VillagerRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.registries.GameData;
import pokecube.core.ai.utils.AISaveHandler;
import pokecube.core.blocks.berries.BerryGenManager;
import pokecube.core.blocks.healtable.ContainerHealTable;
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
import pokecube.core.handlers.PokedexInspector;
import pokecube.core.handlers.playerdata.PokecubePlayerCustomData;
import pokecube.core.handlers.playerdata.PokecubePlayerData;
import pokecube.core.handlers.playerdata.PokecubePlayerStats;
import pokecube.core.interfaces.IEntityProvider;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.pokecubes.EntityPokecube;
import pokecube.core.items.pokecubes.EntityPokecubeBase;
import pokecube.core.items.pokemobeggs.EntityPokemobEgg;
import pokecube.core.moves.MoveQueue.MoveQueuer;
import pokecube.core.moves.PokemobTerrainEffects;
import pokecube.core.moves.animations.EntityMoveUse;
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
import pokecube.core.world.gen.WorldGenTemplates;
import pokecube.core.world.gen.template.PokecubeTemplates;
import pokecube.core.world.gen.village.buildings.TemplatePokecenter;
import pokecube.core.world.gen.village.buildings.TemplatePokemart;
import pokecube.core.world.gen.village.handlers.PokeCentreCreationHandler;
import pokecube.core.world.gen.village.handlers.PokeMartCreationHandler;
import thut.api.maths.Vector3;
import thut.api.terrain.TerrainSegment;
import thut.core.common.handlers.PlayerDataHandler;
import thut.lib.CompatWrapper;

@Mod( // @formatter:off
        modid = PokecubeMod.ID, name = "Pokecube", version = PokecubeMod.VERSION, dependencies = "required-after:forge@"
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
        MinecraftForge.EVENT_BUS.register(this);
        File file = new File("./config/", ID + ".cfg");
        String seperator = System.getProperty("file.separator");
        String folder = file.getAbsolutePath();
        String name = file.getName();
        folder = folder.replace(name, "pokecube" + seperator + name);
        file = new File(folder);
        config = new Config(new Configuration(file).getConfigFile());
        helper = new Mod_Pokecube_Helper();
    }

    @Override
    public Entity createPokemob(PokedexEntry entry, World world)
    {
        Entity entity = null;
        Class<?> clazz = null;
        if (!registered.get(entry.getPokedexNb())) return null;
        try
        {
            PokedexEntry base = entry.base ? entry : entry.getBaseForme();
            clazz = pokedexmap.get(base);
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
            System.err.println("Problem with entity with: " + entity);
            System.err.println(clazz + " " + entry);
        }

        if (entity != null)
        {
            entity = (Entity) ((IPokemob) entity).setPokedexEntry(entry);
        }
        return entity;
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
            System.err.println(clazz + " " + pokedexmap);
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
    private void initRecipes(FMLInitializationEvent evt)
    {
        helper.registerRecipes(evt);
    }

    @EventHandler
    private void init(FMLInitializationEvent evt)
    {
        System.out.println("mod_pokecube.init() " + FMLCommonHandler.instance().getEffectiveSide());
        TerrainSegment.terrainEffectClasses.add(PokemobTerrainEffects.class);
        new PokedexInspector();
        proxy.initClient();
        proxy.registerRenderInformation();
        moveQueues = new MoveQueuer();
        PlayerDataHandler.dataMap.add(PokecubePlayerData.class);
        PlayerDataHandler.dataMap.add(PokecubePlayerStats.class);
        PlayerDataHandler.dataMap.add(PokecubePlayerCustomData.class);

        if (config.villagePokecenters)
            VillagerRegistry.instance().registerVillageCreationHandler(new PokeCentreCreationHandler());
        if (config.villagePokemarts)
            VillagerRegistry.instance().registerVillageCreationHandler(new PokeMartCreationHandler());
        try
        {
            if (config.villagePokecenters) MapGenStructureIO.registerStructureComponent(TemplatePokecenter.class,
                    "poke_adventures:PokeCentreStructure");
            if (config.villagePokemarts) MapGenStructureIO.registerStructureComponent(TemplatePokemart.class,
                    "poke_adventures:PokeMartStructure");
        }
        catch (Throwable e1)
        {
            System.out.println("Error registering Structures with Vanilla Minecraft");
        }

        if (config.generateFossils) GameRegistry.registerWorldGenerator(new WorldGenFossils(), 10);
        if (config.nests) GameRegistry.registerWorldGenerator(new WorldGenNests(), 10);
        GameRegistry.registerWorldGenerator(new WorldGenTemplates(), 10);
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
        helper.addVillagerTrades();
        SpecialCaseRegister.register();
        MinecraftForge.EVENT_BUS.post(new PostPostInit());
        MovesAdder.postInitMoves();
    }

    @SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> evt)
    {
        helper.itemRegistry(evt.getRegistry());
        proxy.initItemModels();
    }

    @SubscribeEvent
    public void registerBlocks(RegistryEvent.Register<Block> evt)
    {
        helper.blockRegistry(evt.getRegistry());
        proxy.initBlockModels();
    }

    @SubscribeEvent
    public void registerTiles(RegistryEvent.Register<Block> evt)// TODO move to
                                                                // tile entity
                                                                // if it exists.
    {
        helper.tileRegistry(evt.getRegistry());
    }

    @EventHandler
    public void registerSounds(FMLPostInitializationEvent evt)
    {
        System.out.println("Regstering Sounds");
        Database.initSounds(evt);
        ResourceLocation sound = new ResourceLocation(PokecubeMod.ID + ":pokecube_caught");
        GameData.register_impl(EntityPokecubeBase.POKECUBESOUND = new SoundEvent(sound).setRegistryName(sound));
        GameData.register_impl(ContainerHealTable.HEAL_SOUND.setRegistryName(PokecubeMod.ID + ":pokecenter"));
        sound = new ResourceLocation(PokecubeMod.ID + ":pokecenterloop");
        GameData.register_impl(new SoundEvent(sound).setRegistryName(sound));
    }

    // TODO swap this to proper events for 1.11.2/1.12
    @EventHandler
    public void registerMobs(FMLPreInitializationEvent evt)
    {
        System.out.println("Regstering Mobs");
        CompatWrapper.registerModEntity(EntityPokemob.class, "genericMob", getUniqueEntityId(this), this, 80, 1, true);
        CompatWrapper.registerModEntity(EntityPokemobPart.class, "genericMobPart", getUniqueEntityId(this), this, 80, 1,
                true);
        CompatWrapper.registerModEntity(EntityProfessor.class, "Professor", getUniqueEntityId(this), this, 80, 3, true);
        CompatWrapper.registerModEntity(EntityPokemobEgg.class, "pokemobEgg", getUniqueEntityId(this), this, 80, 3,
                false);
        CompatWrapper.registerModEntity(EntityPokecube.class, "cube", getUniqueEntityId(this), this, 80, 1, true);
        CompatWrapper.registerModEntity(EntityMoveUse.class, "moveuse", getUniqueEntityId(this), this, 80, 3, true);
    }

    @EventHandler
    private void preInit(FMLPreInitializationEvent evt)
    {
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
    public void registerPokemon(boolean createEgg, Object mod, PokedexEntry entry)
    {
        if (!entry.base)
        {
            Pokedex.getInstance().getRegisteredEntries().add(entry);
            return;
        }
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
                    int id = getUniqueEntityId(mod);
                    if (!entry.base) name = entry.getBaseName();
                    CompatWrapper.registerModEntity(clazz, name, id, mod, 80, 3, true);

                    if (!pokemobEggs.containsKey(entry.getPokedexNb()))
                    {
                        pokemobEggs.put(new Integer(entry.getPokedexNb()),
                                CompatWrapper.getEggInfo(entry.getName(), 0xE8E0A0, 0x78C848));
                    }
                    pokedexmap.put(entry, clazz);
                    for (PokedexEntry e : entry.forms.values())
                        pokedexmap.put(e, clazz);
                    registered.set(entry.getPokedexNb());
                    Pokedex.getInstance().registerPokemon(entry);
                }
                else
                {
                    System.err.println("Double Registration for " + entry);
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
        PokecubeTemplates.serverInit(event.getServer());
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
        PokecubePlayerStats.initMap();
    }

    /** clears PC when server stops
     * 
     * @param evt */
    @EventHandler
    public void WorldUnloadEvent(FMLServerStoppedEvent evt)
    {
        InventoryPC.clearPC();
        PokecubePlayerStats.reset();
        if (PokecubeSerializer.instance != null) PokecubeSerializer.instance.clearInstance();
        AISaveHandler.clearInstance();
    }

}
