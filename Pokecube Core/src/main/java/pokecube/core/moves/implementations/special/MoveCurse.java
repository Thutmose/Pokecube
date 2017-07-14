package pokecube.core.moves.implementations.special;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.MovePacket;
import pokecube.core.moves.MovesUtils;
import pokecube.core.moves.templates.Move_Basic;
import pokecube.core.utils.PokeType;

public class MoveCurse extends Move_Basic
{

    public MoveCurse()
    {
        super("curse");
    }

    @Override
    public void postAttack(MovePacket packet)
    {
        super.postAttack(packet);
        if (packet.canceled || packet.failed) return;
        if (packet.attacker.isType(PokeType.ghost))
        {
            if (packet.attacked instanceof IPokemob)
            {
                IPokemob target = (IPokemob) packet.attacked;
                if ((target.getChanges() & CHANGE_CURSE) == 0)
                {
                    MovePacket move = new MovePacket(packet.attacker, packet.attacked, getName(), ghost, 0, 0, (byte) 0,
                            CHANGE_CURSE, true);
                    target.onMoveUse(move);
                    if (!move.canceled)
                    {
                        target.addChange(CHANGE_CURSE);
                        ((EntityLivingBase) packet.attacker).attackEntityFrom(DamageSource.MAGIC,
                                ((EntityLivingBase) packet.attacker).getMaxHealth() / 2);
                    }
                }
            }
        }
        else if (packet.attacked != packet.attacker && packet.attacked != null)
        {
            packet = new MovePacket(packet.attacker, packet.attacked, this);
            MovesUtils.handleStats(packet.attacker, packet.attacked, packet, true);
        }
    }
}
