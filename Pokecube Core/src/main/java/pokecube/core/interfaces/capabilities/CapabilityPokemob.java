package pokecube.core.interfaces.capabilities;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import pokecube.core.interfaces.IPokemob;

public class CapabilityPokemob
{
    @CapabilityInject(IPokemob.class)
    public static final Capability<IPokemob> POKEMOB_CAP = null;

    public static IPokemob getPokemobFor(ICapabilityProvider entityIn)
    {
        if (entityIn == null) return null;
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
}
