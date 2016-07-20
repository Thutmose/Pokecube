package pokecube.core.moves.implementations.multihit;

import net.minecraft.entity.Entity;
import pokecube.core.interfaces.IPokemob.MovePacket;
import pokecube.core.moves.MovesUtils;
import pokecube.core.moves.templates.Move_Basic;

public class MoveTriplekick extends Move_Basic
{
    public MoveTriplekick()
    {
        super("triplekick");
    }

    @Override
    public void onAttack(MovePacket packet)
    {
        int PWR = this.getPWR();
        for (int i = 0; i < 3; i++)
        {
            if (sound != null)
            {
                ((Entity) packet.attacker).playSound(sound, 0.5F, 0.4F / (MovesUtils.rand.nextFloat() * 0.4F + 0.8F));
            }
            byte statusChange = STATUS_NON;
            byte changeAddition = CHANGE_NONE;
            if (move.statusChange != STATUS_NON && MovesUtils.rand.nextInt(100) <= move.statusChance)
            {
                statusChange = move.statusChange;
            }
            if (move.change != CHANGE_NONE && MovesUtils.rand.nextInt(100) <= move.chanceChance)
            {
                changeAddition = move.change;
            }
            MovePacket second = new MovePacket(packet.attacker, packet.attacked, name, move.type, PWR, move.crit,
                    statusChange, changeAddition);
            super.onAttack(second);
            int finalAttackStrength = second.damageDealt;
            if (finalAttackStrength != 0) PWR += 10;
            else break;
        }

    }
}
