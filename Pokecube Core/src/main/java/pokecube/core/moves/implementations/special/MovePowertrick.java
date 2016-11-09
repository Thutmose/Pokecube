package pokecube.core.moves.implementations.special;

import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.IStatsModifiers;
import pokecube.core.interfaces.IPokemob.MovePacket;
import pokecube.core.interfaces.IPokemob.Stats;
import pokecube.core.moves.templates.Move_Basic;

public class MovePowertrick extends Move_Basic
{
    public static class Modifier implements IStatsModifiers
    {
        public Modifier()
        {
        }

        float[] modifiers;

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
            mods.setModifier(Stats.DEFENSE, -def + atk);
            mods.setModifier(Stats.ATTACK, atk - def);
        }
    }
}
