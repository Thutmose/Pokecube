package pokecube.core.ai.thread.aiRunnables;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.math.BlockPos;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.interfaces.pokemob.ai.LogicStates;
import pokecube.core.utils.TimePeriod;
import thut.api.maths.Vector3;

/** This makes the mob return to its "home" location if it has strayed too far
 * away due to one of the other AI tasks. */
public class AIReturnHome extends AIBase
{
    final private EntityLiving entity;
    final IPokemob             mob;
    final PokedexEntry         entry;
    private double             speed;

    Vector3                    v  = Vector3.getNewVector();
    Vector3                    v1 = Vector3.getNewVector();

    public AIReturnHome(EntityLiving entity)
    {
        this.entity = entity;
        this.setMutex(2);
        mob = CapabilityPokemob.getPokemobFor(entity);
        entry = mob.getPokedexEntry();
        this.speed = entity.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue() * 0.4;
    }

    @Override
    public void reset()
    {
    }

    @Override
    public void run()
    {
        Path path = entity.getNavigator().getPathToPos(mob.getHome());
        if (path != null) addEntityPath(entity, path, speed);
    }

    @Override
    public boolean shouldRun()
    {
        BlockPos home = mob.getHome();
        if (entity.getAttackTarget() != null
                || mob.getHomeDistance() * mob.getHomeDistance() < home.distanceSq(entity.getPosition())
                || mob.getLogicState(LogicStates.SITTING) || (mob.getGeneralState(GeneralStates.TAMED)
                        && !mob.getGeneralState(GeneralStates.STAYING))) { return false; }

        PokedexEntry entry = mob.getPokedexEntry();
        boolean activeTime = false;
        for (TimePeriod active : entry.activeTimes())
        {// TODO find some way to determine actual length of day for things like
         // AR support.
            if (active.contains(entity.getEntityWorld().getWorldTime(), 24000))
            {
                activeTime = true;
            }
        }
        return !activeTime && !mob.getCombatState(CombatStates.ANGRY);
    }

}
