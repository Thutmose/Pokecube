package pokecube.origin;

import java.io.File;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.PokecubeCore;
import pokecube.core.client.render.entity.RenderPokemobs;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.origin.models.ModelPichu;
import pokecube.origin.models.ModelPikachu;
import pokecube.origin.models.ModelRaichu;
import thut.core.common.config.ConfigBase;
import thut.core.common.config.Configure;

@Mod(modid = PokecubeOrigin.MODID, name = "Pokecube Origin", version = PokecubeOrigin.VERSION, dependencies = "required-after:pokecube", updateJSON = PokecubeOrigin.UPDATEURL, acceptableRemoteVersions = "*", acceptedMinecraftVersions = PokecubeOrigin.MCVERSIONS)
public class PokecubeOrigin
{
    public static final String MODID      = "pokecube_origin";
    public static final String VERSION    = "@VERSION@";
    public static final String UPDATEURL  = "https://gist.githubusercontent.com/Thutmose/4d7320c36696cd39b336/raw/mobs.json";

    public final static String MCVERSIONS = "[1.9.4]";
    private Config             config;

    @EventHandler
    public void preInit(FMLPreInitializationEvent e)
    {
        config = new Config(PokecubeCore.core.getPokecubeConfig(e).getConfigFile());
    }

    @SideOnly(Side.CLIENT)
    @EventHandler
    private void postInit(FMLPostInitializationEvent evt)
    {
        if (!config.active) return;
        PokedexEntry pichu = Database.getEntry("pichu");
        PokedexEntry pikachu = Database.getEntry("pikachu");
        PokedexEntry raichu = Database.getEntry("raichu");
        pichu.setModId(MODID);
        pikachu.setModId(MODID);
        raichu.setModId(MODID);
        RenderPokemobs.addModel(pichu.getName() + "" + pichu.getModId(), new ModelPichu());
        RenderPokemobs.addModel(pikachu.getName() + "" + pikachu.getModId(), new ModelPikachu());
        RenderPokemobs.addModel(raichu.getName() + "" + raichu.getModId(), new ModelRaichu());
    }

    public static class Config extends ConfigBase
    {
        @Configure(category = "misc")
        private boolean active = true;

        public Config()
        {
            super(null);
        }

        public Config(File configFile)
        {
            super(configFile, new Config());
            MinecraftForge.EVENT_BUS.register(this);
            populateSettings();
            applySettings();
            save();
        }

        @Override
        protected void applySettings()
        {
        }
    }
}
