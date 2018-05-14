package pokecube.compat.tesla;

import net.darkhax.tesla.api.ITeslaProducer;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import pokecube.adventures.blocks.siphon.TileEntitySiphon;
import pokecube.adventures.commands.Config;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.Stats;
import pokecube.core.utils.PokeType;

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

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing)
    {
        return (capability == TeslaHandler.TESLA_PRODUCER) ? TeslaHandler.TESLA_PRODUCER.cast(this) : null;
    }

    @Override
    public long takePower(long power, boolean simulated)
    {
        if (!pokemob.isType(PokeType.getType("electric"))) return 0;
        EntityLiving living = pokemob.getEntity();
        int spAtk = pokemob.getStat(Stats.SPATTACK, true);
        int atk = pokemob.getStat(Stats.ATTACK, true);
        int level = pokemob.getLevel();
        int maxEnergy = TileEntitySiphon.getMaxEnergy(level, spAtk, atk, pokemob.getPokedexEntry());
        int pokeEnergy = maxEnergy;
        int dE;
        long energyTime = living.getEntityWorld().getTotalWorldTime();
        if (living.getEntityData().hasKey("energyRemaining"))
        {
            long time = living.getEntityData().getLong("energyTime");
            if (energyTime != time)
            {
                pokeEnergy = maxEnergy;
            }
            else
            {
                pokeEnergy = living.getEntityData().getInteger("energyRemaining");
            }
        }
        dE = (maxEnergy);
        dE = (int) Math.min(dE, power);
        if (!simulated)
        {
            living.getEntityData().setLong("energyTime", energyTime);
            living.getEntityData().setInteger("energyRemaining", pokeEnergy - dE);
            int drain = 0;
            if (pokeEnergy - dE < 0)
            {
                drain = dE - pokeEnergy;
            }
            if (living.ticksExisted % 2 == 0)
            {
                int time = pokemob.getHungerTime();
                pokemob.setHungerTime(
                        time + Config.instance.energyHungerCost + drain * Config.instance.energyHungerCost);
            }
        }
        return dE;
    }
}
