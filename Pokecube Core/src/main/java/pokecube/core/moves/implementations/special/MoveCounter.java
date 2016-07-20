package pokecube.core.moves.implementations.special;

import net.minecraft.entity.EntityLivingBase;
import pokecube.core.interfaces.IPokemob.MovePacket;
import pokecube.core.moves.PokemobDamageSource;
import pokecube.core.moves.templates.Move_Basic;

public class MoveCounter extends Move_Basic
{

    public MoveCounter()
    {
        super("counter");
    }

    @Override
    public void postAttack(MovePacket packet)
    {
        super.postAttack(packet);
        if (packet.canceled || packet.failed) return;
        if (!packet.attacker.getMoveStats().biding)
        {
            packet.attacker.getMoveStats().SELFRAISECOUNTER = 30;
            packet.attacker.getMoveStats().biding = true;
            packet.attacker.getMoveStats().PHYSICALDAMAGETAKENCOUNTER = 0;
        }
        else
        {
            if (packet.attacker.getMoveStats().SELFRAISECOUNTER == 0)
            {
                int damage = 2 * packet.attacker.getMoveStats().PHYSICALDAMAGETAKENCOUNTER;
                packet.attacker.getMoveStats().PHYSICALDAMAGETAKENCOUNTER = 0;
                if (packet.attacked != null) packet.attacked.attackEntityFrom(
                        new PokemobDamageSource("mob", (EntityLivingBase) packet.attacker, this), damage);
                packet.attacker.getMoveStats().biding = false;
            }
        }
    }
}
