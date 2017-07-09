package pokecube.core.client.gui.config;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.config.GuiConfig;
import pokecube.core.handlers.Config;
import pokecube.core.interfaces.PokecubeMod;

public class ModGuiConfig extends GuiConfig
{
    public ModGuiConfig(GuiScreen guiScreen)
    {
        super(guiScreen, Config.getConfigElements(Config.instance), PokecubeMod.ID, false, false,
                GuiConfig.getAbridgedConfigPath(Config.instance.getConfigFile().getAbsolutePath()));
    }
}
