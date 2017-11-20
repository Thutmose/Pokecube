package pokecube.core.ai.thread.aiRunnables;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigate;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IMoveConstants.AIRoutine;
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
        this.speed = 0.6;
        if (pokemob.getPokemonOwner() != null) ownerPos.set(pokemob.getPokemonOwner());
    }

    @Override
    public void reset()
    {
        ownerPos.set(theOwner);
        this.theOwner = null;
        pokemob.setPokemonAIState(IMoveConstants.PATHING, false);
    }

    @Override
    public void run()
    {
        if (theOwner == null)
        {
            theOwner = (EntityLivingBase) pokemob.getOwner();
            this.cooldown = 0;
            ownerPos.set(theOwner);
            pokemob.setPokemonAIState(IMoveConstants.PATHING, true);
        }
        if (Vector3.isVisibleEntityFromEntity(thePet, theOwner))
        {
            this.thePet.getLookHelper().setLookPositionWithEntity(this.theOwner, 10.0F,
                    this.thePet.getVerticalFaceSpeed());
        }
        else if (!this.petPathfinder.noPath()
                && petPathfinder.getPath().getCurrentPathIndex() < petPathfinder.getPath().getCurrentPathLength() - 3)
        {
            double x, y, z;
            x = petPathfinder.getPath().getPathPointFromIndex(petPathfinder.getPath().getCurrentPathIndex() + 1).xCoord
                    + 0.5;
            y = petPathfinder.getPath().getPathPointFromIndex(petPathfinder.getPath().getCurrentPathIndex() + 1).yCoord
                    + 0.5;
            z = petPathfinder.getPath().getPathPointFromIndex(petPathfinder.getPath().getCurrentPathIndex() + 1).zCoord
                    + 0.5;
            this.thePet.getLookHelper().setLookPosition(x, y, z, 10, this.thePet.getVerticalFaceSpeed());
        }

        if (!pokemob.getPokemonAIState(IMoveConstants.SITTING))
        {
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
    }

    @Override
    public boolean shouldRun()
    {
        if(!pokemob.isRoutineEnabled(AIRoutine.FOLLOW)) return false;
        EntityLivingBase entitylivingbase = (EntityLivingBase) pokemob.getOwner();
        this.petPathfinder = thePet.getNavigator();
        if (entitylivingbase == null)
        {
            return false;
        }
        else if (pokemob.getPokemonAIState(IMoveConstants.PATHING)
                && this.thePet.getDistanceSqToEntity(entitylivingbase) > this.maxDist * this.maxDist)
        {
            return true;
        }
        else if (pokemob.getPokemonAIState(IMoveConstants.SITTING))
        {
            return false;
        }
        else if (pokemob != null && pokemob.getPokemonAIState(IMoveConstants.STAYING))
        {
            return false;
        }
        else if (thePet.getAttackTarget() != null || pokemob.getPokemonAIState(IMoveConstants.EXECUTINGMOVE))
        {
            return false;
        }
        else if (this.thePet.getDistanceSqToEntity(entitylivingbase) < this.minDist * this.minDist)
        {
            return false;
        }
        else if ((Vector3.getNewVector().set(entitylivingbase)).distToSq(ownerPos) < this.minDist * this.minDist)
        {
            return false;
        }
        else if (!petPathfinder.noPath())
        {
            Vector3 p = v1.set(petPathfinder.getPath().getFinalPathPoint());
            v.set(entitylivingbase);
            if (p.distToSq(v) <= 2) { return false; }
            return true;
        }
        else
        {
            return true;
        }
    }

}