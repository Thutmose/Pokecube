package pokecube.modelloader;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.blocks.healtable.ContainerHealTable;
import pokecube.core.database.Database;
import pokecube.core.database.Pokedex;
import pokecube.core.database.PokedexEntry;
import pokecube.core.events.onload.InitDatabase;
import pokecube.core.events.onload.RegisterPokemobsEvent;
import pokecube.core.handlers.playerdata.PokecubePlayerStats;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.pokecubes.EntityPokecubeBase;
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
    public static final String        MODELPATH               = "models/pokemobs/";
    public static final String        TEXTUREPATH             = "textures/entities/";

    @Instance(ID)
    public static ModPokecubeML       instance;

    public static boolean             checkResourcesForModels = true;

    public static ArrayList<String>   addedPokemon            = Lists.newArrayList();
    public static Map<String, String> textureProviders        = Maps.newHashMap();

    public static Set<String>         scanPaths               = Sets.newHashSet("assets/pokecube_ml/models/pokemobs/");

    public static boolean             info                    = false;
    public static boolean             preload                 = true;

    @SidedProxy(clientSide = "pokecube.modelloader.client.ClientProxy", serverSide = "pokecube.modelloader.CommonProxy")
    public static CommonProxy         proxy;
    public static File                configDir               = new File("./config/");

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
        PokecubePlayerStats.initAchievements();
        registerDatabase(evt);
        MinecraftForge.EVENT_BUS.post(new RegisterPokemobsEvent.Pre());
        MinecraftForge.EVENT_BUS.post(new RegisterPokemobsEvent.Register());
        System.out.println("Registered " + PokecubeCore.pokedexmap.size());
        MinecraftForge.EVENT_BUS.post(new RegisterPokemobsEvent.Post());
        postInitPokemobs();
    }

    public void registerDatabase(FMLPreInitializationEvent evt)
    {
        MinecraftForge.EVENT_BUS.post(new InitDatabase.Pre());
        PokecubeTerrainChecker.init();
        MoveAnimationHelper.Instance();
        Database.init();
        System.out.println("Registering Moves");
        MovesAdder.registerMoves();
        MinecraftForge.EVENT_BUS.post(new InitDatabase.Post());
    }

    private void postInitPokemobs()
    {
        for (PokedexEntry p : Pokedex.getInstance().getRegisteredEntries())
        {
            p.setSound("mobs." + p.getName());
            p.getSoundEvent();
            p.updateMoves();
        }
        System.out.println("Loaded " + Pokedex.getInstance().getEntries().size() + " Pokemon and "
                + Pokedex.getInstance().getRegisteredEntries().size() + " Formes");
    }

    @SubscribeEvent
    public void RegisterPokemobsEvent(RegisterPokemobsEvent.Pre event)
    {
        proxy.searchModels();
    }

    @SubscribeEvent
    public void RegisterPokemobsEvent(RegisterPokemobsEvent.Register event)
    {
        if (info)
        {
            for (PokedexEntry e : Database.allFormes)
            {
                System.out.println(e.getName());
            }
        }
        proxy.providesModels(ID, this, addedPokemon.toArray(new String[0]));
        for (String s : addedPokemon)
        {
            loadMob(s.toLowerCase(Locale.ENGLISH));
        }
        ExtraDatabase.apply();
        System.out.println(addedPokemon.size() + " " + addedPokemon);
        for (String s : addedPokemon)
        {
            registerMob(s.toLowerCase(Locale.ENGLISH));
        }
    }

    @SubscribeEvent
    public void RegisterPokemobsEvent(RegisterPokemobsEvent.Post event)
    {
        for (PokedexEntry e : Database.allFormes)
        {
            if (e.getBaseForme() != null && e.texturePath.equals("textures/entities/"))
                e.texturePath = e.getBaseForme().texturePath;
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

        ArrayList<String> list = Lists.newArrayList();
        ResourceLocation xml = new ResourceLocation(ModPokecubeML.ID, getModelDirectory(null) + mob + ".xml");
        try
        {
            proxy.fileAsList(this, xml, list);
            if (!list.isEmpty())
            {
                ExtraDatabase.addXMLEntry(ID, mob, list);
            }
            else
            {
                System.err.println("Failed to aquire XML for " + mob);
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
        proxy.postInit();
    }

    /** This function is called by Forge at initialization.
     * 
     * @param evt */
    @EventHandler
    public void preInit(FMLPreInitializationEvent evt)
    {
        proxy.preInit();
        doMetastuff();

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
        ArrayList<String> toAdd = Lists.newArrayList();

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
                                if (name.trim().isEmpty()) continue;
                                boolean has = false;
                                for (String s1 : toAdd)
                                {
                                    if (s1.equals(name))
                                    {
                                        has = true;
                                        break;
                                    }
                                }
                                if (!has)
                                {
                                    System.out.println("Adding " + name);
                                    toAdd.add(name);
                                }
                            }
                        }
                    }
                    zip.close();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            else
            {
                // System.err.println("No Resource Pack " + pack);
            }
        }
        addedPokemon = toAdd;
    }

    private void registerMob(String mob)
    {
        PokedexEntry e;
        if ((e = Database.getEntry(mob)) != null)
        {
            if (textureProviders.containsKey(e.getTrimmedName().toLowerCase(Locale.ENGLISH)))
            {
                e.setModId(textureProviders.get(e.getTrimmedName().toLowerCase(Locale.ENGLISH)));
            }
            else if (e.getBaseForme() != null
                    && textureProviders.containsKey(e.getBaseForme().getTrimmedName().toLowerCase(Locale.ENGLISH)))
            {
                e.setModId(textureProviders.get(e.getBaseForme().getTrimmedName().toLowerCase(Locale.ENGLISH)));
            }
            else
            {
                e.setModId(ID);
            }
            if (e.getBaseForme() == null) PokecubeMod.core.registerPokemon(true, this, e);
            else Pokedex.getInstance().getRegisteredEntries().add(e);
        }
        else if (e == null)
        {
            System.err.println("Failed to register " + mob);
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
