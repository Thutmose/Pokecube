package pokecube.core.ai.pokemob;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;

public class PokemobAILook extends EntityAIBase
{
    protected EntityLiving            theWatcher;
    /** The closest entity which is being watched by this one. */
    protected Entity                  closestEntity;
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

    public PokemobAILook(EntityLiving entitylivingIn, Class<? extends Entity> watchTargetClass, float maxDistance)
    {
        this(entitylivingIn, watchTargetClass, maxDistance, 0.02f);
    }

    public PokemobAILook(EntityLiving entitylivingIn, Class<? extends Entity> watchTargetClass, float maxDistance,
            float chanceIn)
    {
        this.theWatcher = entitylivingIn;
        this.watchedClass = watchTargetClass;
        this.maxDistanceForPlayer = maxDistance;
        this.chance = chanceIn;
        this.setMutexBits(2);
    }

    /** Returns whether the EntityAIBase should begin execution. */
    public boolean shouldExecute()
    {
        if (this.theWatcher.getRNG().nextFloat() >= this.chance)
        {
            return false;
        }
        else if (theWatcher.getRNG().nextFloat() < 0.02F)
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
                this.closestEntity = this.theWatcher.worldObj.getClosestPlayerToEntity(this.theWatcher,
                        (double) this.maxDistanceForPlayer);
            }
            else
            {
                this.closestEntity = this.theWatcher.worldObj.findNearestEntityWithinAABB(
                        this.watchedClass, this.theWatcher.getEntityBoundingBox()
                                .expand((double) this.maxDistanceForPlayer, 3.0D, (double) this.maxDistanceForPlayer),
                        this.theWatcher);
            }

            return this.closestEntity != null;
        }
    }

    /** Returns whether an in-progress EntityAIBase should continue executing */
    public boolean continueExecuting()
    {
        if (idle) return this.idleTime >= 0;
        return !this.closestEntity.isEntityAlive() ? false
                : (this.theWatcher.getDistanceSqToEntity(
                        this.closestEntity) > (double) (this.maxDistanceForPlayer * this.maxDistanceForPlayer) ? false
                                : this.lookTime > 0);
    }

    /** Execute a one shot task or start executing a continuous task */
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

    /** Resets the task */
    public void resetTask()
    {
        this.closestEntity = null;
        idle = false;
    }
    /**
     * Determine if this AI Task is interruptible by a higher (= lower value) priority task. All vanilla AITask have
     * this value set to true.
     */
    public boolean isInterruptible()
    {
        return true;
    }
    /** Updates the task */
    public void updateTask()
    {
        if (idle)
        {
            --this.idleTime;
            this.theWatcher.getLookHelper().setLookPosition(this.theWatcher.posX + this.lookX,
                    this.theWatcher.posY + (double) this.theWatcher.getEyeHeight(), this.theWatcher.posZ + this.lookZ,
                    10.0F, (float) this.theWatcher.getVerticalFaceSpeed());
            return;
        }
        this.theWatcher.getLookHelper().setLookPosition(this.closestEntity.posX,
                this.closestEntity.posY + (double) this.closestEntity.getEyeHeight(), this.closestEntity.posZ, 10.0F,
                (float) this.theWatcher.getVerticalFaceSpeed());
        --this.lookTime;
    }

}
