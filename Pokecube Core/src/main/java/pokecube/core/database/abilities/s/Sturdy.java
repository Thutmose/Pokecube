package pokecube.core.database.abilities.s;

import net.minecraft.entity.EntityLivingBase;
import pokecube.core.database.abilities.Ability;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.MovePacket;

public class Sturdy extends Ability
{
    @Override
    public int beforeDamage(IPokemob mob, MovePacket move, int damage)
    {
        if (mob == move.attacked)
        {
            EntityLivingBase target = (EntityLivingBase) mob;
            float hp = target.getHealth();
            float maxHp = target.getMaxHealth();
            if (hp == maxHp && damage >= hp) { return (int) (maxHp) - 1; }
        }
        return damage;
    }
}
