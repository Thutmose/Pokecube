package pokecube.compat.rf;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

import cofh.api.energy.IEnergyReceiver;
import net.minecraft.entity.EntityLiving;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.blocks.siphon.SiphonTickEvent;
import pokecube.adventures.blocks.siphon.TileEntitySiphon;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.utils.PokeType;
import thut.api.entity.IHungrymob;
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
        int output = getInput(event.getTile());
        event.getTile().theoreticalOutput = output;
        event.getTile().currentOutput = 0;
        Vector3 v = Vector3.getNewVector().set(event.getTile());
        for (EnumFacing side : EnumFacing.values())
        {
            TileEntity te = v.getTileEntity(event.getTile().getWorld(), side);
            if (te != null && te instanceof IEnergyReceiver)
            {
                IEnergyReceiver h = (IEnergyReceiver) te;
                int toSend = h.receiveEnergy(side.getOpposite(), output, true);
                if (toSend > 0)
                {
                    tiles.put(h, toSend);
                }
            }
        }
        for (Map.Entry<IEnergyReceiver, Integer> entry : tiles.entrySet())
        {
            int fraction = output / tiles.size();
            int request = entry.getValue();
            if (request > fraction)
            {
                request = fraction;
            }
            if (fraction == 0 || output <= 0) continue;
            IEnergyReceiver h = entry.getKey();
            output -= request;
            event.getTile().currentOutput += request;
            h.receiveEnergy(sides.get(h), request, false);
        }
    }

    public int getInput(TileEntitySiphon siphon)
    {
        if (siphon.getWorld() == null) return 0;
        Vector3 v = Vector3.getNewVector().set(siphon);
        AxisAlignedBB box = v.getAABB().expand(10, 10, 10);
        List<EntityLiving> l = siphon.getWorld().getEntitiesWithinAABB(EntityLiving.class, box);
        int ret = 0;
        for (Object o : l)
        {
            if (o != null && o instanceof IPokemob)
            {
                IPokemob poke = (IPokemob) o;
                EntityLiving living = (EntityLiving) o;
                if (poke.isType(PokeType.electric))
                {
                    int spAtk = poke.getActualStats()[3];
                    int atk = poke.getActualStats()[1];
                    int level = poke.getLevel();
                    double dSq = living.getDistanceSq(siphon.getPos().getX() + 0.5, siphon.getPos().getY() + 0.5,
                            siphon.getPos().getZ() + 0.5);
                    dSq = Math.max(dSq, 1);
                    int maxEnergy = TileEntitySiphon.getMaxEnergy(level, spAtk, atk, poke.getPokedexEntry());
                    int pokeEnergy = maxEnergy;
                    int dE;
                    long energyTime = siphon.getWorld().getTotalWorldTime();
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
                    dE = (int) (pokeEnergy / dSq);
                    // If out of power, no power
                    dE = Math.max(0, dE);
                    ret += dE;
                    // Always drain at least 1
                    dE = Math.max(1, dE);

                    living.getEntityData().setLong("energyTime", energyTime);
                    living.getEntityData().setInteger("energyRemaining", pokeEnergy - dE);
                    if (first && living.ticksExisted % 2 == 0)
                    {
                        int time = ((IHungrymob) poke).getHungerTime();
                        ((IHungrymob) poke).setHungerTime(time + 1);
                    }
                }
            }
        }
        return Math.min(ret, PokecubeAdv.conf.maxOutput);
    }
}
