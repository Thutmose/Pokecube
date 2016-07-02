package pokecube.compat.tesla;

import net.darkhax.tesla.api.ITeslaConsumer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import pokecube.adventures.blocks.cloner.TileEntityCloner;

public class ProviderCloner implements ITeslaConsumer, ICapabilityProvider
{
    private final TileEntityCloner tile;

    public ProviderCloner(TileEntityCloner tile)
    {
        this.tile = tile;
    }

    @Override
    public long givePower(long power, boolean simulated)
    {
        return tile.receiveEnergy(null, (int) power, simulated);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing)
    {
        return capability == TeslaHandler.TESLA_CONSUMER;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing)
    {
        return (capability == TeslaHandler.TESLA_CONSUMER) ? (T) this : null;
    }
}
