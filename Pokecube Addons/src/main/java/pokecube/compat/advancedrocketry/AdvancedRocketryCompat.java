package pokecube.compat.advancedrocketry;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pokecube.core.events.SpawnEvent;

public class AdvancedRocketryCompat
{

    @SubscribeEvent
    public void spawn(SpawnEvent.Pick.Pre event)
    {
        // TODO determine if this is an AdvancedRocketry dimension, and if so,
        // change spawn accordingly.
    }

//    @SubscribeEvent
//    public void breathe(zmaster587.advancedRocketry.api.event.AtmosphereEvent.AtmosphereTickEvent event)
//    {
//        // TODO determine if the mob is a pokemob that can breathe vaccume, if so, cancel event.
//    }
}
