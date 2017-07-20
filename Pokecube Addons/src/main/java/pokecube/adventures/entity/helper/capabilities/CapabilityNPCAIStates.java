package pokecube.adventures.entity.helper.capabilities;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

public class CapabilityNPCAIStates
{
    @CapabilityInject(IHasNPCAIStates.class)
    public static final Capability<IHasNPCAIStates> AISTATES_CAP = null;
    public static Storage                        storage;

    public static IHasNPCAIStates getNPCAIStates(ICapabilityProvider entityIn)
    {
        IHasNPCAIStates pokemobHolder = null;
        if (entityIn.hasCapability(AISTATES_CAP, null))
            pokemobHolder = entityIn.getCapability(AISTATES_CAP, null);
        else if (entityIn instanceof IHasNPCAIStates) return (IHasNPCAIStates) entityIn;
        return pokemobHolder;
    }
    
    public static interface IHasNPCAIStates
    {
        public static final int                    STATIONARY       = 1;
        public static final int                    INBATTLE         = 2;
        public static final int                    THROWING         = 4;
        public static final int                    PERMFRIENDLY     = 8;

        boolean getAIState(int state);

        void setAIState(int state, boolean flag);

        int getTotalState();

        void setTotalState(int state);
    }

    public static class Storage implements Capability.IStorage<IHasNPCAIStates>
    {

        @Override
        public NBTBase writeNBT(Capability<IHasNPCAIStates> capability, IHasNPCAIStates instance, EnumFacing side)
        {
            return new NBTTagInt(instance.getTotalState());
        }

        @Override
        public void readNBT(Capability<IHasNPCAIStates> capability, IHasNPCAIStates instance, EnumFacing side, NBTBase nbt)
        {
            if (nbt instanceof NBTTagInt) instance.setTotalState(((NBTTagInt) nbt).getInt());
        }

    }

    public static class DefaultAIStates implements IHasNPCAIStates, ICapabilitySerializable<NBTTagInt>
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
