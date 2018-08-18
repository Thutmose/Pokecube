package pokecube.core.ai.thread.aiRunnables.combat;

import java.util.Random;
import java.util.logging.Level;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.thread.aiRunnables.AIBase;
import pokecube.core.ai.utils.pathing.PokemobNavigator;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.Stats;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.moves.MovesUtils;
import thut.api.maths.Vector3;

public class AIDodge extends AIBase
{
    final EntityLiving attacker;
    final IPokemob     pokemob;
    Entity             target;
    double             movementSpeed;
    int                dodgeCooldown = 10;

    public AIDodge(IPokemob entity)
    {
        this.attacker = entity.getEntity();
        this.pokemob = entity;
        this.movementSpeed = attacker.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue()
                * 0.8;
        this.setMutex(0);
    }

    /** Gets a random sound to play on dodging, selects from the options in
     * config. */
    private SoundEvent getDodgeSound()
    {
        if (PokecubeCore.core.getConfig().dodges.length == 1) return PokecubeCore.core.getConfig().dodges[0];
        return PokecubeCore.core.getConfig().dodges[new Random().nextInt(PokecubeCore.core.getConfig().dodges.length)];
    }

    @Override
    public void reset()
    {
        this.target = null;
    }

    /** If the mob should dodge, then make it jump in a random perpendicular
     * direction to where the current combat target is in. This should result in
     * whatever attack is incomming from missing, assuming the incomming attack
     * is dodgeable, and has a thin enough radius of effect. It also make a
     * sound when it occurs. */
    @Override
    public void run()
    {
        if (PokecubeCore.debug)
        {
            PokecubeMod.log(Level.INFO, "Dodge: " + attacker);
        }
        /*
         * Set dodging state to notify attack AI that target is dodging.
         */
        if (!pokemob.getCombatState(CombatStates.DODGING))
        {
            pokemob.setCombatState(CombatStates.DODGING, true);
            dodgeCooldown = PokecubeCore.core.getConfig().attackCooldown;
        }
        /*
         * Compute a random perpendicular direction.
         */
        Vector3 loc = Vector3.getNewVector().set(attacker);
        Vector3 target = Vector3.getNewVector().set(attacker.getAttackTarget());
        Vector3 temp = Vector3.getNewVector();
        Vector3 perp = target.subtractFrom(loc).rotateAboutLine(Vector3.secondAxis, Math.PI / 2, temp);
        if (Math.random() > 0.5) perp = perp.scalarMultBy(-1);
        perp = perp.normalize();
        if (perp.isNaN())
        {
            new Exception().printStackTrace();
            perp.clear();
        }

        double evasionMod = pokemob.getFloatStat(Stats.EVASION, true);
        /*
         * Scale dodge by pokemob's size
         */
        perp.scalarMultBy(pokemob.getPokedexEntry().width * pokemob.getSize());
        /*
         * Scale by evasion modifier
         */
        perp.scalarMultBy(evasionMod);
        if (perp.magSq() > 4) perp.norm().scalarMultBy(2);

        /*
         * Only flying or floating things can dodge properly in the air.
         */
        if (!(pokemob.flys() || pokemob.floats()) && !attacker.onGround) perp.scalarMultBy(0.2);
        /*
         * Apply the dodge
         */
        perp.addVelocities(attacker);
        toRun.add(new PlaySound(attacker.dimension, Vector3.getNewVector().set(attacker), getDodgeSound(),
                SoundCategory.HOSTILE, 1, 1));
    }

    /** Check if the mob should dodge. It checks that the mob can dodge (ie is
     * on ground if it can't float or fly), and then factors in evasion for
     * whether or not the mob should be dodging now.
     * 
     * @return */
    @Override
    public boolean shouldRun()
    {
        // We are already set to dodge, cooldown ensures dodge state lasts long
        // enough to make some ranged attacks miss
        if (pokemob.getCombatState(CombatStates.DODGING))
        {
            if (dodgeCooldown-- < 0) return true;
            // Off cooldown, reset dodge state.
            else pokemob.setCombatState(CombatStates.DODGING, false);
        }
        // Only dodge if there is an attack target.
        if ((target = attacker.getAttackTarget()) == null) return false;
        // Only flying or floating can dodge while in the air
        if (!attacker.onGround && !(pokemob.floats() || pokemob.flys())) return false;
        // Only pokemobs that can path can dodge
        if (attacker.getNavigator() instanceof PokemobNavigator
                && !((PokemobNavigator) attacker.getNavigator()).canNavigate())
            return false;

        IPokemob target = CapabilityPokemob.getPokemobFor(attacker.getAttackTarget());
        if (target != null)
        {
            boolean shouldDodgeMove = target.getCombatState(CombatStates.EXECUTINGMOVE);
            if (shouldDodgeMove)
            {
                /*
                 * Check if the enemy is using a self move, if so, no point in
                 * trying to dodge this.
                 */
                Move_Base move = MovesUtils.getMoveFromName(target.getMove(target.getMoveIndex()));
                if (move == null || ((move.getAttackCategory() & IMoveConstants.CATEGORY_SELF) > 0))
                {
                    shouldDodgeMove = false;
                }
            }
            return shouldDodgeMove;
        }
        /*
         * Scale amount jumped by evasion stat.
         */
        double evasionMod = pokemob.getFloatStat(Stats.EVASION, true) / 30d;
        return Math.random() > (1 - evasionMod);
    }
}
