package pokecube.adventures.entity.helper.capabilities;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
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
    public static Storage                           storage;

    public static IHasNPCAIStates getNPCAIStates(ICapabilityProvider entityIn)
    {
        IHasNPCAIStates pokemobHolder = null;
        if (entityIn == null) return null;
        if (entityIn.hasCapability(AISTATES_CAP, null)) pokemobHolder = entityIn.getCapability(AISTATES_CAP, null);
        else if (entityIn instanceof IHasNPCAIStates) return (IHasNPCAIStates) entityIn;
        return pokemobHolder;
    }

    public static interface IHasNPCAIStates
    {
        public static final int STATIONARY     = 1 << 0;
        public static final int INBATTLE       = 1 << 1;
        public static final int THROWING       = 1 << 2;
        public static final int PERMFRIENDLY   = 1 << 3;
        public static final int FIXEDDIRECTION = 1 << 4;
        public static final int MATES          = 1 << 5;
        public static final int INVULNERABLE   = 1 << 6;
        public static final int TRADES         = 1 << 7;

        /** @return Direction to face if FIXEDDIRECTION */
        public float getDirection();

        /** @param direction
         *            Direction to face if FIXEDDIRECTION */
        public void setDirection(float direction);

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
            NBTTagCompound tag = new NBTTagCompound();
            tag.setInteger("AI", instance.getTotalState());
            tag.setFloat("D", instance.getDirection());
            return tag;
        }

        @Override
        public void readNBT(Capability<IHasNPCAIStates> capability, IHasNPCAIStates instance, EnumFacing side,
                NBTBase nbt)
        {
            if (nbt instanceof NBTTagInt) instance.setTotalState(((NBTTagInt) nbt).getInt());
            else if (nbt instanceof NBTTagCompound)
            {
                NBTTagCompound tag = (NBTTagCompound) nbt;
                instance.setTotalState(tag.getInteger("AI"));
                instance.setDirection(tag.getFloat("D"));
            }
        }

    }

    public static class DefaultAIStates implements IHasNPCAIStates, ICapabilitySerializable<NBTBase>
    {
        int   state = 0;
        float direction;

        public DefaultAIStates()
        {
        }

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

        @Override
        public <T> T getCapability(Capability<T> capability, EnumFacing facing)
        {
            return hasCapability(capability, facing) ? AISTATES_CAP.cast(this) : null;
        }

        @Override
        public NBTBase serializeNBT()
        {
            return storage.writeNBT(AISTATES_CAP, this, null);
        }

        @Override
        public void deserializeNBT(NBTBase nbt)
        {
            storage.readNBT(AISTATES_CAP, this, null, nbt);
        }

        @Override
        public float getDirection()
        {
            return direction;
        }

        @Override
        public void setDirection(float direction)
        {
            this.direction = direction;
        }

    }
}
