package pokecube.core.client.gui.config;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.config.GuiConfig;
import pokecube.core.handlers.Config;
import pokecube.core.interfaces.PokecubeMod;

public class ModGuiConfig extends GuiConfig
{
    public ModGuiConfig(GuiScreen guiScreen)
    {
        super(guiScreen, Config.getConfigElements(PokecubeMod.core.getConfig()), PokecubeMod.ID, false, false,
                GuiConfig.getAbridgedConfigPath(PokecubeMod.core.getConfig().getConfigFile().getAbsolutePath()));
    }
}
