package pokecube.adventures.entity.helper.capabilities;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

public class CapabilityAIStates
{
    @CapabilityInject(IHasAIStates.class)
    public static final Capability<IHasAIStates> AISTATES_CAP = null;
    public static Storage                        storage;

    public static interface IHasAIStates
    {
        public static final DataParameter<Integer> AIACTIONSTATESDW = EntityDataManager
                .<Integer> createKey(EntityLivingBase.class, DataSerializers.VARINT);

        public static final int                    STATIONARY       = 1;
        public static final int                    INBATTLE         = 2;
        public static final int                    THROWING         = 4;
        public static final int                    PERMFRIENDLY     = 8;

        boolean getAIState(int state);

        void setAIState(int state, boolean flag);

        int getTotalState();

        void setTotalState(int state);
    }

    public static class Storage implements Capability.IStorage<IHasAIStates>
    {

        @Override
        public NBTBase writeNBT(Capability<IHasAIStates> capability, IHasAIStates instance, EnumFacing side)
        {
            return new NBTTagInt(instance.getTotalState());
        }

        @Override
        public void readNBT(Capability<IHasAIStates> capability, IHasAIStates instance, EnumFacing side, NBTBase nbt)
        {
            if (nbt instanceof NBTTagInt) instance.setTotalState(((NBTTagInt) nbt).getInt());
        }

    }

    public static class DefaultAIStates implements IHasAIStates, ICapabilitySerializable<NBTTagInt>
    {
        int state = 0;

        @Override
        public boolean getAIState(int state)
        {
            return (this.state & state) > 0;
        }

        @Override
        public void setAIState(int state, boolean flag)
        {
            if (flag)
            {
                this.state = Integer.valueOf((this.state | state));
            }
            else
            {
                this.state = Integer.valueOf((this.state & -state - 1));
            }
        }

        @Override
        public int getTotalState()
        {
            return this.state;
        }

        @Override
        public void setTotalState(int state)
        {
            this.state = state;
        }

        @Override
        public boolean hasCapability(Capability<?> capability, EnumFacing facing)
        {
            return capability == AISTATES_CAP;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T getCapability(Capability<T> capability, EnumFacing facing)
        {
            return hasCapability(capability, facing) ? (T) this : null;
        }

        @Override
        public NBTTagInt serializeNBT()
        {
            return (NBTTagInt) storage.writeNBT(AISTATES_CAP, this, null);
        }

        @Override
        public void deserializeNBT(NBTTagInt nbt)
        {
            storage.readNBT(AISTATES_CAP, this, null, nbt);
        }

    }
}
