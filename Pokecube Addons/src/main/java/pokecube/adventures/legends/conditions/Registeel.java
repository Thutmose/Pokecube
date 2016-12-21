package pokecube.adventures.legends.conditions;

import java.util.ArrayList;

import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import pokecube.adventures.legends.Condition;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.stats.CaptureStats;
import pokecube.core.database.stats.ISpecialCaptureCondition;
import pokecube.core.database.stats.ISpecialSpawnCondition;
import pokecube.core.interfaces.IPokemob;
import thut.api.maths.Vector3;

public class Registeel extends Condition implements ISpecialCaptureCondition, ISpecialSpawnCondition
{
    @Override
    public boolean canCapture(Entity trainer, IPokemob pokemon)
    {
        if (!canCapture(trainer)) return false;
        boolean relicanth = CaptureStats.getTotalNumberOfPokemobCaughtBy(trainer.getUniqueID(),
                Database.getEntry("relicanth")) > 0;
        boolean wailord = CaptureStats.getTotalNumberOfPokemobCaughtBy(trainer.getUniqueID(),
                Database.getEntry("wailord")) > 0;
        if ((relicanth && wailord)) return true;
        if (pokemon != null && !trainer.getEntityWorld().isRemote)
        {
            sendNoTrust(trainer);
        }
        return false;
    }

    public void onSpawn(IPokemob mob)
    {
        mob = mob.setForSpawn(54500);
        Vector3 location = Vector3.getNewVector().set(mob).add(0, -1, 0);
        ArrayList<Vector3> locations = new ArrayList<Vector3>();
        boolean check = false;
        World world = ((Entity) mob).getEntityWorld();
        locations.add(location.add(0, -1, -1));
        locations.add(location.add(0, -1, +1));
        locations.add(location.add(0, -1, 0));
        locations.add(location.add(0, -2, 0));
        check = isBlock(world, locations, Blocks.IRON_BLOCK);
        if (!check)
        {
            locations.clear();
            locations.add(location.add(-1, -1, 0));
            locations.add(location.add(1, -1, 0));
            locations.add(location.add(0, -1, 0));
            locations.add(location.add(0, -2, 0));
            check = isBlock(world, locations, Blocks.IRON_BLOCK);
            if (check)
            {
                for (Vector3 v : locations)
                {
                    v.setAir(world);
                }
                location.setAir(world);
            }
        }
        else
        {
            for (Vector3 v : locations)
            {
                v.setAir(world);
            }
            location.setAir(world);
        }

    }

    @Override
    public boolean canSpawn(Entity trainer, Vector3 location)
    {
        if (!super.canSpawn(trainer, location)) return false;
        ArrayList<Vector3> locations = new ArrayList<Vector3>();
        boolean check = false;
        World world = trainer.getEntityWorld();
        locations.add(location.add(0, -1, -1));
        locations.add(location.add(0, -1, +1));
        locations.add(location.add(0, -1, 0));
        locations.add(location.add(0, -2, 0));
        check = isBlock(world, locations, Blocks.IRON_BLOCK);
        if (!check)
        {
            locations.clear();
            locations.add(location.add(-1, -1, 0));
            locations.add(location.add(1, -1, 0));
            locations.add(location.add(0, -1, 0));
            locations.add(location.add(0, -2, 0));

            check = isBlock(world, locations, Blocks.IRON_BLOCK);
        }
        if (!check)
        {
            String message = "msg.reginotlookright.txt";
            trainer.addChatMessage(new TextComponentTranslation(message));
            return false;
        }
        return true;
    }

    @Override
    public PokedexEntry getEntry()
    {
        return Database.getEntry("registeel");
    }

}
