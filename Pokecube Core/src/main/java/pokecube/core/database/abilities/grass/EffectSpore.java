package pokecube.core.database.abilities.grass;

import java.util.Random;

import net.minecraft.entity.EntityLivingBase;
import pokecube.core.database.abilities.Ability;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.interfaces.IPokemob.MovePacket;
import pokecube.core.utils.PokeType;

public class EffectSpore extends Ability
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
        if (attacker == mob || move.pre || attacker == move.attacked || attacker.isType(PokeType.grass)) return;
        if (move.hit && attack.getAttackCategory() == IMoveConstants.CATEGORY_CONTACT && Math.random() > 0.7)
        {
            int num = new Random().nextInt(30);
            if (num < 9)
            {
                move.attacker.setStatus(IMoveConstants.STATUS_PSN);
            }
            if (num < 19)
            {
                move.attacker.setStatus(IMoveConstants.STATUS_PAR);
            }
            else
            {
                move.attacker.setStatus(IMoveConstants.STATUS_SLP);
            }
        }
    }

    @Override
    public void onAgress(IPokemob mob, EntityLivingBase target)
    {
    }

}
