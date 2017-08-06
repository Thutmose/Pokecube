package pokecube.compat.tesla;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

import net.darkhax.tesla.api.ITeslaConsumer;
import net.darkhax.tesla.api.ITeslaHolder;
import net.darkhax.tesla.api.ITeslaProducer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.blocks.afa.TileEntityAFA;
import pokecube.adventures.blocks.cloner.tileentity.TileClonerBase;
import pokecube.adventures.blocks.siphon.SiphonTickEvent;
import pokecube.adventures.blocks.siphon.TileEntitySiphon;
import pokecube.adventures.blocks.warppad.TileEntityWarpPad;
import pokecube.compat.ai.AITeslaInterferance;
import pokecube.core.events.handlers.EventsHandler;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import thut.api.maths.Vector3;
import thut.lib.CompatClass;
import thut.lib.CompatClass.Phase;

public class TeslaHandler
{
    @CapabilityInject(ITeslaConsumer.class)
    public static Capability<ITeslaConsumer> TESLA_CONSUMER = null;
    @CapabilityInject(ITeslaProducer.class)
    public static Capability<ITeslaProducer> TESLA_PRODUCER = null;
    @CapabilityInject(ITeslaHolder.class)
    public static Capability<ITeslaHolder>   TESLA_HOLDER   = null;

    @Optional.Method(modid = "tesla")
    @CompatClass(phase = Phase.PRE)
    public static void TeslaCompat()
    {
        new TeslaHandler();
    }

    public TeslaHandler()
    {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void addTeslaInterferance(EntityJoinWorldEvent evt)
    {
        IPokemob mob = CapabilityPokemob.getPokemobFor(evt.getEntity());
        if (mob != null)
        {
            mob.getEntity().tasks.addTask(1, new AITeslaInterferance(mob));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    /** Priority low, so that the IPokemob capability is added first. */
    public void onEntityCapabilityAttach(AttachCapabilitiesEvent<Entity> event)
    {
        if (!event.getCapabilities().containsKey(EventsHandler.POKEMOBCAP)
                || event.getCapabilities().containsKey(new ResourceLocation("pokecube:tesla"))
                || event.getObject().getEntityWorld() == null)
            return;
        IPokemob pokemob = event.getCapabilities().get(EventsHandler.POKEMOBCAP)
                .getCapability(CapabilityPokemob.POKEMOB_CAP, null);
        if (pokemob != null)
        {
            event.addCapability(new ResourceLocation("pokecube:tesla"), new ProviderPokemob(pokemob));
        }
    }

    @SubscribeEvent
    public void onTileEntityCapabilityAttach(AttachCapabilitiesEvent<TileEntity> event)
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

    @SubscribeEvent
    public void SiphonEvent(SiphonTickEvent event)
    {
        ITeslaProducer producer = event.getTile().getCapability(TESLA_PRODUCER, null);
        Map<ITeslaConsumer, Long> tiles = Maps.newHashMap();
        Map<IEnergyStorage, Integer> tiles2 = Maps.newHashMap();
        long output = producer.takePower(PokecubeAdv.conf.maxOutput, true);
        event.getTile().theoreticalOutput = (int) output;
        event.getTile().currentOutput = 0;
        long start = output;
        Vector3 v = Vector3.getNewVector().set(event.getTile());
        for (EnumFacing side : EnumFacing.values())
        {
            TileEntity te = v.getTileEntity(event.getTile().getWorld(), side);
            ITeslaConsumer cap;
            IEnergyStorage cap2;
            if (te != null && (cap = te.getCapability(TESLA_CONSUMER, side.getOpposite())) != null)
            {
                long toSend = cap.givePower(output, true);
                if (toSend > 0)
                {
                    tiles.put(cap, toSend);
                }
            }
            else if (te != null && (cap2 = te.getCapability(CapabilityEnergy.ENERGY, side.getOpposite())) != null)
            {
                Integer toSend = cap2.receiveEnergy((int) output, true);
                if (toSend > 0 && cap2.canReceive())
                {
                    tiles2.put(cap2, toSend);
                }
            }
        }
        for (Map.Entry<ITeslaConsumer, Long> entry : tiles.entrySet())
        {
            long fraction = output / tiles.size();
            long request = entry.getValue();
            if (request > fraction)
            {
                request = fraction;
            }
            if (fraction == 0 || output <= 0) continue;
            ITeslaConsumer h = entry.getKey();
            output -= request;
            h.givePower(request, false);
        }
        for (Map.Entry<IEnergyStorage, Integer> entry : tiles2.entrySet())
        {
            Integer fraction = (int) (output / tiles2.size());
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
        producer.takePower(start - output, false);
    }

    public static long getOutput(TileEntitySiphon tile, long power, boolean simulated)
    {
        if (tile.getWorld() == null || power == 0) return 0;
        Vector3 v = Vector3.getNewVector().set(tile);
        AxisAlignedBB box = v.getAABB().grow(10, 10, 10);
        List<EntityLiving> l = tile.getWorld().getEntitiesWithinAABB(EntityLiving.class, box);
        long ret = 0;
        power = Math.min(power, PokecubeAdv.conf.maxOutput);
        for (EntityLiving living : l)
        {
            if (living != null)
            {
                ITeslaProducer producer = living.getCapability(TeslaHandler.TESLA_PRODUCER, null);
                if (producer != null)
                {
                    double dSq = Math.max(1, living.getDistanceSq(tile.getPos().getX() + 0.5,
                            tile.getPos().getY() + 0.5, tile.getPos().getZ() + 0.5));
                    long input = (long) (producer.takePower((long) (PokecubeAdv.conf.maxOutput / dSq), simulated));
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
}
