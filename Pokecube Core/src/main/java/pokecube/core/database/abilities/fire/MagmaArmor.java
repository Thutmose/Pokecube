package pokecube.core.database.abilities.fire;

import net.minecraft.entity.EntityLivingBase;
import pokecube.core.database.abilities.Ability;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.MovePacket;

public class MagmaArmor extends Ability
{

    @Override
    public void onUpdate(IPokemob mob)
    {
        if (mob.getStatus() == IMoveConstants.STATUS_FRZ) mob.setStatus(IMoveConstants.STATUS_NON);
    }

    @Override
    public void onMoveUse(IPokemob mob, MovePacket move)
    {
        IPokemob attacker = move.attacker;
        if (attacker == mob || !move.pre || attacker == move.attacked) return;
        if (move.statusChange == IMoveConstants.STATUS_FRZ) move.statusChange = IMoveConstants.STATUS_FRZ;
    }

    @Override
    public void onAgress(IPokemob mob, EntityLivingBase target)
    {
    }

}
