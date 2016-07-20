package pokecube.core.moves.implementations.multihit;

import pokecube.core.interfaces.IPokemob.MovePacket;
import pokecube.core.moves.templates.Move_Basic;

public class MoveDoublehit extends Move_Basic
{
    public MoveDoublehit()
    {
        super("doublehit");
    }

    @Override
    public void onAttack(MovePacket packet)
    {
        MovePacket second = new MovePacket(packet.attacker, packet.attacked, packet.attack, packet.attackType,
                packet.PWR, packet.criticalLevel, packet.statusChange, packet.changeAddition);
        super.onAttack(packet);
        super.onAttack(second);
    }
}
