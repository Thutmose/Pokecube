package pokecube.core.moves.zmoves;

import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Move_Base;

public class ZMoveManager
{
    /** Returns a Z move based on the current selected attack of the user, if no
     * zmove available, or not able to use one (ie no crystal held), or the mob
     * is on cooldown for zmoves, then this will return null.
     * 
     * @param user
     * @return */
    public static Move_Base getZMove(IPokemob user)
    {
        if (user.getPokemonAIState(IMoveConstants.USEDZMOVE)) return null;
        return null;
    }
}
