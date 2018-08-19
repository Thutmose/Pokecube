package pokecube.adventures.world;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.logging.Level;

import com.google.common.collect.Lists;

import pokecube.core.database.Database;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.world.gen.template.PokecubeTemplates;

public class WorldGenInits
{
    public static final List<String> FILENAMES = Lists.newArrayList();

    public static void init()
    {
        for (String s : FILENAMES)
        {
            try
            {
                copyFile(s);
            }
            catch (IOException e)
            {
                PokecubeMod.log(Level.WARNING, "Error loading " + s, e);
            }
        }
    }

    private static void copyFile(String fileName) throws IOException
    {
        String defaults = "/assets/pokecube_adventures/structures/";
        String name = defaults + fileName + ".nbt";
        File file = new File(PokecubeTemplates.TEMPLATES, fileName + ".nbt");
        if (file.exists()) return;
        InputStream res = (Database.class).getResourceAsStream(name);
        Files.copy(res, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }
}
