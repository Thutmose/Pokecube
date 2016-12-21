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

public class Groudon extends Condition implements ISpecialCaptureCondition, ISpecialSpawnCondition
{
    @Override
    public boolean canCapture(Entity trainer, IPokemob pokemon)
    {
        if (!canCapture(trainer)) return false;
        int count1 = CaptureStats.getTotalUniqueOfTypeCaughtBy(trainer.getUniqueID(), PokeType.ground);
        int count2 = KillStats.getTotalUniqueOfTypeKilledBy(trainer.getUniqueID(), PokeType.water);
        int count4 = SpecialCaseRegister.countSpawnableTypes(PokeType.water);
        int count3 = SpecialCaseRegister.countSpawnableTypes(PokeType.ground);
        double captureFactor = (double) count1 / (double) count3;
        double killFactor = (double) count2 / (double) count4;
        if (killFactor >= 0.5 && captureFactor >= 0.5) { return true; }
        if (pokemon != null && !trainer.getEntityWorld().isRemote)
        {
            sendNoTrust(trainer);
        }
        return false;
    }

    @Override
    public PokedexEntry getEntry()
    {
        return Database.getEntry("groudon");
    }

}
