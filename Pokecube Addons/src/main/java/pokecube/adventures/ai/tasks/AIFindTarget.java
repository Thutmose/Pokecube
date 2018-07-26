package pokecube.adventures.ai.tasks;

import java.util.List;

import com.google.common.base.Predicate;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import pokecube.adventures.entity.helper.capabilities.CapabilityHasPokemobs.ITargetWatcher;
import pokecube.adventures.entity.helper.capabilities.CapabilityNPCAIStates.IHasNPCAIStates;
import pokecube.core.moves.MovesUtils;
import thut.api.maths.Vector3;

public class AIFindTarget extends AITrainerBase implements ITargetWatcher
{
    // The entity (normally a player) that is the target of this trainer.
    final Class<? extends EntityLivingBase>[] targetClass;
    // Predicated to return true for invalid targets
    final Predicate<EntityLivingBase>         validTargets;

    private float                             agroChance = 1f;

    @SafeVarargs
    public AIFindTarget(EntityLivingBase entityIn, float agressionProbability,
            Class<? extends EntityLivingBase>... targetClass)
    {
        super(entityIn);
        this.trainer.addTargetWatcher(this);
        this.targetClass = targetClass;
        validTargets = new Predicate<EntityLivingBase>()
        {
            private boolean validClass(EntityLivingBase input)
            {
                for (Class<? extends EntityLivingBase> s : targetClass)
                {
                    if (s.isInstance(input)) return true;
                }
                return false;
            }

            @Override
            public boolean apply(EntityLivingBase input)
            {
                // If the input has attacked us recently, then return true
                // regardless of following checks.
                if (input.getLastAttackedEntity() == entity
                        && input.ticksExisted - input.getLastAttackedEntityTime() < 50)
                    return true;
                // Only target valid classes.
                if (!validClass(input) || !input.attackable()) return false;
                // Don't target pets
                if (input instanceof IEntityOwnable && ((IEntityOwnable) input).getOwner() == entityIn) return false;
                // Don't target invulnerable players (spectator/creative)
                if (input instanceof EntityPlayer
                        && (((EntityPlayer) input).capabilities.isCreativeMode || ((EntityPlayer) input).isSpectator()))
                    return false;
                // Return true if player can battle the input.
                return trainer.canBattle(input);
            }
        };
        agroChance = agressionProbability;
    }

    @SafeVarargs
    public AIFindTarget(EntityLivingBase entityIn, Class<? extends EntityLivingBase>... targetClass)
    {
        this(entityIn, 1, targetClass);
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
        if (trainer.getTarget() != null)
        { // Check if target is invalid.
            if (trainer.getTarget() != null && trainer.getTarget().isDead)
            {
                trainer.setTarget(null);
                trainer.resetPokemob();
                return false;
            }
        }
        // Dead trainers can't fight.
        if (!entity.isEntityAlive() || entity.ticksExisted % 20 != 0) return false;
        // Permfriendly trainers shouldn't fight.
        if (aiTracker != null && aiTracker.getAIState(IHasNPCAIStates.PERMFRIENDLY)) return false;
        // Trainers on cooldown shouldn't fight, neither should friendly ones
        if (trainer.getCooldown() > entity.getEntityWorld().getTotalWorldTime()
                || !trainer.isAgressive()) { return false; }
        return true;
    }

    public void updateTask()
    {
        // If target is valid, return.
        if (trainer.getTarget() != null) return;

        // Check random chance of actually aquiring a target.
        if (Math.random() > agroChance) return;

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
            if (!targets.isEmpty()) for (Object o : targets)
            {
                EntityLivingBase e = (EntityLivingBase) o;
                double dist = e.getDistance(entity);
                // Only visible or valid targets.
                if (validTargetSet(e) && dist < sight)
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

    @Override
    public boolean validTargetSet(EntityLivingBase target)
    {
        return validTargets.apply(target);
    }
}
