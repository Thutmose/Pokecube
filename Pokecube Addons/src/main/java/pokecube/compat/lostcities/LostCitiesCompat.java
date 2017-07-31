package pokecube.compat.lostcities;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pokecube.core.events.onload.InitDatabase;
import pokecube.core.interfaces.PokecubeMod;
import thut.api.terrain.TerrainSegment;
import thut.lib.CompatClass;
import thut.lib.CompatClass.Phase;

public class LostCitiesCompat
{

    @Optional.Method(modid = "lostcities")
    @CompatClass(takesEvent = false, phase = Phase.CONSTRUCT)
    public static void initCompat()
    {
        MinecraftForge.EVENT_BUS.register(new LostCitiesCompat());
    }

    @SubscribeEvent
    public void init(InitDatabase.Post event)
    {
        TerrainSegment.defaultChecker = new LostCityTerrainChecker(TerrainSegment.defaultChecker);
        if (PokecubeMod.debug)
            PokecubeMod.log("Register Terrain Checker for Lost Cities. " + TerrainSegment.defaultChecker);
    }
}
