package pokecube.compat.ai;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.minecraft.entity.EntityLiving;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pokecube.core.events.RecallEvent;
import pokecube.core.events.SpawnEvent;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import thut.api.maths.Vector3;

public class ELNInterfacer
{
    static class CablePacket
    {
        Object  resistor;
        Object  voltageSource;
        Object  loadA;
        Object  loadB;
        Object  cable;
        Object  cableLoad;
        Vector3 location;

        public CablePacket(Vector3 cableLoc, Object resist, Object source, Object a, Object b, Object cab,
                Object cabLoad)
        {
            resistor = resist;
            voltageSource = source;
            loadA = a;
            loadB = b;
            cable = cab;
            cableLoad = cabLoad;
            location = cableLoc;
        }

        public double getDistance(EntityLiving entity)
        {
            return location.distToEntity(entity);
        }

        public void resetCable() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
        {
            disconnect(cable);
            breakConnection(resistor);
            breakConnection(voltageSource);
            getCableComponents(cable).clear();
            List<Object> toRemove = new ArrayList<Object>();
            List<Object> cableLoads = getCableLoads(cable);
            for (Object cableload : cableLoads)
            {
                if (cableload != cableLoad) toRemove.add(cableload);
            }
            cableLoads.removeAll(toRemove);
            connect(cable);
        }
    }

    static Class<?>                                      coordonate;
    static Class<?>                                      line;
    static Class<?>                                      state;
    static Class<?>                                      resistor;
    static Class<?>                                      voltageSource;
    static Class<?>                                      nodeManager;
    static Class<?>                                      rootSystem;
    static Method                                        resistorSetR;
    static Field                                         nodeManagerInstance;
    static Method                                        nodeManagerGetNodeFromCoordonate;
    static Class<?>                                      directions;
    static Method                                        dirValues;
    static Class<?>                                      lrdu;
    static Method                                        getElectricalLoad;
    static Class<?>                                      electricalLoad;
    static Class<?>                                      nbtElectricalLoad;
    static Method                                        loadSetRs;
    static Method                                        sourceSetU;
    static Method                                        getSubSystem;
    static Class<?>                                      subsystem;

    static Method                                        getRoot;
    static Class<?>                                      sixNode;
    static Class<?>                                      sixNodeElement;

    static Field                                         sixNodeSEL;
    static Class<?>                                      cable;
    static Field                                         cableCompList;
    static Field                                         cableLoadList;
    static Field                                         cableLoad;
    static Method                                        reconnect;
    static Method                                        disconnect;

    static Method                                        connect;
    static Class<?>                                      bipole;
    static Method                                        breakConnection;

    static Method                                        connectTo;

    static HashMap<EntityLiving, ArrayList<CablePacket>> mobEffects = new HashMap<EntityLiving, ArrayList<CablePacket>>();

    static void addCableEffect(EntityLiving entity, CablePacket cable)
    {
        ArrayList<CablePacket> effects = mobEffects.get(entity);
        if (effects == null)
        {
            effects = new ArrayList<ELNInterfacer.CablePacket>();
            mobEffects.put(entity, effects);
        }
        for (CablePacket p : effects)
        {
            if (p.location.sameBlock(cable.location))
            {
                try
                {
                    p.resetCable();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                effects.remove(p);
                break;
            }
        }
        effects.add(cable);
    }

    static void breakConnection(Object component)
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        breakConnection.invoke(component);
    }

    static void connect(Object cable) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        connect.invoke(cable);
    }

    static void connectResistorToLoadAndSource(Object resistor, Object sourcePinA, Object load)
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        connectTo.invoke(resistor, sourcePinA, load);
    }

    static void disconnect(Object cable)
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        disconnect.invoke(cable);
    }

    public static void doELNInterference(EntityLiving entity, int currentRadius, int statFactor, TileEntity tile)
            throws Exception
    {
        currentRadius = Math.max(1, currentRadius);
        double scaling = 0.005;
        double factor = scaling * statFactor * 1d / (currentRadius * currentRadius);
        try
        {
            Object loadA = makeLoad("aLoad");
            Object loadB = makeLoad("bLoad");
            Object resistor = makeResistor(100000, loadA, loadB);
            Object voltageSource = makeSource(factor, loadA);

            Object coord = coordonate.getConstructor(TileEntity.class).newInstance(tile);
            Object lrduDown = lrdu.getEnumConstants()[2];
            Object node;
            node = nodeManagerGetNodeFromCoordonate.invoke(nodeManagerInstance.get(nodeManager), coord);

            if (node == null || !sixNode.isInstance(node)) return;

            for (Object dir : (Object[]) dirValues.invoke(directions))
            {
                Object load = getElectricalLoad.invoke(node, dir, lrduDown);
                if (load != null)
                {
                    Object cable = getCable(node);
                    List<Object> cableElectricalComps = getCableComponents(cable);
                    List<Object> cableLoads = getCableLoads(cable);
                    Object cableLoad = getCableLoad(cable);
                    Vector3 cableLoc = Vector3.getNewVector().set(tile);
                    CablePacket pack = new CablePacket(cableLoc, resistor, voltageSource, loadA, loadB, cable,
                            cableLoad);
                    addCableEffect(entity, pack);
                    if (cableElectricalComps.isEmpty())
                    {
                        connectResistorToLoadAndSource(resistor, loadA, load);
                        cableElectricalComps.add(resistor);
                        cableElectricalComps.add(voltageSource);
                        cableLoads.add(loadA);
                        cableLoads.add(loadB);
                        reconnect(cable);
                    }
                    else if (cableElectricalComps.size() == 2)
                    {
                        Object resistor1 = cableElectricalComps.get(0);
                        Object voltageSource1 = cableElectricalComps.get(1);
                        disconnect(cable);
                        breakConnection(resistor1);
                        breakConnection(voltageSource1);
                        cableElectricalComps.clear();
                        List<Object> toRemove = new ArrayList<Object>();
                        for (Object cableload : cableLoads)
                        {
                            if (cableload != cableLoad) toRemove.add(cableload);
                        }
                        cableLoads.removeAll(toRemove);
                        connect(cable);

                        connectResistorToLoadAndSource(resistor, loadA, load);
                        cableElectricalComps.add(resistor);
                        cableElectricalComps.add(voltageSource);
                        cableLoads.add(loadA);
                        cableLoads.add(loadB);
                        reconnect(cable);
                    }
                    break;
                }

            }
        }
        catch (Exception e)
        {
            // e.printStackTrace();
        }
    }

    static Object getCable(Object snode) throws IllegalArgumentException, IllegalAccessException
    {
        Object[] object = (Object[]) sixNodeSEL.get(snode);

        if (!cable.isInstance(object[2])) throw new IllegalArgumentException();

        return object[2];
    }

    @SuppressWarnings("unchecked")
    static List<Object> getCableComponents(Object cable) throws IllegalArgumentException, IllegalAccessException
    {
        return (List<Object>) cableCompList.get(cable);
    }

    static Object getCableLoad(Object cable) throws IllegalArgumentException, IllegalAccessException
    {
        return cableLoad.get(cable);
    }

    @SuppressWarnings("unchecked")
    static List<Object> getCableLoads(Object cable) throws IllegalArgumentException, IllegalAccessException
    {
        return (List<Object>) cableLoadList.get(cable);
    }

    static Object makeLoad(String name) throws InstantiationException, IllegalAccessException, IllegalArgumentException,
            InvocationTargetException, NoSuchMethodException, SecurityException
    {
        Object load = nbtElectricalLoad.getConstructor(String.class).newInstance(name);
        loadSetRs.invoke(load, 1e-9);
        return load;
    }

    static Object makeResistor(double resistance, Object loadA, Object loadB)
            throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
            NoSuchMethodException, SecurityException
    {
        Object res = resistor.getConstructor(state, state).newInstance(loadA, loadB);
        resistorSetR.invoke(res, resistance);
        return res;
    }

    static Object makeSource(double voltage, Object loadA) throws InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException
    {
        Object source = voltageSource.getConstructor(String.class, state, state).newInstance("", loadA, null);
        sourceSetU.invoke(source, voltage);
        return source;
    }

    static void reconnect(Object cable)
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        reconnect.invoke(cable);
    }

    static void refreshCableEffects(EntityLiving entity)
    {
        ArrayList<CablePacket> effects = mobEffects.get(entity);
        if (effects != null)
        {
            ArrayList<CablePacket> toRemove = new ArrayList<ELNInterfacer.CablePacket>();
            for (CablePacket p : effects)
            {
                if (p.getDistance(entity) > 8)
                {
                    toRemove.add(p);
                }
            }
            for (CablePacket p : toRemove)
            {
                try
                {
                    p.resetCable();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                effects.remove(p);
            }
        }
    }

    static void resetCableEffects(EntityLiving entity)
    {
        ArrayList<CablePacket> effects = mobEffects.get(entity);
        if (effects != null)
        {
            for (CablePacket p : effects)
            {
                try
                {
                    p.resetCable();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }

    }

    public ELNInterfacer()
    {
        try
        {
            coordonate = Class.forName("mods.eln.misc.Coordonate");
            state = Class.forName("mods.eln.sim.mna.state.State");
            line = Class.forName("mods.eln.sim.mna.component.Line");
            resistor = Class.forName("mods.eln.sim.mna.component.Resistor");
            voltageSource = Class.forName("mods.eln.sim.mna.component.VoltageSource");
            nodeManager = Class.forName("mods.eln.node.NodeManager");
            rootSystem = Class.forName("mods.eln.sim.mna.RootSystem");
            resistorSetR = resistor.getMethod("setR", double.class);
            nodeManagerInstance = nodeManager.getDeclaredField("instance");
            nodeManagerGetNodeFromCoordonate = nodeManager.getDeclaredMethod("getNodeFromCoordonate", coordonate);
            directions = Class.forName("mods.eln.misc.Direction");
            dirValues = directions.getMethod("values");
            lrdu = Class.forName("mods.eln.misc.LRDU");
            getElectricalLoad = Class.forName("mods.eln.node.NodeBase").getMethod("getElectricalLoad", directions,
                    lrdu);
            electricalLoad = Class.forName("mods.eln.sim.ElectricalLoad");
            nbtElectricalLoad = Class.forName("mods.eln.sim.nbt.NbtElectricalLoad");
            loadSetRs = electricalLoad.getMethod("setRs", double.class);
            sourceSetU = voltageSource.getMethod("setU", double.class);
            getSubSystem = electricalLoad.getMethod("getSubSystem");
            subsystem = Class.forName("mods.eln.sim.mna.SubSystem");
            getRoot = subsystem.getMethod("getRoot");
            sixNode = Class.forName("mods.eln.node.six.SixNode");
            sixNodeSEL = sixNode.getDeclaredField("sideElementList");
            sixNodeElement = Class.forName("mods.eln.node.six.SixNodeElement");
            cable = Class.forName("mods.eln.sixnode.electricalcable.ElectricalCableElement");
            cableLoad = cable.getDeclaredField("electricalLoad");
            cableCompList = sixNodeElement.getDeclaredField("electricalComponentList");
            cableLoadList = sixNodeElement.getDeclaredField("electricalLoadList");

            reconnect = sixNodeElement.getDeclaredMethod("reconnect");
            disconnect = sixNodeElement.getDeclaredMethod("disconnectJob");
            connect = sixNodeElement.getDeclaredMethod("connectJob");

            bipole = Class.forName("mods.eln.sim.mna.component.Bipole");
            breakConnection = bipole.getMethod("breakConnection");
            connectTo = bipole.getMethod("connectTo", state, state);

            MinecraftForge.EVENT_BUS.register(this);
        }
        catch (Exception e)
        {
            // e.printStackTrace();
        }
    }

    @SubscribeEvent
    public void EntityLivingDeath(LivingDeathEvent evt)
    {
        IPokemob pokemob = CapabilityPokemob.getPokemobFor(evt.getEntityLiving());
        if (pokemob != null && evt.getEntityLiving() instanceof EntityLiving)
        {
            resetCableEffects((EntityLiving) evt.getEntityLiving());
        }
    }

    @SubscribeEvent
    public void EntityLivingUpdate(LivingUpdateEvent evt)
    {
        IPokemob pokemob = CapabilityPokemob.getPokemobFor(evt.getEntityLiving());
        if (pokemob != null && evt.getEntityLiving() instanceof EntityLiving)
        {
            refreshCableEffects((EntityLiving) evt.getEntityLiving());
        }
    }

    @SubscribeEvent
    public void PokemobDespawn(SpawnEvent.Despawn evt)
    {
        resetCableEffects((EntityLiving) evt.pokemob);
    }

    @SubscribeEvent
    public void PokemobRecall(RecallEvent evt)
    {
        resetCableEffects((EntityLiving) evt.recalled);
    }
}
