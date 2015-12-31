package pokecube.core.database.abilities.misc;

import net.minecraft.entity.EntityLivingBase;
import pokecube.core.database.abilities.Ability;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.MovePacket;
import pokecube.core.utils.PokeType;

public class Aerilate extends Ability
{

    @Override
    public void onUpdate(IPokemob mob)
    {
    }

    @Override
    public void onMoveUse(IPokemob mob, MovePacket move)
    {

        if (!move.pre) return;
        if (move.attackType == PokeType.normal && mob == move.attacker)
        {
            move.attackType = PokeType.flying;
        }
    }

    @Override
    public void onAgress(IPokemob mob, EntityLivingBase target)
    {
    }
}
