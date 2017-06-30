package pokecube.core.handlers.playerdata.advancements.criteria;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.ICriterionTrigger;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class Triggers
{
    public static final CatchPokemobTrigger CATCHPOKEMOB = register(new CatchPokemobTrigger());
    public static final KillPokemobTrigger  KILLPOKEMOB  = register(new KillPokemobTrigger());
    public static final HatchPokemobTrigger HATCHPOKEMOB = register(new HatchPokemobTrigger());
    public static final FirstPokemobTrigger FIRSTPOKEMOB = register(new FirstPokemobTrigger());

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static <T extends ICriterionTrigger> T register(T criterion)
    {
        Method method = ReflectionHelper.findMethod(CriteriaTriggers.class, "register", "func_192118_a",
                ICriterionTrigger.class);
        method.setAccessible(true);
        try
        {
            return (T) method.invoke(null, criterion);
        }
        catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
        {
            e.printStackTrace();
        }
        return null;
    }
}
