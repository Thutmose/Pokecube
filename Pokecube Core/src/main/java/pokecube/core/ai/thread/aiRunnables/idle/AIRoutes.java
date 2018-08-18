package pokecube.core.ai.thread.aiRunnables.idle;

import net.minecraft.entity.EntityLiving;
import net.minecraft.world.World;
import pokecube.core.ai.properties.IGuardAICapability;
import pokecube.core.ai.thread.aiRunnables.AIBase;
import pokecube.core.ai.utils.GuardAI;

/** This is the AIBase version of GuardAI */
public class AIRoutes extends AIBase
{
    public final GuardAI wrapped;
    private boolean      running;

    public AIRoutes(EntityLiving mob, IGuardAICapability cap)
    {
        this.wrapped = new GuardAI(mob, cap);
    }

    @Override
    public void reset()
    {
    }

    @Override
    public void doMainThreadTick(World world)
    {
        super.doMainThreadTick(world);

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
    /** This is just wrapping the guardAI, so it doesn't do anything here. */
    public boolean shouldRun()
    {
        return false;
    }

}
