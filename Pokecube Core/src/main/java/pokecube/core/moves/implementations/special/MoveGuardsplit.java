package pokecube.core.moves.implementations.special;

import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.MovePacket;
import pokecube.core.moves.templates.Move_Basic;

public class MoveGuardsplit extends Move_Basic
{

    public MoveGuardsplit()
    {
        super("guardsplit");
    }

    @Override
    public void postAttack(MovePacket packet)
    {
        super.postAttack(packet);
        if (packet.canceled || packet.failed) return;
        if (packet.attacked instanceof IPokemob)
        {
            IPokemob target = (IPokemob) packet.attacked;
            int[] targetStats = target.getBaseStats();
            int[] attackerStats = packet.attacker.getBaseStats();
            byte[] targetMods = target.getModifiers();
            byte[] attackerMods = packet.attacker.getModifiers();

            targetStats[2] = attackerStats[2] = (targetStats[2] + attackerStats[2]) / 2;
            targetStats[4] = attackerStats[4] = (targetStats[4] + attackerStats[4]) / 2;

            targetMods[2] = attackerMods[2] = (byte) ((targetMods[2] + attackerMods[2]) / 2);
            targetMods[4] = attackerMods[4] = (byte) ((targetMods[4] + attackerMods[4]) / 2);

            target.setStats(targetStats);
            packet.attacker.setStats(attackerStats);
            target.setModifiers(targetMods);
            packet.attacker.setModifiers(attackerMods);
        }
    }
}
