package pokecube.core.moves.implementations.special;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.MovePacket;
import pokecube.core.moves.templates.Move_Basic;

public class MoveWhirlwind extends Move_Basic
{

    public MoveWhirlwind()
    {
        super("whirlwind");
    }

    @Override
    public void postAttack(MovePacket packet)
    {
        super.postAttack(packet);
        if (packet.canceled || packet.failed) return;
        if (packet.attacked instanceof IPokemob)
        {
            if (((IPokemob) packet.attacked).getLevel() > packet.attacker.getLevel())
            {
                // TODO message here for move failing;
                return;
            }
            ((IPokemob) packet.attacked).setPokemonAIState(IMoveConstants.ANGRY, false);
            if (((IPokemob) packet.attacked).getPokemonAIState(IMoveConstants.TAMED))
                ((IPokemob) packet.attacked).returnToPokecube();
        }
        // ends the battle
        if (packet.attacked instanceof EntityLiving)
        {
            ((EntityLiving) packet.attacked).setAttackTarget(null);
        }
        if (packet.attacked instanceof EntityCreature)
        {
            ((EntityCreature) packet.attacker).setAttackTarget(null);
        }
        ((EntityCreature) packet.attacker).setAttackTarget(null);
        packet.attacker.setPokemonAIState(IMoveConstants.ANGRY, false);
    }
}
