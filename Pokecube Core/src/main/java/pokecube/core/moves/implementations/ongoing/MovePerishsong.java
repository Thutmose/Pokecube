package pokecube.core.moves.implementations.ongoing;

import net.minecraft.entity.EntityLiving;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.moves.templates.Move_Ongoing;

public class MovePerishsong extends Move_Ongoing
{

    public MovePerishsong()
    {
        super("perishsong");
    }

    @Override
    public void doOngoingEffect(EntityLiving mob)
    {
        Move_Ongoing move = this;
        boolean isPokemob = mob instanceof IPokemob;
        if (isPokemob == true)
        {
            int duration = ((IPokemob) mob).getOngoingEffects().get(move);
            if (duration == 0)
            {
                mob.setHealth(0);
            }
            // TODO perish counter here.
        }
        else
        {
            // TODO Insert code for an on-screen message here.
        }
    }

    @Override
    public int getDuration()
    {
        return 3;
    }

    @Override
    public boolean onSource()
    {
        return true;
    }

}
