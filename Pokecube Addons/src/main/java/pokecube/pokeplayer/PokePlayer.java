package pokecube.pokeplayer;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod( // @formatter:off
        modid = PokePlayer.ID, 
        name = "Pokecube Mystery Dungeon", 
        version = PokePlayer.version, 
        dependencies = PokePlayer.DEPSTRING, 
      //  guiFactory = "pokecube.adventures.client.gui.config.ModGuiFactory", 
      //  updateJSON = PokePlayer.UPDATEURL, 
        acceptedMinecraftVersions = PokePlayer.MCVERSIONS
        )// @formatter:on
public class PokePlayer
{
    public static final String ID         = "pokeplayer";
    public static final String version    = "@VERSION";
    public final static String MCVERSIONS = "@MCVERSION";
    public final static String DEPSTRING  = "required-after:pokecube@@POKECUBEVERSION";
    public final static String UPDATEURL  = "https://gist.githubusercontent.com/Thutmose/4d7320c36696cd39b336/raw/pokeplayer.json";

    @SidedProxy(clientSide = "pokecube.pokeplayer.client.ProxyClient", serverSide = "ppokecube.pokeplayer.Proxy")
    public static Proxy        proxy;

    @EventHandler
    public void load(FMLInitializationEvent evt)
    {
        MinecraftForge.EVENT_BUS.register(proxy);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent e)
    {

    }

    @SubscribeEvent
    public void interactEvent(PlayerInteractEvent event)
    {

    }
}
