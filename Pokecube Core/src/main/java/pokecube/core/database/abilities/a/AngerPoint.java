package pokecube.core.database.abilities.a;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import pokecube.core.database.abilities.Ability;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.MovePacket;
import pokecube.core.moves.MovesUtils;

public class AngerPoint extends Ability
{

    @Override
    public void onUpdate(IPokemob mob)
    {
    }

    @Override
    public void onMoveUse(IPokemob mob, MovePacket move)
    {
        if(move.didCrit && mob == move.attacked)
        {
            MovesUtils.handleStats2(mob, (Entity) move.attacker, IMoveConstants.ATTACK, IMoveConstants.RAISE);
        }
    }

    @Override
    public void onAgress(IPokemob mob, EntityLivingBase target)
    {
    }

}
