package pokecube.core.moves.implementations.attacks.ongoing;

import net.minecraft.entity.EntityLiving;
import net.minecraft.util.DamageSource;
import pokecube.core.moves.templates.Move_Ongoing;

public class MoveInfestation extends Move_Ongoing
{

    public MoveInfestation()
    {
        super("infestation");
    }

    @Override
    public void doOngoingEffect(EntityLiving mob)
    {
        float thisMaxHP = mob.getMaxHealth();
        int damage = Math.max(1, (int) (0.125 * thisMaxHP));
        mob.attackEntityFrom(DamageSource.generic, damage);
    }
}
