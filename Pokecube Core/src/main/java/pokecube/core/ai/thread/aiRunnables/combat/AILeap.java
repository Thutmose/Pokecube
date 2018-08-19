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
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import thut.api.maths.Vector3;

public class AILeap extends AIBase
{
    final EntityLiving attacker;
    final IPokemob     pokemob;
    Entity             target;
    int                leapCooldown = 10;
    double             movementSpeed;

    public AILeap(IPokemob entity)
    {
        this.attacker = entity.getEntity();
        this.pokemob = entity;
        this.movementSpeed = attacker.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue()
                * 0.8;
        this.setMutex(0);
    }

    /** Gets a random sound to play on leaping, selects from the options in
     * config. */
    private SoundEvent getLeapSound()
    {
        if (PokecubeCore.core.getConfig().leaps.length == 1) return PokecubeCore.core.getConfig().leaps[0];
        return PokecubeCore.core.getConfig().leaps[new Random().nextInt(PokecubeCore.core.getConfig().leaps.length)];
    }

    @Override
    public void reset()
    {
        this.target = null;
    }

    @Override
    public void run()
    {
        // This case is where target is the attack target.
        if (target != null)
        {
            // Use horizontal distance to allow floating things to leap
            // downwards.
            double d0 = this.attacker.posX - this.target.posX;
            double d2 = this.attacker.posZ - this.target.posZ;
            double d3 = this.attacker.posY - this.target.posY;
            /* Don't leap up if too far. */
            if (d3 < -5) return;

            double dist = d0 * d0 + d2 * d2;
            float diff = attacker.width + target.width;
            diff = diff * diff;

            // Wait till it is a bit closer than this...
            if (dist >= 16.0D) { return; }
        }
        pokemob.setCombatState(CombatStates.LEAPING, false);

        leapCooldown = PokecubeCore.core.getConfig().attackCooldown / 2;

        // Target loc could just be a position
        Vector3 targetLoc = target != null ? Vector3.getNewVector().set(target) : pokemob.getTargetPos();
        Vector3 leaperLoc = Vector3.getNewVector().set(attacker);
        Vector3 dir = targetLoc.subtract(leaperLoc);
        /*
         * Scale by 0.2 to make it take roughly half a second for leap to reach
         * target. Note that this doesn't factor in gravity, so leaps up take a
         * bit longer than that.
         */
        double d = dir.mag() * 0.2;
        dir.norm();
        if (d > 5) dir.scalarMultBy(d * 0.2f);
        if (dir.magSq() < 1) dir.norm();
        if (dir.isNaN())
        {
            new Exception().printStackTrace();
            dir.clear();
        }
        if (PokecubeCore.debug)
        {
            PokecubeMod.log(Level.INFO, "Leap: " + attacker + " " + dir);
        }
        double dy = Math.abs(dir.y);
        /*
         * Make adjustments so mobs can hit flying things more easily.
         */
        if (!attacker.onGround && dy > pokemob.getSize() * pokemob.getPokedexEntry().height && dy < 3) dir.y *= 2;
        /*
         * Apply the leap
         */
        dir.addVelocities(attacker);
        toRun.add(new PlaySound(attacker.dimension, Vector3.getNewVector().set(attacker), getLeapSound(),
                SoundCategory.HOSTILE, 1, 1));
    }

    @Override
    public boolean shouldRun()
    {
        return leapCooldown-- < 0 && pokemob.getCombatState(CombatStates.LEAPING)
                && ((target = attacker.getAttackTarget()) != null || pokemob.getTargetPos() != null);
    }

}
