package pokecube.core.interfaces.pokemob;

import pokecube.core.interfaces.IMoveConstants;
import thut.api.entity.ai.IAIMob;

public interface IHasMobAIStates extends IMoveConstants, IAIMob
{
    /** @param state
     * @return the value of the AI state state. */
    boolean getPokemonAIState(int state);

    /*
     * Sets AI state state to flag.
     */
    void setPokemonAIState(int state, boolean flag);

    int getTotalAIState();

    void setTotalAIState(int state);
}
