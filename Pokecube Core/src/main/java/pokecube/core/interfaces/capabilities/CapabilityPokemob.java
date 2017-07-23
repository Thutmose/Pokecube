package pokecube.core.interfaces.capabilities;

import java.util.logging.Level;

import net.minecraft.entity.EntityLiving;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.impl.PokemobSaves;

public class CapabilityPokemob
{
    @CapabilityInject(IPokemob.class)
    public static final Capability<IPokemob> POKEMOB_CAP = null;

    public static IPokemob getPokemobFor(ICapabilityProvider entityIn)
    {
        if (entityIn == null) return null;
        // For now, I will default to instanceof, as to not break things in
        // testing.
        if (IPokemob.class.isInstance(entityIn)) return IPokemob.class.cast(entityIn);

        IPokemob pokemobHolder = null;
        if (entityIn.hasCapability(POKEMOB_CAP, null)) return entityIn.getCapability(POKEMOB_CAP, null);
        else if (IPokemob.class.isInstance(entityIn)) return IPokemob.class.cast(entityIn);
        return pokemobHolder;
    }

    public static class Storage implements Capability.IStorage<IPokemob>
    {

        @Override
        public NBTBase writeNBT(Capability<IPokemob> capability, IPokemob instance, EnumFacing side)
        {
            if (instance instanceof DefaultPokemob) return ((DefaultPokemob) instance).writePokemobData();
            return null;
        }

        @Override
        public void readNBT(Capability<IPokemob> capability, IPokemob instance, EnumFacing side, NBTBase nbt)
        {
            if (instance instanceof DefaultPokemob && nbt instanceof NBTTagCompound)
                ((DefaultPokemob) instance).readPokemobData((NBTTagCompound) nbt);
        }

    }

    public static class DefaultPokemob extends PokemobSaves implements ICapabilitySerializable<NBTTagCompound>
    {
        public DefaultPokemob()
        {
            initInventory();
        }

        @Override
        public void setEntity(EntityLiving entityIn)
        {
            super.setEntity(entityIn);
            initInventory();
        }

        @Override
        public boolean hasCapability(Capability<?> capability, EnumFacing facing)
        {
            return capability == POKEMOB_CAP;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T getCapability(Capability<T> capability, EnumFacing facing)
        {
            if (hasCapability(capability, facing)) return (T) this;
            return null;
        }

        @Override
        public NBTTagCompound serializeNBT()
        {
            NBTTagCompound tag;
            try
            {
                tag = writePokemobData();
            }
            catch (Exception e)
            {
                PokecubeMod.log(Level.WARNING, "Error Saving Pokemob", e);
                tag = new NBTTagCompound();
            }
            return tag;
        }

        @Override
        public void deserializeNBT(NBTTagCompound nbt)
        {
            try
            {
                readPokemobData(nbt);
            }
            catch (Exception e)
            {
                PokecubeMod.log(Level.WARNING, "Error Loading Pokemob", e);
            }
        }

    }
}
