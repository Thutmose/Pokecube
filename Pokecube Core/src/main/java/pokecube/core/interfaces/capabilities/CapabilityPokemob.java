package pokecube.core.interfaces.capabilities;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import pokecube.core.interfaces.IPokemob;

public class CapabilityPokemob
{
    // TODO see about getting every cast to IPokemob using this.

    @CapabilityInject(IPokemob.class)
    public static final Capability<IPokemob> POKEMOB_CAP = null;

    public static IPokemob getPokemobFor(ICapabilityProvider entityIn)
    {
        if (entityIn == null) return null;
        IPokemob pokemobHolder = null;
        if (entityIn.hasCapability(POKEMOB_CAP, null)) pokemobHolder = entityIn.getCapability(POKEMOB_CAP, null);
        else if (entityIn instanceof IPokemob) return IPokemob.class.cast(entityIn);
        return pokemobHolder;
    }

    public static class Storage implements Capability.IStorage<IPokemob>
    {

        @Override
        public NBTBase writeNBT(Capability<IPokemob> capability, IPokemob instance, EnumFacing side)
        {
            return null;
        }

        @Override
        public void readNBT(Capability<IPokemob> capability, IPokemob instance, EnumFacing side, NBTBase nbt)
        {
        }

    }

    public abstract static class DefaultPokemob implements IPokemob
    {

    }
}
