package pokecube.core.interfaces.pokemob;

import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.interfaces.pokemob.ai.LogicStates;
import thut.api.entity.ai.IAIMob;

public interface IHasMobAIStates extends IAIMob, IMoveConstants
{
    /** @return total combat state for saving */
    int getTotalCombatState();

    /** Used for loading combat state.
     * 
     * @param state */
    void setTotalCombatState(int state);

    /** the value of the AI state state. */
    boolean getCombatState(CombatStates state);

    /** Sets AI state state to flag. */
    void setCombatState(CombatStates state, boolean flag);

    ///////////////////////////////////////////////////

    /** @return Total logic state for saving */
    int getTotalLogicState();

    /** Used for loading logic state.
     * 
     * @param state */
    void setTotalLogicState(int state);

    /** the value of the AI state state. */
    boolean getLogicState(LogicStates state);

    /** Sets AI state state to flag. */
    void setLogicState(LogicStates state, boolean flag);

    ///////////////////////////////////////////////////
    /** @return Total general state for saving */
    int getTotalGeneralState();

    /** Used for loading general state.
     * 
     * @param state */
    void setTotalGeneralState(int state);

    /** the value of the AI state state. */
    boolean getGeneralState(GeneralStates state);

    /** Sets AI state state to flag. */
    void setGeneralState(GeneralStates state, boolean flag);

    /** Initializes the ai */
    void initAI();

    /** This should default to whatever the routine defaults to, see
     * {@link AIRoutine#getDefault()}
     * 
     * @param routine
     * @return */
    boolean isRoutineEnabled(AIRoutine routine);

    /** @param routine
     * @param enabled */
    void setRoutineState(AIRoutine routine, boolean enabled);
}
