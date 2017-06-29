package pokecube.adventures.ai.trainers;

import java.util.List;

import com.google.common.base.Predicate;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import pokecube.adventures.comands.Config;
import pokecube.adventures.entity.helper.EntityHasAIStates;
import pokecube.adventures.entity.trainers.EntityTrainer;
import thut.api.maths.Vector3;

public class AITrainerFindTarget extends EntityAIBase
{
    World                             world;

    // The entity (normally a player) that is the target of this trainer.
    Class<? extends EntityLivingBase> targetClass;
    // The trainer Entity
    final EntityTrainer               trainer;

    public AITrainerFindTarget(EntityTrainer trainer, Class<? extends EntityLivingBase> targetClass)
    {
        this.trainer = trainer;
        this.world = trainer.getEntityWorld();
        this.setMutexBits(3);
        this.targetClass = targetClass;
    }

    @Override
    public boolean shouldExecute()
    {
        trainer.lowerCooldowns();
        // Dead trainers can't fight.
        if (!trainer.isEntityAlive()) return false;
        // Trainers on cooldown shouldn't fight, neither should friendly ones
        if (trainer.cooldown > trainer.getEntityWorld().getTotalWorldTime() || trainer.friendlyCooldown > 0)
            return false;
        return true;
    }

    @Override
    public void updateTask()
    { // Predicated to return true for invalid targets
        Predicate<EntityLivingBase> matcher = new Predicate<EntityLivingBase>()
        {
            @Override
            public boolean apply(EntityLivingBase input)
            {
                if (input instanceof EntityPlayer) { return ((EntityPlayer) input).capabilities.isCreativeMode
                        || ((EntityPlayer) input).isSpectator() || trainer.hasDefeated(input); }

                return false;
            }
        };
        // Check if target is invalid.
        if (trainer.getTarget() != null && (trainer.getTarget().isDead || matcher.apply(trainer.getTarget())))
        {
            trainer.setTarget(null);
            trainer.resetPokemob();
            return;
        }
        // If target is valid, return.
        if (trainer.getTarget() != null) return;

        // Look for targets
        Vector3 here = Vector3.getNewVector().set(trainer);
        EntityLivingBase target = null;
        List<? extends EntityLivingBase> targets = world.getEntitiesWithinAABB(targetClass,
                here.getAABB().grow(16, 16, 16));
        int sight = trainer.sight <= 0 ? Config.instance.trainerSightRange : trainer.sight;
        for (Object o : targets)
        {
            EntityLivingBase e = (EntityLivingBase) o;
            // Only visible or valid targets.
            if (Vector3.isVisibleEntityFromEntity(trainer, e) && !matcher.apply(e)
                    && e.getDistanceToEntity(trainer) < sight)
            {
                target = e;
                break;
            }
        }
        // If no target, return false.
        if (target == null)
        {
            // If trainer was in battle (any of these 3) reset trainer before
            // returning.
            if (trainer.outMob != null || trainer.getAIState(EntityHasAIStates.THROWING)
                    || trainer.getAIState(EntityHasAIStates.INBATTLE))
            {
                trainer.resetPokemob();
            }
            return;
        }
        // Set trainers target
        trainer.setTarget(target);
    }
}
