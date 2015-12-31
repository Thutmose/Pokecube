package pokecube.mobs;

import net.minecraft.util.ChatComponentText;
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
import pokecube.modelloader.ModPokecubeML;

@Mod(modid = PokecubeMobs.MODID, name = "Pokecube Mobs", version = PokecubeMobs.VERSION, dependencies = "required-after:pokecube", updateJSON = PokecubeMobs.UPDATEURL, acceptableRemoteVersions = "*", acceptedMinecraftVersions = PokecubeMobs.MCVERSIONS)
public class PokecubeMobs
{
    public static final String MODID      = "pokecube_mobs";
    public static final String VERSION    = "@VERSION@";
    public static final String UPDATEURL  = "https://raw.githubusercontent.com/Thutmose/Pokecube/master/Pokecube%20Mobs/versions.json";
    public final static String MCVERSIONS = "[1.8.8,1.8.9]";

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        new UpdateNotifier();
        ModPokecubeML.proxy.registerModelProvider(MODID, this);
    }

    public static class UpdateNotifier
    {
        public UpdateNotifier()
        {
            MinecraftForge.EVENT_BUS.register(this);
        }

        @SubscribeEvent
        public void onPlayerJoin(TickEvent.PlayerTickEvent event)
        {
            if (event.player.worldObj.isRemote && event.player == FMLClientHandler.instance().getClientPlayerEntity())
            {
                MinecraftForge.EVENT_BUS.unregister(this);

                Object o = Loader.instance().getIndexedModList().get(PokecubeMobs.MODID);
                CheckResult result = ForgeVersion.getResult(((ModContainer) o));
                if (result.status == Status.OUTDATED)
                {
                    String mess = "Current Listed Release Version of Pokecube Mobs is " + result.target
                            + ", but you have " + PokecubeMobs.VERSION + ".";
                    mess += "\nIf you find bugs, please update and check if they still occur before reporting them.";
                    (event.player).addChatMessage(new ChatComponentText(mess));
                }

            }
        }
    }
}
