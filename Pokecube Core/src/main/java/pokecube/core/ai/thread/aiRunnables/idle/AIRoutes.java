package pokecube.core.ai.thread.aiRunnables.idle;

import net.minecraft.entity.EntityLiving;
import net.minecraft.world.World;
import pokecube.core.ai.properties.IGuardAICapability;
import pokecube.core.ai.thread.aiRunnables.AIBase;
import pokecube.core.ai.utils.GuardAI;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;

/** This is the AIBase version of GuardAI */
public class AIRoutes extends AIBase
{
    final IPokemob       pokemob;
    public final GuardAI wrapped;
    private boolean      running;

    public AIRoutes(EntityLiving mob, IGuardAICapability cap)
    {
        this.wrapped = new GuardAI(mob, cap);
        pokemob = CapabilityPokemob.getPokemobFor(mob);
    }

    @Override
    public void reset()
    {
    }

    @Override
    public void doMainThreadTick(World world)
    {
        super.doMainThreadTick(world);
        if (!shouldRun()) return;
        if (!running)
        {
            running = wrapped.shouldExecute();
            if (running)
            {
                wrapped.startExecuting();
            }
        }
        else
        {
            if (!wrapped.shouldContinueExecuting())
            {
                running = false;
                wrapped.resetTask();
                return;
            }
            wrapped.updateTask();
        }

    }

    @Override
    /** This is just wrapping the guardAI, so it doesn't do anything here. */
    public void run()
    {
    }

    @Override
    /** Runs if not a pokemob, or the pokemob is wild, or the pokemob is on
     * stay. */
    public boolean shouldRun()
    {
        // Shouldn't run if angry
        if (pokemob != null && pokemob.getCombatState(CombatStates.ANGRY)) return false;

        return pokemob == null || !pokemob.getGeneralState(GeneralStates.TAMED)
                || pokemob.getGeneralState(GeneralStates.STAYING);
    }

}
