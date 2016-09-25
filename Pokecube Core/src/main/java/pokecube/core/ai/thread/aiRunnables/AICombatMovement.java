package pokecube.core.ai.thread.aiRunnables;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.pathfinding.Path;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import thut.api.maths.Vector3;

public class AICombatMovement extends AIBase
{
    final EntityLiving attacker;
    final IPokemob     pokemob;
    Entity             target;
    Vector3            centre;
    double             movementSpeed;

    public AICombatMovement(EntityLiving par1EntityLiving)
    {
        this.attacker = par1EntityLiving;
        this.pokemob = (IPokemob) attacker;
        this.movementSpeed = attacker.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue()
                * 0.8;
        centre = null;
        this.setMutex(0);
    }

    @Override
    public void reset()
    {
        if (target == null) centre = null;
    }

    @Override
    public void run()
    {
        if (centre == null)
        {
            Vector3 targetLoc = Vector3.getNewVector().set(target);
            Vector3 attackerLoc = Vector3.getNewVector().set(attacker);
            Vector3 diff = targetLoc.subtractFrom(attackerLoc).reverse().scalarMultBy(0.5);
            centre = attackerLoc.addTo(diff);
        }
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
        combatDistance = Math.max(combatDistance, 4);
        int combatDistanceSq = combatDistance * combatDistance;
        if (diff.magSq() > combatDistanceSq)
        {
            diff.norm().scalarMultBy(3);
            Path path = attacker.getNavigator().getPathToPos(diff.addTo(centre).getPos());
            addEntityPath(attacker, path, movementSpeed);
        }
        else
        {
            Vector3 perp = diff.horizonalPerp().scalarMultBy(combatDistance);
            perp.addTo(here);
            Path path = attacker.getNavigator().getPathToPos(perp.getPos());
            addEntityPath(attacker, path, movementSpeed);
        }
    }

    @Override
    public boolean shouldRun()
    {
        return (target = attacker.getAttackTarget()) != null && pokemob.getPokemonAIState(IMoveConstants.ANGRY);
    }
}
