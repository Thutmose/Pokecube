package pokecube.modelloader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.ProgressManager;
import net.minecraftforge.fml.common.ProgressManager.ProgressBar;
import net.minecraftforge.fml.common.network.IGuiHandler;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.modelloader.CommonProxy.CachedLocs.CachedLoc;
import pokecube.modelloader.common.Config;
import pokecube.modelloader.common.ExtraDatabase;
import thut.core.client.render.model.ModelFactory;

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

    static class CachedLocs
    {
        String                    modid;
        boolean                   has[];
        Map<String, List<String>> xmls = Maps.newHashMap();
        Set<CachedLoc>            locs = Sets.newHashSet();

        public CachedLocs(String modid)
        {
            this.modid = modid;
        }

        public void save()
        {
            File cacheFile = new File(CACHEPATH + modid + ".json");
            String output = prettyGson.toJson(this);
            try
            {
                if (PokecubeMod.debug) PokecubeMod.log("Saving " + cacheFile);
                cacheFile.getParentFile().mkdirs();
                FileWriter writer = new FileWriter(cacheFile);
                writer.append(output);
                writer.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        public boolean valid()
        {
            if (!PokecubeMod.core.getConfig().useCache) return false;
            if (locs.isEmpty()) return false;
            for (CachedLoc loc : locs)
                if (!loc.stillValid()) return false;
            return true;
        }

        static class CachedLoc
        {
            static Map<String, String> checksums = Maps.newHashMap();

            String                     checksum;
            String                     file;

            public CachedLoc(String file)
            {
                this.file = file;
                try
                {
                    this.checksum = computeChecksum(file);
                }
                catch (NoSuchAlgorithmException | IOException e)
                {
                    throw new RuntimeException("Error with file? " + file + " " + e);
                }
            }

            private String computeChecksum(String file2) throws NoSuchAlgorithmException, IOException
            {
                if (checksums.containsKey(file2)) return checksums.get(file2);
                MessageDigest digest = MessageDigest.getInstance("SHA1");
                InputStream stream = FileUtils.openInputStream(new File(file2));
                digest.update(IOUtils.toByteArray(stream));
                String sum = new String(digest.digest());
                checksums.put(file2, sum);
                return sum;
            }

            public boolean stillValid()
            {
                try
                {
                    String newChecksum = computeChecksum(file);
                    return newChecksum.equals(checksum);
                }
                catch (FileNotFoundException noFile)
                {
                    return false;
                }
                catch (NoSuchAlgorithmException | IOException e)
                {
                    e.printStackTrace();
                }
                return false;
            }

            @Override
            public boolean equals(Object other)
            {
                if (other instanceof CachedLoc) { return other.toString().equals(toString()); }
                return false;
            }

            @Override
            public String toString()
            {
                return file;
            }
        }
    }

    private static final Gson                        gson           = new Gson();
    private static final Gson                        prettyGson     = new GsonBuilder().setPrettyPrinting().create();
    private static final String                      CACHEPATH      = ModPokecubeML.ID + File.separator;

    public static HashMap<String, Object>            modelProviders = Maps.newHashMap();
    public static HashMap<String, IMobProvider>      mobProviders   = Maps.newHashMap();
    public static HashMap<String, ArrayList<String>> modModels      = Maps.newHashMap();
    private static Map<String, CachedLocs>           fileCache      = Maps.newHashMap();
    public static Set<File>                          validModJars   = Sets.newHashSet();

    private static final char                        DOT            = '.';

    private static final char                        SLASH          = '/';

    HashMap<String, XMLLocs>                         xmlFiles       = Maps.newHashMap();
    public Map<String, String>                       notFound       = Maps.newHashMap();
    public Set<String>                               found          = Sets.newHashSet();

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

    private void checkInFolder(File resourceDir, boolean[] ret, ResourceLocation[] files, Set<String> fileNames)
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
                    fileNames.add(folder.toString());
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
                    fileNames.add(resourceDir.toString());
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

    List<String> fileAsList(Object mod, ResourceLocation file) throws Exception
    {
        CachedLocs cached = getCached((String) mod);
        if (cached.xmls.containsKey(file.toString())) { return cached.xmls.get(file.toString()); }
        ArrayList<String> toFill = Lists.newArrayList();
        String name = file.toString();
        XMLLocs locations = xmlFiles.get(name);
        outer:
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
                    break outer;
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

                break outer;
            }
        }
        cached.xmls.put(file.toString(), toFill);
        return toFill;
    }

    private void filesExist(Object mod, boolean[] ret, ResourceLocation[] file, Set<String> files)
            throws UnsupportedEncodingException
    {
        File resourceDir = new File(ModPokecubeML.configDir.getParent(), "resourcepacks");
        // Check Resource Packs
        if (ModPokecubeML.checkResourcesForModels) checkInFolder(resourceDir, ret, file, files);

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
            if (PokecubeMod.debug) PokecubeMod.log(Level.INFO, "Checking in " + resourceDir + " " + mod);
            checkInFolder(resourceDir, ret, file, files);
        }
        else
        {
            resourceDir = new File(ModPokecubeML.configDir.getParent(), "mods");
            if (PokecubeMod.debug) PokecubeMod.log(Level.INFO, "Checking in " + validModJars);
            for (File temp : validModJars)
            {
                checkInFolder(temp, ret, file, files);
            }
        }
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
        if (PokecubeMod.debug) PokecubeMod.log("Searching for Models...");
        ArrayList<String> toAdd = ModPokecubeML.addedPokemon;
        if (toAdd == null)
        {
            Thread.dumpStack();
            return;
        }
        ArrayList<String> entries = Lists.newArrayList();
        for (PokedexEntry entry : Database.getSortedFormes())
        {
            String name = entry.getTrimmedName();
            entries.add(name);
        }
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
        Collections.sort(modList, Config.instance.modIdComparator);
        xmlFiles.clear();
        for (String modId : modList)
        {
            bar.step(modId);
            IMobProvider mod = mobProviders.get(modId);
            boolean[] hasArr = providesModels(modId, mod, entryArr);
            ProgressBar bar2 = ProgressManager.push("Pokemob", hasArr.length);
            for (int i = 0; i < hasArr.length; i++)
            {
                String entry = entryArr[i];
                PokedexEntry pokeentry = Database.getEntry(entry);
                if (!hasArr[i] || has[i])
                {
                    bar2.step("skip");
                    if (!found.contains(entry) && !pokeentry.dummy)
                    {
                        String path = modId + ":" + mod.getModelDirectory(pokeentry);
                        if (notFound.containsKey(entry)) path = notFound.get(entry) + ", " + path;
                        notFound.put(entry, path);
                    }
                    continue;
                }
                found.add(entry);
                notFound.remove(entry);
                bar2.step(entry);
                if (!toAdd.contains(entry)) toAdd.add(entry);
                ModPokecubeML.textureProviders.put(entry, modId);
                ResourceLocation xml = new ResourceLocation(modId, mod.getModelDirectory(pokeentry) + entry + ".xml");
                pokeentry.texturePath = mod.getTextureDirectory(pokeentry);
                pokeentry.setModId(modId);
                try
                {
                    List<String> list = fileAsList(modId, xml);
                    if (!list.isEmpty())
                    {
                        ExtraDatabase.addXMLEntry(modId, entry, list);
                    }
                }
                catch (Exception e)
                {
                    PokecubeMod.log(Level.WARNING, "Error with finding XML for " + entry, e);
                }
            }
            getCached(modId, true).save();
            ProgressManager.pop(bar2);
        }
        ProgressManager.pop(bar);
        CachedLoc.checksums.clear();
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

    public CachedLocs getCached(String modid, boolean create)
    {
        CachedLocs cache = fileCache.get(modid);
        if (cache == null || (create && !cache.valid()))
        {
            cache = loadOrCreateCache(modid);
            if (create || cache.valid())
            {
                fileCache.put(modid, cache);
            }
            else
            {
                fileCache.remove(modid);
                cache = null;
            }
        }
        return cache;
    }

    private CachedLocs loadOrCreateCache(String modid)
    {
        File cacheFile = new File(CACHEPATH + modid + ".json");
        if (cacheFile.exists())
        {
            try
            {
                FileReader reader = new FileReader(cacheFile);
                CachedLocs cache = gson.fromJson(reader, CachedLocs.class);
                if (cache.valid()) return cache;
            }
            catch (JsonSyntaxException | JsonIOException | FileNotFoundException e)
            {
                PokecubeMod.log(Level.WARNING, "Error with cache " + cacheFile, e);
            }
        }
        return new CachedLocs(modid);
    }

    private CachedLocs getCached(String modid)
    {
        return getCached(modid, false);
    }

    boolean[] providesModels(String modid, Object mod, String... entry)
    {
        CachedLocs cached = getCached(modid, true);
        if (!PokecubeMod.debug && cached.has != null && cached.has.length == entry.length) { return cached.has; }
        ResourceLocation[] tex;
        boolean[] ret = new boolean[entry.length];
        Set<String> files = Sets.newHashSet();
        try
        {
            tex = toLocations(modid, ".xml", entry);
            filesExist(mod, ret, tex, files);
            List<String> extensions = Lists.newArrayList(ModelFactory.getValidExtensions());
            Collections.sort(extensions, Config.instance.extensionComparator);
            for (String ext : extensions)
            {
                tex = toLocations(modid, "." + ext, entry);
                filesExist(mod, ret, tex, files);
            }
            tex = toLocations(modid, ".tbl", entry);
            filesExist(mod, ret, tex, files);
        }
        catch (UnsupportedEncodingException e)
        {
            PokecubeMod.log(Level.WARNING, "Error with model check " + modid + " " + entry, e);
        }
        cached.has = ret.clone();
        for (String file : files)
        {
            cached.locs.add(new CachedLoc(file));
        }
        cached.save();
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

    public void reloadModel(PokedexEntry model)
    {

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
