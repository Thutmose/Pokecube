package pokecube.modelloader.common;

import java.io.File;

import net.minecraftforge.common.MinecraftForge;
import thut.core.common.config.ConfigBase;
import thut.core.common.config.Configure;

public class Config extends ConfigBase
{
    @Configure(category = "loading")
    public String[] priorityOrder = { "pokecube_ml", "pokecube_mobs" };

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
        save();
    }

    @Override
    protected void applySettings()
    {
        // TODO Auto-generated method stub

    }
}
