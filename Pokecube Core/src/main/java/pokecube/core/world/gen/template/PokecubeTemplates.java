package pokecube.core.world.gen.template;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraft.world.gen.structure.template.TemplateManager;
import net.minecraftforge.fml.common.FMLCommonHandler;
import pokecube.core.database.Database;

public class PokecubeTemplates
{
    public static final String                 POKECENTER      = "pokecenter";
    public static final String                 RUIN_1          = "ruin_1";

    public static String                       TEMPLATES;

    private static TemplateManager             MANAGER;
    final static Map<String, PokecubeTemplate> templates       = Maps.newHashMap();
    public static final List<String>           TEMPLATESTOINIT = Lists.newArrayList(POKECENTER, RUIN_1);

    public static TemplateManager getManager()
    {
        if (MANAGER == null) MANAGER = new TemplateManager(TEMPLATES,
                FMLCommonHandler.instance().getMinecraftServerInstance().getDataFixer());
        return MANAGER;
    }

    public static void serverInit(MinecraftServer server)
    {
        for (String s : TEMPLATESTOINIT)
            initTemplate(s);
    }

    public static void initFiles()
    {
        try
        {
            File temp = new File(TEMPLATES);
            if (!temp.exists())
            {
                temp.mkdirs();
            }
            try
            {
                copyFile(POKECENTER);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            try
            {
                copyFile(RUIN_1);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private static void copyFile(String fileName) throws IOException
    {
        String defaults = "/assets/pokecube/structures/";
        String name = defaults + fileName + ".nbt";
        File file = new File(TEMPLATES, fileName + ".nbt");
        if (file.exists()) return;
        InputStream res = (Database.class).getResourceAsStream(name);
        Files.copy(res, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    public static PokecubeTemplate getTemplate(String name)
    {
        PokecubeTemplate ret = templates.get(name);
        if (ret == null)
        {
            Template t = getManager().getTemplate(FMLCommonHandler.instance().getMinecraftServerInstance(),
                    new ResourceLocation(name));
            if (t == null) return null;
            ret = new PokecubeTemplate(t, name);
            templates.put(name, ret);
        }
        if (ret != null) ret = new PokecubeTemplate(ret, name);
        return ret;
    }

    static void initTemplate(String name)
    {
        Template t = getManager().getTemplate(FMLCommonHandler.instance().getMinecraftServerInstance(),
                new ResourceLocation(name));
        if (t != null) templates.put(name, new PokecubeTemplate(t, name));
    }

    public static void clear()
    {
        templates.clear();
    }

}
