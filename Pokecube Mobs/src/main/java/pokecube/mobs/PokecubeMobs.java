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
import pokecube.core.handlers.HeldItemHandler;
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

    public PokecubeMobs()
    {
        ModPokecubeML.scanPaths.add("assets/pokecube_mobs/gen_1/models/");
        ModPokecubeML.scanPaths.add("assets/pokecube_mobs/gen_2/models/");
        ModPokecubeML.scanPaths.add("assets/pokecube_mobs/gen_3/models/");
        ModPokecubeML.scanPaths.add("assets/pokecube_mobs/gen_4/models/");
        ModPokecubeML.scanPaths.add("assets/pokecube_mobs/gen_5/models/");
        ModPokecubeML.scanPaths.add("assets/pokecube_mobs/gen_6/models/");
        ModPokecubeML.scanPaths.add("assets/pokecube_mobs/gen_7/models/");

        HeldItemHandler.megaVariants.add("absolmega");
        HeldItemHandler.megaVariants.add("aerodactylmega");
        HeldItemHandler.megaVariants.add("aggronmega");
        HeldItemHandler.megaVariants.add("alakazammega");
        HeldItemHandler.megaVariants.add("ampharosmega");
        HeldItemHandler.megaVariants.add("banettemega");
        HeldItemHandler.megaVariants.add("beedrillmega");
        HeldItemHandler.megaVariants.add("blastoisemega");
        HeldItemHandler.megaVariants.add("blazikenmega");
        HeldItemHandler.megaVariants.add("cameruptmega");
        HeldItemHandler.megaVariants.add("charizardmega-y");
        HeldItemHandler.megaVariants.add("charizardmega-x");
        HeldItemHandler.megaVariants.add("gallademega");
        HeldItemHandler.megaVariants.add("garchompmega");
        HeldItemHandler.megaVariants.add("gardevoirmega");
        HeldItemHandler.megaVariants.add("gengarmega");
        HeldItemHandler.megaVariants.add("glaliemega");
        HeldItemHandler.megaVariants.add("gyaradosmega");
        HeldItemHandler.megaVariants.add("heracrossmega");
        HeldItemHandler.megaVariants.add("houndoommega");
        HeldItemHandler.megaVariants.add("kangaskhanmega");
        HeldItemHandler.megaVariants.add("latiasmega");
        HeldItemHandler.megaVariants.add("latiosmega");
        HeldItemHandler.megaVariants.add("lucariomega");
        HeldItemHandler.megaVariants.add("manectricmega");
        HeldItemHandler.megaVariants.add("mawilemega");
        HeldItemHandler.megaVariants.add("mewtwomega-y");
        HeldItemHandler.megaVariants.add("mewtwomega-x");
        HeldItemHandler.megaVariants.add("pidgeotmega");
        HeldItemHandler.megaVariants.add("pinsirmega");
        HeldItemHandler.megaVariants.add("sableyemega");
        HeldItemHandler.megaVariants.add("salamencemega");
        HeldItemHandler.megaVariants.add("sceptilemega");
        HeldItemHandler.megaVariants.add("scizormega");
        HeldItemHandler.megaVariants.add("sharpedomega");
        HeldItemHandler.megaVariants.add("slowbromega");
        HeldItemHandler.megaVariants.add("steelixmega");
        HeldItemHandler.megaVariants.add("swampertmega");
        HeldItemHandler.megaVariants.add("tyranitarmega");
        HeldItemHandler.megaVariants.add("venusaurmega");
        HeldItemHandler.sortMegaVariants();
    }

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
            return "gen_1/models/";
        case 2:
            return "gen_2/models/";
        case 3:
            return "gen_3/models/";
        case 4:
            return "gen_4/models/";
        case 5:
            return "gen_5/models/";
        case 6:
            return "gen_6/models/";
        case 7:
            return "gen_7/models/";
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
            PokedexEntry real = entry;
            if (entry.getBaseForme() != null) entry = entry.getBaseForme();
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
            genMap.put(real, gen);
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
            return "gen_1/textures/";
        case 2:
            return "gen_2/textures/";
        case 3:
            return "gen_3/textures/";
        case 4:
            return "gen_4/textures/";
        case 5:
            return "gen_5/textures/";
        case 6:
            return "gen_6/textures/";
        case 7:
            return "gen_7/textures/";
        }
        return "textures/entities/";
    }

    @Override
    public Object getMod()
    {
        return this;
    }
}
