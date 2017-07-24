package pokecube.modelloader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.ProgressManager;
import net.minecraftforge.fml.common.ProgressManager.ProgressBar;
import net.minecraftforge.fml.common.network.IGuiHandler;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.modelloader.common.ExtraDatabase;

/** This class actually does nothing on server side. But its implementation on
 * client side does.
 * 
 * @author Manchou */
public class CommonProxy implements IGuiHandler
{
    private static class XMLLocs
    {
        Set<ZippedLoc> jarlocs     = Sets.newHashSet();
        Set<File>      directFiles = Sets.newHashSet();
    }

    private static class ZippedLoc
    {
        File    file;
        String  subPath;
        ZipFile zip;

        public ZippedLoc(File jar, String path)
        {
            file = jar;
            subPath = path;
        }

        public void close() throws IOException
        {
            zip.close();
        }

        public InputStream getStream() throws ZipException, IOException
        {
            zip = new ZipFile(file);
            ZipEntry entry = zip.getEntry(subPath);
            return zip.getInputStream(entry);
        }
    }

    public static HashMap<String, Object>            modelProviders = Maps.newHashMap();
    public static HashMap<String, IMobProvider>      mobProviders   = Maps.newHashMap();
    public static HashMap<String, ArrayList<String>> modModels      = Maps.newHashMap();

    private static final char                        DOT            = '.';

    private static final char                        SLASH          = '/';

    HashMap<String, XMLLocs>                         xmlFiles       = Maps.newHashMap();

    private void addXML(ResourceLocation xml, Object location)
    {
        XMLLocs locs = xmlFiles.get(xml.toString());
        if (locs == null)
        {
            xmlFiles.put(xml.toString(), locs = new XMLLocs());
        }
        if (location instanceof File) locs.directFiles.add((File) location);
        else if (location instanceof ZippedLoc) locs.jarlocs.add((ZippedLoc) location);
    }

    private void checkInFolder(File resourceDir, boolean[] ret, ResourceLocation[] files)
    {
        if (!resourceDir.exists() || files.length == 0) return;
        int n = 0;
        if (resourceDir.isDirectory()) for (File folder : resourceDir.listFiles())
        {
            if (folder.isDirectory())
            {
                File subDir = new File(folder, "assets" + File.separator + files[0].getResourceDomain());
                if (!subDir.exists()) continue;
                for (n = 0; n < files.length; n++)
                {
                    ResourceLocation file = files[n];
                    if (file == null) continue;
                    File f = new File(folder, "assets" + File.separator + file.getResourceDomain() + File.separator
                            + file.getResourcePath());
                    if (f.exists())
                    {
                        if (file.getResourcePath().contains(".xml"))
                        {
                            addXML(file, f);
                        }
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
                        if (!s.contains(files[0].getResourceDomain())) continue;
                        for (n = 0; n < files.length; n++)
                        {
                            ResourceLocation file = files[n];
                            if (file == null) continue;
                            if (s.contains(file.getResourceDomain()) && s.endsWith(file.getResourcePath()))
                            {
                                if (file.getResourcePath().contains(".xml"))
                                {
                                    ZippedLoc loc = new ZippedLoc(folder, entry.getName());
                                    addXML(file, loc);
                                }
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
                        if (!s.contains(files[0].getResourceDomain())) continue;
                        for (n = 0; n < files.length; n++)
                        {
                            ResourceLocation file = files[n];
                            if (file == null) continue;
                            if (s.contains(file.getResourceDomain()) && s.endsWith(file.getResourcePath()))
                            {
                                if (file.getResourcePath().contains(".xml"))
                                {
                                    ZippedLoc loc = new ZippedLoc(resourceDir, entry.getName());
                                    addXML(file, loc);
                                }
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

    void fileAsList(Object mod, ResourceLocation file, ArrayList<String> toFill) throws Exception
    {
        String name = file.toString();
        XMLLocs locations = xmlFiles.get(name);
        if (locations != null)
        {
            // TODO sort this to allow prioritizing resources.
            for (File f : locations.directFiles)
            {
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
                    return;
                }
            }
            for (ZippedLoc f : locations.jarlocs)
            {
                Reader reader = new InputStreamReader(f.getStream());
                BufferedReader br = new BufferedReader(reader);
                String line = null;
                while ((line = br.readLine()) != null)
                {
                    toFill.add(line);
                }
                br.close();

                try
                {
                    f.close();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                return;
            }
        }
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
            FMLLog.log.debug("Checking in " + resourceDir + " " + mod);
        }
        else resourceDir = new File(ModPokecubeML.configDir.getParent(), "mods");
        checkInFolder(resourceDir, ret, file);
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
    {
        return null;
    }

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
    {
        return null;
    }

    public void init()
    {
    }

    public void searchModels()
    {
        PokecubeMod.log("Searching for Models...");
        ArrayList<String> toAdd = ModPokecubeML.addedPokemon;
        if (toAdd == null)
        {
            Thread.dumpStack();
            return;
        }
        ArrayList<String> entries = Lists.newArrayList();
        for (PokedexEntry entry : Database.allFormes)
        {
            String name = entry.getTrimmedName().toLowerCase(Locale.ENGLISH);
            entries.add(name);
        }
        Collections.sort(entries, new Comparator<String>()
        {
            @Override
            public int compare(String o1, String o2)
            {
                PokedexEntry e1 = Database.getEntry(o1);
                PokedexEntry e2 = Database.getEntry(o2);
                return e1.getPokedexNb() - e2.getPokedexNb();
            }
        });
        String[] entryArr = entries.toArray(new String[0]);
        boolean[] has = new boolean[entryArr.length];
        for (int i = 0; i < has.length; i++)
        {
            if (entryArr[i] == null)
            {
                Thread.dumpStack();
                continue;
            }
            if (toAdd.contains(entryArr[i]))
            {
                has[i] = true;
                ModPokecubeML.textureProviders.put(entryArr[i], ModPokecubeML.ID);
            }
        }

        ProgressBar bar = ProgressManager.push("Model Locations", mobProviders.size());

        List<String> modList = Lists.newArrayList(mobProviders.keySet());
        // Sort to prioritise default mod
        Collections.sort(modList, new Comparator<String>()
        {
            @Override
            public int compare(String o1, String o2)
            {
                if (o1.equals(PokecubeMod.defaultMod)) return Integer.MAX_VALUE;
                else if (o2.equals(PokecubeMod.defaultMod)) return Integer.MIN_VALUE;
                return o1.compareTo(o2);
            }
        });
        xmlFiles.clear();
        for (String modId : modList)
        {
            bar.step(modId);
            IMobProvider mod = mobProviders.get(modId);
            boolean[] hasArr = providesModels(modId, mod, entryArr);
            ProgressBar bar2 = ProgressManager.push("Pokemob", hasArr.length);
            for (int i = 0; i < hasArr.length; i++)
            {
                if (!hasArr[i] || has[i])
                {
                    bar2.step("skip");
                    continue;
                }
                String entry = entryArr[i];
                PokedexEntry pokeentry = Database.getEntry(entry);
                bar2.step(entry);
                toAdd.add(entry);
                ModPokecubeML.textureProviders.put(entry, modId);
                ArrayList<String> list = Lists.newArrayList();
                ResourceLocation xml = new ResourceLocation(modId, mod.getModelDirectory(pokeentry) + entry + ".xml");
                pokeentry.texturePath = mod.getTextureDirectory(pokeentry);
                try
                {
                    fileAsList(mod, xml, list);
                    if (!list.isEmpty())
                    {
                        ExtraDatabase.addXMLEntry(modId, entry, list);
                    }
                }
                catch (Exception e)
                {

                }
            }
            ProgressManager.pop(bar2);
        }
        ProgressManager.pop(bar);
    }

    public void populateModels()
    {
    }

    public void postInit()
    {
        ExtraDatabase.cleanup();
    }

    public void preInit()
    {
        modelProviders.put(ModPokecubeML.ID, ModPokecubeML.instance);
    }

    boolean[] providesModels(String modid, Object mod, String... entry)
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

    /** This should be called in the constructor of the IMobProvider.
     * 
     * @param modid
     * @param mod */
    public static void registerModelProvider(String modid, IMobProvider mod)
    {
        modelProviders.put(modid, mod);
        if (!mobProviders.containsKey(modid))
        {
            mobProviders.put(modid, mod);
        }
    }

    /** Client side only register stuff... */
    public void registerRenderInformation()
    {
        // unused server side. -- see ClientProxyPokecubeTemplate for
        // implementation
    }

    private ResourceLocation[] toLocations(String modid, String ext, String... entries)
    {
        ResourceLocation[] ret = new ResourceLocation[entries.length];
        IMobProvider mod = mobProviders.get(modid);
        for (int i = 0; i < entries.length; i++)
        {
            ret[i] = new ResourceLocation(modid,
                    mod.getModelDirectory(Database.getEntry(entries[i])) + entries[i] + ext);
        }
        return ret;
    }
}
