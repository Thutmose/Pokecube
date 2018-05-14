package pokecube.compat.forgeenergy;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.entity.Entity;
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
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.blocks.afa.TileEntityAFA;
import pokecube.adventures.blocks.cloner.tileentity.TileClonerBase;
import pokecube.adventures.blocks.siphon.SiphonTickEvent;
import pokecube.adventures.blocks.siphon.TileEntitySiphon;
import pokecube.adventures.blocks.warppad.TileEntityWarpPad;
import pokecube.adventures.commands.Config;
import pokecube.core.events.handlers.EventsHandler;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.Stats;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.utils.PokeType;
import thut.api.maths.Vector3;

public class EnergyHandler
{
    public static int getOutput(TileEntitySiphon tile, int power, boolean simulated)
    {
        if (tile.getWorld() == null || power == 0) return 0;
        Vector3 v = Vector3.getNewVector().set(tile);
        AxisAlignedBB box = v.getAABB().grow(10, 10, 10);
        List<EntityLiving> l = tile.getWorld().getEntitiesWithinAABB(EntityLiving.class, box);
        int ret = 0;
        power = Math.min(power, PokecubeAdv.conf.maxOutput);
        for (EntityLiving living : l)
        {
            if (living != null)
            {
                IEnergyStorage producer = living.getCapability(CapabilityEnergy.ENERGY, null);
                if (producer != null)
                {
                    double dSq = Math.max(1, living.getDistanceSq(tile.getPos().getX() + 0.5,
                            tile.getPos().getY() + 0.5, tile.getPos().getZ() + 0.5));
                    int input = (int) (producer.extractEnergy((int) (PokecubeAdv.conf.maxOutput / dSq), simulated));
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

    @SubscribeEvent(priority = EventPriority.LOW)
    /** Priority low, so that the IPokemob capability is added first. */
    public void onEntityCapabilityAttach(AttachCapabilitiesEvent<Entity> event)
    {
        if (!event.getCapabilities().containsKey(EventsHandler.POKEMOBCAP)
                || event.getCapabilities().containsKey(new ResourceLocation("pokecube:energy"))
                || event.getObject().getEntityWorld() == null)
            return;
        IPokemob pokemob = event.getCapabilities().get(EventsHandler.POKEMOBCAP)
                .getCapability(CapabilityPokemob.POKEMOB_CAP, null);
        if (pokemob != null)
        {
            event.addCapability(new ResourceLocation("pokecube:energy"), new ProviderPokemob(pokemob));
        }
    }

    @SubscribeEvent
    public void onTileCapabilityAttach(AttachCapabilitiesEvent<TileEntity> event)
    {
        if (event.getObject() instanceof TileEntityAFA)
        {
            event.addCapability(new ResourceLocation("pokecube:tesla"),
                    new ProviderAFA((TileEntityAFA) event.getObject()));
        }
        else if (event.getObject() instanceof TileEntitySiphon)
        {
            event.addCapability(new ResourceLocation("pokecube:tesla"),
                    new ProviderSiphon((TileEntitySiphon) event.getObject()));
        }
        else if (event.getObject() instanceof TileClonerBase)
        {
            event.addCapability(new ResourceLocation("pokecube:tesla"),
                    new ProviderCloner((TileClonerBase) event.getObject()));
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

        @Override
        public <T> T getCapability(Capability<T> capability, EnumFacing facing)
        {
            return (capability == CapabilityEnergy.ENERGY) ? CapabilityEnergy.ENERGY.cast(this) : null;
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

        @Override
        public <T> T getCapability(Capability<T> capability, EnumFacing facing)
        {
            return (capability == CapabilityEnergy.ENERGY) ? CapabilityEnergy.ENERGY.cast(this) : null;
        }
    }

    public static class ProviderCloner extends EnergyStorage implements ICapabilityProvider
    {
        private final TileClonerBase tile;

        public ProviderCloner(TileClonerBase tile)
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
            return tile.addEnergy(maxReceive, simulate);
        }

        @Override
        public boolean hasCapability(Capability<?> capability, EnumFacing facing)
        {
            return capability == CapabilityEnergy.ENERGY;
        }

        @Override
        public <T> T getCapability(Capability<T> capability, EnumFacing facing)
        {
            return (capability == CapabilityEnergy.ENERGY) ? CapabilityEnergy.ENERGY.cast(this) : null;
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

        @Override
        public <T> T getCapability(Capability<T> capability, EnumFacing facing)
        {
            return (capability == CapabilityEnergy.ENERGY) ? CapabilityEnergy.ENERGY.cast(this) : null;
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

        @Override
        public <T> T getCapability(Capability<T> capability, EnumFacing facing)
        {
            return (capability == CapabilityEnergy.ENERGY) ? CapabilityEnergy.ENERGY.cast(this) : null;
        }

        @Override
        public boolean canExtract()
        {
            return pokemob.isType(PokeType.getType("electric"));
        }

        @Override
        public int extractEnergy(int power, boolean simulate)
        {
            if (!canExtract()) return 0;
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
            if (!simulate)
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

}
