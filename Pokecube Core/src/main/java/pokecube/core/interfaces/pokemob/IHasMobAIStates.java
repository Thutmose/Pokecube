package pokecube.core.interfaces.pokemob;

import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.interfaces.pokemob.ai.LogicStates;
import thut.api.entity.ai.IAIMob;

public interface IHasMobAIStates extends IAIMob, IMoveConstants
{
    int getTotalCombatState();

    void setTotalCombatState(int state);

    /** the value of the AI state state. */
    boolean getCombatState(CombatStates state);

    /** Sets AI state state to flag. */
    void setCombatState(CombatStates state, boolean flag);

    ///////////////////////////////////////////////////

    int getTotalLogicState();

    void setTotalLogicState(int state);

    /** the value of the AI state state. */
    boolean getLogicState(LogicStates state);

    /** Sets AI state state to flag. */
    void setLogicState(LogicStates state, boolean flag);

    ///////////////////////////////////////////////////
    int getTotalGeneralState();

    void setTotalGeneralState(int state);

    /** the value of the AI state state. */
    boolean getGeneralState(GeneralStates state);

    /** Sets AI state state to flag. */
    void setGeneralState(GeneralStates state, boolean flag);

    void initAI();

    /** This should default to true.
     * 
     * @param routine
     * @return */
    boolean isRoutineEnabled(AIRoutine routine);

    /** @param routine
     * @param enabled */
    void setRoutineState(AIRoutine routine, boolean enabled);
}
