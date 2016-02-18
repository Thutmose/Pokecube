package pokecube.core.ai.properties;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import pokecube.core.utils.TimePeriod;

public interface IGuardAICapability
{
    BlockPos getPos();

    void setPos(BlockPos pos);

    float getRoamDistance();

    void setRoamDistance(float roam);

    TimePeriod getActiveTime();

    void setActiveTime(TimePeriod active);

    GuardState getState();

    void setState(GuardState state);

    public static class Storage implements Capability.IStorage<IGuardAICapability>
    {
        @Override
        public NBTBase writeNBT(Capability<IGuardAICapability> capability, IGuardAICapability instance, EnumFacing side)
        {
            NBTTagCompound ret = new NBTTagCompound();
            NBTTagCompound tag = new NBTTagCompound();
            writeToTag(tag, instance.getPos());
            ret.setTag("pos", tag);
            tag = new NBTTagCompound();
            if (instance.getActiveTime() != null)
            {
                tag.setInteger("start", instance.getActiveTime().startTick);
                tag.setInteger("end", instance.getActiveTime().endTick);
            }
            ret.setTag("activeTime", tag);
            ret.setInteger("state", instance.getState().ordinal());
            ret.setFloat("roamDistance", instance.getRoamDistance());
            return ret;
        }

        @Override
        public void readNBT(Capability<IGuardAICapability> capability, IGuardAICapability instance, EnumFacing side,
                NBTBase nbt)
        {
            if (nbt instanceof NBTTagCompound)
            {
                NBTTagCompound data = (NBTTagCompound) nbt;
                instance.setPos(readFromTag(data.getCompoundTag("pos")));
                instance.setRoamDistance(data.getFloat("roamDistance"));
                instance.setState(GuardState.values()[data.getInteger("state")]);
                NBTTagCompound tag = data.getCompoundTag("activeTime");
                instance.setActiveTime(new TimePeriod(tag.getInteger("start"), tag.getInteger("end")));
            }
        }

        private void writeToTag(NBTTagCompound tag, BlockPos pos)
        {
            if (pos == null) return;
            tag.setInteger("x", pos.getX());
            tag.setInteger("y", pos.getY());
            tag.setInteger("z", pos.getZ());
        }

        private BlockPos readFromTag(NBTTagCompound tag)
        {
            return new BlockPos(tag.getInteger("x"), tag.getInteger("y"), tag.getInteger("z"));
        }

    }

    public static enum GuardState
    {
        IDLE, RUNNING, COOLDOWN
    }
}
