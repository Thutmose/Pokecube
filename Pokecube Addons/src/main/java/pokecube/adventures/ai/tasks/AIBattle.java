package pokecube.adventures.ai.tasks;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.commands.Config;
import pokecube.adventures.entity.helper.MessageState;
import pokecube.adventures.entity.helper.capabilities.CapabilityNPCAIStates.IHasNPCAIStates;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.events.handlers.PCEventsHandler;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.moves.MovesUtils;
import pokecube.core.utils.PokeType;
import thut.api.maths.Vector3;
import thut.lib.CompatWrapper;

public class AIBattle extends AITrainerBase
{
    private boolean  canPath   = true;
    private BlockPos battleLoc = null;

    public AIBattle(EntityLivingBase trainer)
    {
        super(trainer);
    }

    public AIBattle(EntityLivingBase trainer, boolean canPath)
    {
        super(trainer);
        this.canPath = canPath;
    }

    @Override
    public void doMainThreadTick(World world)
    {
        super.doMainThreadTick(world);
        trainer.lowerCooldowns();
        if (trainer.getTarget() != null) updateTask();
        else if (trainer.getOutID() != null) resetTask();
    }

    private boolean checkPokemobTarget()
    {
        Entity mobTarget = trainer.getOutMob().getEntity().getAttackTarget();
        IPokemob target = CapabilityPokemob.getPokemobFor(mobTarget);
        if (!trainer.getOutMob().getCombatState(CombatStates.ANGRY))
            trainer.getOutMob().setCombatState(CombatStates.ANGRY, true);
        // check if pokemob's target is same as trainers.
        if (mobTarget != trainer.getTarget() && target == null)
        {
            // If not, set it as such.
            trainer.getOutMob().getEntity().setAttackTarget(trainer.getTarget());
        }
        // Return if trainer's pokemob's target is also a pokemob.
        return CapabilityPokemob.getPokemobFor(trainer.getOutMob().getEntity().getAttackTarget()) != null;
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

        // check if can mega evolve
        IPokemob out = trainer.getOutMob();
        if (trainer.canMegaEvolve() && out != null && out.getPokedexEntry().hasMegaForm)
        {
            List<PokedexEntry> formes = Database.getFormes(out.getPokedexEntry());
            if (!formes.isEmpty())
            {
                int start = entity.getRNG().nextInt(formes.size());
                for (int i = 0; i < formes.size(); i++)
                {
                    PokedexEntry mega = formes.get((i + start) % formes.size());
                    if (mega.isMega)
                    {
                        out.megaEvolve(mega);
                        break;
                    }
                }
            }

        }
        return false;
    }

    void doAggression()
    {
        // Check if maybe mob was sent out, but just not seen
        List<IPokemob> pokemobs = PCEventsHandler.getOutMobs(entity);
        if (!pokemobs.isEmpty())
        {
            for (IPokemob pokemob : pokemobs)
            {
                // Ones not added to chunk are in pokecubes, so wait for them to
                // exit.
                if (pokemob.getEntity().addedToChunk)
                {
                    trainer.setOutMob(pokemob);
                    return;
                }
            }
            return;
        }
        // If no mob was found, then it means trainer was not throwing cubes, as
        // those are counted along with active pokemobs.
        aiTracker.setAIState(IHasNPCAIStates.THROWING, false);
        // If the trainer is on attack cooldown, then check if to send message
        // about next pokemob, or to return early.
        if (trainer.getAttackCooldown() > 0)
        {
            // If no next pokemob, reset trainer and return early.
            if (!CompatWrapper.isValid(trainer.getNextPokemob()))
            {
                aiTracker.setAIState(IHasNPCAIStates.INBATTLE, false);
                trainer.onDefeated(trainer.getTarget());
                trainer.resetPokemob();
                return;
            }
            // If cooldown is at specific number, send the message for sending
            // out next pokemob.
            if (trainer.getAttackCooldown() == Config.instance.trainerSendOutDelay / 2)
            {
                ItemStack nextStack = trainer.getNextPokemob();
                if (CompatWrapper.isValid(nextStack))
                {
                    IPokemob next = PokecubeManager.itemToPokemob(nextStack, world);
                    if (next != null)
                    {
                        messages.sendMessage(MessageState.ABOUTSEND, trainer.getTarget(), entity.getDisplayName(),
                                next.getPokemonDisplayName(), trainer.getTarget().getDisplayName());
                        messages.doAction(MessageState.ABOUTSEND, trainer.getTarget());
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
        IPokemob mob = CapabilityPokemob.getPokemobFor(target);
        if (mob != null)
        {
            pwr *= PokeType.getAttackEfficiency(attack.getType(user), mob.getType1(), mob.getType2());
        }
        return pwr;
    }

    /** Resets the task */
    public void resetTask()
    {
        trainer.resetPokemob();
        battleLoc = null;
    }

    /** Searches for pokemobs most damaging move against the target, and sets it
     * as current attack */
    private void setMostDamagingMove()
    {
        IPokemob outMob = trainer.getOutMob();
        int index = outMob.getMoveIndex();
        int max = 0;
        Entity target = outMob.getEntity().getAttackTarget();
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

    /** Updates the task */
    public void updateTask()
    {
        if (trainer.getTarget() == null) return;
        // Check if trainer has any pokemobs, if not, cancel agression, no
        // reward.
        if (!CompatWrapper.isValid(trainer.getPokemob(0)))
        {
            trainer.setTarget(null);
            return;
        }

        // Stop trainer from pathing if it shouldn't do so during battle
        if (!canPath && entity instanceof EntityLiving)
        {
            if (battleLoc == null) battleLoc = entity.getPosition();
            ((EntityLiving) entity).getNavigator()
                    .setPath(((EntityLiving) entity).getNavigator().getPathToPos(battleLoc), 0.75);
        }

        // If target is no longer visbile, forget about it and reset.
        if (!Vector3.isVisibleEntityFromEntity(entity, trainer.getTarget()))
        {
            if (noSeeTicks++ > PokecubeAdv.conf.trainerDeAgressTicks)
            {
                trainer.setTarget(null);
                trainer.resetPokemob();
            }
            return;
        }
        noSeeTicks = 0;
        // Check if in range, if too far, target has run away, so forget about
        // it.
        double distance = entity.getDistanceSq(trainer.getTarget());
        if (distance > 1024)
        {
            trainer.setTarget(null);
            trainer.resetPokemob();
        }
        else if (trainer.getOutMob() != null && !trainer.getOutMob().getEntity().isDead
                && trainer.getOutMob().getEntity().addedToChunk)
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
            trainer.setOutMob(null);
            // Do agression code for sending out next pokemob.
            doAggression();
        }
    }
}
