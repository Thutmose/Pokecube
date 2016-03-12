package pokecube.adventures.client.gui.config;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;
import pokecube.adventures.PokecubeAdv;
import pokecube.compat.Config;

public class ModGuiConfig extends GuiConfig
{
    public ModGuiConfig(GuiScreen guiScreen)
    {
        super(guiScreen, getConfigElements(), PokecubeAdv.ID, false, false,
                GuiConfig.getAbridgedConfigPath(Config.instance.getConfigFile().getAbsolutePath()));
    }

    private static List<IConfigElement> getConfigElements()
    {
        List<IConfigElement> list = new ArrayList<>();

        Config config = Config.instance;
        for (String cat : config.getCategoryNames())
        {
            ConfigCategory cc = config.getCategory(cat);
            if (!cc.isChild())
            {
                ConfigElement ce = new ConfigElement(cc);
                list.add(ce);
            }
        }
        return list;
    }
}
