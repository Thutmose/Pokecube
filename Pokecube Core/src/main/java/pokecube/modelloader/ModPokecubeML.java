package pokecube.modelloader;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.database.Database;
import pokecube.core.database.Database.EnumDatabase;
import pokecube.core.database.Pokedex;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.PokedexEntryLoader;
import pokecube.core.database.PokedexEntryLoader.XMLDatabase;
import pokecube.core.database.PokedexEntryLoader.XMLPokedexEntry;
import pokecube.core.events.handlers.MoveEventsHandler;
import pokecube.core.events.onload.InitDatabase;
import pokecube.core.events.onload.RegisterPokemobsEvent;
import pokecube.core.interfaces.IMoveAction;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.moves.animations.MoveAnimationHelper;
import pokecube.core.moves.implementations.MovesAdder;
import pokecube.core.world.terrain.PokecubeTerrainChecker;
import pokecube.modelloader.common.Config;
import pokecube.modelloader.common.ExtraDatabase;
import pokecube.modelloader.items.ItemModelReloader;

@Mod(modid = ModPokecubeML.ID, name = "Pokecube Model Loader", version = "0.1.0", acceptedMinecraftVersions = "*")
public class ModPokecubeML implements IMobProvider
{
    /** The id of your mod */
    public final static String        ID                      = "pokecube_ml";
    public static final String        MODELPATH               = "entity/models/";
    public static final String        TEXTUREPATH             = PokedexEntry.TEXTUREPATH;

    @Instance(ID)
    public static ModPokecubeML       instance;

    public static boolean             checkResourcesForModels = true;

    public static ArrayList<String>   addedPokemon            = Lists.newArrayList();
    public static Map<String, String> textureProviders        = Maps.newHashMap();

    public static Set<String>         scanPaths               = Sets.newHashSet("assets/pokecube_ml/entity/models/");

    public static boolean             preload                 = false;

    @SidedProxy(clientSide = "pokecube.modelloader.client.ClientProxy", serverSide = "pokecube.modelloader.CommonProxy")
    public static CommonProxy         proxy;
    public static File                configDir               = new File("./config/");

    public static void sort(List<String> list)
    {
        Collections.sort(list, new Comparator<String>()
        {
            @Override
            public int compare(String o1, String o2)
            {
                PokedexEntry e1 = Database.getEntry(o1);
                PokedexEntry e2 = Database.getEntry(o2);
                return Database.COMPARATOR.compare(e1, e2);
            }
        });
    }

    private void doMetastuff()
    {
        ModMetadata meta = FMLCommonHandler.instance().findContainerFor(this).getMetadata();
        meta.parent = PokecubeMod.ID;
    }

    public ModPokecubeML()
    {
        MinecraftForge.EVENT_BUS.register(this);
        File file = new File(configDir, ID + ".cfg");
        String seperator = System.getProperty("file.separator");
        String folder = file.getAbsolutePath();
        String name = file.getName();
        folder = folder.replace(name, "pokecube" + seperator + name);
        file = new File(folder);
        new Config(new Configuration(file).getConfigFile());
        CommonProxy.registerModelProvider(ID, this);
    }

    @EventHandler
    public void registerMobs(FMLPreInitializationEvent evt)
    {
        registerDatabase(evt);
        MinecraftForge.EVENT_BUS.post(new RegisterPokemobsEvent.Pre());
        MinecraftForge.EVENT_BUS.post(new RegisterPokemobsEvent.Register());
        PokecubeMod.log("Registered " + PokecubeCore.pokedexmap.size());
        MinecraftForge.EVENT_BUS.post(new RegisterPokemobsEvent.Post());
        postInitPokemobs();
    }

    public void registerDatabase(FMLPreInitializationEvent evt)
    {
        MinecraftForge.EVENT_BUS.post(new InitDatabase.Pre());

        // Apply configs after all other addons apply their changes.
        for (int i = 0; i < EnumDatabase.values().length; i++)
        {
            String[] args = PokecubeCore.core.getConfig().configDatabases[i].split(",");
            for (String s : args)
            {
                if (s.isEmpty()) continue;
                if (s.indexOf(".") == -1) s = s.trim() + ".json";
                Database.addDatabase(s.trim(), EnumDatabase.values()[i]);
            }
        }

        populateAddonMods();
        PokecubeTerrainChecker.init();
        Database.init();
        if (PokecubeMod.debug) PokecubeMod.log("Registering Moves");
        MovesAdder.registerMoves();
        MinecraftForge.EVENT_BUS.post(new InitDatabase.Post());
    }

    private void populateAddonMods()
    {
        for (IMobProvider obj : CommonProxy.mobProviders.values())
        {
            Object mod = obj.getMod();
            ModContainer cont = Loader.instance().getReversedModObjectList().get(mod);
            CommonProxy.validModJars.add(cont.getSource());
        }
    }

    private void postInitPokemobs()
    {
        PokecubeMod.log("Loaded " + Pokedex.getInstance().getEntries().size() + " Pokemon and "
                + Pokedex.getInstance().getRegisteredEntries().size() + " Formes");
    }

    @SubscribeEvent
    public void RegisterPokemobsEvent(InitDatabase.Load event)
    {
        try
        {
            if (checkResourcesForModels)
            {
                processResources();
            }
        }
        catch (Exception e)
        {
        }
        proxy.searchModels();
        if (PokecubeMod.debug)
        {
            List<PokedexEntry> all = Database.getSortedFormes();
            StringBuilder builder = new StringBuilder("All Pokedex Entries:");
            for (PokedexEntry e : all)
            {
                builder.append("\n-   ").append(e.getName());
            }
            PokecubeMod.log(builder.toString());
            // this makes english lang file, waste of time most times, so
            // commented out for now.
            // builder = new StringBuilder("Lang File Ouput:");
            // for (PokedexEntry e : all)
            // {
            // PokedexEntry base = e.getBaseForme();
            // if (base == null) base = e;
            // builder.append("\npkmn.").append(e.getName()).append(".name=").append(base.getName());
            // }
            // PokecubeMod.log(builder.toString());
        }
        for (String s : addedPokemon)
        {
            loadMob(s);
        }
        ExtraDatabase.apply();
        if (PokecubeMod.debug)
        {
            sort(addedPokemon);
            PokecubeMod.log(addedPokemon + " " + addedPokemon.size());
        }
    }

    @SubscribeEvent
    public void RegisterPokemobsEvent(RegisterPokemobsEvent.Register event)
    {
        // Debug output list of missing mobs, as well as where they were
        // searched for.
        if (PokecubeMod.debug)
        {
            List<String> vars = Lists.newArrayList(proxy.notFound.keySet());
            vars.removeAll(addedPokemon);
            vars.removeIf(new Predicate<String>()
            {
                @Override
                public boolean test(String t)
                {
                    PokedexEntry entry = Database.getEntry(t);
                    return entry == null || entry.dummy;
                }
            });
            ModPokecubeML.sort(vars);
            for (String s : vars)
            {
                String var = "No " + s + " found in " + proxy.notFound.get(s);
                if (var != null && !var.toLowerCase(Locale.ENGLISH).trim().contains("null")) PokecubeMod.log(var);
            }
            proxy.notFound.clear();
        }
        // Sort and cleanup dummies from registration list.
        sort(addedPokemon);
        addedPokemon.removeIf(new Predicate<String>()
        {
            @Override
            public boolean test(String t)
            {
                PokedexEntry entry = Database.getEntry(t);
                return entry == null || entry.dummy;
            }
        });
        for (String s : addedPokemon)
        {
            if (PokecubeMod.debug) PokecubeMod.log("reg: " + s);
            registerMob(s);
        }
        // Debug output list of registerd mobs, the reg: log above is for if
        // things go wrong, to know which one it went wrong on.
        if (PokecubeMod.debug)
        {
            List<String> vars = Lists.newArrayList(addedPokemon);
            ModPokecubeML.sort(vars);
            vars.replaceAll(new UnaryOperator<String>()
            {
                @Override
                public String apply(String t)
                {
                    PokedexEntry entry = Database.getEntry(t);
                    return entry.getName();
                }
            });
            for (String s : vars)
            {
                PokecubeMod.log(s + " Located and Registered.");
            }
        }
    }

    @SubscribeEvent
    public void RegisterPokemobsEvent(RegisterPokemobsEvent.Post event)
    {
        /** Cleanup modids and texture paths. */
        for (PokedexEntry e : Database.getSortedFormes())
        {
            if (e.getBaseForme() != null && e.getModId() == null)
            {
                PokecubeMod.log("Set MODID: " + this + " " + e.getModId());
                e.texturePath = e.getBaseForme().texturePath;
                e.setModId(e.getBaseForme().getModId());
            }
        }
    }

    @EventHandler
    public void init(FMLInitializationEvent evt)
    {
        proxy.init();
        NetworkRegistry.INSTANCE.registerGuiHandler(this, proxy);
    }

    private void loadMob(String mob)
    {
        if (textureProviders.containsKey(mob) && !textureProviders.get(mob).equals(ID)) return;

        ResourceLocation xml = new ResourceLocation(ModPokecubeML.ID, getModelDirectory(null) + mob + ".xml");
        try
        {
            List<String> list = proxy.fileAsList(ID, xml);
            if (!list.isEmpty())
            {
                ExtraDatabase.addXMLEntry(ID, mob, list);
            }
            else
            {
                PokecubeMod.log(Level.WARNING, "No XML for " + mob);
            }
        }
        catch (Exception e1)
        {
            e1.printStackTrace();
        }
    }

    @EventHandler
    private void postInit(FMLPostInitializationEvent evt)
    {
        MoveAnimationHelper.Instance();
        proxy.postInit();
        for (IMoveAction action : MoveEventsHandler.getInstance().actionMap.values())
        {
            action.init();
        }
    }

    /** This function is called by Forge at initialization.
     * 
     * @param evt */
    @EventHandler
    public void preInit(FMLPreInitializationEvent evt)
    {
        proxy.preInit();
        doMetastuff();
    }

    @SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> evt)
    {
        PokecubeItems.register(new ItemModelReloader().setUnlocalizedName("modelreloader")
                .setRegistryName(ID, "modelreloader").setCreativeTab(PokecubeMod.creativeTabPokecube),
                evt.getRegistry());
    }

    private void processResources()
    {
        File resourceDir = new File(ModPokecubeML.configDir.getParent(), "resourcepacks");
        ArrayList<File> files = Lists.newArrayList(resourceDir.listFiles());
        for (File pack : files)
        {
            if (pack.exists() && !pack.isDirectory())
            {
                try
                {
                    ZipFile zip = new ZipFile(pack);
                    Enumeration<? extends ZipEntry> entries = zip.entries();
                    int n = 0;
                    while (entries.hasMoreElements() && n < 10)
                    {
                        ZipEntry entry = entries.nextElement();
                        String s = entry.getName();
                        for (String modelDir : scanPaths)
                        {
                            if (s.contains(modelDir))
                            {
                                String name = s.replace(modelDir, "").split("\\.")[0];
                                if (name.trim().isEmpty() || !(s.endsWith(".xml") || s.endsWith(".json"))) continue;
                                boolean mobsDatabase = name.equals("_mobs_");
                                if (mobsDatabase)
                                {
                                    if (PokecubeMod.debug) PokecubeMod.log("Adding From " + name + " " + s);
                                    XMLDatabase database = PokedexEntryLoader.initDatabase(zip.getInputStream(entry),
                                            s.endsWith(".json"));
                                    for (XMLPokedexEntry xmlentry : database.pokemon)
                                    {
                                        PokedexEntry pEntry = Database.getEntry(xmlentry.name);
                                        if (PokecubeMod.debug) PokecubeMod.log("Adding " + pEntry);
                                        if (!addedPokemon.contains(pEntry.getTrimmedName()))
                                            addedPokemon.add(pEntry.getTrimmedName());
                                    }
                                }
                                else if (name.equals("_moves_"))
                                {
                                    // TODO load in moves database from here.
                                }
                            }
                        }
                    }
                    zip.close();
                }
                catch (Exception e)
                {
                    PokecubeMod.log(Level.WARNING, "Error with " + pack, e);
                }
            }
            else
            {
                // System.err.println("No Resource Pack " + pack);
            }
        }
    }

    private void registerMob(String mob)
    {
        PokedexEntry e;
        if ((e = Database.getEntry(mob)) != null)
        {
            if (e.getModId() == null)
            {
                if (e.getBaseForme() != null && textureProviders.containsKey(e.getTrimmedName()))
                {
                    e.setModId(textureProviders.get(e.getTrimmedName()));
                }
                else if (textureProviders.containsKey(e.getTrimmedName()))
                {
                    e.setModId(textureProviders.get(e.getTrimmedName()));
                }
                else
                {
                    e.setModId(ID);
                }
            }
            if (!Pokedex.getInstance().isRegistered(e)) PokecubeMod.core.registerPokemon(true, this, e);
        }
        else if (e == null)
        {
            PokecubeMod.log("Failed to register " + mob);
        }
    }

    @Override
    public String getModelDirectory(PokedexEntry entry)
    {
        return MODELPATH;
    }

    @Override
    public String getTextureDirectory(PokedexEntry entry)
    {
        return TEXTUREPATH;
    }

    @Override
    public Object getMod()
    {
        return this;
    }
}
