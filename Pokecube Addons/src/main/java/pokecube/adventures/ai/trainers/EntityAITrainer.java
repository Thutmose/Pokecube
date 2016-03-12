package pokecube.adventures.ai.trainers;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import pokecube.adventures.entity.trainers.EntityLeader;
import pokecube.adventures.entity.trainers.EntityTrainer;
import pokecube.core.events.handlers.PCEventsHandler;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.moves.MovesUtils;
import thut.api.maths.Vector3;

public class EntityAITrainer extends EntityAIBase
{

    World                             world;

    // The entity (normally a player) that is the target of this trainer.
    Class<? extends EntityLivingBase> targetClass;
    Vector3                           loc = Vector3.getNewVector();

    // The trainer Entity
    final EntityTrainer               trainer;

    public EntityAITrainer(EntityTrainer trainer, Class<? extends EntityLivingBase> targetClass)
    {
        this.trainer = trainer;
        this.world = trainer.worldObj;
        this.setMutexBits(3);
        this.targetClass = targetClass;
    }

    @Override
    public boolean shouldExecute()
    {
        trainer.lowerCooldowns();
        if (!trainer.isEntityAlive()) return false;
        if (trainer.getTarget() != null || trainer.cooldown > 0) return true;

        Vector3 here = loc.set(trainer);
        EntityLivingBase target = null;
        List<? extends EntityLivingBase> targets = world.getEntitiesWithinAABB(targetClass,
                here.getAABB().expand(16, 16, 16));
        for (Object o : targets)
        {
            EntityLivingBase e = (EntityLivingBase) o;
            if (Vector3.isVisibleEntityFromEntity(trainer, e))
            {
                target = e;
                break;
            }
        }

        if (target == null) return false;

        boolean onCooldown = false;
        for (int i = 0; i < trainer.attackCooldown.length; i++)
        {
            if (trainer.attackCooldown[i] > 0)
            {
                onCooldown = true;
                break;
            }
        }
        if (onCooldown) return false;

        if (target instanceof EntityPlayer)
        {
            EntityPlayer player = (EntityPlayer) target;
            if (player.capabilities.isCreativeMode) target = null;
            else if (trainer.friendlyCooldown > 0) target = null;
            else if (trainer instanceof EntityLeader)
            {
                if (((EntityLeader) trainer).hasDefeated(target)) target = null;
            }
        }
        if (target != trainer.getTarget()) trainer.setTarget(target);
        if (trainer.getTarget() == null) return false;
        return trainer.getTarget() != null;
    }

    /** Execute a one shot task or start executing a continuous task */
    @Override
    public void startExecuting()
    {
    }

    /** Resets the task */
    @Override
    public void resetTask()
    {
        PCEventsHandler.recallAllPokemobs(trainer);
    }

    /** Updates the task */
    @Override
    public void updateTask()
    {
        double distance = trainer.getDistanceSqToEntity(trainer.getTarget());
        trainer.faceEntity(trainer.getTarget(), trainer.rotationPitch, trainer.rotationYaw);
        if (distance > 100 || trainer.cooldown > 0)
        {

        }
        else if (trainer.outMob != null)
        {
            if (checkPokemobTarget())
            {
                if (!considerSwapPokemob()) considerSwapMove();
            }
            else setMostDamagingMove();
        }
        else
        {
            doAggression();
        }
    }

    void doAggression()
    {
        boolean angry = trainer.getTarget() != null;

        if (angry)
        {
            angry = trainer.getTarget() != null;
            if (!Vector3.isVisibleEntityFromEntity(trainer, trainer.getTarget()))
            {
                angry = false;
                trainer.setTarget(null);
            }
        }

        if (trainer instanceof EntityLeader)
        {
            if (((EntityLeader) trainer).hasDefeated(trainer.getTarget()))
            {
                trainer.setTarget(null);
                return;
            }
        }
        if (angry && !trainer.worldObj.isRemote && trainer.outMob == null)
        {
            trainer.throwCubeAt(trainer.getTarget());
        }
    }

    private void considerSwapMove()
    {
        // TODO choose between damaging/stats/status moves
        setMostDamagingMove();
    }

    private void setMostDamagingMove()
    {
        IPokemob outMob = trainer.outMob;
        int index = outMob.getMoveIndex();
        int max = 0;
        Entity target = ((EntityLiving) outMob).getAttackTarget();
        String[] moves = outMob.getMoves();
        for (int i = 0; i < 4; i++)
        {
            String s = moves[i];
            if (s != null)
            {
                int temp = getPower(s, outMob, target);
                if (temp > max)
                {
                    index = i;
                    max = temp;
                }
            }
        }
        outMob.setMoveIndex(index);
    }

    private int getPower(String move, IPokemob user, Entity target)
    {
        Move_Base attack = MovesUtils.getMoveFromName(move);
        return attack.getPWR(user, target);
    }

    private boolean considerSwapPokemob()
    {
        // TODO check if the target pokemob is bad matchup, consider swapping to
        // better choice.
        return false;
    }

    private boolean checkPokemobTarget()
    {
        if (trainer.getTarget() != null)
        {
            if (!trainer.outMob.getPokemonAIState(IPokemob.ANGRY)
                    || ((EntityLiving) trainer.outMob).getAttackTarget() == null)
            {
                ((EntityLiving) trainer.outMob).setAttackTarget(trainer.getTarget());
            }
        }
        return ((EntityLiving) trainer.outMob).getAttackTarget() instanceof IPokemob;
    }
}
