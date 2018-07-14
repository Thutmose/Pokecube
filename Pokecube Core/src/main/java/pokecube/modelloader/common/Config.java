package pokecube.modelloader.common;

import java.io.File;
import java.util.Comparator;
import java.util.Set;

import com.google.common.collect.Sets;

import net.minecraftforge.common.MinecraftForge;
import pokecube.modelloader.ModPokecubeML;
import thut.core.common.config.ConfigBase;
import thut.core.common.config.Configure;

public class Config extends ConfigBase
{
    public static Config      instance;

    @Configure(category = "loading")
    public String[]           priorityOrder             = { "pokecube_ml", "pokecube_mobs" };

    @Configure(category = "loading")
    public String[]           modelPriority             = { "x3d" };

    @Configure(category = "loading")
    public boolean            preload                   = false;

    @Configure(category = "loading")
    public String[]           preloadedMobs             = {};

    @Configure(category = "loading")
    public boolean            checkResourcePacksForMobs = true;

    public Comparator<String> modIdComparator;
    public Comparator<String> extensionComparator;
    public Set<String>        toPreload                 = Sets.newHashSet();

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
                if (o2.equals("pokecube_mno")) return Integer.MIN_VALUE;
                if (o1.equals("pokecube_mno")) return Integer.MAX_VALUE;
                for (int i = 0; i < priorityOrder.length; i++)
                {
                    if (o1.equals(priorityOrder[i])) prioro1 = i;
                    if (o2.equals(priorityOrder[i])) prioro2 = i;
                }
                return prioro1 - prioro2;
            }
        };
        extensionComparator = new Comparator<String>()
        {
            @Override
            public int compare(String o1, String o2)
            {
                int prioro1 = 0;
                int prioro2 = 0;
                for (int i = 0; i < modelPriority.length; i++)
                {
                    if (o1.equals(modelPriority[i])) prioro1 = i;
                    if (o2.equals(modelPriority[i])) prioro2 = i;
                }
                if ((prioro1 - prioro2) == 0) return o1.compareTo(o2);
                return prioro1 - prioro2;
            }
        };

        toPreload.clear();
        for (String s : preloadedMobs)
            toPreload.add(s);
        ModPokecubeML.preload = preload;
        ModPokecubeML.checkResourcesForModels = checkResourcePacksForMobs;
    }
}
