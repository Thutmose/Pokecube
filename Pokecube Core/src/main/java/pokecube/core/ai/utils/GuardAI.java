package pokecube.core.ai.utils;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.math.BlockPos;
import pokecube.core.ai.properties.IGuardAICapability;
import pokecube.core.ai.properties.IGuardAICapability.GuardState;
import pokecube.core.events.handlers.EventsHandler;
import pokecube.core.utils.TimePeriod;

/** Guards a given point. Constructor parameters:
 * <ul>
 * <li>BlockPos position - the position to guard; the entity's current position
 * if <i>null</i>
 * <li>float roamingDistance - how far the AI should be able to stray from the
 * guard point; at least 1.0f
 * <li>float pathSearchDistance - how far from the guard post should the AI
 * still try to find its way back; at least the same as roamingDistance + 1.0f
 * <li>TimePeriod guardingPeriod - which part of the day should this entity
 * guard its post; the full day if <i>null</i>
 * </ul>
*/
public class GuardAI extends EntityAIBase
{
    public static interface ShouldRun
    {
        default boolean shouldRun()
        {
            return true;
        }
    }

    public final IGuardAICapability capability;
    private final EntityLiving      entity;
    public int                      cooldownTicks;
    public ShouldRun                shouldRun = new ShouldRun()
                                              {
                                              };

    public GuardAI(EntityLiving entity, IGuardAICapability capability)
    {
        this.entity = entity;
        this.capability = capability;
    }

    @Override
    public boolean shouldContinueExecuting()
    {
        if (!shouldRun.shouldRun()) return false;
        if (!capability.hasActiveTask(entity.getEntityWorld().getWorldTime(), 24000)) return false;
        capability.getActiveTask().continueTask(entity);
        switch (capability.getState())
        {
        case RUNNING:
            if (capability.getActiveTask().getPos() == null || (entity.getNavigator().noPath() && entity
                    .getDistanceSq(capability.getActiveTask().getPos()) < capability.getActiveTask().getRoamDistance()
                            * capability.getActiveTask().getRoamDistance() / 2))
            {
                capability.setState(GuardState.COOLDOWN);
                return true;
            }
        case COOLDOWN:
            if (cooldownTicks < 20 * 15)
            {
                ++cooldownTicks;
                return true;
            }
            cooldownTicks = 0;
            capability.setState(GuardState.IDLE);
            return false;
        default:
            return false;
        }
    }

    @Override
    public void resetTask()
    {
        super.resetTask();
        capability.setState(GuardState.IDLE);
        if (capability.getActiveTask() != null) capability.getActiveTask().endTask(entity);
    }

    public void setPos(BlockPos pos)
    {
        if (capability.hasActiveTask(entity.getEntityWorld().getWorldTime(), 24000))
            capability.getActiveTask().setPos(pos);
        else capability.getPrimaryTask().setPos(pos);
    }

    public void setTimePeriod(TimePeriod time)
    {
        if (capability.hasActiveTask(entity.getEntityWorld().getWorldTime(), 24000))
            capability.getActiveTask().setActiveTime(time);
        else capability.getPrimaryTask().setActiveTime(time);
    }

    @Override
    public boolean shouldExecute()
    {
        if (capability == null)
        {
            System.out.println(entity.getCapability(EventsHandler.GUARDAI_CAP, null));
            return false;
        } // TODO find some way to determine actual length of day for things
          // like
          // AR support.
        if (null == entity || entity.isDead
                || !capability.hasActiveTask(entity.getEntityWorld().getWorldTime(), 24000)) { return false; }
        BlockPos pos = capability.getActiveTask().getPos();
        if (pos == null || pos.equals(BlockPos.ORIGIN)) return false;
        double distanceToGuardPointSq = entity.getDistanceSq(capability.getActiveTask().getPos());
        double maxDist = capability.getActiveTask().getRoamDistance() * capability.getActiveTask().getRoamDistance();
        maxDist = Math.max(maxDist, entity.width);
        return (distanceToGuardPointSq > maxDist);
    }

    @Override
    public void startExecuting()
    {
        capability.setState(GuardState.RUNNING);
        capability.getActiveTask().startTask(entity);
    }

    @Override
    public void updateTask()
    {
        super.updateTask();
        if (capability.getState() == GuardState.RUNNING)
        {
            double maxDist = capability.getActiveTask().getRoamDistance()
                    * capability.getActiveTask().getRoamDistance();
            capability.getActiveTask().continueTask(entity);
            if (entity.getDistanceSq(capability.getActiveTask().getPos()) < maxDist)
                capability.setState(GuardState.COOLDOWN);
        }
    }
}
