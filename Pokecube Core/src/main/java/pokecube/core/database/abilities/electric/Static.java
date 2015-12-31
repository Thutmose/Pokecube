package pokecube.core.database.abilities.electric;

import net.minecraft.entity.EntityLivingBase;
import pokecube.core.database.abilities.Ability;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.interfaces.IPokemob.MovePacket;

public class Static extends Ability
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
        if (attacker == mob || move.pre || attacker == move.attacked) return;
        if (move.hit && attack.getAttackCategory() == IMoveConstants.CATEGORY_CONTACT && Math.random() > 0.7)
        {
            move.attacker.setStatus(IMoveConstants.STATUS_PAR);
        }
    }

    @Override
    public void onAgress(IPokemob mob, EntityLivingBase target)
    {
    }
}
