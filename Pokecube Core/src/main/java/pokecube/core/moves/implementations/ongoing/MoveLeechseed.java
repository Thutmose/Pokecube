package pokecube.core.moves.implementations.ongoing;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.moves.templates.Move_Ongoing;

public class MoveLeechseed extends Move_Ongoing
{

    public MoveLeechseed()
    {
        super("leechseed");
    }

    @Override
    public void doOngoingEffect(EntityLiving mob)
    {
        if (mob.getAttackTarget() != null)
        {
            EntityLivingBase target = mob.getAttackTarget();
            float factor = 0.0625f;
            if (mob instanceof IPokemob)
            {
                factor *= (((IPokemob) mob).getMoveStats().TOXIC_COUNTER + 1);
            }
            float thisMaxHP = mob.getMaxHealth();
            int damage = Math.max(1, (int) (factor * thisMaxHP));
            mob.attackEntityFrom(DamageSource.GENERIC, damage);
            target.setHealth(Math.min(target.getHealth() + damage, target.getMaxHealth()));
        }
    }

    @Override
    public int getDuration()
    {
        return -1;
    }

}
