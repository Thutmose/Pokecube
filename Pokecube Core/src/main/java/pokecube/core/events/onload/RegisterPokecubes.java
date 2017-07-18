package pokecube.core.events.onload;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraftforge.fml.common.eventhandler.Event;
import pokecube.core.interfaces.IPokecube.PokecubeBehavior;

/** This event is fired during the item registration phase. Add any pokecubes
 * you want to register here. The name of the cube will be <prefix_>cube */
public class RegisterPokecubes extends Event
{
    public Map<Integer, String>           cubePrefixes = Maps.newHashMap();
    public Map<Integer, PokecubeBehavior> behaviors    = Maps.newHashMap();

    public RegisterPokecubes()
    {
    }

}
