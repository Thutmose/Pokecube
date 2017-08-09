package pokecube.adventures.ai.tasks;

import java.util.List;

import com.google.common.base.Predicate;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import pokecube.adventures.entity.helper.capabilities.CapabilityNPCAIStates.IHasNPCAIStates;
import thut.api.maths.Vector3;

public class AIFindTarget extends AITrainerBase
{

    // The entity (normally a player) that is the target of this trainer.
    Class<? extends EntityLivingBase> targetClass;

    public AIFindTarget(EntityLivingBase trainer, Class<? extends EntityLivingBase> targetClass)
    {
        super(trainer);
        this.targetClass = targetClass;
    }

    @Override
    public void doMainThreadTick(World world)
    {
        super.doMainThreadTick(world);
        trainer.lowerCooldowns();
        if (shouldExecute()) updateTask();
    }

    public boolean shouldExecute()
    {
        // Dead trainers can't fight.
        if (!entity.isEntityAlive()) return false;
        // Trainers on cooldown shouldn't fight, neither should friendly ones
        if (trainer.getCooldown() > entity.getEntityWorld().getTotalWorldTime() || !trainer.isAgressive())
        {
            if (trainer.getTarget() != null)
            { // Check if target is invalid.
                if (trainer.getTarget() != null && trainer.getTarget().isDead)
                {
                    trainer.setTarget(null);
                    trainer.resetPokemob();
                }
            }
            return false;
        }
        return true;
    }

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
        int range = trainer.getAgressDistance() + 1;
        EntityLivingBase target = null;
        List<? extends EntityLivingBase> targets = world.getEntitiesWithinAABB(targetClass,
                here.getAABB().expand(range, range, range));

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
            if (trainer.getOutMob() != null || aiTracker.getAIState(IHasNPCAIStates.THROWING)
                    || aiTracker.getAIState(IHasNPCAIStates.INBATTLE))
            {
                trainer.resetPokemob();
            }
            return;
        }
        // Set trainers target
        trainer.setTarget(target);
    }
}
