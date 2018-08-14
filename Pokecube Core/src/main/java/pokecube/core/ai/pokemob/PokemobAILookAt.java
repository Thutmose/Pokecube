package pokecube.core.ai.pokemob;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.ai.LogicStates;

public class PokemobAILookAt extends EntityAIBase
{
    protected EntityLiving            theWatcher;
    /** The closest entity which is being watched by this one. */
    protected Entity                  closestEntity;
    /** the watcher casted to a pokemob. */
    protected IPokemob                pokemob;
    /** This is the Maximum distance that the AI will look for the Entity */
    protected float                   maxDistanceForPlayer;
    boolean                           idle = false;
    private int                       lookTime;
    private float                     chance;
    /** X offset to look at */
    private double                    lookX;
    /** Z offset to look at */
    private double                    lookZ;
    /** A decrementing tick that stops the entity from being idle once it
     * reaches 0. */
    private int                       idleTime;
    protected Class<? extends Entity> watchedClass;

    public PokemobAILookAt(EntityLiving entitylivingIn, Class<? extends Entity> watchTargetClass, float maxDistance)
    {
        this(entitylivingIn, watchTargetClass, maxDistance, 0.02f);
    }

    public PokemobAILookAt(EntityLiving entitylivingIn, Class<? extends Entity> watchTargetClass, float maxDistance,
            float chanceIn)
    {
        this.theWatcher = entitylivingIn;
        this.watchedClass = watchTargetClass;
        this.maxDistanceForPlayer = maxDistance;
        this.pokemob = CapabilityPokemob.getPokemobFor(theWatcher);
        this.chance = chanceIn;
        this.setMutexBits(2);
    }

    /** Returns whether an in-progress EntityAIBase should continue executing */
    @Override
    public boolean shouldContinueExecuting()
    {
        if (pokemob.getLogicState(LogicStates.SLEEPING) || (pokemob.getStatus() & IPokemob.STATUS_SLP) > 0)
            return false;
        if (idle) return this.idleTime >= 0;
        return !this.closestEntity.isEntityAlive() ? false
                : (this.theWatcher.getDistanceSq(this.closestEntity) > this.maxDistanceForPlayer
                        * this.maxDistanceForPlayer ? false : this.lookTime > 0);
    }

    /** Determine if this AI Task is interruptible by a higher (= lower value)
     * priority task. All vanilla AITask have this value set to true. */
    @Override
    public boolean isInterruptible()
    {
        return true;
    }

    /** Resets the task */
    @Override
    public void resetTask()
    {
        this.closestEntity = null;
        idle = false;
    }

    /** Returns whether the EntityAIBase should begin execution. */
    @Override
    public boolean shouldExecute()
    {
        if (pokemob.getLogicState(LogicStates.SLEEPING) || (pokemob.getStatus() & IPokemob.STATUS_SLP) > 0)
            return false;
        if (this.theWatcher.getRNG().nextFloat() >= this.chance)
        {
            return false;
        }
        else if (theWatcher.getRNG().nextFloat() < 0.01)
        {
            idle = true;
            return true;
        }
        else
        {
            idle = false;
            if (this.theWatcher.getAttackTarget() != null)
            {
                this.closestEntity = this.theWatcher.getAttackTarget();
            }

            if (this.watchedClass == EntityPlayer.class)
            {
                this.closestEntity = this.theWatcher.getEntityWorld().getClosestPlayerToEntity(this.theWatcher,
                        this.maxDistanceForPlayer);
            }
            else
            {
                this.closestEntity = this.theWatcher.getEntityWorld().findNearestEntityWithinAABB(this.watchedClass,
                        this.theWatcher.getEntityBoundingBox().grow(this.maxDistanceForPlayer, 3.0D,
                                this.maxDistanceForPlayer),
                        this.theWatcher);
            }

            if (closestEntity != null && closestEntity.getRidingEntity() == theWatcher) closestEntity = null;

            return this.closestEntity != null;
        }
    }

    /** Execute a one shot task or start executing a continuous task */
    @Override
    public void startExecuting()
    {
        if (idle)
        {
            double d0 = (Math.PI * 2D) * this.theWatcher.getRNG().nextDouble();
            this.lookX = Math.cos(d0);
            this.lookZ = Math.sin(d0);
            this.idleTime = 20 + this.theWatcher.getRNG().nextInt(20);
        }
        else this.lookTime = 40 + this.theWatcher.getRNG().nextInt(40);
    }

    /** Updates the task */
    @Override
    public void updateTask()
    {
        if (idle)
        {
            --this.idleTime;
            this.theWatcher.getLookHelper().setLookPosition(this.theWatcher.posX + this.lookX,
                    this.theWatcher.posY + this.theWatcher.getEyeHeight(), this.theWatcher.posZ + this.lookZ, 5.0F,
                    this.theWatcher.getVerticalFaceSpeed());
            return;
        }
        this.theWatcher.getLookHelper().setLookPosition(this.closestEntity.posX,
                this.closestEntity.posY + this.closestEntity.getEyeHeight(), this.closestEntity.posZ, 5.0F,
                this.theWatcher.getVerticalFaceSpeed());
        --this.lookTime;
    }

}
