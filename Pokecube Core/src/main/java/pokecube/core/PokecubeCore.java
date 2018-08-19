package pokecube.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import com.google.common.collect.ListMultimap;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.profiler.ISnooperInfo;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.PlayerOrderedLoadingCallback;
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
import net.minecraftforge.fml.common.event.FMLModIdMappingEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.VillagerRegistry;
import pokecube.core.ai.thread.aiRunnables.combat.AIFindTarget;
import pokecube.core.blocks.berries.BerryGenManager;
import pokecube.core.blocks.healtable.TileHealTable;
import pokecube.core.blocks.pc.InventoryPC;
import pokecube.core.commands.Commands;
import pokecube.core.commands.CountCommand;
import pokecube.core.commands.CullCommand;
import pokecube.core.commands.GiftCommand;
import pokecube.core.commands.KillCommand;
import pokecube.core.commands.MakeCommand;
import pokecube.core.commands.MeteorCommand;
import pokecube.core.commands.RecallCommand;
import pokecube.core.commands.ResetCommand;
import pokecube.core.commands.SecretBaseCommand;
import pokecube.core.commands.StructureCommand;
import pokecube.core.commands.TMCommand;
import pokecube.core.database.CombatTypeLoader;
import pokecube.core.database.Database;
import pokecube.core.database.Pokedex;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.abilities.AbilityManager;
import pokecube.core.database.worldgen.WorldgenHandler;
import pokecube.core.entity.pokemobs.EntityPokemob;
import pokecube.core.entity.pokemobs.EntityPokemobPart;
import pokecube.core.entity.pokemobs.helper.EntityPokemobBase;
import pokecube.core.entity.professor.EntityProfessor;
import pokecube.core.events.PostPostInit;
import pokecube.core.events.handlers.EventsHandler;
import pokecube.core.events.handlers.MoveEventsHandler;
import pokecube.core.events.handlers.PCEventsHandler;
import pokecube.core.events.handlers.SpawnHandler;
import pokecube.core.handlers.Config;
import pokecube.core.handlers.PokedexInspector;
import pokecube.core.handlers.playerdata.PokecubePlayerCustomData;
import pokecube.core.handlers.playerdata.PokecubePlayerData;
import pokecube.core.handlers.playerdata.PokecubePlayerStats;
import pokecube.core.handlers.playerdata.advancements.triggers.Triggers;
import pokecube.core.interfaces.IEntityProvider;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.items.pokecubes.EntityPokecube;
import pokecube.core.items.pokemobeggs.EntityPokemobEgg;
import pokecube.core.moves.MoveQueue.MoveQueuer;
import pokecube.core.moves.PokemobTerrainEffects;
import pokecube.core.moves.animations.EntityMoveUse;
import pokecube.core.moves.animations.MoveAnimationHelper;
import pokecube.core.network.EntityProvider;
import pokecube.core.network.NetworkWrapper;
import pokecube.core.network.PokecubePacketHandler;
import pokecube.core.network.PokecubePacketHandler.StarterInfo;
import pokecube.core.utils.PCSaveHandler;
import pokecube.core.utils.Permissions;
import pokecube.core.utils.PokecubeSerializer;
import pokecube.core.utils.Tools;
import pokecube.core.world.dimensions.PokecubeDimensionManager;
import pokecube.core.world.dimensions.secretpower.WorldProviderSecretBase;
import pokecube.core.world.gen.WorldGenFossils;
import pokecube.core.world.gen.WorldGenNests;
import pokecube.core.world.gen.WorldGenTemplates;
import pokecube.core.world.gen.template.PokecubeTemplates;
import pokecube.core.world.gen.village.buildings.pokecenter.PokeCentreCreationHandler;
import pokecube.core.world.gen.village.buildings.pokecenter.TemplatePokecenter;
import thut.api.maths.Vector3;
import thut.api.terrain.TerrainSegment;
import thut.core.common.commands.CommandConfig;
import thut.core.common.config.Configure;
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

    public static void registerSpawns()
    {
        int n = 0;
        List<PokedexEntry> spawns = new ArrayList<PokedexEntry>();
        Database.spawnables.clear();
        for (PokedexEntry dbe : Database.getSortedFormes())
        {
            if (dbe.getSpawnData() != null)
            {
                dbe.getSpawnData().postInit();
                Database.spawnables.add(dbe);
            }
        }
        Collections.sort(Database.spawnables, Database.COMPARATOR);
        for (PokedexEntry dbe : Database.spawnables)
        {
            if (debug) PokecubeMod.log("_ " + dbe + " __");
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
    public Config              config;
    public Config              config_client;
    public Config              currentConfig;
    IEntityProvider            provider;
    public EventsHandler       events;

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
        config_client = new Config(new Configuration(new File(folder + ".dummy")).getConfigFile());
        config = new Config(new Configuration(file).getConfigFile());

        /** Sync values to the dummy config over from the main one. */
        for (Field f : Config.class.getDeclaredFields())
        {
            Configure conf = f.getAnnotation(Configure.class);
            if (conf != null)
            {
                try
                {
                    f.setAccessible(true);
                    f.set(config_client, f.get(config));
                }
                catch (IllegalArgumentException | IllegalAccessException e)
                {
                    PokecubeMod.log(Level.WARNING, "Error syncing " + f.getName(), e);
                }
            }
        }

        currentConfig = config;
        helper = new Mod_Pokecube_Helper();
        CombatTypeLoader.loadTypes();
        checkConfigFiles();
        MoveEventsHandler.getInstance();
    }

    @Override
    public Entity createPokemob(PokedexEntry entry, World world)
    {
        Entity entity = null;
        Class<?> clazz = null;
        if (entry == null || !Pokedex.getInstance().isRegistered(entry))
        {
            PokecubeMod.log(Level.WARNING, "Attempted to create unregistered mob, " + entry,
                    new IllegalArgumentException());
            return null;
        }
        try
        {
            clazz = pokedexmap.get(entry);
            if (clazz != null)
            {
                entity = (Entity) clazz.getConstructor(new Class[] { World.class }).newInstance(new Object[] { world });
            }
        }
        catch (Exception e)
        {
            PokecubeMod.log(Level.WARNING, "Error creating " + entry + " " + clazz, e);
        }
        if (entity == null)
        {
            log(Level.SEVERE, "Problem with entity with: " + entity + " " + entry);
        }
        if (entity != null)
        {
            IPokemob pokemob = CapabilityPokemob.getPokemobFor(entity);
            if (pokemob.getPokedexEntry() != entry)
            {
                if (debug) log(entry + " " + pokemob.getPokedexEntry() + " " + clazz);
                pokemob = pokemob.setPokedexEntry(entry);
                entity = pokemob.getEntity();
            }
        }
        return entity;
    }

    @Override
    public Config getConfig()
    {
        return currentConfig;
    }

    /** Returns the class of the {@link EntityLiving} for the given pokedexNb.
     * If no Pokemob has been registered for this pokedex entry, it returns
     * <code>null</code>.
     * 
     * @param entry
     *            the pokedex entry
     * @return the {@link Class} of the pokemob */
    @Override
    public Class<? extends Entity> getEntityClassForEntry(PokedexEntry entry)
    {
        try
        {
            return pokedexmap.get(entry);
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
    public PokedexEntry[] getStarters()
    {
        PokecubeMod.core.starters.clear();
        for (PokedexEntry entry : Database.getSortedFormes())
        {
            if (entry.isStarter && !PokecubeMod.core.starters.contains(entry))
            {
                PokecubeMod.core.starters.add(entry);
            }
        }
        PokecubeMod.core.starters.sort(Database.COMPARATOR);
        return starters.toArray(new PokedexEntry[0]);
    }

    @EventHandler
    private void initRecipes(FMLInitializationEvent evt)
    {
        helper.registerRecipes(evt);
    }

    @EventHandler
    private void init(FMLInitializationEvent evt)
    {
        if (PokecubeMod.debug) PokecubeMod.log("Pokecube Init " + FMLCommonHandler.instance().getEffectiveSide());
        TerrainSegment.terrainEffectClasses.add(PokemobTerrainEffects.class);
        new PokedexInspector();
        proxy.initClient();
        proxy.registerRenderInformation();
        moveQueues = new MoveQueuer();
        PlayerDataHandler.dataMap.add(PokecubePlayerData.class);
        PlayerDataHandler.dataMap.add(PokecubePlayerStats.class);
        PlayerDataHandler.dataMap.add(PokecubePlayerCustomData.class);

        // Register the village stuff.
        if (config.villagePokecenters)
        {
            VillagerRegistry.instance().registerVillageCreationHandler(new PokeCentreCreationHandler());
            MapGenStructureIO.registerStructureComponent(TemplatePokecenter.class, ID + ":pokecenter");
        }

        // Register worldgen stuff
        if (config.generateFossils) GameRegistry.registerWorldGenerator(new WorldGenFossils(), 10);
        if (config.nests) GameRegistry.registerWorldGenerator(new WorldGenNests(), 10);
        GameRegistry.registerWorldGenerator(new WorldGenTemplates(), 10);

        helper.initAllBlocks();
        proxy.registerKeyBindings();
        NetworkRegistry.INSTANCE.registerGuiHandler(this, proxy);
        helper.postInit();

        // Check if we need to remove any mob spawns from maps.
        removeAllMobs();

        PokecubeItems.init();
        // Initialize the triggers.
        Triggers.init();
    }

    @EventHandler
    private void postInit(FMLPostInitializationEvent evt)
    {
        if (PokecubeMod.debug) PokecubeMod.log("Pokecube Core Post Init");
        // Initialize permissions for secret base stuff
        WorldProviderSecretBase.initPerms();
        // Initialize the target blacklists.
        AIFindTarget.initIDs();
        // Send database postinit.
        Database.postInit();
        // Apply settings for custom starters.
        StarterInfo.processStarterInfo();
        // Initizalize abilities.
        AbilityManager.init();
        // Fire postpostinit event to addons.
        MinecraftForge.EVENT_BUS.post(new PostPostInit());
        // Register all our permissions.
        Permissions.register();
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
        if (PokecubeMod.debug) PokecubeMod.log("Regstering Sounds");
        Database.initSounds(evt);
    }

    // TODO swap this to proper events for 1.11.2/1.12
    @EventHandler
    public void registerMobs(FMLPreInitializationEvent evt)
    {
        if (PokecubeMod.debug) PokecubeMod.log("Regstering Mobs");
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
            if (debug) PokecubeMod.log("Changing Default Mobs to " + config.defaultMobs);
            defaultMod = config.defaultMobs;
        }

        config.save();
        config.initDefaultStarts();
        events = new EventsHandler();
        ForgeChunkManager.setForcedChunkLoadingCallback(this, new PlayerOrderedLoadingCallback()
        {
            @Override
            public void ticketsLoaded(List<Ticket> tickets, World world)
            {
                Iterator<Ticket> next = tickets.iterator();
                while (next.hasNext())
                {
                    Ticket ticket = next.next();
                    if (!ticket.getModId().equals(ID)) continue;
                    if (!ticket.isPlayerTicket() || !PokecubeCore.core.getConfig().chunkLoadPokecenters)
                    {
                        ForgeChunkManager.releaseTicket(ticket);
                        continue;
                    }
                    NBTTagCompound posTag = ticket.getModData().getCompoundTag("pos");
                    BlockPos pos = new BlockPos(posTag.getInteger("x"), posTag.getInteger("y"), posTag.getInteger("z"));
                    TileEntity tile = world.getTileEntity(pos);
                    if (!(tile instanceof TileHealTable))
                    {
                        PokecubeMod.log("invalid ticket for " + pos);
                        ForgeChunkManager.releaseTicket(ticket);
                    }
                    else
                    {
                        ChunkPos location = new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4);
                        if (debug) PokecubeMod.log("Forcing Chunk at " + location);
                        ForgeChunkManager.forceChunk(ticket, location);
                    }
                }
            }

            @Override
            public ListMultimap<String, Ticket> playerTicketsLoaded(ListMultimap<String, Ticket> tickets, World world)
            {
                return tickets;
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
                if (e1 instanceof UnknownHostException)
                {
                    PokecubeMod.log(Level.WARNING, "Error loading pokegifts, unknown host " + location);
                }
                else
                {
                    if (fileIn != null) try
                    {
                        fileIn.close();
                    }
                    catch (IOException e)
                    {
                        PokecubeMod.log(Level.WARNING, "Error with PokeGifts", e);
                    }
                    fileIn = null;
                    PokecubeMod.log(Level.WARNING, "Error with PokeGifts", e1);
                }
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
                    PokecubeMod.log(Level.WARNING, "Error with PokeGifts", e);
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

    /** Registers a Pokemob into the Pokedex, this finds or generates a class
     * for the mob automatically, if you want to provide your own class for the
     * pokemob, call
     * {@link PokecubeCore#registerPokemonByClass(Class, boolean, Object, PokedexEntry)}
     * instead */
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
                if (debug) log("Generated class " + c + " for " + entry);
            }
            catch (ClassNotFoundException e)
            {
                PokecubeMod.log(Level.SEVERE, "Error Making Class for  " + entry, e);
            }
        }
        else
        {
            registerPokemonByClass(c, createEgg, mod, entry);
            if (debug) log("Loaded class " + c + " for " + entry);
        }
    }

    /** Registers a Pokemob into the Pokedex, this method should only be used if
     * you want to provide your own pokemob class..
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
        /** Dummy entries are not to be registered, they are just there for
         * copying values from. */
        if (entry.dummy)
        {
            if (debug)
            {
                PokecubeMod.log("Skipping Dummy: " + entry);
            }
            return;
        }
        if (pokedexmap == null)
        {
            pokedexmap = new HashMap();
        }
        if (clazz != null)
        {
            try
            {
                // Entries should only be registered once, if an addon wants to
                // overwrite an existing one, it should just edit the entry
                // directly.
                if (pokedexmap.containsKey(entry))
                {
                    PokecubeMod.log("Error: " + mod + " Tried to register a second " + entry);
                    return;
                }

                // In some cases, the modid isn't loaded in, this makes it to
                // one of the form modids, as generally they will be added by
                // the same mod.
                modid:
                if (entry.getModId() == null)
                {
                    for (PokedexEntry e : Database.getFormes(entry))
                    {
                        if (e.getModId() != null)
                        {
                            entry.setModId(e.getModId());
                            break modid;
                        }
                    }
                    entry.setModId(defaultMod);
                }
                // Register the mob with minecraft.
                CompatWrapper.registerModEntity(clazz, entry.getTrimmedName(), getUniqueEntityId(mod), mod, 80, 3,
                        true);

                // Register the pokemob with proxy, This does things like
                // register that it has genes, animations, etc.
                proxy.registerClass(clazz, entry);
                // Make an egg for the mob.
                if (!pokemobEggs.containsKey(entry.getPokedexNb()))
                {
                    pokemobEggs.put(new Integer(entry.getPokedexNb()),
                            CompatWrapper.getEggInfo(entry.getName(), 0xE8E0A0, 0x78C848));
                }
                // Register the entry with the pokedex.
                Pokedex.getInstance().registerPokemon(entry);
            }
            catch (Throwable e)
            {
                e.printStackTrace();
            }
        }
    }

    /** Removes the biome spawn entries for the default mobs. */
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

    /** Registers commands, mob spawns and structure spawns. <br>
     * <br>
     * Structure/mob spawns are here, they need to be done after world has
     * started loading to account for registry remapping. */
    @EventHandler
    public void serverLoad(FMLServerStartingEvent event)
    {
        event.registerServerCommand(new Commands());
        event.registerServerCommand(new CommandConfig("pokesettings", getConfig()));
        event.registerServerCommand(new MakeCommand());
        event.registerServerCommand(new GiftCommand());
        event.registerServerCommand(new TMCommand());
        event.registerServerCommand(new RecallCommand());
        event.registerServerCommand(new SecretBaseCommand());
        event.registerServerCommand(new KillCommand());
        event.registerServerCommand(new CountCommand());
        event.registerServerCommand(new CullCommand());
        event.registerServerCommand(new MeteorCommand());
        event.registerServerCommand(new ResetCommand());
        event.registerServerCommand(new StructureCommand());
        PokecubeTemplates.serverInit(event.getServer());
        registerSpawns();
        try
        {
            PokecubeDimensionManager.getInstance().onServerStart(event);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /** Cleans up some values to be clear for next world. */
    @EventHandler
    public void serverStop(FMLServerStoppingEvent event)
    {
        events.meteorprocessor.clear();
        BerryGenManager.berryLocations.clear();
        PokecubeDimensionManager.getInstance().onServerStop(event);
        WorldGenTemplates.TemplateGenStartBuilding.clear();
        SpawnHandler.clear();
    }

    /** Second cleanup pass for after world stops. */
    @EventHandler
    public void serverStop(FMLServerStoppedEvent evt)
    {
        InventoryPC.clearPC();
        MoveAnimationHelper.Instance().clear();
        if (PokecubeSerializer.instance != null) PokecubeSerializer.instance.clearInstance();
        if (debug)
        {
            // limit to 1% precision
            double value = ((int) (EntityPokemobBase.averagePokemobTick * 100)) / 100d;
            log("Average Pokemob Tick Time for this Session: " + value + "\u00B5s");
        }
        EntityPokemobBase.averagePokemobTick = 0;
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

    @EventHandler
    public void handshake(FMLModIdMappingEvent evt)
    {
        proxy.handshake(evt.isFrozen);
    }

    public static void checkConfigFiles()
    {
        File file = new File("./config/pokecube.cfg");
        String seperator = System.getProperty("file.separator");
        String folder = file.getAbsolutePath();
        String name = file.getName();
        PokecubeTemplates.TEMPLATES = folder.replace(name, "pokecube" + seperator + "structures" + seperator + "");
        PokecubeTemplates.initFiles();
        WorldgenHandler.DEFAULT = new File(PokecubeTemplates.TEMPLATES, "worldgen.json");
    }
}
