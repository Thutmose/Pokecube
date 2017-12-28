package pokecube.compat.tesla;

import net.darkhax.tesla.api.ITeslaConsumer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import pokecube.adventures.blocks.warppad.TileEntityWarpPad;

public class ProviderWarppad implements ITeslaConsumer, ICapabilityProvider
{
    private final TileEntityWarpPad tile;

    public ProviderWarppad(TileEntityWarpPad tile)
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

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing)
    {
        return (capability == TeslaHandler.TESLA_CONSUMER) ? TeslaHandler.TESLA_CONSUMER.cast(this) : null;
    }
}
