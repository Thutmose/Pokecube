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
import net.minecraftforge.client.event.sound.SoundLoadEvent;
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
import pokecube.modelloader.client.custom.animation.AnimationLoader;
import pokecube.modelloader.items.ItemModelReloader;

@Mod(modid = ModPokecubeML.ID, name = "Pokecube Model Loader", version = "0.1.0", acceptedMinecraftVersions = PokecubeMod.MCVERSIONS)
public class ModPokecubeML
{
    /** The id of your mod */
    public final static String ID = "pokecube_ml";

    @Instance(ID)
    public static ModPokecubeML instance;

    public static ArrayList<String>         addedPokemon;
    public static Map<PokedexEntry, String> textureProviders = Maps.newHashMap();

    public static boolean info    = false;
    public static boolean preload = true;

    @SidedProxy(clientSide = "pokecube.modelloader.client.ClientProxy", serverSide = "pokecube.modelloader.CommonProxy")
    public static CommonProxy proxy;
    public static File        configDir;

    private void doMetastuff()
    {
        ModMetadata meta = FMLCommonHandler.instance().findContainerFor(this).getMetadata();
        meta.parent = PokecubeMod.ID;
    }

    /** This function is called by Forge at initialization.
     * 
     * @param evt */
    @EventHandler
    public void preInit(FMLPreInitializationEvent evt)
    {
        Configuration config = PokecubeMod.core.getPokecubeConfig(evt);
        proxy.registerModelProvider(ID, this);
        proxy.preInit();
        doMetastuff();
        config.load();
        String[] pokemon = config.getStringList("pokemon", Configuration.CATEGORY_GENERAL, new String[] { "Zubat" },
                "extra Pokemobs to register on load");
        info = config.getBoolean("printAll", Configuration.CATEGORY_GENERAL, info,
                "will print all pokemon names to console on load");
        preload = config.getBoolean("preloadModels", Configuration.CATEGORY_GENERAL, preload,
                "Will load all of the models when refreshed, if this is false, it will only load the model when it is first seen in game.");
        String[] files = config.getStringList("packs", Configuration.CATEGORY_GENERAL,
                new String[] { "Pokecube_Resources", "Gen_1", "Gen_2", "Gen_3", "Gen_4", "Gen_5", "Gen_6" },
                "Resource Packs to add models");
        config.save();
        configDir = evt.getModConfigurationDirectory();
        ArrayList<String> toAdd = Lists.newArrayList(pokemon);

        File resourceDir = new File(ModPokecubeML.configDir.getParent(), "resourcepacks");

        String modelDir = "assets/pokecube_ml/models/pokemobs/";
        for (String file : files)
        {
            File pack = new File(resourceDir, file + ".zip");
            if (pack.exists())
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
//                System.err.println("No Resource Pack " + pack);
            }
        }

        addedPokemon = toAdd;

        GameRegistry.registerItem(
                new ItemModelReloader().setUnlocalizedName("modelreloader").setCreativeTab(CreativeTabs.tabTools),
                "modelreloader");
        MinecraftForge.EVENT_BUS.register(this);
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

        for (String s : addedPokemon)
        {
            PokedexEntry e;
            if ((e = Database.getEntry(s)) != null)
            {
                PokecubeMod.core.registerPokemon(true, this, s);
                if (textureProviders.containsKey(e))
                {
                    e.setModId(textureProviders.get(e));
                }
            }
        }
        proxy.registerRenderInformation();
        NetworkRegistry.INSTANCE.registerGuiHandler(this, proxy);
    }

    @EventHandler
    private void postInit(FMLPostInitializationEvent evt)
    {
        proxy.postInit();
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void SoundLoad(SoundLoadEvent e)
    {
        AnimationLoader.load();
    }
}
