package pokecube.adventures.blocks.siphon;

import org.nfunk.jep.JEP;

import cofh.api.energy.IEnergyProvider;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.SimpleComponent;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.Optional.Interface;
import net.minecraftforge.fml.common.Optional.InterfaceList;
import pokecube.adventures.PokecubeAdv;
import pokecube.core.database.PokedexEntry;

@InterfaceList({ @Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "OpenComputers"),
        @Interface(iface = "cofh.api.energy.IEnergyProvider", modid = "CoFHAPI") })
public class TileEntitySiphon extends TileEntity implements ITickable, IEnergyProvider, SimpleComponent
{
    AxisAlignedBB     box;
    public static JEP parser;
    public int        currentOutput     = 0;
    public int        theoreticalOutput = 0;

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
        int ret = currentOutput;
        if (ret > maxExtract) ret = maxExtract;
        return ret;
    }

    @Override
    public String getComponentName()
    {
        return "pokesiphon";
    }

    public static int getEnergyGain(int level, int spAtk, int atk, PokedexEntry entry)
    {
        int power = Math.max(atk, spAtk);
        if (parser == null)
        {
            initParser();
        }
        parser.setVarValue("x", level);
        parser.setVarValue("a", power);
        double value = parser.getValue();
        if (Double.isNaN(value))
        {
            initParser();
            parser.setVarValue("x", level);
            parser.setVarValue("a", power);
            value = parser.getValue();
            System.err.println(atk + " " + spAtk + " " + value);
            if (Double.isNaN(value))
            {
                value = 0;
            }
        }
        power = (int) value;
        return Math.max(1, power);
    }

    @Override
    public int getEnergyStored(EnumFacing facing)
    {
        return 0;
    }

    public static int getMaxEnergy(int level, int spAtk, int atk, PokedexEntry entry)
    {
        return getEnergyGain(level, spAtk, atk, entry);
    }

    @Override
    public int getMaxEnergyStored(EnumFacing facing)
    {
        return PokecubeAdv.conf.maxOutput;
    }

    @Callback
    @Optional.Method(modid = "OpenComputers")
    public Object[] getPower(Context context, Arguments args)
    {
        return new Object[] { currentOutput };
    }

    private static void initParser()
    {
        parser = new JEP();
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
    public void update()
    {
        if (!worldObj.isRemote) MinecraftForge.EVENT_BUS.post(new SiphonTickEvent(this));
    }

}
