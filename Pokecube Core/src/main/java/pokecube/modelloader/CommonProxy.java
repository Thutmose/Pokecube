/**
 *
 */
package pokecube.modelloader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.google.common.collect.Lists;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.network.IGuiHandler;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.modelloader.common.ExtraDatabase;

/** This class actually does nothing on server side. But its implementation on
 * client side does.
 * 
 * @author Manchou */
public class CommonProxy implements IGuiHandler
{
    public static HashMap<String, Object>            modelProviders = new HashMap<String, Object>();
    public static HashMap<String, ArrayList<String>> modModels      = new HashMap<String, ArrayList<String>>();
    private HashMap<String, Object>                  mobProviders   = new HashMap<String, Object>();
    public static final String                       MODELPATH      = "models/pokemobs/";
    /** texture folder */
    public final static String                       TEXTUREPATH    = "textures/entities/";

    public void registerModelProvider(String modid, Object mod)
    {
        if (!mobProviders.containsKey(modid)) mobProviders.put(modid, mod);
    }

    /** Client side only register stuff... */
    public void registerRenderInformation()
    {
        // unused server side. -- see ClientProxyPokecubeTemplate for
        // implementation
    }

    public void preInit()
    {
        modelProviders.put(ModPokecubeML.ID, ModPokecubeML.instance);
    }

    public void postInit()
    {
        ExtraDatabase.cleanup();
    }

    public void init()
    {
        ArrayList<String> toAdd = ModPokecubeML.addedPokemon;
        ArrayList<PokedexEntry> entries = Lists.newArrayList();
        for (PokedexEntry entry : Database.allFormes)
        {
            entries.add(entry);
        }
        PokedexEntry[] entryArr = entries.toArray(new PokedexEntry[0]);
        boolean[] has = new boolean[entryArr.length];

        for (int i = 0; i < has.length; i++)
        {
            if (entryArr[i] == null)
            {
                new Exception().printStackTrace();
                continue;
            }
            if (toAdd.contains(entryArr[i].getName()))
            {
                has[i] = true;
                ModPokecubeML.textureProviders.put(entryArr[i], ModPokecubeML.ID);
            }
        }
        for (String modId : mobProviders.keySet())
        {
            Object mod = mobProviders.get(modId);
            boolean[] hasArr = providesModels(modId, mod, entryArr);
            for (int i = 0; i < hasArr.length; i++)
            {
                if (!hasArr[i] || has[i]) continue;
                PokedexEntry entry = entryArr[i];
                toAdd.add(entry.getName());
                ModPokecubeML.textureProviders.put(entry, modId);
                ArrayList<String> list = Lists.newArrayList();
                ResourceLocation xml = new ResourceLocation(modId, MODELPATH + entry.getName() + ".xml");
                try
                {
                    fileAsList(mod, xml, list);
                    if (!list.isEmpty())
                    {
                        ExtraDatabase.addXML(entry.getName(), list);
                    }
                }
                catch (Exception e)
                {

                }
            }
        }
        ExtraDatabase.apply();
    }

    private boolean[] providesModels(String modid, Object mod, PokedexEntry... entry)
    {
        ResourceLocation[] tex;
        boolean[] ret = new boolean[entry.length];
        try
        {
            tex = toLocations(modid, ".x3d", entry);
            filesExist(mod, ret, tex);
            tex = toLocations(modid, ".xml", entry);
            filesExist(mod, ret, tex);
            tex = toLocations(modid, ".tbl", entry);
            filesExist(mod, ret, tex);
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
        return ret;
    }

    private ResourceLocation[] toLocations(String modid, String ext, PokedexEntry... entries)
    {
        ResourceLocation[] ret = new ResourceLocation[entries.length];
        for (int i = 0; i < entries.length; i++)
        {
            ret[i] = new ResourceLocation(modid, MODELPATH + entries[i].getName() + ext);
        }
        return ret;
    }

    private void filesExist(Object mod, boolean[] ret, ResourceLocation[] file) throws UnsupportedEncodingException
    {
        File resourceDir = new File(ModPokecubeML.configDir.getParent(), "resourcepacks");
        // Check Resource Packs
        checkInFolder(resourceDir, ret, file);

        // Check jars.
        String scannedPackage = mod.getClass().getPackage().getName();
        String scannedPath = scannedPackage.replace(DOT, SLASH);
        URL scannedUrl = Thread.currentThread().getContextClassLoader().getResource(scannedPath);
        if (scannedUrl == null) return;
        resourceDir = new File(java.net.URLDecoder.decode(scannedUrl.getFile(), Charset.defaultCharset().name()));
        if (resourceDir.toString().contains("file:") && resourceDir.toString().contains(".jar"))
        {
            String name = resourceDir.toString();
            name = name.replace("file:", "");
            name = name.replaceAll("(.jar)(.*)", ".jar");
            resourceDir = new File(name);
            FMLLog.getLogger().debug("Checking in " + resourceDir + " " + mod);
        }
        else resourceDir = new File(ModPokecubeML.configDir.getParent(), "mods");
        checkInFolder(resourceDir, ret, file);
    }

    void fileAsList(Object mod, ResourceLocation file, ArrayList<String> toFill) throws Exception
    {
        File resourceDir = new File(ModPokecubeML.configDir.getParent(), "resourcepacks");
        // Check Resource Packs
        if (ModPokecubeML.checkResourcesForModels && fillFromFolder(mod, resourceDir, file, toFill)) return;
        // Check jars.
        String scannedPackage = mod.getClass().getPackage().getName();
        String scannedPath = scannedPackage.replace(DOT, SLASH);
        URL scannedUrl = Thread.currentThread().getContextClassLoader().getResource(scannedPath);
        if (scannedUrl == null) return;
        resourceDir = new File(java.net.URLDecoder.decode(scannedUrl.getFile(), Charset.defaultCharset().name()));
        if (resourceDir.toString().contains("file:") && resourceDir.toString().contains(".jar"))
        {
            String name = resourceDir.toString();
            name = name.replace("file:", "");
            name = name.replaceAll("(.jar)(.*)", ".jar");
            resourceDir = new File(name);
            FMLLog.getLogger().debug("Checking in " + resourceDir + " " + mod);
        }
        else resourceDir = new File(ModPokecubeML.configDir.getParent(), "mods");
        fillFromFolder(mod, resourceDir, file, toFill);
    }

    private boolean fillFromFolder(Object mod, File resourceDir, ResourceLocation file, ArrayList<String> toFill)
            throws Exception
    {
        if (!resourceDir.exists()) return false;
        if (resourceDir.isDirectory()) for (File folder : resourceDir.listFiles())
        {
            if (folder.isDirectory())
            {
                File f = new File(folder,
                        "assets" + File.separator + file.getResourceDomain() + File.separator + file.getResourcePath());
                if (f.exists())
                {
                    FileReader reader = new FileReader(f);
                    BufferedReader br = new BufferedReader(reader);
                    String line = null;
                    while ((line = br.readLine()) != null)
                    {
                        toFill.add(line);
                    }
                    br.close();
                    return true;
                }
            }
            else if (folder.getName().contains(".zip") || folder.getName().contains(".jar"))
            {
                try
                {
                    ZipFile zip = new ZipFile(folder);
                    Enumeration<? extends ZipEntry> entries = zip.entries();
                    while (entries.hasMoreElements())
                    {
                        ZipEntry entry = entries.nextElement();
                        String s = entry.getName();
                        if (s.contains(file.getResourceDomain()) && s.endsWith(file.getResourcePath()))
                        {
                            InputStreamReader reader = new InputStreamReader(zip.getInputStream(entry));
                            BufferedReader br = new BufferedReader(reader);
                            String line = null;
                            while ((line = br.readLine()) != null)
                            {
                                toFill.add(line);
                            }
                            br.close();
                            return true;
                        }
                    }
                    zip.close();
                }
                catch (Exception e)
                {
                    if (!folder.getName().contains(".jar")) e.printStackTrace();
                }

            }
        }
        else
        {
            if (resourceDir.getName().contains(".zip") || resourceDir.getName().contains(".jar"))
            {
                try
                {
                    ZipFile zip = new ZipFile(resourceDir);
                    Enumeration<? extends ZipEntry> entries = zip.entries();
                    while (entries.hasMoreElements())
                    {
                        ZipEntry entry = entries.nextElement();
                        String s = entry.getName();
                        if (s.contains(file.getResourceDomain()) && s.endsWith(file.getResourcePath()))
                        {
                            InputStreamReader reader = new InputStreamReader(zip.getInputStream(entry));
                            BufferedReader br = new BufferedReader(reader);
                            String line = null;
                            while ((line = br.readLine()) != null)
                            {
                                toFill.add(line);
                            }
                            br.close();
                            return true;
                        }
                    }
                    zip.close();
                }
                catch (Exception e)
                {
                    if (!resourceDir.getName().contains(".jar")) e.printStackTrace();
                }

            }
        }
        return false;
    }

    private static final char DOT   = '.';

    private static final char SLASH = '/';

    private void checkInFolder(File resourceDir, boolean[] ret, ResourceLocation[] files)
    {
        if (!resourceDir.exists()) return;
        int n = 0;
        if (resourceDir.isDirectory()) for (File folder : resourceDir.listFiles())
        {
            if (folder.isDirectory())
            {
                for (n = 0; n < files.length; n++)
                {
                    ResourceLocation file = files[n];
                    if (file == null) continue;
                    File f = new File(folder, "assets" + File.separator + file.getResourceDomain() + File.separator
                            + file.getResourcePath());
                    if (f.exists())
                    {
                        ret[n] = true;
                    }
                }
            }
            else if (folder.getName().contains(".zip") || folder.getName().contains(".jar"))
            {
                try
                {
                    ZipFile zip = new ZipFile(folder);
                    Enumeration<? extends ZipEntry> entries = zip.entries();
                    while (entries.hasMoreElements())
                    {
                        ZipEntry entry = entries.nextElement();
                        String s = entry.getName();
                        for (n = 0; n < files.length; n++)
                        {
                            ResourceLocation file = files[n];
                            if (file == null) continue;
                            if (s.contains(file.getResourceDomain()) && s.endsWith(file.getResourcePath()))
                            {
                                ret[n] = true;
                            }
                        }
                    }
                    zip.close();
                }
                catch (Exception e)
                {
                    if (!folder.getName().contains(".jar")) e.printStackTrace();
                }

            }
        }
        else
        {
            if (resourceDir.getName().contains(".zip") || resourceDir.getName().contains(".jar"))
            {
                try
                {
                    ZipFile zip = new ZipFile(resourceDir);
                    Enumeration<? extends ZipEntry> entries = zip.entries();
                    while (entries.hasMoreElements())
                    {
                        ZipEntry entry = entries.nextElement();
                        String s = entry.getName();
                        for (n = 0; n < files.length; n++)
                        {
                            ResourceLocation file = files[n];
                            if (file == null) continue;
                            if (s.contains(file.getResourceDomain()) && s.endsWith(file.getResourcePath()))
                            {
                                ret[n] = true;
                            }
                        }
                    }
                    zip.close();
                }
                catch (Exception e)
                {
                    if (!resourceDir.getName().contains(".jar")) e.printStackTrace();
                }

            }
        }
    }

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
    {
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
    {
        return null;
    }

    public void populateModels()
    {
    }
}
