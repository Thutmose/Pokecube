package pokecube.compat.rf;

import java.util.Map;

import com.google.common.collect.Maps;

import cofh.api.energy.IEnergyReceiver;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pokecube.adventures.blocks.siphon.SiphonTickEvent;
import thut.api.maths.Vector3;

public class SiphonHandler
{
    public SiphonHandler()
    {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void SiphonEvent(SiphonTickEvent event)
    {
        Map<IEnergyReceiver, Integer> tiles = Maps.newHashMap();
        Map<IEnergyReceiver, EnumFacing> sides = Maps.newHashMap();
        int input = event.getTile().getInput(true);
        Vector3 v = Vector3.getNewVector().set(event.getTile());
        for (EnumFacing side : EnumFacing.values())
        {
            TileEntity te = v.getTileEntity(event.getTile().getWorld(), side);
            if (te != null && te instanceof IEnergyReceiver)
            {
                IEnergyReceiver h = (IEnergyReceiver) te;
                int toSend = h.receiveEnergy(side.getOpposite(), input, true);
                if (toSend > 0)
                {
                    tiles.put(h, toSend);
                }
            }
        }
        for (Map.Entry<IEnergyReceiver, Integer> entry : tiles.entrySet())
        {
            int fraction = input / tiles.size();
            int request = entry.getValue();
            if (request > fraction)
            {
                request = fraction;
            }
            if (fraction == 0 || input <= 0) continue;
            IEnergyReceiver h = entry.getKey();
            input -= request;
            h.receiveEnergy(sides.get(h), request, false);
        }
    }
}
