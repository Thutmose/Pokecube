package pokecube.core.moves.implementations.attacks.fixedorcustom;

import net.minecraft.entity.Entity;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.Stats;
import pokecube.core.moves.templates.Move_Basic;

public class MoveGyroball extends Move_Basic
{

    public MoveGyroball()
    {
        super("gyroball");
    }

    @Override
    public int getPWR(IPokemob user, Entity target)
    {
        if (!(target instanceof IPokemob)) return 50;
        int targetSpeed = ((IPokemob) target).getStat(Stats.VIT, true);
        int userSpeed = user.getStat(Stats.VIT, true);
        int pwr = 25 * targetSpeed / userSpeed;
        return pwr;
    }
}
