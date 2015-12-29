package pokecube.core.database.abilities.misc;

import net.minecraft.entity.EntityLivingBase;
import pokecube.core.database.abilities.Ability;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.MovePacket;

public class ShedSkin extends Ability
{

    @Override
    public void onUpdate(IPokemob mob)
    {
        if (mob.getStatus() != IMoveConstants.STATUS_NON)
        {
            EntityLivingBase poke = (EntityLivingBase) mob;
            if (poke.ticksExisted % 20 == 0 && Math.random() < 0.3)
            {
                mob.setStatus(IMoveConstants.STATUS_NON);
            }
        }
    }

    @Override
    public void onMoveUse(IPokemob mob, MovePacket move)
    {
    }

    @Override
    public void onAgress(IPokemob mob, EntityLivingBase target)
    {
    }
}
