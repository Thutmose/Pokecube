package pokecube.core.moves.implementations.normal;

import java.util.Set;

import com.google.common.collect.Sets;

import net.minecraft.entity.Entity;
import net.minecraft.util.text.TextComponentTranslation;
import pokecube.core.interfaces.IPokemob.MovePacket;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.moves.MovesUtils;
import pokecube.core.moves.templates.Move_Basic;

public class MoveSketch extends Move_Basic
{
    public static Set<String> unSketchables = Sets.newHashSet();

    static
    {
        unSketchables.add("chatter");
    }

    public MoveSketch()
    {
        super("sketch");
    }

    @Override
    public void postAttack(MovePacket packet)
    {
        super.postAttack(packet);
        if (packet.attacker.getTransformedTo() != null) return;
        String lastHitBy = ((Entity) packet.attacker).getEntityData().getString("lastMoveHitBy");
        Move_Base toSketch = MovesUtils.getMoveFromName(lastHitBy);
        if (unSketchables.contains(lastHitBy) || toSketch == null) return;
        for (int i = 0; i < packet.attacker.getMoves().length; i++)
        {
            if (packet.attacker.getMoves()[i] != null && packet.attacker.getMoves()[i].equals(name))
            {
                packet.attacker.setMove(i, toSketch.name);
                packet.attacker.displayMessageToOwner(new TextComponentTranslation("pokemob.move.sketched",
                        packet.attacker.getPokemonDisplayName(), MovesUtils.getMoveName(lastHitBy)));
                return;
            }
        }
    }
}
