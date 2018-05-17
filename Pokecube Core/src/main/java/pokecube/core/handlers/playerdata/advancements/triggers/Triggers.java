package pokecube.core.handlers.playerdata.advancements.triggers;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.ICriterionTrigger;

public class Triggers
{
    public static final CatchPokemobTrigger      CATCHPOKEMOB      = register(new CatchPokemobTrigger());
    public static final KillPokemobTrigger       KILLPOKEMOB       = register(new KillPokemobTrigger());
    public static final HatchPokemobTrigger      HATCHPOKEMOB      = register(new HatchPokemobTrigger());
    public static final FirstPokemobTrigger      FIRSTPOKEMOB      = register(new FirstPokemobTrigger());
    public static final EvolvePokemobTrigger     EVOLVEPOKEMOB     = register(new EvolvePokemobTrigger());
    public static final InspectPokemobTrigger    INSPECTPOKEMOB    = register(new InspectPokemobTrigger());
    public static final MegaEvolvePokemobTrigger MEGAEVOLVEPOKEMOB = register(new MegaEvolvePokemobTrigger());
    public static final BreedPokemobTrigger      BREEDPOKEMOB      = register(new BreedPokemobTrigger());
    public static final UseMoveTrigger           USEMOVE           = register(new UseMoveTrigger());

    public static void init()
    {
    }

    @SuppressWarnings({ "rawtypes" })
    public static <T extends ICriterionTrigger> T register(T criterion)
    {
        return CriteriaTriggers.register(criterion);
    }
}
