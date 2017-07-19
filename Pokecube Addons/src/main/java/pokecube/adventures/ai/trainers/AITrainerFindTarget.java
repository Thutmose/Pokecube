package pokecube.adventures.ai.trainers;

import java.util.List;

import com.google.common.base.Predicate;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import pokecube.adventures.entity.helper.capabilities.CapabilityAIStates;
import pokecube.adventures.entity.helper.capabilities.CapabilityAIStates.IHasAIStates;
import pokecube.adventures.entity.helper.capabilities.CapabilityHasPokemobs;
import pokecube.adventures.entity.helper.capabilities.CapabilityHasPokemobs.IHasPokemobs;
import thut.api.maths.Vector3;

public class AITrainerFindTarget extends EntityAIBase
{
    World                             world;

    // The entity (normally a player) that is the target of this trainer.
    Class<? extends EntityLivingBase> targetClass;
    final EntityLivingBase            entity;
    final IHasAIStates                aiStates;
    final IHasPokemobs                trainer;

    public AITrainerFindTarget(EntityLivingBase trainer, Class<? extends EntityLivingBase> targetClass)
    {
        this.entity = trainer;
        if (trainer.hasCapability(CapabilityAIStates.AISTATES_CAP, null))
            this.aiStates = trainer.getCapability(CapabilityAIStates.AISTATES_CAP, null);
        else this.aiStates = (IHasAIStates) trainer;
        if (trainer.hasCapability(CapabilityHasPokemobs.HASPOKEMOBS_CAP, null))
            this.trainer = trainer.getCapability(CapabilityHasPokemobs.HASPOKEMOBS_CAP, null);
        else this.trainer = (IHasPokemobs) trainer;
        this.world = trainer.getEntityWorld();
        this.setMutexBits(0);
        this.targetClass = targetClass;
    }

    @Override
    public boolean shouldExecute()
    {
        // Dead trainers can't fight.
        if (!entity.isEntityAlive()) return false;
        // Trainers on cooldown shouldn't fight, neither should friendly ones
        if (trainer.getCooldown() > entity.getEntityWorld().getTotalWorldTime() || !trainer.isAgressive()) return false;
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
                        || ((EntityPlayer) input).isSpectator() || !trainer.canBattle(input); }
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
        Vector3 here = Vector3.getNewVector().set(entity);
        EntityLivingBase target = null;
        List<? extends EntityLivingBase> targets = world.getEntitiesWithinAABB(targetClass,
                here.getAABB().expand(16, 16, 16));

        int sight = trainer.getAgressDistance();
        for (Object o : targets)
        {
            EntityLivingBase e = (EntityLivingBase) o;
            // Only visible or valid targets.
            if (Vector3.isVisibleEntityFromEntity(entity, e) && !matcher.apply(e)
                    && e.getDistanceToEntity(entity) < sight)
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
            if (trainer.getOutMob() != null || aiStates.getAIState(IHasAIStates.THROWING)
                    || aiStates.getAIState(IHasAIStates.INBATTLE))
            {
                trainer.resetPokemob();
            }
            return;
        }
        // Set trainers target
        trainer.setTarget(target);
    }
}
