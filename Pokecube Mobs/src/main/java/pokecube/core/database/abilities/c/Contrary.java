package pokecube.core.database.abilities.c;

import net.minecraft.entity.EntityLivingBase;
import pokecube.core.database.abilities.Ability;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.MovePacket;

public class Contrary extends Ability
{

    @Override
    public void onAgress(IPokemob mob, EntityLivingBase target)
    {
    }

    @Override
    public void onMoveUse(IPokemob mob, MovePacket move)
    {
        if (mob == move.attacked && move.pre)
        {
            move.attackedStatModification = move.attackedStatModification.clone();
            for (int i = 0; i < move.attackedStatModification.length; i++)
            {
                move.attackedStatModification[i] = -move.attackedStatModification[i];
            }
        }
    }

    @Override
    public void onUpdate(IPokemob mob)
    {
    }

}
