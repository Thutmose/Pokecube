package pokecube.compat.blocks.rf;

import java.util.List;

import cofh.api.energy.EnergyStorage;
import cofh.api.energy.IEnergyProvider;
import cofh.api.energy.IEnergyReceiver;
import li.cil.oc.api.Network;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.Environment;
import li.cil.oc.api.network.Message;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.network.Visibility;
import net.minecraft.entity.EntityLiving;
import net.minecraft.nbt.NBTTagCompound;
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

public class TileEntitySiphon extends TileEntity implements ITickable, Environment, IEnergyProvider
{
    AxisAlignedBB           box;
    public static int       maxOutput = 256;
    int                     lastInput = 0;
    protected EnergyStorage storage   = new EnergyStorage(maxOutput);

    public TileEntitySiphon()
    {
        try
        {
            node = Network.newNode(this, Visibility.Network).withConnector()
                    .withComponent("pokesiphon", Visibility.Network).create();
        }
        catch (Exception e)
        {
             e.printStackTrace();
        }
    }

    public TileEntitySiphon(World world)
    {
        this();
    }

    @Override
    public void update()
    {
        if (!addedToNetwork)
        {
            addedToNetwork = true;
            Network.joinOrCreateNetwork(this);
        }
        Vector3 v = Vector3.getNewVectorFromPool().set(this);
        if (box == null)
        {
            box = v.getAABB().expand(5, 5, 5);
        }
        lastInput = getInput();
        storage.setEnergyStored(lastInput);

        for (EnumFacing side : EnumFacing.values())
        {
            TileEntity te = v.getTileEntity(worldObj, side);
            if (te != null && te instanceof IEnergyReceiver)
            {
                IEnergyReceiver h = (IEnergyReceiver) te;
                int toSend = h.receiveEnergy(side.getOpposite(), storage.getEnergyStored(), true);
                storage.extractEnergy(toSend, false);
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
                EntityLiving living = (EntityLiving) o;
                if (poke.isType(PokeType.electric))
                {
                    int spAtk = poke.getBaseStats()[3];
                    int atk = poke.getBaseStats()[1];
                    int level = poke.getLevel();
                    double dSq = living.getDistanceSq(getPos().getX() + 0.5, getPos().getY() + 0.5,
                            getPos().getZ() + 0.5) * 1024;
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

        return Math.min(ret, maxOutput);
    }

    public int getEnergyGain(int level, int spAtk, int atk, PokedexEntry entry)
    {
        int power = Math.max(atk, spAtk) * level;
        return power;
    }

    public int getMaxEnergy(int level, int spAtk, int atk, PokedexEntry entry)
    {
        return 10 * getEnergyGain(level, spAtk, atk, entry);
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound)
    {
        super.writeToNBT(tagCompound);
        if (node != null && node.host() == this)
        {
            final NBTTagCompound nodeNbt = new NBTTagCompound();
            node.save(nodeNbt);
            tagCompound.setTag("oc:node", nodeNbt);
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound)
    {
        super.readFromNBT(tagCompound);
        if (node != null && node.host() == this)
        {
            // This restores the node's address, which is required for networks
            // to continue working without interruption across loads. If the
            // node is a power connector this is also required to restore the
            // internal energy buffer of the node.
            node.load(tagCompound.getCompoundTag("oc:node"));
        }
    }

    protected Node node;

    // See updateEntity().
    protected boolean addedToNetwork = false;

    // -----------------------------------------------------------------------
    // //

    @Override
    public Node node()
    {
        return node;
    }

    @Override
    public void onConnect(final Node node)
    {
        // This is called when the call to Network.joinOrCreateNetwork(this) in
        // updateEntity was successful, in which case `node == this`.
        // This is also called for any other node that gets connected to the
        // network our node is in, in which case `node` is the added node.
        // If our node is added to an existing network, this is called for each
        // node already in said network.
    }

    @Override
    public void onDisconnect(final Node node)
    {
        // This is called when this node is removed from its network when the
        // tile entity is removed from the world (see onChunkUnload() and
        // invalidate()), in which case `node == this`.
        // This is also called for each other node that gets removed from the
        // network our node is in, in which case `node` is the removed node.
        // If a net-split occurs this is called for each node that is no longer
        // connected to our node.
    }

    @Override
    public void onMessage(final Message message)
    {
        // This is used to deliver messages sent via node.sendToXYZ. Handle
        // messages at your own discretion. If you do not wish to handle a
        // message you should *not* throw an exception, though.
    }

    @Override
    public void onChunkUnload()
    {
        super.onChunkUnload();
        // Make sure to remove the node from its network when its environment,
        // meaning this tile entity, gets unloaded.
        if (node != null) node.remove();
    }

    @Override
    public void invalidate()
    {
        super.invalidate();
        // Make sure to remove the node from its network when its environment,
        // meaning this tile entity, gets unloaded.
        if (node != null) node.remove();
    }

    @Callback
    public Object[] getEnergy(Context context, Arguments args)
    {
        return new Object[] { storage.getEnergyStored() };
    }

    @Callback
    public Object[] getPower(Context context, Arguments args)
    {
        return new Object[] { lastInput };
    }

    @Override
    public boolean canConnectEnergy(EnumFacing facing)
    {
        return true;
    }

    @Override
    public int extractEnergy(EnumFacing facing, int maxExtract, boolean simulate)
    {
        return storage.extractEnergy(maxExtract, simulate);
    }

    @Override
    public int getEnergyStored(EnumFacing facing)
    {
        return storage.getEnergyStored();
    }

    @Override
    public int getMaxEnergyStored(EnumFacing facing)
    {
        return storage.getMaxEnergyStored();
    }

}
