package pokecube.core.database.stats;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentString;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.moves.implementations.actions.ActionTeleport;
import pokecube.core.utils.PokeType;

public class SpecialCaseRegister
{
    public static int countSpawnableTypes(PokeType type)
    {
        int ret = 0;
        for (PokedexEntry e : Database.spawnables)
        {
            if (type == null || e.isType(type)) ret++;
        }
        return ret;
    }

    public static ISpecialCaptureCondition getCaptureCondition(PokedexEntry entry)
    {
        if (entry != null && ISpecialCaptureCondition.captureMap
                .containsKey(entry)) { return ISpecialCaptureCondition.captureMap.get(entry); }
        return null;
    }

    public static ISpecialCaptureCondition getCaptureCondition(String name)
    {
        return getCaptureCondition(Database.getEntry(name));
    }

    public static ISpecialSpawnCondition getSpawnCondition(PokedexEntry entry)
    {
        if (entry != null && ISpecialSpawnCondition.spawnMap
                .containsKey(entry)) { return ISpecialSpawnCondition.spawnMap.get(entry); }
        return null;
    }

    public static ISpecialSpawnCondition getSpawnCondition(String name)
    {
        return getSpawnCondition(Database.getEntry(name));
    }

    public static void register()
    {

        ISpecialCaptureCondition mewCondition = new ISpecialCaptureCondition()
        {

            @Override
            public boolean canCapture(Entity trainer)
            {
                return false;
            }

            @Override
            public boolean canCapture(Entity trainer, IPokemob pokemon)
            {
                int caught = CaptureStats.getNumberUniqueCaughtBy(trainer.getUniqueID());

                if (caught < Database.spawnables.size() - 1)
                {
                    if (trainer instanceof EntityPlayer) ((EntityPlayer) trainer)
                            .addChatMessage(new TextComponentString("You do not have enough badges to control Mew!"));
                    ActionTeleport.teleportRandomly((EntityLivingBase) pokemon);
                    return false;
                }

                return true;
            }
        };

        ISpecialCaptureCondition.captureMap.put(Database.getEntry("mew"), mewCondition);
    }

    public static void register(String name, ISpecialCaptureCondition condition)
    {
        if (Database.entryExists(name))
        {
            ISpecialCaptureCondition.captureMap.put(Database.getEntry(name), condition);
        }
    }

    public static void register(String name, ISpecialSpawnCondition condition)
    {
        if (Database.entryExists(name)) ISpecialSpawnCondition.spawnMap.put(Database.getEntry(name), condition);
    }
}
