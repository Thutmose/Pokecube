package pokecube.core.database.abilities.r;

import net.minecraft.entity.Entity;
import pokecube.core.database.abilities.Ability;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.MovePacket;
import pokecube.core.moves.MovesUtils;
import pokecube.core.utils.PokeType;

public class Rattled extends Ability
{
    @Override
    public void onMoveUse(IPokemob mob, MovePacket move)
    {
        if (mob == move.attacked && !move.pre && isCorrectType(move.attackType))
        {
            MovesUtils.handleStats2(mob, (Entity) mob, IMoveConstants.VIT, IMoveConstants.RAISE);
        }
    }

    private boolean isCorrectType(PokeType type)
    {
        return type == PokeType.dark || type == PokeType.bug || type == PokeType.ghost;
    }
}
