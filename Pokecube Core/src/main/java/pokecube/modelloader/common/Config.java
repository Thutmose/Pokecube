package pokecube.modelloader.common;

import java.io.File;
import java.util.Comparator;

import net.minecraftforge.common.MinecraftForge;
import thut.core.common.config.ConfigBase;
import thut.core.common.config.Configure;

public class Config extends ConfigBase
{
    public static Config      instance;

    @Configure(category = "loading")
    public String[]           priorityOrder = { "pokecube_ml", "pokecube_mobs" };

    public Comparator<String> modIdComparator;

    public Config()
    {
        super(null);
    }

    public Config(File path)
    {
        super(path, new Config());
        MinecraftForge.EVENT_BUS.register(this);
        populateSettings();
        applySettings();
        instance = this;
        save();
    }

    @Override
    protected void applySettings()
    {
        modIdComparator = new Comparator<String>()
        {
            @Override
            public int compare(String o1, String o2)
            {
                int prioro1 = Integer.MAX_VALUE;
                int prioro2 = Integer.MIN_VALUE;
                for (int i = 0; i < priorityOrder.length; i++)
                {
                    if (o1.equals(priorityOrder[i])) prioro1 = i;
                    if (o2.equals(priorityOrder[i])) prioro2 = i;
                }
                return prioro1 - prioro2;
            }
        };

        // TODO checkResourcesForModels =
        // config.getBoolean("checkForResourcepacks", "General", true,
    }
}
