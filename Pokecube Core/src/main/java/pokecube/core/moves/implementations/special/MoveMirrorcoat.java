package pokecube.core.moves.implementations.special;

import net.minecraft.entity.EntityLivingBase;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.IPokemob.MovePacket;
import pokecube.core.moves.PokemobDamageSource;
import pokecube.core.moves.templates.Move_Basic;

public class MoveMirrorcoat extends Move_Basic
{

    public MoveMirrorcoat()
    {
        super("mirrorcoat");
    }

    @Override
    public void postAttack(MovePacket packet)
    {
        super.postAttack(packet);
        if (packet.canceled || packet.failed) return;
        if (!packet.attacker.getMoveStats().biding)
        {
            packet.attacker.getMoveStats().SELFRAISECOUNTER = (int) (PokecubeMod.core.getConfig().attackCooldown * 1.5);
            packet.attacker.setAttackCooldown(packet.attacker.getMoveStats().SELFRAISECOUNTER);
            packet.attacker.getMoveStats().biding = true;
            packet.attacker.getMoveStats().SPECIALDAMAGETAKENCOUNTER = 0;
        }
        else
        {
            if (packet.attacker.getMoveStats().SELFRAISECOUNTER == 0)
            {
                int damage = 2 * packet.attacker.getMoveStats().SPECIALDAMAGETAKENCOUNTER;
                packet.attacker.getMoveStats().SPECIALDAMAGETAKENCOUNTER = 0;
                if (packet.attacked != null) packet.attacked.attackEntityFrom(
                        new PokemobDamageSource("mob", (EntityLivingBase) packet.attacker, this), damage);
                packet.attacker.getMoveStats().biding = false;
            }
        }
    }
}
