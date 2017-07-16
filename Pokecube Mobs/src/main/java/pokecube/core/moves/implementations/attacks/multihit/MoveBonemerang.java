package pokecube.core.moves.implementations.attacks.multihit;

import pokecube.core.interfaces.IPokemob.MovePacket;
import pokecube.core.moves.templates.Move_Basic;

public class MoveBonemerang extends Move_Basic
{
    public MoveBonemerang()
    {
        super("bonemerang");
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
