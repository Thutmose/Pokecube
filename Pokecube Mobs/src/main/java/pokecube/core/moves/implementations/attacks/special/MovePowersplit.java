package pokecube.core.moves.implementations.attacks.special;

import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.IStatsModifiers;
import pokecube.core.interfaces.IPokemob.MovePacket;
import pokecube.core.interfaces.IPokemob.Stats;
import pokecube.core.moves.templates.Move_Basic;
import pokecube.core.network.pokemobs.PacketSyncModifier;

public class MovePowersplit extends Move_Basic
{
    public static class Modifier implements IStatsModifiers
    {
        public Modifier()
        {
        }

        float[] modifiers = new float[Stats.values().length];

        @Override
        public boolean isFlat()
        {
            return true;
        }

        @Override
        public int getPriority()
        {
            return 250;
        }

        @Override
        public float getModifier(Stats stat)
        {
            return modifiers[stat.ordinal()];
        }

        @Override
        public float getModifierRaw(Stats stat)
        {
            return modifiers[stat.ordinal()];
        }

        @Override
        public void setModifier(Stats stat, float value)
        {
            modifiers[stat.ordinal()] = value;
        }

        @Override
        public boolean persistant()
        {
            return false;
        }

    }

    static
    {
        IPokemob.StatModifiers.registerModifier("powersplit", Modifier.class);
    }

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
            IPokemob attacked = (IPokemob) packet.attacked;
            int spatk = packet.attacker.getStat(Stats.SPATTACK, true);
            int atk = packet.attacker.getStat(Stats.ATTACK, true);

            int spatk2 = attacked.getStat(Stats.SPATTACK, true);
            int atk2 = attacked.getStat(Stats.ATTACK, true);

            int averageAtk = (atk + atk2) / 2;
            int averageSpatk = (spatk + spatk2) / 2;
            Modifier mods = packet.attacker.getModifiers().getModifiers("powersplit", Modifier.class);
            Modifier mods2 = attacked.getModifiers().getModifiers("powersplit", Modifier.class);

            mods.setModifier(Stats.ATTACK, -atk + averageAtk);
            mods2.setModifier(Stats.ATTACK, -atk2 + averageAtk);
            
            mods.setModifier(Stats.SPATTACK, -spatk + averageSpatk);
            mods2.setModifier(Stats.SPATTACK, -spatk2 + averageSpatk);
            PacketSyncModifier.sendUpdate("powersplit", packet.attacker);
            PacketSyncModifier.sendUpdate("powersplit", attacked);
        }
    }
}
