package pokecube.core.ai.thread.aiRunnables;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigate;
import pokecube.core.interfaces.IMoveConstants.AIRoutine;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.interfaces.pokemob.ai.LogicStates;
import thut.api.maths.Vector3;

/** This attempts to make the mob follow the owner around in the world. It
 * triggers if the owner gets too far away from the mob, and the mob is set to
 * follow. */
public class AIFollowOwner extends AIBase
{
    final IPokemob             pokemob;
    final private EntityLiving thePet;
    private EntityLivingBase   theOwner;

    private double             speed;
    private PathNavigate       petPathfinder;
    private int                cooldown;
    private boolean            pathing  = false;
    float                      maxDist;
    float                      minDist;
    Vector3                    ownerPos = Vector3.getNewVector();
    Vector3                    v        = Vector3.getNewVector();
    Vector3                    v1       = Vector3.getNewVector();

    public AIFollowOwner(IPokemob entity, float min, float max)
    {
        this.thePet = entity.getEntity();
        this.minDist = min;
        this.maxDist = max;
        pokemob = entity;
        this.speed = 1;
        if (pokemob.getPokemonOwner() != null) ownerPos.set(pokemob.getPokemonOwner());
    }

    @Override
    public void reset()
    {
        ownerPos.set(theOwner);
        this.theOwner = null;
        pathing = false;
    }

    @Override
    public void run()
    {
        if (theOwner == null)
        {
            theOwner = (EntityLivingBase) pokemob.getOwner();
            this.cooldown = 0;
            ownerPos.set(theOwner);
            pathing = true;
        }
        // Look at owner.
        if (Vector3.isVisibleEntityFromEntity(thePet, theOwner))
        {
            this.thePet.getLookHelper().setLookPositionWithEntity(this.theOwner, 10.0F,
                    this.thePet.getVerticalFaceSpeed());
        }
        // Look at path you are walking to.
        else if (!this.petPathfinder.noPath()
                && petPathfinder.getPath().getCurrentPathIndex() < petPathfinder.getPath().getCurrentPathLength() - 3)
        {
            double x, y, z;
            x = petPathfinder.getPath().getPathPointFromIndex(petPathfinder.getPath().getCurrentPathIndex() + 1).x
                    + 0.5;
            y = petPathfinder.getPath().getPathPointFromIndex(petPathfinder.getPath().getCurrentPathIndex() + 1).y
                    + 0.5;
            z = petPathfinder.getPath().getPathPointFromIndex(petPathfinder.getPath().getCurrentPathIndex() + 1).z
                    + 0.5;
            this.thePet.getLookHelper().setLookPosition(x, y, z, 10, this.thePet.getVerticalFaceSpeed());
        }
        // Only path every couple ticks, or when owner has moved.
        if (--this.cooldown <= 0)
        {
            this.cooldown = 2;
            double dl = v.set(theOwner).distToSq(ownerPos);
            if (dl < 1) return;
            ownerPos.set(theOwner);
            this.speed = Math.sqrt(theOwner.motionX * theOwner.motionX + theOwner.motionZ * theOwner.motionZ);
            this.speed = Math.max(0.6, speed);
            Path path = this.petPathfinder.getPathToEntityLiving(theOwner);
            if (path != null) addEntityPath(thePet, path, speed);
        }
    }

    @Override
    public boolean shouldRun()
    {
        if (!pokemob.isRoutineEnabled(AIRoutine.FOLLOW)) return false;
        EntityLivingBase entitylivingbase = (EntityLivingBase) pokemob.getOwner();
        this.petPathfinder = thePet.getNavigator();
        // Nothing to follow
        if (entitylivingbase == null)
        {
            return false;
        }
        // Sit means no follow
        else if (pokemob.getLogicState(LogicStates.SITTING))
        {
            return false;
        }
        // Stay means no follow either
        else if (pokemob.getGeneralState(GeneralStates.STAYING))
        {
            return false;
        }
        // Pathing and too far away, should follow.
        else if (pathing && this.thePet.getDistanceSq(entitylivingbase) > this.maxDist * this.maxDist)
        {
            return true;
        }
        // Has a target and trying to execute a move, shouldn't follow
        else if (thePet.getAttackTarget() != null || pokemob.getCombatState(CombatStates.EXECUTINGMOVE))
        {
            return false;
        }
        // Too close, shouldn't follow
        else if (this.thePet.getDistanceSq(entitylivingbase) < this.minDist * this.minDist)
        {
            return false;
        }
        // Owner hasn't moved very far since last attmept, shouldn't follow.
        else if ((Vector3.getNewVector().set(entitylivingbase)).distToSq(ownerPos) < this.minDist * this.minDist)
        {
            return false;
        }
        // Follow owner if path takes too far from owner.
        else if (!petPathfinder.noPath())
        {
            Vector3 p = v1.set(petPathfinder.getPath().getFinalPathPoint());
            v.set(entitylivingbase);
            if (p.distToSq(v) <= 2) { return false; }
            return true;
        }
        // Follow owner.
        else
        {
            return true;
        }
    }

}