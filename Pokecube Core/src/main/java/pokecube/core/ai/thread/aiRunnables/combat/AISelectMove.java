package pokecube.core.ai.thread.aiRunnables.combat;

import java.util.Random;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import pokecube.core.ai.thread.aiRunnables.AIBase;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.moves.MovesUtils;
import pokecube.core.utils.Tools;

public class AISelectMove extends AIBase
{
    final EntityLiving attacker;
    final IPokemob     pokemob;
    Entity             target;
    private int        moveIndexCounter = 0;

    public AISelectMove(IPokemob entity)
    {
        this.attacker = entity.getEntity();
        this.pokemob = entity;
        this.setMutex(0);
    }

    @Override
    public void reset()
    {
        this.target = null;
        moveIndexCounter = 0;
    }

    /** Pick a random move on a bit of a random timer.
     * 
     * @return if move swapped */
    protected boolean selectRandomMove()
    {
        Random rand = new Random();
        if (moveIndexCounter++ > rand.nextInt(30))
        {
            int nb = rand.nextInt(5);
            int index = 0;
            for (int i = 0; i < 4; i++)
            {
                index = (nb + i) % 4;
                if (pokemob.getDisableTimer(index) > 0) continue;
                if (pokemob.getMove(index) == null) continue;
                break;
            }
            moveIndexCounter = 0;
            if (index != pokemob.getMoveIndex())
            {
                pokemob.setMoveIndex(index);
                return true;
            }
        }
        return false;
    }

    /** Determine which move to use based on whatever should apply the most
     * damage to the current target.
     * 
     * @return if move swapped */
    protected boolean selectHighestDamage()
    {
        int index = pokemob.getMoveIndex();
        int max = 0;
        String[] moves = pokemob.getMoves();
        double dist = this.attacker.getDistanceSq(this.target.posX, this.target.posY, this.target.posZ);
        for (int i = 0; i < 4; i++)
        {
            String s = moves[i];
            // Cannot select a disabled move.
            if (pokemob.getDisableTimer(i) > 0) continue;
            if (s != null)
            {
                Move_Base m = MovesUtils.getMoveFromName(s);
                int temp = Tools.getPower(s, pokemob, target);
                if (dist > 5 && (m.getAttackCategory() & IMoveConstants.CATEGORY_DISTANCE) > 0)
                {
                    temp *= 1.5;
                }
                if (temp > max)
                {
                    index = i;
                    max = temp;
                }
            }
        }
        // Update index if it changed.
        if (index != pokemob.getMoveIndex())
        {
            pokemob.setMoveIndex(index);
            return true;
        }
        return false;
    }

    /** If in combat, select a move to use. */
    @Override
    public void run()
    {
        // Pokemobs hunting or guarding will always select whatever is strongest
        if (pokemob.getCombatState(CombatStates.GUARDING) || pokemob.getCombatState(CombatStates.HUNTING))
        {
            selectHighestDamage();
            return;
        }

        // Tame pokemobs only run this if they are on guard, otherwise their
        // owner is selecting moves for them.
        if (pokemob.getGeneralState(GeneralStates.TAMED)) return;

        // Select a random move to use.
        selectRandomMove();
    }

    /** Check if the mob is in combat.
     * 
     * @return */
    @Override
    public boolean shouldRun()
    {
        // Should not swap moves if this is set.
        if (pokemob.getCombatState(CombatStates.NOMOVESWAP)) return false;
        // Only swap moves during combat.
        return pokemob.getCombatState(CombatStates.ANGRY) && (target = attacker.getAttackTarget()) != null;
    }
}
