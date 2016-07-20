package pokecube.core.moves.implementations.special;

import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.MovePacket;
import pokecube.core.moves.templates.Move_Basic;

public class MovePowersplit extends Move_Basic
{

    public MovePowersplit()
    {
        super("powersplit");
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

            targetStats[1] = attackerStats[1] = (targetStats[1] + attackerStats[1]) / 2;
            targetStats[3] = attackerStats[3] = (targetStats[3] + attackerStats[3]) / 2;

            targetMods[1] = attackerMods[1] = (byte) ((targetMods[1] + attackerMods[1]) / 2);
            targetMods[3] = attackerMods[3] = (byte) ((targetMods[3] + attackerMods[3]) / 2);

            target.setStats(targetStats);
            packet.attacker.setStats(attackerStats);
            target.setModifiers(targetMods);
            packet.attacker.setModifiers(attackerMods);
        }
    }
}
