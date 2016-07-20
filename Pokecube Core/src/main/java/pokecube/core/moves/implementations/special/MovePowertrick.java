package pokecube.core.moves.implementations.special;

import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.MovePacket;
import pokecube.core.moves.templates.Move_Basic;

public class MovePowertrick extends Move_Basic
{

    public MovePowertrick()
    {
        super("powertrick");
    }

    @Override
    public void postAttack(MovePacket packet)
    {
        super.postAttack(packet);
        if (packet.canceled || packet.failed) return;
        if (packet.attacked instanceof IPokemob)
        {
            int[] attackerStats = packet.attacker.getBaseStats();
            byte[] attackerMods = packet.attacker.getModifiers();

            int def = attackerStats[2];
            attackerStats[2] = attackerStats[1];
            attackerStats[1] = def;

            byte def2 = attackerMods[2];
            attackerMods[2] = attackerMods[1];
            attackerMods[1] = def2;

            packet.attacker.setModifiers(attackerMods);
            packet.attacker.setStats(attackerStats);
        }
    }
}
