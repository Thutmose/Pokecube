package pokecube.core.moves.templates;

import pokecube.core.interfaces.IPokemob.MovePacket;

public class Move_Doublehit extends Move_Basic
{
    public Move_Doublehit(String name)
    {
        super(name);
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
