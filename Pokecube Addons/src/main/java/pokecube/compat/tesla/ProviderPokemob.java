package pokecube.compat.tesla;

import net.darkhax.tesla.api.ITeslaProducer;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import pokecube.adventures.blocks.siphon.TileEntitySiphon;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.utils.PokeType;
import thut.api.entity.IHungrymob;

public class ProviderPokemob implements ITeslaProducer, ICapabilityProvider
{
    final IPokemob pokemob;

    public ProviderPokemob(IPokemob pokemob)
    {
        this.pokemob = pokemob;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing)
    {
        return capability == TeslaHandler.TESLA_PRODUCER;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing)
    {
        return (capability == TeslaHandler.TESLA_PRODUCER) ? (T) this : null;
    }

    @Override
    public long takePower(long power, boolean simulated)
    {
        if (!pokemob.isType(PokeType.electric)) return 0;
        EntityLiving living = (EntityLiving) pokemob;
        int spAtk = pokemob.getActualStats()[3];
        int atk = pokemob.getActualStats()[1];
        int level = pokemob.getLevel();
        int maxEnergy = TileEntitySiphon.getMaxEnergy(level, spAtk, atk, pokemob.getPokedexEntry());
        int pokeEnergy = maxEnergy;
        int dE;
        long energyTime = living.getEntityWorld().getTotalWorldTime();
        boolean first = true;
        if (living.getEntityData().hasKey("energyRemaining"))
        {
            long time = living.getEntityData().getLong("energyTime");
            if (energyTime != time)
            {
                pokeEnergy = maxEnergy;
            }
            else
            {
                first = false;
                pokeEnergy = living.getEntityData().getInteger("energyRemaining");
            }
        }
        dE = (int) (pokeEnergy);
        dE = (int) Math.min(dE, power);
        if (!simulated)
        {
            living.getEntityData().setLong("energyTime", energyTime);
            living.getEntityData().setInteger("energyRemaining", pokeEnergy - dE);
            if (first && living.ticksExisted % 2 == 0)
            {
                int time = ((IHungrymob) pokemob).getHungerTime();
                ((IHungrymob) pokemob).setHungerTime(time + 1);
            }
        }
        return dE;
    }
}
