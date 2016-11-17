package pokecube.core.moves.zmoves;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.moves.MovesUtils;

public class ZMoveManager
{
    /** Map of move index to zMove. */
    public static Int2ObjectOpenHashMap<Move_Base> zMoves = new Int2ObjectOpenHashMap<>();

    /** Returns a Z move based on the current selected attack of the user, if no
     * zmove available, or not able to use one (ie no crystal held), or the mob
     * is on cooldown for zmoves, then this will return null.
     * 
     * @param user
     * @return */
    public static Move_Base getZMove(IPokemob user)
    {
        if (user.getPokemonAIState(IMoveConstants.USEDZMOVE)) return null;
        int selected = user.getMoveIndex();
        if (selected >= user.getMoves().length) return null;
        Move_Base move = MovesUtils.getMoveFromName(user.getMoves()[selected]);
        if (move == null) return null;
        return zMoves.get(move.getIndex());
    }
}
