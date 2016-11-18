package pokecube.core.ai.thread.aiRunnables;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.SoundEvents;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.SoundCategory;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.StatModifiers.DefaultModifiers;
import pokecube.core.interfaces.IPokemob.Stats;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.moves.MovesUtils;
import thut.api.maths.Vector3;

/** This IAIRunnable manages the movement of the mob while it is in combat, but
 * on cooldown between attacks. It also manages the leaping at targets, and the
 * dodging of attacks. */
public class AICombatMovement extends AIBase
{
    final EntityLiving attacker;
    final IPokemob     pokemob;
    Entity             target;
    Vector3            centre;
    double             movementSpeed;

    public AICombatMovement(EntityLiving par1EntityLiving)
    {
        this.attacker = par1EntityLiving;
        this.pokemob = (IPokemob) attacker;
        this.movementSpeed = attacker.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue()
                * 0.8;
        centre = null;
        this.setMutex(0);
    }

    @Override
    public void reset()
    {
        if (target == null) centre = null;
    }

    @Override
    public void run()
    {
        if (centre == null)
        {
            Vector3 targetLoc = Vector3.getNewVector().set(target);
            Vector3 attackerLoc = Vector3.getNewVector().set(attacker);
            Vector3 diff = targetLoc.addTo(attackerLoc).scalarMultBy(0.5);
            centre = diff;
            centre.y = Math.min(attackerLoc.y, targetLoc.y);
        }
        // See if it should dodge or leap.
        tryDodge();
        tryLeap();
        // If the mob has a path already, check if it is near the end, if not,
        // return early.
        if (!attacker.getNavigator().noPath())
        {
            Vector3 end = Vector3.getNewVector().set(attacker.getNavigator().getPath().getFinalPathPoint());
            Vector3 here = Vector3.getNewVector().set(attacker);
            float f = this.attacker.width;
            f = Math.max(f, 0.5f);
            if (here.distTo(end) > f) { return; }
        }

        Vector3 here = Vector3.getNewVector().set(attacker);
        Vector3 diff = here.subtract(centre);
        if (diff.magSq() < 1) diff.norm();
        int combatDistance = PokecubeMod.core.getConfig().combatDistance;
        combatDistance = Math.max(combatDistance, 2);
        int combatDistanceSq = combatDistance * combatDistance;
        // If the mob has left the combat radius, try to return to the centre of
        // combat. Otherwise, find a random spot in a consistant direction
        // related to the center to run in, this results in the mobs somewhat
        // circling the middle, and reversing direction every 10 seconds or so.
        if (diff.magSq() > combatDistanceSq)
        {
            pokemob.setPokemonAIState(IMoveConstants.LEAPING, false);
            Path path = attacker.getNavigator().getPathToPos(centre.getPos());
            addEntityPath(attacker, path, movementSpeed);
        }
        else
        {
            Vector3 perp = diff.horizonalPerp().scalarMultBy(combatDistance);
            int revTime = 200;
            if (attacker.ticksExisted % revTime > revTime / 2) perp.reverse();
            perp.addTo(here);
            if (Math.abs(perp.y - centre.y) > combatDistance / 2) perp.y = centre.y;
            Path path = attacker.getNavigator().getPathToPos(perp.getPos());
            addEntityPath(attacker, path, movementSpeed);
        }
    }

    /** If the mob should dodge, then make it jump in a random perpendicular
     * direction to where the current combat target is in. This should result in
     * whatever attack is incomming from missing, assuming the incomming attack
     * is dodgeable, and has a thin enough radius of effect. It also make a
     * sound when it occurs. */
    public void tryDodge()
    {
        if (!shouldDodge()) return;
        Vector3 loc = Vector3.getNewVector().set(pokemob);
        Vector3 target = Vector3.getNewVector().set(attacker.getAttackTarget());
        Vector3 temp = Vector3.getNewVector();
        Vector3 perp = target.subtractFrom(loc).rotateAboutLine(Vector3.secondAxis, Math.PI / 2, temp);
        if (Math.random() > 0.5) perp = perp.scalarMultBy(-1);
        pokemob.setPokemonAIState(IMoveConstants.DODGING, true);
        perp = perp.normalize();
        if (perp.isNaN())
        {
            new Exception().printStackTrace();
            perp.clear();
        }
        perp.scalarMultBy(pokemob.getPokedexEntry().width * pokemob.getSize());
        perp.addVelocities(attacker);
        toRun.add(new PlaySound(attacker.dimension, Vector3.getNewVector().set(attacker),
                SoundEvents.ENTITY_GENERIC_SMALL_FALL, SoundCategory.HOSTILE, 1, 1));
    }

    /** Check if the mob should dodge. It checks that the mob can dodge (ie is
     * on ground if it can't float or fly), and then factors in evasion for
     * whether or not the mob should be dodging now.
     * 
     * @return */
    boolean shouldDodge()
    {
        boolean dodge = false;
        if (!attacker.onGround && !(pokemob.getPokedexEntry().floats() || pokemob.getPokedexEntry().flys()))
            return dodge;
        if (attacker.getAttackTarget() instanceof IPokemob)
        {
            IPokemob target = (IPokemob) attacker.getAttackTarget();
            boolean shouldDodgeMove = target.getPokemonAIState(IMoveConstants.EXECUTINGMOVE);
            if (shouldDodgeMove)
            {
                Move_Base move = MovesUtils.getMoveFromName(target.getMove(target.getMoveIndex()));
                if (move == null || ((move.getAttackCategory() & IMoveConstants.CATEGORY_SELF) > 0))
                {
                    shouldDodgeMove = false;
                }
            }
            if (!shouldDodgeMove) return shouldDodgeMove;
        }
        DefaultModifiers mods = pokemob.getModifiers().getDefaultMods();
        double evasionMod = mods.getModifier(Stats.EVASION) / 30d;
        dodge = Math.random() > (1 - evasionMod);
        return dodge;
    }

    /** Attempts to leap at the target during combat, make a sound when it
     * leaps. */
    public void tryLeap()
    {
        if (!pokemob.getPokemonAIState(IMoveConstants.LEAPING)) return;

        if (target instanceof IPokemob)
        {
            IPokemob targ = (IPokemob) target;
            if (!targ.getPokemonAIState(IMoveConstants.ANGRY))
            {
                ((EntityLiving) targ).setAttackTarget(attacker);
                targ.setPokemonAIState(IMoveConstants.ANGRY, true);
            }
        }
        double d0 = this.attacker.posX - this.target.posX;
        double d2 = this.attacker.posZ - this.target.posZ;
        // Use horizontal distance to allow floating things to leap downwards.
        double dist = d0 * d0 + d2 * d2;
        float diff = attacker.width + target.width;
        diff = diff * diff;
        if (!(dist >= diff && dist <= 16.0D ? (this.attacker.getRNG().nextInt(5) == 0) : false))
        {
            // TODO see if need to path to target
            return;
        }
        pokemob.setPokemonAIState(IMoveConstants.LEAPING, false);
        Vector3 targetLoc = Vector3.getNewVector().set(target);
        Vector3 leaperLoc = Vector3.getNewVector().set(attacker);
        Vector3 dir = targetLoc.subtract(leaperLoc).scalarMultBy(0.5f);
        if (dir.magSq() < 1) dir.norm();
        if (dir.isNaN())
        {
            new Exception().printStackTrace();
            dir.clear();
        }
        if (!attacker.onGround) dir.y *= 2;
        dir.addVelocities(attacker);
        toRun.add(new PlaySound(attacker.dimension, Vector3.getNewVector().set(attacker),
                SoundEvents.ENTITY_GENERIC_SMALL_FALL, SoundCategory.HOSTILE, 1, 1));
    }

    @Override
    public boolean shouldRun()
    {
        return (target = attacker.getAttackTarget()) != null && pokemob.getPokemonAIState(IMoveConstants.ANGRY);
    }
}
