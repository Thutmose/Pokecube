package pokecube.adventures.legends.conditions;

import net.minecraft.entity.Entity;
import pokecube.adventures.legends.Condition;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.stats.CaptureStats;
import pokecube.core.database.stats.ISpecialCaptureCondition;
import pokecube.core.database.stats.ISpecialSpawnCondition;
import pokecube.core.database.stats.KillStats;
import pokecube.core.database.stats.SpecialCaseRegister;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.utils.PokeType;

public class Celebi extends Condition implements ISpecialCaptureCondition, ISpecialSpawnCondition
{
    @Override
    public boolean canCapture(Entity trainer, IPokemob pokemon)
    {
        if (!canCapture(trainer)) return false;
        int count1 = CaptureStats.getTotalUniqueOfTypeCaughtBy(trainer.getUniqueID(), PokeType.getType("grass"));
        int count2 = KillStats.getTotalUniqueOfTypeKilledBy(trainer.getUniqueID(), PokeType.getType("grass"));
        int count3 = SpecialCaseRegister.countSpawnableTypes(PokeType.getType("grass"));
        double captureFactor = (double) count1 / (double) count3;
        if (captureFactor >= 0.75 && count1 >= count2) { return true; }
        if (!trainer.getEntityWorld().isRemote)
        {
            if (captureFactor < 0.75)
            {
                sendNoTrust(trainer);
            }
            else if (count1 < count2)
            {
                sendAngered(trainer);
            }
        }
        return false;
    }

    @Override
    public PokedexEntry getEntry()
    {
        return Database.getEntry("celebi");
    }

}
