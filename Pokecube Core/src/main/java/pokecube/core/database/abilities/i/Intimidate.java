package pokecube.core.database.abilities.i;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import pokecube.core.database.abilities.Ability;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.moves.MovesUtils;

public class Intimidate extends Ability
{

    @Override
    public void onAgress(IPokemob mob, EntityLivingBase target)
    {
        if(target instanceof IPokemob)
        {
            IPokemob targ = (IPokemob) target;
            MovesUtils.handleStats2(targ, (Entity) mob, IMoveConstants.ATTACK, IMoveConstants.FALL);
        }
    }
    
    @Override
    public void onUpdate(IPokemob mob)
    {
        // TODO interface with spawn event to make lower levels not spawn.

    }

}
