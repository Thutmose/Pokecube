package pokecube.core.ai.thread.aiRunnables.combat;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.pathfinding.Path;
import pokecube.core.ai.thread.aiRunnables.AIBase;
import pokecube.core.ai.utils.pathing.PokemobNavigator;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import thut.api.maths.Vector3;

/** This IAIRunnable manages the movement of the mob while it is in combat, but
 * on cooldown between attacks. It also manages the leaping at targets, and the
 * dodging of attacks. */
public class AICombatMovement extends AIBase
{
    final EntityLiving attacker;
    final IPokemob     pokemob;
    Entity             target;
    Vector3            centre;
    double             movementSpeed;

    public AICombatMovement(IPokemob entity)
    {
        this.attacker = entity.getEntity();
        this.pokemob = entity;
        this.movementSpeed = attacker.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue()
                * 0.8;
        centre = null;
        this.setMutex(0);
    }

    protected void calculateCentre()
    {
        if (centre == null)
        {
            Vector3 targetLoc = Vector3.getNewVector().set(target);
            Vector3 attackerLoc = Vector3.getNewVector().set(attacker);
            Vector3 diff = targetLoc.addTo(attackerLoc).scalarMultBy(0.5);
            centre = diff;
            centre.y = Math.min(attackerLoc.y, targetLoc.y);
        }
    }

    @Override
    public void reset()
    {
        if (target == null) centre = null;
    }

    @Override
    public void run()
    {
        // Figure out where centre of combat is
        calculateCentre();
        // If the mob has a path already, check if it is near the end, if not,
        // return early.
        if (!attacker.getNavigator().noPath())
        {
            Vector3 end = Vector3.getNewVector().set(attacker.getNavigator().getPath().getFinalPathPoint());
            Vector3 here = Vector3.getNewVector().set(attacker);
            float f = this.attacker.width;
            f = Math.max(f, 0.5f);
            if (here.distTo(end) > f) { return; }
        }

        Vector3 here = Vector3.getNewVector().set(attacker);
        Vector3 diff = here.subtract(centre);
        if (diff.magSq() < 1) diff.norm();
        int combatDistance = PokecubeMod.core.getConfig().combatDistance;
        combatDistance = Math.max(combatDistance, 2);
        int combatDistanceSq = combatDistance * combatDistance;
        // If the mob has left the combat radius, try to return to the centre of
        // combat. Otherwise, find a random spot in a consistant direction
        // related to the center to run in, this results in the mobs somewhat
        // circling the middle, and reversing direction every 10 seconds or so.
        if (diff.magSq() > combatDistanceSq)
        {
            pokemob.setCombatState(CombatStates.LEAPING, false);
            Path path = attacker.getNavigator().getPathToPos(centre.getPos());
            // Path back to center of ring.
            if (path != null) addEntityPath(attacker, path, movementSpeed);
            // Could not path to center, so null it to re-calulate next run.
            else centre = null;
        }
        else
        {
            Vector3 perp = diff.horizonalPerp().scalarMultBy(combatDistance);
            int revTime = 200;
            if (attacker.ticksExisted % revTime > revTime / 2) perp.reverse();
            perp.addTo(here);
            if (Math.abs(perp.y - centre.y) > combatDistance / 2) perp.y = centre.y;
            Path path = attacker.getNavigator().getPathToPos(perp.getPos());
            addEntityPath(attacker, path, movementSpeed);
        }
    }

    @Override
    public boolean shouldRun()
    {
        // Not currently able to move.
        if (attacker.getNavigator() instanceof PokemobNavigator
                && !((PokemobNavigator) attacker.getNavigator()).canNavigate())
            return false;
        // Has target and is angry.
        return (target = attacker.getAttackTarget()) != null && pokemob.getCombatState(CombatStates.ANGRY);
    }
}