package pokecube.core.database.abilities.l;

import net.minecraft.entity.EntityLivingBase;
import pokecube.core.database.abilities.Ability;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.MovePacket;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.utils.PokeType;

public class Levitate extends Ability
{

    @Override
    public void onAgress(IPokemob mob, EntityLivingBase target)
    {
    }

    @Override
    public void onMoveUse(IPokemob mob, MovePacket move)
    {
        Move_Base attack = move.getMove();
        IPokemob attacker = move.attacker;
        if (attacker == mob || !move.pre || attacker == move.attacked) return;
        if (attack.getType(move.attacker) == PokeType.getType("ground")) move.canceled = true;
    }

    @Override
    public void onUpdate(IPokemob mob)
    {
    }

}
