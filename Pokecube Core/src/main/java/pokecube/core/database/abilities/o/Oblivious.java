package pokecube.core.database.abilities.o;

import net.minecraft.entity.EntityLivingBase;
import pokecube.core.database.abilities.Ability;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.MovePacket;

public class Oblivious extends Ability
{

    @Override
    public void onAgress(IPokemob mob, EntityLivingBase target)
    {
    }

    @Override
    public void onMoveUse(IPokemob mob, MovePacket move)
    {
        mob.getMoveStats().infatuateTarget = null;
        move.infatuate[0] = false;
    }

    @Override
    public void onUpdate(IPokemob mob)
    {
        mob.getMoveStats().infatuateTarget = null;
    }

}
