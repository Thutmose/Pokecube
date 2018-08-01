package pokecube.core.ai.properties;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

import com.google.common.collect.Lists;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.pathfinding.PathPoint;
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

    public static class GuardTask implements IGuardTask
    {
        private AttributeModifier executingGuardTask = null;
        private BlockPos          lastPos;
        private int               lastPosCounter     = 10;
        private BlockPos          pos;
        private float             roamDistance       = 2;
        private TimePeriod        activeTime         = new TimePeriod(0, 0);

        public GuardTask()
        {
            executingGuardTask = new AttributeModifier(UUID.fromString("4454b0d8-75ef-4689-8fce-daab61a7e1b0"),
                    "pokecube:guard_task", 5, 2);
        }

        @Override
        public void setActiveTime(TimePeriod active)
        {
            this.activeTime = active;
            if (active == null) activeTime = new TimePeriod(0, 0);
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
        public void startTask(EntityLiving entity)
        {
            entity.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).removeModifier(executingGuardTask);
            entity.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).applyModifier(executingGuardTask);
            double speed = entity.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue();
            entity.getNavigator().tryMoveToXYZ(getPos().getX() + 0.5, getPos().getY(), getPos().getZ() + 0.5, speed);
        }

        @Override
        public void continueTask(EntityLiving entity)
        {
            boolean hasPath = !entity.getNavigator().noPath();
            BlockPos newPos = entity.getPosition();
            double maxDist = this.getRoamDistance() * this.getRoamDistance();
            maxDist = Math.max(maxDist, entity.width);

            if (hasPath)
            {
                if (lastPos != null && lastPos.equals(newPos))
                {
                    if (lastPosCounter-- >= 0)
                    {

                    }
                    else
                    {
                        lastPosCounter = 10;
                        hasPath = false;
                    }
                }
                else
                {
                    lastPosCounter = 10;
                }
            }

            if (!hasPath)
            {
                double speed = entity.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue();
                boolean pathed = entity.getNavigator().tryMoveToXYZ(this.getPos().getX() + 0.5, this.getPos().getY(),
                        this.getPos().getZ() + 0.5, speed);
                IAttributeInstance attri = entity.getNavigator().pathSearchRange;
                if (!pathed)
                {
                    if (!attri.hasModifier(executingGuardTask))
                    {
                        attri.applyModifier(executingGuardTask);
                    }
                }
                else
                {
                    if (attri.hasModifier(executingGuardTask))
                    {
                        attri.removeModifier(executingGuardTask);
                    }
                }
            }
            else
            {
                PathPoint end = entity.getNavigator().getPath().getFinalPathPoint();
                BlockPos endPos = new BlockPos(end.x, end.y, end.z);
                if (endPos.distanceSq(this.getPos()) > maxDist)
                {
                    double speed = entity.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED)
                            .getAttributeValue();
                    entity.getNavigator().tryMoveToXYZ(this.getPos().getX() + 0.5, this.getPos().getY(),
                            this.getPos().getZ() + 0.5, speed);
                }
            }
            lastPos = newPos;
        }

        @Override
        public void endTask(EntityLiving entity)
        {
            entity.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).removeModifier(executingGuardTask);
        }

    }

    private GuardState       state = GuardState.IDLE;
    private List<IGuardTask> tasks = Lists.newArrayList(new GuardTask());
    private IGuardTask       activeTask;

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

    @Override
    public List<IGuardTask> getTasks()
    {
        return tasks;
    }

    @Override
    public boolean hasActiveTask(long time, long daylength)
    {
        if (activeTask != null && activeTask.getActiveTime().contains(time, daylength)) return true;
        for (IGuardTask task : getTasks())
        {
            if (task.getActiveTime().contains(time, daylength))
            {
                activeTask = task;
                return true;
            }
        }
        return false;
    }

    @Override
    public IGuardTask getActiveTask()
    {
        return activeTask;
    }

    @Override
    public void loadTasks(NBTTagList list)
    {
        tasks.clear();
        for (int i = 0; i < list.tagCount(); i++)
        {
            GuardTask task = new GuardTask();
            task.load(list.get(i));
            tasks.add(task);
        }
        if (tasks.isEmpty()) tasks.add(new GuardTask());
    }

    @Override
    public NBTTagList serializeTasks()
    {
        NBTTagList list = new NBTTagList();
        for (IGuardTask task : tasks)
        {
            list.appendTag(task.serialze());
        }
        return list;
    }

    @Override
    public IGuardTask getPrimaryTask()
    {
        if (tasks.isEmpty()) tasks.add(new GuardTask());
        return tasks.get(0);
    }
}
