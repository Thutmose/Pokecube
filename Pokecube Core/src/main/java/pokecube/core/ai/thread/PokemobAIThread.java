package pokecube.core.ai.thread;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.core.Mod_Pokecube_Helper;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.utils.PokecubeSerializer;
import pokecube.core.utils.Tools;
import thut.api.TickHandler;
import thut.api.maths.Vector3;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class PokemobAIThread
{
    private static final BitSet                          tickLock          = new BitSet();
    private static Vector<Object>[]                      aiStuffLists;
    public static final HashMap<Integer, Vector<Object>> worldPlayers      = new HashMap<Integer, Vector<Object>>();
    public static final Comparator<IAIRunnable>          aiComparator      = new Comparator<IAIRunnable>()
    {
        @Override
        public int compare(IAIRunnable o1, IAIRunnable o2)
        {
            return o1.getPriority() - o2.getPriority();
        }
    };
    public static final Comparator<IPokemob>             pokemobComparator = new Comparator<IPokemob>()
    {
        @Override
        public int compare(IPokemob o1, IPokemob o2)
        {
            int speed1 = Tools.getStat(o1.getBaseStats()[5], o1.getIVs()[5], o1.getEVs()[5], o1.getLevel(),
                    o1.getModifiers()[5], o1.getNature());
            int speed2 = Tools.getStat(o2.getBaseStats()[5], o2.getIVs()[5], o2.getEVs()[5], o2.getLevel(),
                    o2.getModifiers()[5], o2.getNature());
            // TODO include checks for mob's selected attack and include attack
            // priority.
            return speed2 - speed1;
        }
    };

    public static void addAI(EntityLiving entity, IAIRunnable task)
    {
        IPokemob pokemob = (IPokemob) entity;
        int id = pokemob.getPokemonUID() % AIThread.threadCount;
        Vector<Object> list = aiStuffLists[id];
        ArrayList toCheck = new ArrayList(list);
        AIStuff entityAI = null;
        for (Object o : toCheck)
        {
            AIStuff aistuff = (AIStuff) o;
            if (aistuff.entity == entity)
            {
                entityAI = aistuff;
                break;
            }
        }
        if (entityAI == null)
        {
            entityAI = new AIStuff(entity);
            list.add(entityAI);
        }
        entityAI.addAITask(task);

    }

    public static void addLogic(EntityLiving entity, ILogicRunnable logic)
    {
        IPokemob pokemob = (IPokemob) entity;
        int id = pokemob.getPokemonUID() % AIThread.threadCount;
        Vector list = aiStuffLists[id];
        ArrayList toCheck = new ArrayList(list);
        AIStuff entityAI = null;
        for (Object o : toCheck)
        {
            AIStuff aistuff = (AIStuff) o;
            if (aistuff.entity == entity)
            {
                entityAI = aistuff;
                break;
            }
        }
        if (entityAI == null)
        {
            entityAI = new AIStuff(entity);
            list.add(entityAI);
        }
        entityAI.addAILogic(logic);
    }

    public static void removeEntity(EntityLiving entity)
    {
        int id = entity.getEntityId() % AIThread.threadCount;
        Vector list = aiStuffLists[id];
        ArrayList toCheck = new ArrayList(list);
        for (Object o : toCheck)
        {
            AIStuff aistuff = (AIStuff) o;
            if (aistuff.entity == entity)
            {
                list.remove(aistuff);
                break;
            }
        }
    }

    public static void clear()
    {
        for (Vector v : aiStuffLists)
        {
            v.clear();
        }
        worldPlayers.clear();
        TickHandler.getInstance().worldCaches.clear();
        tickLock.clear();
    }

    private static boolean canRun(IAIRunnable task, ArrayList<IAIRunnable> tasks)
    {
        int prior = task.getPriority();
        int mutex = task.getMutex();
        for (IAIRunnable ai : tasks)
        {
            if (ai.getPriority() < prior && (mutex & ai.getMutex()) != 0 && ai.shouldRun()) { return false; }
        }
        return task.shouldRun();
    }

    public static void addEntityPath(int id, int dim, PathEntity path, double speed)
    {
        paths.add(new PathInfo(id, dim, path, speed));
    }

    public static void addTargetInfo(int attacker, int target, int dim)
    {
        targets.add(new TargetInfo(attacker, target, dim));
    }

    public static void addMoveInfo(int attacker, int targetEnt, int dim, Vector3 target, float distance)
    {
        moves.add(new MoveInfo(attacker, targetEnt, dim, target, distance));
    }

    public static void addStateInfo(int uid, int state, boolean value)
    {
        states.add(new StateInfo(uid, state, value));
    }

    private static final Vector<PathInfo>   paths   = new Vector();
    private static final Vector<TargetInfo> targets = new Vector();
    private static final Vector<MoveInfo>   moves   = new Vector();
    private static final Vector<StateInfo>  states  = new Vector();

    static
    {
        AIThread.createThreads();
    }

    @SubscribeEvent
    public void tickEventServer(ServerTickEvent evt)
    {
        if (evt.phase == Phase.END)
        {
            synchronized (tickLock)
            {
                for (int i = 0; i < AIThread.threadCount; i++)
                {
                    tickLock.set(i);
                }
            }
        }
    }

    @SubscribeEvent
    public void tickEvent(WorldTickEvent evt)
    {
        if (evt.phase == Phase.START)
        {
            Vector players = worldPlayers.get(evt.world.provider.getDimensionId());
            if (players == null)
            {
                players = new Vector();
            }
            players.clear();
            players.addAll(evt.world.playerEntities);
            worldPlayers.put(evt.world.provider.getDimensionId(), players);
        }
        else try
        {
            ArrayList todo = new ArrayList();

            List unloadedMobs = evt.world.unloadedEntityList;

            todo.addAll(paths);
            for (Object o : todo)
            {
                PathInfo p = (PathInfo) o;
                if (p.dim != evt.world.provider.getDimensionId()) continue;

                Entity e = evt.world.getEntityByID(p.pather);
                if (e == null || !(e instanceof EntityLiving))
                {
                    paths.remove(o);
                    continue;
                }
                EntityLiving mob = (EntityLiving) e;
                if (!mob.worldObj.loadedEntityList.contains(mob) || unloadedMobs.contains(mob))
                {
                    paths.remove(o);
                    continue;
                }

                if (!(mob.isDead || mob.getHealth() <= 0))
                {
                    p.setPath(mob);
                }
                paths.remove(o);
            }
            todo.clear();
            todo.addAll(moves);
            Collections.sort(todo, MoveInfo.compare);
            for (Object o : todo)
            {
                MoveInfo m = (MoveInfo) o;
                if (m.dim != evt.world.provider.getDimensionId()) continue;
                Entity e = evt.world.getEntityByID(m.attacker);
                if (e == null || !(e instanceof EntityLiving))
                {
                    moves.remove(m);
                    continue;
                }
                EntityLiving mob = (EntityLiving) e;
                if (!mob.worldObj.loadedEntityList.contains(mob) || unloadedMobs.contains(mob))
                {
                    moves.remove(m);
                    continue;
                }
                if (!(mob.isDead || mob.getHealth() <= 0))
                {
                    ((IPokemob) mob).executeMove(evt.world.getEntityByID(m.targetEnt), m.target, m.distance);

                }
                moves.remove(m);
            }
            todo.clear();
            todo.addAll(targets);
            for (Object o : todo)
            {
                TargetInfo t = (TargetInfo) o;
                if (t.dim != evt.world.provider.getDimensionId()) continue;
                Entity e = evt.world.getEntityByID(t.attacker);
                Entity e1 = evt.world.getEntityByID(t.target);
                if (e == null || !(e instanceof EntityLiving))
                {
                    targets.remove(t);
                    continue;
                }
                if (!(e1 instanceof EntityLivingBase))
                {
                    e1 = null;
                }
                EntityLiving mob = (EntityLiving) e;

                boolean mobExists = !(unloadedMobs.contains(t.attacker));

                if (!mobExists
                        || (!(e1 instanceof EntityPlayer) && ((e1 != null && !e1.worldObj.loadedEntityList.contains(e1))
                                || (e1 != null && unloadedMobs.contains(e1)))))
                {
                    targets.remove(t);
                    continue;
                }
                if (!(mob.isDead || mob.getHealth() <= 0))
                {
                    mob.setAttackTarget((EntityLivingBase) e1);
                }
                targets.remove(t);
            }
            todo.clear();
            todo.addAll(states);
            for (Object o : todo)
            {
                StateInfo info = (StateInfo) o;
                info.applyState();
                states.remove(o);
            }
            todo.clear();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    static class PathInfo
    {
        public final PathEntity path;
        public final int        pather;
        public final int        dim;
        public final double     speed;

        public PathInfo(int _pather, int dimension, PathEntity _path, double _speed)
        {
            path = _path;
            pather = _pather;
            speed = _speed;
            dim = dimension;
        }

        public void setPath(EntityLiving entity)
        {
            entity.getNavigator().setPath(path, speed);
        }
    }

    static class TargetInfo
    {
        public final int attacker;
        public final int target;
        public final int dim;

        public TargetInfo(int _attacker, int _target, int dimension)
        {
            attacker = _attacker;
            target = _target;
            dim = dimension;
        }
    }

    static class StateInfo
    {
        public final int     pokemobUid;
        public final int     state;
        public final boolean value;

        public StateInfo(int uid, int state_, boolean value_)
        {
            pokemobUid = uid;
            state = state_;
            value = value_;
        }

        public boolean applyState()
        {
            IPokemob pokemob = PokecubeSerializer.getInstance().getPokemob(pokemobUid);
            if (pokemob == null) return false;
            pokemob.setPokemonAIState(state, value);
            return true;
        }
    }

    static class MoveInfo
    {
        public static final Comparator compare = new Comparator<MoveInfo>()
        {
            @Override
            public int compare(MoveInfo o1, MoveInfo o2)
            {
                if (o1.dim != o2.dim) return 0;
                if (FMLCommonHandler.instance().getSide() == Side.SERVER)
                {
                    WorldServer world = FMLCommonHandler.instance().getMinecraftServerInstance()
                            .worldServerForDimension(o1.dim);
                    Entity e1 = world.getEntityByID(o1.attacker);
                    Entity e2 = world.getEntityByID(o2.attacker);
                    if (e1 instanceof IPokemob && e2 instanceof IPokemob) { return pokemobComparator.compare((IPokemob)e1, (IPokemob)e2); }
                }
                return 0;
            }
        };

        public final int     attacker;
        public final int     targetEnt;
        public final int     dim;
        public final Vector3 target;
        public final float   distance;

        public MoveInfo(int _attacker, int _targetEnt, int dimension, Vector3 _target, float _distance)
        {
            attacker = _attacker;
            targetEnt = _targetEnt;
            target = _target;
            distance = _distance;
            dim = dimension;
        }
    }

    static class AIStuff
    {
        public final EntityLiving       entity;
        final ArrayList<IAIRunnable>    aiTasks = new ArrayList<IAIRunnable>();
        final ArrayList<ILogicRunnable> aiLogic = new ArrayList<ILogicRunnable>();

        public AIStuff(EntityLiving entity_)
        {
            entity = entity_;
        }

        public void addAITask(IAIRunnable task)
        {
            aiTasks.add(task);
        }

        public void addAILogic(ILogicRunnable logic)
        {
            aiLogic.add(logic);
        }
    }

    public static class AIThread extends Thread
    {
        public static int     threadCount = 0;
        final int             id;
        public static boolean setAccess   = false;
        public static Field   unloadedField;

        public AIThread(final int number)
        {
            super(new Runnable()
            {
                @Override
                public void run()
                {
                    int id;
                    Thread thread = Thread.currentThread();
                    if (!(thread instanceof AIThread))
                    {
                        new ClassCastException("wrong thread type").printStackTrace();
                        return;
                    }
                    id = number;
                    System.out.println("This is Thread " + id);
                    while (true)
                    {
                        boolean tick = false;
                        synchronized (tickLock)
                        {
                            tick = tickLock.get(id);
                            if (tick && unloadedField == null)
                            {
                                try
                                {
                                    unloadedField = World.class.getDeclaredFields()[4];
                                    unloadedField.setAccessible(true);
                                }
                                catch (Exception e)
                                {
                                    e.printStackTrace();
                                    continue;
                                }
                            }
                        }
                        if (tick && unloadedField != null)
                        {
                            System.currentTimeMillis();
                            ArrayList toRemove = new ArrayList();
                            Vector entities = aiStuffLists[id];
                            ArrayList temp = new ArrayList(entities);
                            for (Object o : temp)
                            {
                                ArrayList unloadedMobs;
                                AIStuff stuff = (AIStuff) o;
                                EntityLiving b = stuff.entity;
                                try
                                {
                                    unloadedMobs = new ArrayList((Collection) unloadedField.get(b.worldObj));
                                }
                                catch (Exception e1)
                                {
                                    e1.printStackTrace();
                                    unloadedMobs = new ArrayList();
                                }
                                ArrayList loadedMobs = new ArrayList(b.worldObj.loadedEntityList);
                                if (b.isDead || (unloadedMobs.contains(b)) || (!loadedMobs.contains(b)))
                                {
                                    toRemove.add(b);
                                    continue;
                                }
                                else
                                {
                                    boolean loaded = b.worldObj.isAreaLoaded(
                                            new BlockPos(MathHelper.floor_double(b.posX),
                                                    MathHelper.floor_double(b.posY), MathHelper.floor_double(b.posZ)),
                                            32);
                                    if (!loaded) continue;

                                    if (b instanceof IPokemob)
                                    {
                                        IPokemob pokemob = (IPokemob) b;
                                        int uid = pokemob.getPokemonUID();
                                        if (uid % threadCount != id) continue;
                                    }

                                    ArrayList list = stuff.aiTasks;
                                    if (list != null) for (IAIRunnable ai : (ArrayList<IAIRunnable>) list)
                                    {
                                        try
                                        {
                                            if (canRun(ai, list))
                                            {
                                                ai.run();
                                            }
                                            else
                                            {
                                                ai.reset();
                                            }
                                        }
                                        catch (Exception e)
                                        {
                                            e.printStackTrace();
                                        }
                                    }
                                    list = stuff.aiLogic;
                                    if (list != null) for (ILogicRunnable runnable : (ArrayList<ILogicRunnable>) list)
                                    {
                                        try
                                        {
                                            runnable.doLogic();
                                        }
                                        catch (Exception e)
                                        {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                            for (Object o : toRemove)
                            {
                                removeEntity((EntityLiving) o);
                            }
                            synchronized (tickLock)
                            {
                                tickLock.set(id, false);
                            }
                        }
                        else
                        {
                            try
                            {
                                Thread.sleep(1);
                            }
                            catch (InterruptedException e)
                            {
                                // e.printStackTrace();
                            }
                        }
                    }
                }
            });
            id = number;
            this.setName("Pokemob AI Thread-" + id);
        }

        public static void createThreads()
        {
            threadCount = Math.max(1, Mod_Pokecube_Helper.maxAIThreads);
            threadCount = Math.min(threadCount, Runtime.getRuntime().availableProcessors());
            aiStuffLists = new Vector[threadCount];
            System.out.println("Creating and starting Pokemob AI Threads.");
            for (int i = 0; i < threadCount; i++)
            {
                AIThread thread = new AIThread(i);
                aiStuffLists[i] = new Vector();
                thread.setPriority(8);
                thread.start();
            }
        }

    }
}
