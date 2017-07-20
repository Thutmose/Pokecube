package pokecube.core.interfaces.pokemob;

import pokecube.core.interfaces.IMoveConstants;

public interface IHasMobAIStates extends IMoveConstants
{
    /** @param state
     * @return the value of the AI state state. */
    boolean getPokemonAIState(int state);

    /*
     * Sets AI state state to flag.
     */
    void setPokemonAIState(int state, boolean flag);
}
