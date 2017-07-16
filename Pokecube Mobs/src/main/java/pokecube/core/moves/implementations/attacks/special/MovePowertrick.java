package pokecube.core.moves.implementations.attacks.special;

import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.IStatsModifiers;
import pokecube.core.interfaces.IPokemob.MovePacket;
import pokecube.core.interfaces.IPokemob.Stats;
import pokecube.core.moves.templates.Move_Basic;
import pokecube.core.network.pokemobs.PacketSyncModifier;

public class MovePowertrick extends Move_Basic
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
            return 200;
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
        IPokemob.StatModifiers.registerModifier("powertrick", Modifier.class);
    }

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
            Modifier mods = packet.attacker.getModifiers().getModifiers(name, Modifier.class);
            int def = packet.attacker.getStat(Stats.DEFENSE, true);
            int atk = packet.attacker.getStat(Stats.ATTACK, true);
            float modDef = mods.getModifierRaw(Stats.DEFENSE);
            float modAtk = mods.getModifierRaw(Stats.ATTACK);
            mods.setModifier(Stats.DEFENSE, modDef - def + atk);
            mods.setModifier(Stats.ATTACK, modAtk - atk + def);
            PacketSyncModifier.sendUpdate("powertrick", packet.attacker);
        }
    }
}
