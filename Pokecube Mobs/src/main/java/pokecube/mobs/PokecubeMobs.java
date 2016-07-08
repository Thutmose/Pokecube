package pokecube.mobs;

import java.util.Map;

import com.google.common.collect.Maps;

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
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.PokedexEntry.EvolutionData;
import pokecube.modelloader.IMobProvider;
import pokecube.modelloader.ModPokecubeML;
import thut.core.client.ClientProxy;

@Mod(modid = PokecubeMobs.MODID, name = "Pokecube Mobs", version = PokecubeMobs.VERSION, dependencies = "required-after:pokecube", updateJSON = PokecubeMobs.UPDATEURL, acceptableRemoteVersions = "*", acceptedMinecraftVersions = PokecubeMobs.MCVERSIONS)
public class PokecubeMobs implements IMobProvider
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
            if (event.player.getEntityWorld().isRemote
                    && event.player == FMLClientHandler.instance().getClientPlayerEntity())
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

    Map<PokedexEntry, Integer> genMap     = Maps.newHashMap();
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

    @Override
    public String getModelDirectory(PokedexEntry entry)
    {
        int gen = getGen(entry);
        switch (gen)
        {
        case 1:
            return "Gen_1/models/";
        case 2:
            return "Gen_2/models/";
        case 3:
            return "Gen_3/models/";
        case 4:
            return "Gen_4/models/";
        case 5:
            return "Gen_5/models/";
        case 6:
            return "Gen_6/models/";
        case 7:
            return "Gen_7/models/";
        }
        return "models/";
    }

    private int getGen(PokedexEntry entry)
    {
        int gen;
        if (genMap.containsKey(entry))
        {
            gen = genMap.get(entry);
        }
        else
        {
            gen = entry.getGen();
            for (EvolutionData d : entry.getEvolutions())
            {
                int gen1 = d.evolution.getGen();
                if (genMap.containsKey(d.evolution))
                {
                    gen1 = genMap.get(d.evolution);
                }
                if (gen1 < gen)
                {
                    gen = gen1;
                }
                for (EvolutionData d1 : d.evolution.getEvolutions())
                {
                    gen1 = d1.evolution.getGen();
                    if (genMap.containsKey(d1.evolution))
                    {
                        gen1 = genMap.get(d1.evolution);
                    }
                    if (d.evolution == entry && gen1 < gen)
                    {
                        gen = gen1;
                    }
                }
            }
            for (PokedexEntry e : Database.allFormes)
            {
                int gen1 = e.getGen();
                if (genMap.containsKey(e))
                {
                    gen1 = genMap.get(e);
                }
                for (EvolutionData d : e.getEvolutions())
                {
                    if (d.evolution == entry && gen1 < gen)
                    {
                        gen = gen1;
                    }
                }
            }
            genMap.put(entry, gen);
        }
        return gen;
    }

    @Override
    public String getTextureDirectory(PokedexEntry entry)
    {
        int gen = getGen(entry);
        switch (gen)
        {
        case 1:
            return "Gen_1/textures/";
        case 2:
            return "Gen_2/textures/";
        case 3:
            return "Gen_3/textures/";
        case 4:
            return "Gen_4/textures/";
        case 5:
            return "Gen_5/textures/";
        case 6:
            return "Gen_6/textures/";
        case 7:
            return "Gen_7/textures/";
        }
        return "textures/entities/";
    }

    @Override
    public Object getMod()
    {
        return this;
    }
}
