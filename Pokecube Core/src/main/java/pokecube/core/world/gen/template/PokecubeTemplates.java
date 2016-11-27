package pokecube.core.world.gen.template;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraft.world.gen.structure.template.TemplateManager;
import net.minecraftforge.fml.common.FMLCommonHandler;
import pokecube.core.database.Database;

public class PokecubeTemplates
{
    public static final String                 POKECENTER = "pokecenter";
    public static final String                 POKEMART   = "pokemart";
    public static final String                 RUIN_1     = "ruin_1";

    public static String                       TEMPLATES;

    private static TemplateManager             MANAGER;
    final static Map<String, PokecubeTemplate> templates  = Maps.newHashMap();

    public static TemplateManager getManager()
    {
        return MANAGER;
    }

    public static void serverInit(MinecraftServer server)
    {
        MANAGER = new TemplateManager(TEMPLATES, server.getDataFixer());
        Template t = getManager().getTemplate(server, new ResourceLocation(POKECENTER));
        PokecubeTemplate template = new PokecubeTemplate(t, POKECENTER);
        templates.put(POKECENTER, template);
        t = getManager().getTemplate(server, new ResourceLocation(POKEMART));
        template = new PokecubeTemplate(t, POKEMART);
        templates.put(POKEMART, template);
        t = getManager().func_189942_b(server, new ResourceLocation(RUIN_1));
        template = new PokecubeTemplate(t, RUIN_1);
        templates.put(RUIN_1, template);
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
                copyFile(POKEMART);
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
            ret = new PokecubeTemplate(t, name);
            templates.put(name, ret);
        }
        return ret;
    }

}
