package pokecube.core.moves.implementations.ice;

import net.minecraft.entity.Entity;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.MovePacket;
import pokecube.core.moves.templates.Move_Basic;

public class MoveIceball extends Move_Basic
{

    public MoveIceball()
    {
        super("iceball");
    }

    @Override
    public void onAttack(MovePacket packet)
    {
        super.onAttack(packet);
        if (packet.damageDealt == 0)
        {
            packet.attacker.getMoveStats().ROLLOUTCOUNTER = 0;
        }
        else packet.attacker.getMoveStats().ROLLOUTCOUNTER++;
    }

    @Override
    public int getPWR(IPokemob attacker, Entity attacked)
    {
        double defCurl = attacker.getMoveStats().DEFENSECURLCOUNTER > 0 ? 2 : 1;
        double rollOut = attacker.getMoveStats().ROLLOUTCOUNTER;
        if (rollOut > 4)
        {
            rollOut = attacker.getMoveStats().ROLLOUTCOUNTER = 0;
        }
        return (int) Math.min(this.getPWR(), (rollOut * 1.5) * this.getPWR() * defCurl);
    }
}
