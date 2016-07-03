package pokecube.compat.tesla;

import java.util.Map;

import com.google.common.collect.Maps;

import net.darkhax.tesla.api.ITeslaConsumer;
import net.darkhax.tesla.api.ITeslaHolder;
import net.darkhax.tesla.api.ITeslaProducer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pokecube.adventures.blocks.afa.TileEntityAFA;
import pokecube.adventures.blocks.cloner.TileEntityCloner;
import pokecube.adventures.blocks.siphon.SiphonTickEvent;
import pokecube.adventures.blocks.siphon.TileEntitySiphon;
import pokecube.adventures.blocks.warppad.TileEntityWarpPad;
import thut.api.maths.Vector3;

public class TeslaHandler
{
    @CapabilityInject(ITeslaConsumer.class)
    public static Capability<ITeslaConsumer> TESLA_CONSUMER = null;
    @CapabilityInject(ITeslaProducer.class)
    public static Capability<ITeslaProducer> TESLA_PRODUCER = null;
    @CapabilityInject(ITeslaHolder.class)
    public static Capability<ITeslaHolder>   TESLA_HOLDER   = null;

    public TeslaHandler()
    {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onTileEntityCapabilityAttach(AttachCapabilitiesEvent.TileEntity event)
    {
        if (event.getTileEntity() instanceof TileEntityAFA)
        {
            event.addCapability(new ResourceLocation("pokecube:tesla"),
                    new ProviderAFA((TileEntityAFA) event.getTileEntity()));
        }
        else if (event.getTileEntity() instanceof TileEntitySiphon)
        {
            event.addCapability(new ResourceLocation("pokecube:tesla"),
                    new ProviderSiphon((TileEntitySiphon) event.getTileEntity()));
        }
        else if (event.getTileEntity() instanceof TileEntityCloner)
        {
            event.addCapability(new ResourceLocation("pokecube:tesla"),
                    new ProviderCloner((TileEntityCloner) event.getTileEntity()));
        }
        else if (event.getTileEntity() instanceof TileEntityWarpPad)
        {
            event.addCapability(new ResourceLocation("pokecube:tesla"),
                    new ProviderWarppad((TileEntityWarpPad) event.getTileEntity()));
        }
    }

    @SubscribeEvent
    public void onEntityCapabilityAttach(AttachCapabilitiesEvent.Entity event)
    {
    }

    @SubscribeEvent
    public void SiphonEvent(SiphonTickEvent event)
    {
        Map<ITeslaConsumer, Long> tiles = Maps.newHashMap();
        int input = event.getTile().getInput(true);
        Vector3 v = Vector3.getNewVector().set(event.getTile());
        for (EnumFacing side : EnumFacing.values())
        {
            TileEntity te = v.getTileEntity(event.getTile().getWorld(), side);
            ITeslaConsumer cap;
            if (te != null && (cap = te.getCapability(TESLA_CONSUMER, side.getOpposite())) != null)
            {
                long toSend = cap.givePower(input, true);
                if (toSend > 0)
                {
                    tiles.put(cap, toSend);
                }
            }
        }
        for (Map.Entry<ITeslaConsumer, Long> entry : tiles.entrySet())
        {
            int fraction = input / tiles.size();
            long request = entry.getValue();
            if (request > fraction)
            {
                request = fraction;
            }
            if (fraction == 0 || input <= 0) continue;
            ITeslaConsumer h = entry.getKey();
            input -= request;
            h.givePower(request, false);
        }
    }
}
