package pokecube.core.moves.implementations.multihit;

import java.util.Random;

import pokecube.core.interfaces.IPokemob.MovePacket;
import pokecube.core.moves.templates.Move_Basic;

public class MoveSpikecannon extends Move_Basic
{
    public MoveSpikecannon()
    {
        super("spikecannon");
    }

    @Override
    public void onAttack(MovePacket packet)
    {
        int count = 2;
        int random = (new Random()).nextInt(6);
        switch (random)
        {
        case 1:
            count = 2;
        case 2:
            count = 3;
        case 3:
            count = 3;
        case 4:
            count = 4;
        case 5:
            count = 5;
        default:
            count = 2;
        }
        for (int i = 0; i <= count; i++)
        {
            MovePacket second = new MovePacket(packet.attacker, packet.attacked, packet.attack, packet.attackType,
                    packet.PWR, packet.criticalLevel, packet.statusChange, packet.changeAddition);
            super.onAttack(second);
        }
    }
}
