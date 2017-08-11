package pokecube.core.events;

import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;
import pokecube.core.database.SpawnBiomeMatcher;
import pokecube.core.database.SpawnBiomeMatcher.SpawnCheck;

public class SpawnCheckEvent extends Event
{
    public final SpawnBiomeMatcher matcher;

    public SpawnCheckEvent(SpawnBiomeMatcher spawnBiomeMatcher)
    {
        this.matcher = spawnBiomeMatcher;
    }

    public static class Init extends SpawnCheckEvent
    {
        public Init(SpawnBiomeMatcher spawnBiomeMatcher)
        {
            super(spawnBiomeMatcher);
        }
    }

    @Cancelable
    /** This should be canceled if the spawn does not match the checker, it will
     * only be called if every other condition for the spawnBiomeMatcher is
     * met. */
    public static class Check extends SpawnCheckEvent
    {
        public final SpawnCheck checker;

        public Check(SpawnBiomeMatcher spawnBiomeMatcher, SpawnCheck checker)
        {
            super(spawnBiomeMatcher);
            this.checker = checker;
        }
    }
}
