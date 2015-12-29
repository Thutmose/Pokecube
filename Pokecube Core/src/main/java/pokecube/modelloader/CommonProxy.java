/**
 *
 */
package pokecube.modelloader;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;

/** This class actually does nothing on server side. But its implementation on
 * client side does.
 * 
 * @author Manchou */
public class CommonProxy implements IGuiHandler
{
    private HashMap<String, Object> mobProviders = new HashMap<String, Object>();
    public static final String      MODELPATH    = "models/pokemobs/";
    /** texture folder */
    public final static String      TEXTUREPATH  = "textures/entities/";

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
    }

    public void postInit()
    {
    }

    public void init()
    {
        ArrayList<String> toAdd = ModPokecubeML.addedPokemon;
        for (String modId : mobProviders.keySet())
        {
            for (PokedexEntry entry : Database.allFormes)
            {
                String name = entry.getName();
                if (toAdd.contains(name) || !providesModel(modId, entry)) continue;
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
                    ModPokecubeML.textureProviders.put(entry, modId);
                    toAdd.add(name);
                }
            }
        }
    }

    private boolean providesModel(String modid, PokedexEntry entry)
    {
        ResourceLocation tex;
        tex = new ResourceLocation(modid, MODELPATH + entry.getName() + ".tbl");
        if (fileExists(tex)) return true;
        tex = new ResourceLocation(modid, MODELPATH + entry.getName() + ".xml");
        if (fileExists(tex)) return true;
        tex = new ResourceLocation(modid, MODELPATH + entry.getName() + ".x3d");
        if (fileExists(tex)) return true;
        return false;
    }

    private boolean fileExists(ResourceLocation file)
    {
        File resourceDir = new File(ModPokecubeML.configDir.getParent(), "resourcepacks");
        // Check Resource Packs
        if (checkInFolder(file, resourceDir)) return true;
        // Check jars.
        resourceDir = new File(ModPokecubeML.configDir.getParent(), "mods");
        if (checkInFolder(file, resourceDir)) return true;
        return false;
    }

    private boolean checkInFolder(ResourceLocation file, File resourceDir)
    {
        if(!resourceDir.exists()) return false;
        for (File folder : resourceDir.listFiles())
        {
            if (folder.isDirectory())
            {
                File f = new File(folder,
                        "assets" + File.separator + file.getResourceDomain() + File.separator + file.getResourcePath());
                if (f.exists()) { return true; }
            }
            else if (folder.getName().contains(".zip") || folder.getName().contains(".jar"))
            {

                try
                {
                    ZipFile zip = new ZipFile(folder);
                    Enumeration<? extends ZipEntry> entries = zip.entries();
                    int n = 0;
                    while (entries.hasMoreElements() && n < 10)
                    {
                        ZipEntry entry = entries.nextElement();
                        String s = entry.getName();
                        if (s.contains(file.getResourceDomain()) && s.endsWith(file.getResourcePath())) { return true; }
                    }
                    zip.close();
                }
                catch (Exception e)
                {
                    if (!folder.getName().contains(".jar")) e.printStackTrace();
                }

            }
        }
        return false;
    }

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
    {
        // TODO Auto-generated method stub
        return null;
    }
}
