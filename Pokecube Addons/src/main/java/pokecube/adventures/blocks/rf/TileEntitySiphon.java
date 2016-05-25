package pokecube.adventures.blocks.rf;

import java.util.List;
import java.util.Map;

import org.nfunk.jep.JEP;

import com.google.common.collect.Maps;

import cofh.api.energy.IEnergyProvider;
import cofh.api.energy.IEnergyReceiver;
import net.minecraft.entity.EntityLiving;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional.Interface;
import pokecube.adventures.PokecubeAdv;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.utils.PokeType;
import thut.api.entity.IHungrymob;
import thut.api.maths.Vector3;

@Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "OpenComputers")
public class TileEntitySiphon extends TileEntity implements ITickable, IEnergyProvider//, SimpleComponent, 
{
    AxisAlignedBB box;
    public JEP    parser    = new JEP();
    int           lastInput = 0;

    public TileEntitySiphon()
    {
        initParser();
    }

    public TileEntitySiphon(World world)
    {
        this();
    }

    @Override
    public boolean canConnectEnergy(EnumFacing facing)
    {
        return true;
    }

    @Override
    public int extractEnergy(EnumFacing facing, int maxExtract, boolean simulate)
    {
        int ret = maxExtract;
        if (lastInput < maxExtract)
        {
            ret = lastInput;
        }
        if (!simulate) lastInput -= ret;
        return ret;
    }

//    @Override
//    public String getComponentName()
//    {
//        return "pokesiphon";
//    }

    public int getEnergyGain(int level, int spAtk, int atk, PokedexEntry entry)
    {
        int power = Math.max(atk, spAtk);
        parser.setVarValue("x", level);
        parser.setVarValue("a", power);
        double value = parser.getValue();
        if (Double.isNaN(value))
        {
            initParser();
            parser.setVarValue("x", level);
            parser.setVarValue("a", power);
            value = parser.getValue();
            new Exception().printStackTrace();
        }
        power = (int) value;
        return Math.max(1, power);
    }

    @Override
    public int getEnergyStored(EnumFacing facing)
    {
        return lastInput;
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
                EntityLiving living = (EntityLiving) o;
                if (poke.isType(PokeType.electric))
                {
                    int spAtk = poke.getActualStats()[3];
                    int atk = poke.getActualStats()[1];
                    int level = poke.getLevel();
                    double dSq = living.getDistanceSq(getPos().getX() + 0.5, getPos().getY() + 0.5,
                            getPos().getZ() + 0.5);
                    dSq = Math.max(dSq, 1);
                    int maxEnergy = getMaxEnergy(level, spAtk, atk, poke.getPokedexEntry());
                    int pokeEnergy = maxEnergy;
                    int dE;
                    long energyTime = worldObj.getTotalWorldTime();
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

    public int getMaxEnergy(int level, int spAtk, int atk, PokedexEntry entry)
    {
        return getEnergyGain(level, spAtk, atk, entry);
    }

    @Override
    public int getMaxEnergyStored(EnumFacing facing)
    {
        return PokecubeAdv.conf.maxOutput;
    }

//    @Callback
//    @Optional.Method(modid = "OpenComputers")
//    public Object[] getPower(Context context, Arguments args)
//    {
//        return new Object[] { lastInput };
//    }

    private void initParser()
    {
        parser.initFunTab(); // clear the contents of the function table
        parser.addStandardFunctions();
        parser.initSymTab(); // clear the contents of the symbol table
        parser.addStandardConstants();
        parser.addComplex(); // among other things adds i to the symbol
                             // table
        parser.addVariable("x", 0);
        parser.addVariable("a", 0);
        parser.parseExpression(PokecubeAdv.conf.powerFunction);
    }

    @Override
    public void invalidate()
    {
        super.invalidate();
    }

    @Override
    public void onChunkUnload()
    {
        super.onChunkUnload();
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound)
    {
        super.readFromNBT(tagCompound);
    }

    @Override
    public void update()
    {
        Vector3 v = Vector3.getNewVector().set(this);
        if (box == null)
        {
            box = v.getAABB().expand(10, 10, 10);
        }
        lastInput = getInput();
        Map<IEnergyReceiver, Integer> tiles = Maps.newHashMap();
        Map<IEnergyReceiver, EnumFacing> sides = Maps.newHashMap();
        for (EnumFacing side : EnumFacing.values())
        {
            TileEntity te = v.getTileEntity(worldObj, side);
            if (te != null && te instanceof IEnergyReceiver)
            {
                IEnergyReceiver h = (IEnergyReceiver) te;
                int toSend = h.receiveEnergy(side.getOpposite(), lastInput, true);
                if (toSend > 0)
                {
                    tiles.put(h, toSend);
                }
            }
        }
        for (Map.Entry<IEnergyReceiver, Integer> entry : tiles.entrySet())
        {
            int fraction = lastInput / tiles.size();
            int request = entry.getValue();
            if (request > fraction)
            {
                request = fraction;
            }
            if (fraction == 0 || lastInput <= 0) continue;
            IEnergyReceiver h = entry.getKey();
            lastInput -= request;
            h.receiveEnergy(sides.get(h), request, false);
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound)
    {
        return super.writeToNBT(tagCompound);
    }

}
