package pokecube.adventures.ai.tasks;

import java.util.List;

import com.google.common.base.Predicate;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import pokecube.adventures.entity.helper.capabilities.CapabilityNPCAIStates.IHasNPCAIStates;
import pokecube.core.moves.MovesUtils;
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
        if (aiTracker != null && aiTracker.getAIState(IHasNPCAIStates.FIXEDDIRECTION) && trainer.getTarget() == null)
        {
            entity.setRotationYawHead(aiTracker.getDirection());
            entity.prevRotationYawHead = aiTracker.getDirection();
            entity.rotationYawHead = aiTracker.getDirection();
            entity.rotationYaw = aiTracker.getDirection();
            entity.prevRotationYaw = aiTracker.getDirection();
        }
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
        EntityLivingBase target = null;
        int sight = trainer.getAgressDistance();
        targetTrack:
        {
            here.addTo(0, entity.getEyeHeight(), 0);
            Vector3 look = Vector3.getNewVector().set(entity.getLook(1));
            here.addTo(look);
            look.scalarMultBy(sight);
            look.addTo(here);
            List<EntityLivingBase> targets = MovesUtils.targetsHit(entity, look);
            for (Object o : targets)
            {
                EntityLivingBase e = (EntityLivingBase) o;
                double dist = e.getDistanceToEntity(entity);
                // Only visible or valid targets.
                if (!matcher.apply(e) && dist < sight)
                {
                    target = e;
                    break targetTrack;
                }
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
