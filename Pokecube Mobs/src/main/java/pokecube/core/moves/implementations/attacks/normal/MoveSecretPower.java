package pokecube.core.moves.implementations.attacks.normal;

import pokecube.core.interfaces.IMoveAnimation;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.MovePacket;
import pokecube.core.moves.templates.Move_Basic;

public class MoveSecretPower extends Move_Basic
{
    public MoveSecretPower()
    {
        super("secretpower");
    }

    @Override
    public void preAttack(MovePacket packet)
    {
        // TODO before super call, add in the needed stats/status/change effects
        // based on terrain.
        super.preAttack(packet);
    }

    @Override
    public IMoveAnimation getAnimation(IPokemob user)
    {
        // TODO make this return animations for the relevant attacks based on
        // location instead.
        return super.getAnimation();
    }
}
