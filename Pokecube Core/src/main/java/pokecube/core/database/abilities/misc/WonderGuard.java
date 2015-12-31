package pokecube.core.database.abilities.misc;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import pokecube.core.database.abilities.Ability;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.interfaces.IPokemob.MovePacket;
import pokecube.core.utils.PokeType;

public class WonderGuard extends Ability
{

    @Override
    public void onUpdate(IPokemob mob)
    {
    }

    @Override
    public void onMoveUse(IPokemob mob, MovePacket move)
    {

        Move_Base attack = move.getMove();

        IPokemob attacker = move.attacker;

        if (attacker == mob || !move.pre || attacker == move.attacked) return;

        float eff = PokeType.getAttackEfficiency(attack.getType(), mob.getType1(), mob.getType2());
        if (eff <= 1 && attack.getPWR(attacker, (Entity) mob) > 0)
        {
            move.canceled = true;
        }
    }

    @Override
    public void onAgress(IPokemob mob, EntityLivingBase target)
    {
    }

}
