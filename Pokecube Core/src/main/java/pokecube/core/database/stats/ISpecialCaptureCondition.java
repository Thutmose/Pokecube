package pokecube.core.database.stats;

import java.util.HashMap;

import net.minecraft.entity.Entity;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IPokemob;

public interface ISpecialCaptureCondition
{
    public static final HashMap<PokedexEntry, ISpecialCaptureCondition> captureMap = new HashMap<PokedexEntry, ISpecialCaptureCondition>();

    boolean canCapture(Entity trainer);

    boolean canCapture(Entity trainer, IPokemob pokemon);
}
