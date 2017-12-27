package pokecube.core.moves.templates;

import java.util.Random;

import net.minecraft.util.DamageSource;
import pokecube.core.interfaces.entity.IOngoingAffected;
import pokecube.core.interfaces.entity.IOngoingAffected.IOngoingEffect;
import pokecube.core.interfaces.entity.impl.OngoingMoveEffect;

public class Move_Ongoing extends Move_Basic
{

    public Move_Ongoing(String name)
    {
        super(name);
    }

    public void doOngoingEffect(IOngoingAffected mob, IOngoingEffect effect)
    {
        float thisMaxHP = mob.getEntity().getMaxHealth();
        int damage = Math.max(1, (int) (0.0625 * thisMaxHP));
        mob.getEntity().attackEntityFrom(DamageSource.GENERIC, damage);
    }

    /** I have these attacks affecting the target roughly once per 40 ticks,
     * this duration is how many times it occurs -1 can be used for a move that
     * occurs until the mob dies or returns to cube.
     * 
     * @return the number of times this can affect the target */

    public int getDuration()
    {
        Random r = new Random();
        return 4 + r.nextInt(2);
    }

    /** Does this apply an ongoing move to the attacker
     * 
     * @return */
    public boolean onSource()
    {
        return false;
    }

    /** Is and ongoing move applied to the source
     * 
     * @return */
    public boolean onTarget()
    {
        return true;
    }

    public Class<? extends OngoingMoveEffect> getEffectClass()
    {
        return OngoingMoveEffect.class;
    }

    public OngoingMoveEffect makeEffect()
    {
        OngoingMoveEffect effect = new OngoingMoveEffect();
        effect.setDuration(getDuration());
        effect.move = this;
        return effect;
    }
}
