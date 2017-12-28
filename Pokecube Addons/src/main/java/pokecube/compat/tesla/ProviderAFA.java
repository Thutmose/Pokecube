package pokecube.compat.tesla;

import net.darkhax.tesla.api.ITeslaConsumer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import pokecube.adventures.blocks.afa.TileEntityAFA;

public class ProviderAFA implements ITeslaConsumer, ICapabilityProvider
{
    private final TileEntityAFA tile;

    public ProviderAFA(TileEntityAFA tile)
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
        return facing == EnumFacing.DOWN && capability == TeslaHandler.TESLA_CONSUMER;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing)
    {
        return hasCapability(capability, facing) ? TeslaHandler.TESLA_CONSUMER.cast(this) : null;
    }
}
