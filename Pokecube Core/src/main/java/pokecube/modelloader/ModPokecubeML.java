/**
 * 
 */
package pokecube.modelloader;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.creativetab.CreativeTabs;
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
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.modelloader.client.render.AnimationLoader;
import pokecube.modelloader.common.ExtraDatabase;
import pokecube.modelloader.items.ItemModelReloader;

@Mod(modid = ModPokecubeML.ID, name = "Pokecube Model Loader", version = "0.1.0", acceptedMinecraftVersions = PokecubeMod.MCVERSIONS)
public class ModPokecubeML
{
    /** The id of your mod */
    public final static String        ID                      = "pokecube_ml";

    @Instance(ID)
    public static ModPokecubeML       instance;

    public static boolean             checkResourcesForModels = true;

    public static ArrayList<String>   addedPokemon            = Lists.newArrayList();
    public static Map<String, String> textureProviders        = Maps.newHashMap();

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
            loadMob(s);
        }
        ExtraDatabase.apply();
        for (String s : addedPokemon)
        {
            registerMob(s);
        }
        NetworkRegistry.INSTANCE.registerGuiHandler(this, proxy);
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
        config.load();
        checkResourcesForModels = config.getBoolean("checkForResourcepacks", "General", true,
                "Disabling this will prevent Pokecube from checking resource packs for models, it might speed up loading times.");
        config.save();

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

        GameRegistry.registerItem(
                new ItemModelReloader().setUnlocalizedName("modelreloader").setCreativeTab(CreativeTabs.tabTools),
                "modelreloader");
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void processResources()
    {
        ArrayList<String> toAdd = Lists.newArrayList();

        File resourceDir = new File(ModPokecubeML.configDir.getParent(), "resourcepacks");

        String modelDir = "assets/pokecube_ml/models/pokemobs/";

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

    private void loadMob(String mob)
    {
        if (textureProviders.containsKey(mob) && !textureProviders.get(mob).equals(ID)) return;

        ArrayList<String> list = Lists.newArrayList();
        ResourceLocation xml = new ResourceLocation(ModPokecubeML.ID, CommonProxy.MODELPATH + mob + ".xml");
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

    private void registerMob(String mob)
    {
        PokedexEntry e;
        if ((e = Database.getEntry(mob)) != null && e.baseForme == null)
        {
            if (textureProviders.containsKey(e.getName()))
            {
                e.setModId(textureProviders.get(e.getName()));
            }
            else
            {
                e.setModId(ID);
            }
            if (e.baseForme == null) PokecubeMod.core.registerPokemon(true, this, e);
        }
        else
        {
            System.err.println("Failed to register " + mob);
        }
    }
}
