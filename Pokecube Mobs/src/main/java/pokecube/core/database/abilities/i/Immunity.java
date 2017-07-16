package pokecube.core.database.abilities.i;

import net.minecraft.entity.EntityLivingBase;
import pokecube.core.database.abilities.Ability;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.MovePacket;

public class Immunity extends Ability
{

    @Override
    public void onAgress(IPokemob mob, EntityLivingBase target)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void onMoveUse(IPokemob mob, MovePacket move)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void onUpdate(IPokemob mob)
    {
        if ((mob.getStatus() & IMoveConstants.STATUS_PSN) > 0) mob.healStatus();
    }

}
