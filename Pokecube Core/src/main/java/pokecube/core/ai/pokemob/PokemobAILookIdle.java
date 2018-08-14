package pokecube.core.ai.pokemob;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.ai.LogicStates;

public class PokemobAILookIdle extends EntityAIBase
{
    protected EntityLiving theWatcher;
    /** The closest entity which is being watched by this one. */
    protected Entity       closestEntity;
    /** the watcher casted to a pokemob. */
    protected IPokemob     pokemob;
    /** This is the Maximum distance that the AI will look for the Entity */
    protected float        maxDistanceForPlayer;
    boolean                idle = false;
    private int            lookTime;
    /** X offset to look at */
    private double         lookX;
    /** Z offset to look at */
    private double         lookZ;
    /** A decrementing tick that stops the entity from being idle once it
     * reaches 0. */
    private int            idleTime;

    private float          chance;

    public PokemobAILookIdle(EntityLiving entitylivingIn, float maxDistance, float idleChance)
    {
        this.theWatcher = entitylivingIn;
        this.maxDistanceForPlayer = maxDistance;
        this.chance = idleChance;
        this.pokemob = CapabilityPokemob.getPokemobFor(theWatcher);
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
        if (theWatcher.getRNG().nextFloat() > chance) return false;
        if (pokemob.getLogicState(LogicStates.SLEEPING) || (pokemob.getStatus() & IPokemob.STATUS_SLP) > 0)
            return false;
        idle = false;
        if (this.theWatcher.getAttackTarget() != null)
        {
            this.closestEntity = this.theWatcher.getAttackTarget();
        }
        this.closestEntity = this.theWatcher.getEntityWorld().findNearestEntityWithinAABB(EntityLivingBase.class,
                this.theWatcher.getEntityBoundingBox().grow(this.maxDistanceForPlayer, 3.0D, this.maxDistanceForPlayer),
                this.theWatcher);
        if (closestEntity != null && closestEntity.getRidingEntity() == theWatcher) closestEntity = null;
        else if (closestEntity != null) return true;
        idle = true;
        return true;
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
                    this.theWatcher.posY + this.theWatcher.getEyeHeight(), this.theWatcher.posZ + this.lookZ, 3.0F,
                    this.theWatcher.getVerticalFaceSpeed());
            return;
        }
        this.theWatcher.getLookHelper().setLookPosition(this.closestEntity.posX,
                this.closestEntity.posY + this.closestEntity.getEyeHeight(), this.closestEntity.posZ, 3.0F,
                this.theWatcher.getVerticalFaceSpeed());
        --this.lookTime;
    }

}
