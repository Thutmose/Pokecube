package pokecube.core.moves.implementations.special;

import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.MovePacket;
import pokecube.core.interfaces.IPokemob.Stats;
import pokecube.core.moves.implementations.special.MovePowersplit.Modifier;
import pokecube.core.moves.templates.Move_Basic;
import pokecube.core.network.pokemobs.PacketSyncModifier;

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
            IPokemob attacked = (IPokemob) packet.attacked;
            int spdef = packet.attacker.getStat(Stats.SPDEFENSE, true);
            int def = packet.attacker.getStat(Stats.DEFENSE, true);

            int spdef2 = attacked.getStat(Stats.SPDEFENSE, true);
            int def2 = attacked.getStat(Stats.DEFENSE, true);

            int averageDef = (def + def2) / 2;
            int averageSpdef = (spdef + spdef2) / 2;
            Modifier mods = packet.attacker.getModifiers().getModifiers("powersplit", Modifier.class);
            Modifier mods2 = attacked.getModifiers().getModifiers("powersplit", Modifier.class);

            mods.setModifier(Stats.DEFENSE, -def + averageDef);
            mods2.setModifier(Stats.DEFENSE, -def2 + averageDef);

            mods.setModifier(Stats.SPDEFENSE, -spdef + averageSpdef);
            mods2.setModifier(Stats.SPDEFENSE, -spdef2 + averageSpdef);
            PacketSyncModifier.sendUpdate("powersplit", packet.attacker);
            PacketSyncModifier.sendUpdate("powersplit", attacked);
        }
    }
}
