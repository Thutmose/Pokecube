package pokecube.adventures.ai.trainers;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.comands.Config;
import pokecube.adventures.entity.helper.EntityHasAIStates;
import pokecube.adventures.entity.helper.MessageState;
import pokecube.adventures.entity.trainers.EntityTrainer;
import pokecube.core.events.handlers.PCEventsHandler;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.moves.MovesUtils;
import pokecube.core.utils.PokeType;
import thut.api.maths.Vector3;
import thut.lib.CompatWrapper;

public class AITrainerBattle extends EntityAIBase
{
    World               world;
    // The trainer Entity
    final EntityTrainer trainer;
    int                 noSeeTicks = 0;

    public AITrainerBattle(EntityTrainer trainer)
    {
        this.trainer = trainer;
        this.world = trainer.getEntityWorld();
    }

    @Override
    public boolean shouldExecute()
    {
        return trainer.getTarget() != null;
    }

    private boolean checkPokemobTarget()
    {
        Entity mobTarget = ((EntityLiving) trainer.outMob).getAttackTarget();
        // check if pokemob's target is same as trainers.
        if (mobTarget != trainer.getTarget() && !(mobTarget instanceof IPokemob))
        {
            // If not, set it as such.
            ((EntityLiving) trainer.outMob).setAttackTarget(trainer.getTarget());
        }
        // Return if trainer's pokemob's target is also a pokemob.
        return ((EntityLiving) trainer.outMob).getAttackTarget() instanceof IPokemob;
    }

    private void considerSwapMove()
    {
        // TODO choose between damaging/stats/status moves
        setMostDamagingMove();
    }

    private boolean considerSwapPokemob()
    {
        // TODO check if the target pokemob is bad matchup, consider swapping to
        // better choice.
        return false;
    }

    void doAggression()
    {
        // If target is no longer visbile, forget about it and reset.
        if (!Vector3.isVisibleEntityFromEntity(trainer, trainer.getTarget()))
        {
            if (noSeeTicks++ > PokecubeAdv.conf.trainerDeAgressTicks) trainer.resetPokemob();
            return;
        }
        noSeeTicks = 0;
        // Check if maybe mob was sent out, but just not seen
        List<IPokemob> pokemobs = PCEventsHandler.getOutMobs(trainer);
        if (!pokemobs.isEmpty())
        {
            for (IPokemob pokemob : pokemobs)
            {
                // Ones not added to chunk are in pokecubes, so wait for them to
                // exit.
                if (((Entity) pokemob).addedToChunk)
                {
                    trainer.outMob = pokemob;
                    return;
                }
            }
            return;
        }
        // If no mob was found, then it means trainer was not throwing cubes, as
        // those are counted along with active pokemobs.
        trainer.setAIState(EntityHasAIStates.THROWING, false);
        // If the trainer is on attack cooldown, then check if to send message
        // about next pokemob, or to return early.
        if (trainer.attackCooldown > 0)
        {
            // If no next pokemob, reset trainer and return early.
            if (!CompatWrapper.isValid(trainer.getNextPokemob()))
            {
                trainer.setAIState(EntityHasAIStates.INBATTLE, false);
                trainer.onDefeated(trainer.getTarget());
                trainer.resetPokemob();
                return;
            }
            // If cooldown is at specific number, send the message for sending
            // out next pokemob.
            if (trainer.attackCooldown == Config.instance.trainerSendOutDelay / 2)
            {
                ItemStack nextStack = trainer.getNextPokemob();
                if (CompatWrapper.isValid(nextStack))
                {
                    IPokemob next = PokecubeManager.itemToPokemob(nextStack, world);
                    if (next != null)
                    {
                        trainer.getTarget()
                        .sendMessage(trainer.getMessage(MessageState.ABOUTSEND, trainer.getDisplayName(),
                                next.getPokemonDisplayName(), trainer.getTarget().getDisplayName()));
                        trainer.doAction(MessageState.ABOUTSEND, trainer.getTarget());
                    }
                }
            }
            return;
        }
        // Send next cube at the target.
        trainer.throwCubeAt(trainer.getTarget());
    }

    /** @param move
     *            - the attack to check
     * @param user
     *            - the user of the sttack
     * @param target
     *            - the target of the attack
     * @return - the damage that will be dealt by the attack (before reduction
     *         due to armour) */
    private int getPower(String move, IPokemob user, Entity target)
    {
        Move_Base attack = MovesUtils.getMoveFromName(move);
        int pwr = attack.getPWR(user, target);
        if (target instanceof IPokemob)
        {
            IPokemob mob = (IPokemob) target;
            pwr *= PokeType.getAttackEfficiency(attack.getType(user), mob.getType1(), mob.getType2());
        }
        return pwr;
    }

    /** Resets the task */
    @Override
    public void resetTask()
    {
        trainer.resetPokemob();
    }

    /** Searches for pokemobs most damaging move against the target, and sets it
     * as current attack */
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

    /** Execute a one shot task or start executing a continuous task */
    @Override
    public void startExecuting()
    {
    }

    /** Updates the task */
    @Override
    public void updateTask()
    {
        if (trainer.getTarget() == null) return;
        // Check if in range, if too far, target has run away, so forget about
        // it.
        double distance = trainer.getDistanceSqToEntity(trainer.getTarget());
        if (distance > 1024)
        {
            trainer.setTarget(null);
            trainer.resetPokemob();
        }
        else if (trainer.outMob != null && !((Entity) trainer.outMob).isDead && ((Entity) trainer.outMob).addedToChunk)
        {
            // If trainer has a living, real mob out, tell it to do stuff.
            // Check if pokemob has a valid Pokemob as a target.
            if (checkPokemobTarget())
            {
                // If not swapping the pokemob (not implemented), then Ensure
                // using best move for target.
                if (!considerSwapPokemob()) considerSwapMove();
            }
            // Otherwise, set to most damaging more for non pokemobs.
            else setMostDamagingMove();
        }
        else
        {
            // Set out mob to null if it is dead so the trainer forgets about
            // it.
            trainer.outMob = null;
            // Do agression code for sending out next pokemob.
            doAggression();
        }
    }

}
