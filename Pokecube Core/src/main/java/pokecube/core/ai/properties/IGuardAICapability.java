package pokecube.core.ai.properties;

import java.util.List;

import net.minecraft.entity.EntityLiving;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import pokecube.core.utils.TimePeriod;

public interface IGuardAICapability
{
    public static interface IGuardTask
    {
        TimePeriod getActiveTime();

        void setActiveTime(TimePeriod active);

        void startTask(EntityLiving entity);

        void continueTask(EntityLiving entity);

        void endTask(EntityLiving entity);

        BlockPos getPos();

        float getRoamDistance();

        void setPos(BlockPos pos);

        void setRoamDistance(float roam);

        default NBTBase serialze()
        {
            NBTTagCompound tag = new NBTTagCompound();
            if (getPos() != null)
            {
                tag.setTag("pos", NBTUtil.createPosTag(getPos()));
            }
            tag.setFloat("d", getRoamDistance());
            TimePeriod time;
            if ((time = getActiveTime()) != null)
            {
                tag.setLong("start", time.startTick);
                tag.setLong("end", time.endTick);
            }
            return tag;
        }

        default void load(NBTBase tag)
        {
            NBTTagCompound nbt = (NBTTagCompound) tag;
            if (nbt.hasKey("pos")) setPos(NBTUtil.getPosFromTag(nbt.getCompoundTag("pos")));
            setRoamDistance(nbt.getFloat("d"));
            setActiveTime(new TimePeriod((int) nbt.getLong("start"), (int) nbt.getLong("end")));
        }
    }

    public static enum GuardState
    {
        IDLE, RUNNING, COOLDOWN
    }

    public static class Storage implements Capability.IStorage<IGuardAICapability>
    {
        @Override
        public void readNBT(Capability<IGuardAICapability> capability, IGuardAICapability instance, EnumFacing side,
                NBTBase nbt)
        {
            if (nbt instanceof NBTTagCompound)
            {
                NBTTagCompound data = (NBTTagCompound) nbt;
                instance.setState(GuardState.values()[data.getInteger("state")]);
                if (data.hasKey("tasks"))
                {
                    NBTTagList tasks = (NBTTagList) data.getTag("tasks");
                    instance.loadTasks(tasks);
                }
            }
        }

        @Override
        public NBTBase writeNBT(Capability<IGuardAICapability> capability, IGuardAICapability instance, EnumFacing side)
        {
            NBTTagCompound ret = new NBTTagCompound();
            ret.setInteger("state", instance.getState().ordinal());
            ret.setTag("tasks", instance.serializeTasks());
            return ret;
        }
    }

    List<IGuardTask> getTasks();

    GuardState getState();

    void setState(GuardState state);

    // do we have a task with a location, and a position
    boolean hasActiveTask(long time, long daylength);

    IGuardTask getActiveTask();

    // This should be primary task to try, usually will just be
    // getTasks().get(0)
    IGuardTask getPrimaryTask();

    void loadTasks(NBTTagList list);

    NBTTagList serializeTasks();
}
