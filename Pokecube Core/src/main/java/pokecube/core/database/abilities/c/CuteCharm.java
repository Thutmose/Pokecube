package pokecube.core.database.abilities.c;

import net.minecraft.entity.EntityLivingBase;
import pokecube.core.database.abilities.Ability;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.MovePacket;
import pokecube.core.interfaces.Move_Base;

public class CuteCharm extends Ability
{

    @Override
    public void onAgress(IPokemob mob, EntityLivingBase target)
    {
    }

    @Override
    public void onMoveUse(IPokemob mob, MovePacket move)
    {
        if (mob != move.attacked || move.pre || move.attacker == move.attacked) return;
        Move_Base attack = move.getMove();
        if (attack == null || (attack.getAttackCategory() & IMoveConstants.CATEGORY_CONTACT) == 0) return;
        move.infatuateTarget = move.infatuateTarget || Math.random() > 0.7;
    }

    @Override
    public void onUpdate(IPokemob mob)
    {
    }

}
