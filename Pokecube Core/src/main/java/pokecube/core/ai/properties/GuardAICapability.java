package pokecube.core.ai.properties;

import java.util.concurrent.Callable;

import net.minecraft.util.BlockPos;
import pokecube.core.utils.TimePeriod;

public class GuardAICapability implements IGuardAICapability
{
    private BlockPos   pos;
    private float      roamDistance;
    private TimePeriod activeTime;
    private GuardState state = GuardState.IDLE;

    @Override
    public BlockPos getPos()
    {
        return pos;
    }

    @Override
    public void setPos(BlockPos pos)
    {
        this.pos = pos;
    }

    @Override
    public float getRoamDistance()
    {
        return roamDistance;
    }

    @Override
    public void setRoamDistance(float roam)
    {
        this.roamDistance = roam;
    }

    @Override
    public TimePeriod getActiveTime()
    {
        return activeTime;
    }

    @Override
    public void setActiveTime(TimePeriod active)
    {
        this.activeTime = active;
    }

    @Override
    public GuardState getState()
    {
        return state;
    }

    @Override
    public void setState(GuardState state)
    {
        this.state = state;
    }

    public static class Factory implements Callable<IGuardAICapability>
    {
        @Override
        public IGuardAICapability call() throws Exception
        {
            return new GuardAICapability();
        }
    }
}
