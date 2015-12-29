package pokecube.core.database.abilities.misc;

import net.minecraft.entity.EntityLivingBase;
import pokecube.core.database.abilities.Ability;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.MovePacket;

public class Adaptability extends Ability
{

    @Override
    public void onUpdate(IPokemob mob)
    {
    }

    @Override
    public void onMoveUse(IPokemob mob, MovePacket move)
    {

        if (!move.pre) return;
        if (mob == move.attacker) move.stabFactor = 2;
    }

    @Override
    public void onAgress(IPokemob mob, EntityLivingBase target)
    {
    }

}
