package pokecube.core.ai.pokemob;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.pathfinding.PathNavigate;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import thut.api.maths.Vector3;

/** Follow Owner AI. The pokemon should follow owner if it is not sitting, not
 * staying, not guarding and not attacking. Currently the teleporting is
 * disabled, due to the improvements to the pathfinder, and the ability to
 * recall remotely with r.
 * 
 * @author Patrick */
public class PokemobAIFollowOwner extends EntityAIBase
{

    IPokemob                 pokemob;
    private EntityLiving     thePet;
    private IEntityOwnable   pet;
    private EntityLivingBase theOwner;

    private double           speed;
    private PathNavigate     petPathfinder;
    private int              followTicks;
    float                    maxDist;
    float                    minDist;
    Vector3                  v        = Vector3.getNewVector();
    Vector3                  v1       = Vector3.getNewVector();
    Vector3                  ownerPos = Vector3.getNewVector();

    public PokemobAIFollowOwner(EntityLiving entity, float min, float max)
    {
        this.thePet = entity;
        this.minDist = min;
        this.maxDist = max;
        this.setMutexBits(2);
        pokemob = (IPokemob) entity;
        pet = (IEntityOwnable) entity;
        this.speed = 2;
        if (pokemob.getPokemonOwner() != null) ownerPos.set(pokemob.getPokemonOwner());
    }

    /** Returns whether an in-progress EntityAIBase should continue executing */
    @Override
    public boolean continueExecuting()
    {
        if (pokemob.getPokemonAIState(IMoveConstants.FOLLOWING)
                && this.thePet.getDistanceSqToEntity(this.theOwner) > this.maxDist * this.maxDist)
            return true;
        return !this.petPathfinder.noPath() && !pokemob.getPokemonAIState(IMoveConstants.SITTING)
                && this.thePet.getDistanceSqToEntity(this.theOwner) > this.maxDist * this.maxDist;
    }

    /** Resets the task */
    @Override
    public void resetTask()
    {
        ownerPos.set(theOwner);
        this.theOwner = null;
        this.petPathfinder.clearPathEntity();
        pokemob.setPokemonAIState(IMoveConstants.FOLLOWING, false);
    }

    /** Returns whether the EntityAIBase should begin execution. */
    @Override
    public boolean shouldExecute()
    {
        EntityLivingBase entitylivingbase = (EntityLivingBase) pet.getOwner();

        this.petPathfinder = thePet.getNavigator();
        Vector3 ownerV = Vector3.getNewVector();

        if (entitylivingbase == null)
        {
            return false;
        }
        else if (pokemob.getPokemonAIState(IMoveConstants.SITTING))
        {
            return false;
        }
        else if (pokemob != null && (pokemob.getPokemonAIState(IMoveConstants.GUARDING)
                || pokemob.getPokemonAIState(IMoveConstants.STAYING)))
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
        else if ((ownerV.set(entitylivingbase)).distToSq(ownerPos) < 2)
        {
            return false;
        }
        else if (!petPathfinder.noPath())
        {
            Vector3 p = v1.set(petPathfinder.getPath().getFinalPathPoint());
            v.set(entitylivingbase);
            if (p.distToSq(v) <= 2) { return false; }
            this.theOwner = entitylivingbase;
            return true;
        }
        else
        {
            this.theOwner = entitylivingbase;
            return true;
        }
    }

    /** Execute a one shot task or start executing a continuous task */
    @Override
    public void startExecuting()
    {
        this.followTicks = 0;
        ownerPos.set(theOwner);
        pokemob.setPokemonAIState(IMoveConstants.FOLLOWING, true);
    }

    /** Updates the task */
    @Override
    public void updateTask()
    {
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
            if (--this.followTicks <= 0)
            {
                this.followTicks = 10;
                this.petPathfinder.tryMoveToEntityLiving(this.theOwner, this.speed);
            }
        }
    }

}
