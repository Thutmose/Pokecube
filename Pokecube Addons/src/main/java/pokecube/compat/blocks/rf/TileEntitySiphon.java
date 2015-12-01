package pokecube.compat.blocks.rf;

import java.util.List;

import cofh.api.energy.EnergyStorage;
import cofh.api.energy.IEnergyReceiver;
import cofh.api.energy.TileEnergyHandler;
import net.minecraft.entity.EntityLiving;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.world.World;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IHungrymob;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.utils.PokeType;
import thut.api.maths.Vector3;

public class TileEntitySiphon extends TileEnergyHandler implements ITickable
{
    AxisAlignedBB     box;
    public static int maxOutput = 256;

    public TileEntitySiphon()
    {
        storage = new EnergyStorage(maxOutput, 0, maxOutput);
    }

    public TileEntitySiphon(World world)
    {
        this();
    }

    @Override
    public void update()
    {
        Vector3 v = Vector3.getNewVectorFromPool().set(this);
        if (box == null)
        {
            box = v.getAABB().expand(5, 5, 5);
        }
        int input = getInput();
        storage.setEnergyStored(input);

        for (EnumFacing side : EnumFacing.values())
        {
            TileEntity te = v.getTileEntity(worldObj, side);
            if (te != null && te instanceof IEnergyReceiver)
            {
                IEnergyReceiver h = (IEnergyReceiver) te;

                int toSend = h.receiveEnergy(side.getOpposite(), storage.getEnergyStored(), true);
                h.receiveEnergy(side.getOpposite(), toSend, false);
            }
        }

        v.freeVectorFromPool();

    }

    public int getInput()
    {
        List<EntityLiving> l = worldObj.getEntitiesWithinAABB(EntityLiving.class, box);
        int ret = 0;
        for (Object o : l)
        {
            if (o != null && o instanceof IPokemob)
            {
                IPokemob poke = (IPokemob) o;
                if (poke.isType(PokeType.electric))
                {
                    int spAtk = poke.getBaseStats()[3];
                    int atk = poke.getBaseStats()[1];
                    int level = poke.getLevel();
                    double dSq = ((EntityLiving) poke).getDistanceSq(getPos().getX() + 0.5, getPos().getY() + 0.5,
                            getPos().getZ() + 0.5) * 1024;
                    double pokeEnergy = 0;
                    int maxEnergy = getMaxEnergy(level, spAtk, atk, poke.getPokedexEntry());
                    int dE = getEnergyGain(level, spAtk, atk, poke.getPokedexEntry());
                    long energyTime = worldObj.getTotalWorldTime();
                    if (((EntityLiving) poke).getEntityData().hasKey("energyTime"))
                    {
                        long time = ((EntityLiving) poke).getEntityData().getLong("energyTime");
                        int dt = (int) (worldObj.getTotalWorldTime() - time);

                        if (dt > 0)
                        {
                            pokeEnergy = dE * dt;

                            if (pokeEnergy > maxEnergy)
                            {
                                int dt2 = maxEnergy / dE;
                                pokeEnergy = maxEnergy;
                                energyTime -= dt2;
                            }
                        }

                    }
                    else
                    {
                        pokeEnergy = maxEnergy;
                        int dt2 = maxEnergy / dE;
                        energyTime -= dt2;
                    }
                    ret += pokeEnergy / dSq;
                    ((EntityLiving) poke).getEntityData().setLong("energyTime", energyTime);
                    if (((EntityLiving) poke).ticksExisted % 2 == 0)
                    {
                        int time = ((IHungrymob) poke).getHungerTime();
                        ((IHungrymob) poke).setHungerTime(time + 1);
                    }
                }
            }
        }

        return Math.min(ret, maxOutput);
    }

    public int getEnergyGain(int level, int spAtk, int atk, PokedexEntry entry)
    {
        int power = Math.max(atk, spAtk) * level;
        return power;
    }

    public int getMaxEnergy(int level, int spAtk, int atk, PokedexEntry entry)
    {
        return 100 * getEnergyGain(level, spAtk, atk, entry);
    }
}
