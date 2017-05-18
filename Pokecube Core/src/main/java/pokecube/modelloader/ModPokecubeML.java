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

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.database.Database;
import pokecube.core.database.Pokedex;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.modelloader.client.render.AnimationLoader;
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
    public static File                configDir;

    boolean                           postInit                = false;

    private void doMetastuff()
    {
        ModMetadata meta = FMLCommonHandler.instance().findContainerFor(this).getMetadata();
        meta.parent = PokecubeMod.ID;
    }

    @EventHandler
    public void init(FMLInitializationEvent evt)
    {
        proxy.init();
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
        System.out.println(addedPokemon.size()+" "+addedPokemon);
        for (String s : addedPokemon)
        {
            registerMob(s.toLowerCase(Locale.ENGLISH));
        }
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

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void loadModels(ModelBakeEvent e)
    {
        if (!postInit) return;
        System.out.println("Loading Pokemob Models");
        AnimationLoader.load();
    }

    @EventHandler
    private void postInit(FMLPostInitializationEvent evt)
    {
        proxy.postInit();
        for (PokedexEntry e : Database.allFormes)
        {
            if (e.getBaseForme() != null && e.texturePath.equals("textures/entities/"))
                e.texturePath = e.getBaseForme().texturePath;
        }
        postInit = true;
    }

    /** This function is called by Forge at initialization.
     * 
     * @param evt */
    @EventHandler
    public void preInit(FMLPreInitializationEvent evt)
    {
        proxy.registerModelProvider(ID, this);
        proxy.preInit();
        doMetastuff();
        configDir = evt.getModConfigurationDirectory();

        Configuration config = PokecubeMod.core.getPokecubeConfig(evt);
        new Config(config.getConfigFile());

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

        GameRegistry.register(new ItemModelReloader().setUnlocalizedName("modelreloader")
                .setRegistryName(ID, "modelreloader").setCreativeTab(PokecubeMod.creativeTabPokecube));
        MinecraftForge.EVENT_BUS.register(this);
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
