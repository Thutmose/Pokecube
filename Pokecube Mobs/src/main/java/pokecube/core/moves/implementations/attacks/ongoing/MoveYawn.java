package pokecube.core.moves.implementations.attacks.ongoing;

import net.minecraft.entity.EntityLiving;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.moves.MovesUtils;
import pokecube.core.moves.templates.Move_Ongoing;

public class MoveYawn extends Move_Ongoing
{
    public MoveYawn()
    {
        super("yawn");
    }

    @Override
    public void doOngoingEffect(EntityLiving mob)
    {
        Move_Ongoing move = this;
        int duration = ((IPokemob) mob).getOngoingEffects().get(move);

        if (duration == 0)
        {
            MovesUtils.setStatus(mob, STATUS_SLP);
        }
    }

    @Override
    public int getDuration()
    {
        return 2;
    }

}
