package pokecube.core.client.gui.config;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;
import pokecube.core.handlers.Config;
import pokecube.core.interfaces.PokecubeMod;

public class ModGuiConfig extends GuiConfig
{
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

    public ModGuiConfig(GuiScreen guiScreen)
    {
        super(guiScreen, getConfigElements(), PokecubeMod.ID, false, false,
                GuiConfig.getAbridgedConfigPath(Config.instance.getConfigFile().getAbsolutePath()));
    }
}
