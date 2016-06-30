package pokecube.mobs;

import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.common.ForgeVersion.CheckResult;
import net.minecraftforge.common.ForgeVersion.Status;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.modelloader.ModPokecubeML;
import thut.core.client.ClientProxy;

@Mod(modid = PokecubeMobs.MODID, name = "Pokecube Mobs", version = PokecubeMobs.VERSION, dependencies = "required-after:pokecube", updateJSON = PokecubeMobs.UPDATEURL, acceptableRemoteVersions = "*", acceptedMinecraftVersions = PokecubeMobs.MCVERSIONS)
public class PokecubeMobs
{
    public static class UpdateNotifier
    {
        public UpdateNotifier()
        {
            MinecraftForge.EVENT_BUS.register(this);
        }

        @SubscribeEvent
        public void onPlayerJoin(TickEvent.PlayerTickEvent event)
        {
            if (event.player.getEntityWorld().isRemote && event.player == FMLClientHandler.instance().getClientPlayerEntity())
            {
                MinecraftForge.EVENT_BUS.unregister(this);
                Object o = Loader.instance().getIndexedModList().get(PokecubeMobs.MODID);
                CheckResult result = ForgeVersion.getResult(((ModContainer) o));
                if (result.status == Status.OUTDATED)
                {
                    ITextComponent mess = ClientProxy.getOutdatedMessage(result, "Pokecube Mobs");
                    (event.player).addChatMessage(mess);
                }
            }
        }
    }
    public static final String MODID      = "pokecube_mobs";
    public static final String VERSION    = "@VERSION@";
    public static final String UPDATEURL  = "https://gist.githubusercontent.com/Thutmose/4d7320c36696cd39b336/raw/mobs.json";

    public final static String MCVERSIONS = "[1.9.4]";

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        if (event.getSide() == Side.CLIENT) new UpdateNotifier();
        ModPokecubeML.proxy.registerModelProvider(MODID, this);
    }
}
