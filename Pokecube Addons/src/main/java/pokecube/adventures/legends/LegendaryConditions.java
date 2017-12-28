package pokecube.adventures.legends;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraftforge.common.MinecraftForge;
import pokecube.core.database.Pokedex;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.stats.ISpecialCaptureCondition;
import pokecube.core.database.stats.ISpecialSpawnCondition;
import pokecube.core.database.stats.SpecialCaseRegister;
import pokecube.core.interfaces.PokecubeMod;
import thut.lib.CompatParser.ClassFinder;

public class LegendaryConditions
{
    public static List<PokedexEntry> entries = Lists.newArrayList();

    @SuppressWarnings("unchecked")
    public LegendaryConditions()
    {
        MinecraftForge.EVENT_BUS.register(new LegendarySpawns());
        List<Class<?>> foundClasses;
        List<Class<? extends Condition>> conditionclasses = Lists.newArrayList();
        try
        {
            foundClasses = ClassFinder.find(Condition.class.getPackage().getName());
            int num = 0;
            for (Class<?> candidateClass : foundClasses)
            {
                if (Condition.class.isAssignableFrom(candidateClass) && candidateClass != Condition.class)
                {
                    conditionclasses.add((Class<? extends Condition>) candidateClass);
                    num++;
                }
            }
            if (PokecubeMod.debug) PokecubeMod.log("Detected " + num + " Legendary Conditions.");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        int num = 0;
        for (Class<? extends Condition> c : conditionclasses)
        {
            try
            {
                Condition cond = c.newInstance();
                PokedexEntry e = cond.getEntry();
                if (Pokedex.getInstance().isRegistered(e))
                {
                    SpecialCaseRegister.register(e.getName(), (ISpecialCaptureCondition) cond);
                    SpecialCaseRegister.register(e.getName(), (ISpecialSpawnCondition) cond);
                    num++;
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        PokecubeMod.log("Registered " + num + " Legendary Conditions.");
    }
}
