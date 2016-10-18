package pokecube.core.ai.thread.aiRunnables;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.SoundEvents;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.SoundCategory;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.moves.MovesUtils;
import pokecube.core.utils.Tools;
import thut.api.maths.Vector3;

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
        tryDodge();
        tryLeap();
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
        byte[] mods = pokemob.getModifiers();
        double evasionMod = Tools.modifierToRatio(mods[7], true) / 30d;
        dodge = Math.random() > (1 - evasionMod);
        return dodge;
    }

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
        double d0 = this.attacker.getDistanceSqToEntity(this.target);
        float diff = attacker.width + target.width;
        diff = diff * diff;
        if (!(d0 >= diff && d0 <= 16.0D ? (this.attacker.getRNG().nextInt(5) == 0) : false))
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
