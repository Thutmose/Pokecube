package pokecube.core.asm;

import java.util.Map;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import pokecube.core.interfaces.PokecubeMod;

@IFMLLoadingPlugin.Name(value = PokecubeMod.ID)
@IFMLLoadingPlugin.TransformerExclusions(value = {"pokecube.core.asm."})
public class PokecubeLoadingPlugin implements IFMLLoadingPlugin
{

    @Override
    public String[] getASMTransformerClass()
    {
        return new String[]{};
    }

    @Override
    public String getModContainerClass()
    {
        return "pokecube.core.asm.PokecubeCoreContainer";
    }

    @Override
    public String getSetupClass()
    {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data)
    {
    }

    @Override
    public String getAccessTransformerClass()
    {
        return null;
    }
}