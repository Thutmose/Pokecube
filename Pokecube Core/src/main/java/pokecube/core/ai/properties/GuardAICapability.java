package pokecube.core.ai.properties;

import java.util.concurrent.Callable;

import net.minecraft.util.math.BlockPos;
import pokecube.core.utils.TimePeriod;

public class GuardAICapability implements IGuardAICapability
{
    public static class Factory implements Callable<IGuardAICapability>
    {
        @Override
        public IGuardAICapability call() throws Exception
        {
            return new GuardAICapability();
        }
    }

    private BlockPos   pos;
    private float      roamDistance = 2;
    private TimePeriod activeTime;

    private GuardState state        = GuardState.IDLE;

    @Override
    public TimePeriod getActiveTime()
    {
        return activeTime;
    }

    @Override
    public BlockPos getPos()
    {
        return pos;
    }

    @Override
    public float getRoamDistance()
    {
        return roamDistance;
    }

    @Override
    public GuardState getState()
    {
        return state;
    }

    @Override
    public void setActiveTime(TimePeriod active)
    {
        this.activeTime = active;
    }

    @Override
    public void setPos(BlockPos pos)
    {
        this.pos = pos;
    }

    @Override
    public void setRoamDistance(float roam)
    {
        this.roamDistance = roam;
    }

    @Override
    public void setState(GuardState state)
    {
        this.state = state;
    }
}
