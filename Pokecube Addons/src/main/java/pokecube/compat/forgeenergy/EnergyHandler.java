package pokecube.compat.forgeenergy;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.entity.EntityLiving;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.blocks.afa.TileEntityAFA;
import pokecube.adventures.blocks.cloner.TileEntityCloner;
import pokecube.adventures.blocks.siphon.SiphonTickEvent;
import pokecube.adventures.blocks.siphon.TileEntitySiphon;
import pokecube.adventures.blocks.warppad.TileEntityWarpPad;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.utils.PokeType;
import thut.api.entity.IHungrymob;
import thut.api.maths.Vector3;

public class EnergyHandler
{
    public static int getOutput(TileEntitySiphon tile, int power, boolean simulated)
    {
        if (tile.getWorld() == null || power == 0) return 0;
        Vector3 v = Vector3.getNewVector().set(tile);
        AxisAlignedBB box = v.getAABB().expand(10, 10, 10);
        List<EntityLiving> l = tile.getWorld().getEntitiesWithinAABB(EntityLiving.class, box);
        int ret = 0;
        for (EntityLiving living : l)
        {
            if (living != null && living instanceof IPokemob)
            {
                IEnergyStorage producer = living.getCapability(CapabilityEnergy.ENERGY, null);
                if (producer != null)
                {
                    double dSq = Math.max(1, living.getDistanceSq(tile.getPos().getX() + 0.5,
                            tile.getPos().getY() + 0.5, tile.getPos().getZ() + 0.5));
                    int input = (int) (producer.extractEnergy(PokecubeAdv.conf.maxOutput, simulated) / dSq);
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
        if (!simulated) tile.currentOutput = (int) ret;
        return ret;
    }

    @SubscribeEvent
    public void SiphonEvent(SiphonTickEvent event)
    {
        Map<IEnergyStorage, Integer> tiles = Maps.newHashMap();
        Integer output = (int) getOutput(event.getTile(), PokecubeAdv.conf.maxOutput, true);
        event.getTile().theoreticalOutput = (int) output;
        event.getTile().currentOutput = 0;
        IEnergyStorage producer = event.getTile().getCapability(CapabilityEnergy.ENERGY, null);
        Integer start = output;
        Vector3 v = Vector3.getNewVector().set(event.getTile());
        for (EnumFacing side : EnumFacing.values())
        {
            TileEntity te = v.getTileEntity(event.getTile().getWorld(), side);
            IEnergyStorage cap;
            if (te != null && (cap = te.getCapability(CapabilityEnergy.ENERGY, side.getOpposite())) != null)
            {
                Integer toSend = cap.receiveEnergy(output, true);
                if (toSend > 0 && cap.canReceive())
                {
                    tiles.put(cap, toSend);
                }
            }
        }
        for (Map.Entry<IEnergyStorage, Integer> entry : tiles.entrySet())
        {
            Integer fraction = output / tiles.size();
            Integer request = entry.getValue();
            if (request > fraction)
            {
                request = fraction;
            }
            if (fraction == 0 || output <= 0) continue;
            IEnergyStorage h = entry.getKey();
            output -= request;
            h.receiveEnergy(request, false);
        }
        producer.extractEnergy(start - output, false);
    }

    @SubscribeEvent
    public void onEntityCapabilityAttach(AttachCapabilitiesEvent<?> event)
    {
        if (event.getObject() instanceof IPokemob)
        {
            event.addCapability(new ResourceLocation("pokecube:energy"),
                    new ProviderPokemob((IPokemob) event.getObject()));
        }
        else if (event.getObject() instanceof TileEntityAFA)
        {
            event.addCapability(new ResourceLocation("pokecube:tesla"),
                    new ProviderAFA((TileEntityAFA) event.getObject()));
        }
        else if (event.getObject() instanceof TileEntitySiphon)
        {
            event.addCapability(new ResourceLocation("pokecube:tesla"),
                    new ProviderSiphon((TileEntitySiphon) event.getObject()));
        }
        else if (event.getObject() instanceof TileEntityCloner)
        {
            event.addCapability(new ResourceLocation("pokecube:tesla"),
                    new ProviderCloner((TileEntityCloner) event.getObject()));
        }
        else if (event.getObject() instanceof TileEntityWarpPad)
        {
            event.addCapability(new ResourceLocation("pokecube:tesla"),
                    new ProviderWarppad((TileEntityWarpPad) event.getObject()));
        }
    }

    public static class ProviderSiphon extends EnergyStorage implements ICapabilityProvider
    {
        final TileEntitySiphon tile;

        public ProviderSiphon(TileEntitySiphon tile)
        {
            super(0);
            this.tile = tile;
        }

        @Override
        public boolean hasCapability(Capability<?> capability, EnumFacing facing)
        {
            return capability == CapabilityEnergy.ENERGY;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T getCapability(Capability<T> capability, EnumFacing facing)
        {
            return (capability == CapabilityEnergy.ENERGY) ? (T) this : null;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate)
        {
            return getOutput(tile, maxExtract, simulate);
        }

    }

    public static class ProviderAFA extends EnergyStorage implements ICapabilityProvider
    {
        private final TileEntityAFA tile;

        public ProviderAFA(TileEntityAFA tile)
        {
            super(0);
            this.tile = tile;
        }

        @Override
        public boolean canReceive()
        {
            return true;
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate)
        {
            return tile.receiveEnergy(null, maxReceive, simulate);
        }

        @Override
        public boolean hasCapability(Capability<?> capability, EnumFacing facing)
        {
            return capability == CapabilityEnergy.ENERGY;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T getCapability(Capability<T> capability, EnumFacing facing)
        {
            return (capability == CapabilityEnergy.ENERGY) ? (T) this : null;
        }
    }

    public static class ProviderCloner extends EnergyStorage implements ICapabilityProvider
    {
        private final TileEntityCloner tile;

        public ProviderCloner(TileEntityCloner tile)
        {
            super(0);
            this.tile = tile;
        }

        @Override
        public boolean canReceive()
        {
            return true;
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate)
        {
            return tile.receiveEnergy(null, maxReceive, simulate);
        }

        @Override
        public boolean hasCapability(Capability<?> capability, EnumFacing facing)
        {
            return capability == CapabilityEnergy.ENERGY;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T getCapability(Capability<T> capability, EnumFacing facing)
        {
            return (capability == CapabilityEnergy.ENERGY) ? (T) this : null;
        }
    }

    public static class ProviderWarppad extends EnergyStorage implements ICapabilityProvider
    {
        private final TileEntityWarpPad tile;

        public ProviderWarppad(TileEntityWarpPad tile)
        {
            super(0);
            this.tile = tile;
        }

        @Override
        public boolean canReceive()
        {
            return true;
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate)
        {
            return tile.receiveEnergy(null, maxReceive, simulate);
        }

        @Override
        public boolean hasCapability(Capability<?> capability, EnumFacing facing)
        {
            return capability == CapabilityEnergy.ENERGY;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T getCapability(Capability<T> capability, EnumFacing facing)
        {
            return (capability == CapabilityEnergy.ENERGY) ? (T) this : null;
        }
    }

    public static class ProviderPokemob extends EnergyStorage implements ICapabilityProvider
    {
        final IPokemob pokemob;

        public ProviderPokemob(IPokemob pokemob)
        {
            super(0);
            this.pokemob = pokemob;
        }

        @Override
        public boolean hasCapability(Capability<?> capability, EnumFacing facing)
        {
            return capability == CapabilityEnergy.ENERGY;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T getCapability(Capability<T> capability, EnumFacing facing)
        {
            return (capability == CapabilityEnergy.ENERGY) ? (T) this : null;
        }

        @Override
        public boolean canExtract()
        {
            return pokemob.isType(PokeType.electric);
        }

        @Override
        public int extractEnergy(int power, boolean simulate)
        {
            if (!canExtract()) return 0;
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
            dE = (pokeEnergy);
            dE = (int) Math.min(dE, power);
            if (!simulate)
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

}
