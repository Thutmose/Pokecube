/**
 *
 */
package pokecube.modelloader;

import java.util.ArrayList;
import java.util.HashMap;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
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
        try
        {
            ResourceLocation tex = new ResourceLocation(modid, entry.getTexture((byte) 0));
            IResource res = Minecraft.getMinecraft().getResourceManager().getResource(tex);
            res.getInputStream().close();
            return true;
        }
        catch (Exception e1)
        {
            try
            {
                ResourceLocation tex = new ResourceLocation(modid, MODELPATH + entry.getName() + ".tbl");
                IResource res = Minecraft.getMinecraft().getResourceManager().getResource(tex);
                res.getInputStream().close();
                return true;
            }
            catch (Exception e2)
            {
                try
                {
                    ResourceLocation tex = new ResourceLocation(modid, MODELPATH + entry.getName() + ".xml");
                    IResource res = Minecraft.getMinecraft().getResourceManager().getResource(tex);
                    res.getInputStream().close();
                    return true;
                }
                catch (Exception e3)
                {
                    try
                    {
                        ResourceLocation tex = new ResourceLocation(modid, MODELPATH + entry.getName() + ".x3d");
                        IResource res = Minecraft.getMinecraft().getResourceManager().getResource(tex);
                        res.getInputStream().close();
                        return true;
                    }
                    catch (Exception e4)
                    {
                        try
                        {
                            ResourceLocation tex = new ResourceLocation(modid, MODELPATH + entry.getName() + ".b3d");
                            IResource res = Minecraft.getMinecraft().getResourceManager().getResource(tex);
                            res.getInputStream().close();
                            return true;
                        }
                        catch (Exception e5)
                        {

                            try
                            {
                                ResourceLocation tex = new ResourceLocation(modid, MODELPATH + entry.getName() + ".obj");
                                IResource res = Minecraft.getMinecraft().getResourceManager().getResource(tex);
                                res.getInputStream().close();
                                return true;
                            }
                            catch (Exception e6)
                            {

                            }
                        }
                    }
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
