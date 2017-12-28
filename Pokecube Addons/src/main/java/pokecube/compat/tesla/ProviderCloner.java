package pokecube.compat.tesla;

import net.darkhax.tesla.api.ITeslaConsumer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import pokecube.adventures.blocks.cloner.tileentity.TileClonerBase;

public class ProviderCloner implements ITeslaConsumer, ICapabilityProvider
{
    private final TileClonerBase tile;

    public ProviderCloner(TileClonerBase tile)
    {
        this.tile = tile;
    }

    @Override
    public long givePower(long power, boolean simulated)
    {
        return tile.addEnergy((int) Math.min(Integer.MAX_VALUE, power), simulated);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing)
    {
        return capability == TeslaHandler.TESLA_CONSUMER;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing)
    {
        return (capability == TeslaHandler.TESLA_CONSUMER) ? TeslaHandler.TESLA_CONSUMER.cast(this) : null;
    }
}
