package pokecube.compat.tesla;

import java.util.List;

import net.darkhax.tesla.api.ITeslaProducer;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.blocks.siphon.TileEntitySiphon;
import pokecube.core.interfaces.IPokemob;
import thut.api.maths.Vector3;

public class ProviderSiphon implements ITeslaProducer, ICapabilityProvider
{
    final TileEntitySiphon tile;

    public ProviderSiphon(TileEntitySiphon tile)
    {
        this.tile = tile;
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
        long ret = getOutput(power, simulated);
        if (!simulated) tile.currentOutput = (int) ret;
        return ret;
    }

    public long getOutput(long power, boolean simulated)
    {
        if (tile.getWorld() == null || power == 0) return 0;
        Vector3 v = Vector3.getNewVector().set(tile);
        AxisAlignedBB box = v.getAABB().expand(10, 10, 10);
        List<EntityLiving> l = tile.getWorld().getEntitiesWithinAABB(EntityLiving.class, box);
        long ret = 0;
        for (EntityLiving living : l)
        {
            if (living != null && living instanceof IPokemob)
            {
                ITeslaProducer producer = living.getCapability(TeslaHandler.TESLA_PRODUCER, null);
                if (producer != null)
                {
                    double dSq = Math.max(1, living.getDistanceSq(tile.getPos().getX() + 0.5,
                            tile.getPos().getY() + 0.5, tile.getPos().getZ() + 0.5));
                    long input = (long) (producer.takePower(PokecubeAdv.conf.maxOutput, simulated) / dSq);
                    ret += input;
                    if (ret >= power)
                    {
                        ret = power;
                        break;
                    }
                }
            }
        }
        ret = Math.min(ret, PokecubeAdv.conf.maxOutput);
        return ret;
    }

}
