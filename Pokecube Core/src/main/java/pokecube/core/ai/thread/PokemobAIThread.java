package pokecube.core.ai.thread;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import net.minecraft.entity.EntityLiving;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.utils.Tools;
import thut.api.TickHandler;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class PokemobAIThread
{
    /** A collecion of AITasks and AILogics for the entity to run on seperate
     * threads.
     * 
     * @author Thutmose */
    static class AIStuff
    {
        public final EntityLiving       entity;
        final ArrayList<IAIRunnable>    aiTasks = new ArrayList<IAIRunnable>();
        final ArrayList<ILogicRunnable> aiLogic = new ArrayList<ILogicRunnable>();

        public AIStuff(EntityLiving entity_)
        {
            entity = entity_;
        }

        public void addAILogic(ILogicRunnable logic)
        {
            aiLogic.add(logic);
        }

        public void addAITask(IAIRunnable task)
        {
            aiTasks.add(task);
        }

        public void runServerThreadTasks(World world)
        {
            for (IAIRunnable ai : aiTasks)
            {
                ai.doMainThreadTick(world);
            }
        }
    }
    public static class AIThread extends Thread
    {
        public static int threadCount = 0;
        public static void createThreads()
        {
            threadCount = Math.max(1, PokecubeMod.core.getConfig().maxAIThreads);
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

        final int         id;

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
                        }
                        if (tick)
                        {
                            System.currentTimeMillis();
                            ArrayList toRemove = new ArrayList();
                            Vector entities = aiStuffLists[id];
                            ArrayList temp = new ArrayList(entities);
                            for (Object o : temp)
                            {
                                List unloadedMobs;
                                AIStuff stuff = (AIStuff) o;
                                EntityLiving b = stuff.entity;
                                if (b == null)
                                {
                                    toRemove.add(o);
                                    continue;
                                }
                                try
                                {
                                    unloadedMobs = new ArrayList(b.worldObj.unloadedEntityList);
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

                                    ArrayList list = (ArrayList) stuff.aiTasks.clone();
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

    }
    /** Lock used to unsure that AI tasks run at the correct time. */
    private static final BitSet                          tickLock          = new BitSet();
    /** Lists of the AI stuff for each thread. */
    private static Vector<AIStuff>[]                     aiStuffLists;
    /** Map of dimension to players, used for thread-safe player access. */
    public static final HashMap<Integer, Vector<Object>> worldPlayers      = new HashMap<Integer, Vector<Object>>();

    /** Used for sorting the AI runnables for run order. */
    public static final Comparator<IAIRunnable>          aiComparator      = new Comparator<IAIRunnable>()
    {
        @Override
        public int compare(IAIRunnable o1, IAIRunnable o2)
        {
            return o1.getPriority() - o2.getPriority();
        }
    };

    /** Sorts pokemobs by move order. */
    public static final Comparator<IPokemob>             pokemobComparator = new Comparator<IPokemob>()
    {
        @Override
        public int compare(IPokemob o1, IPokemob o2)
        {
            int speed1 = Tools.getStat(o1.getBaseStats()[5], o1.getIVs()[5], o1.getEVs()[5], o1.getLevel(),
                    o1.getModifiers()[5], o1.getNature().getStatsMod()[5]);
            int speed2 = Tools.getStat(o2.getBaseStats()[5], o2.getIVs()[5], o2.getEVs()[5], o2.getLevel(),
                    o2.getModifiers()[5], o2.getNature().getStatsMod()[5]);
            // TODO include checks for mob's selected attack and include attack
            // priority.
            return speed2 - speed1;
        }
    };

    static
    {
        AIThread.createThreads();
    }

    /** Adds the AI task for the given entity.
     * 
     * @param entity
     * @param task */
    public static void addAI(EntityLiving entity, IAIRunnable task)
    {
        IPokemob pokemob = (IPokemob) entity;
        int id = pokemob.getPokemonUID() % AIThread.threadCount;
        Vector<AIStuff> list = aiStuffLists[id];
        ArrayList<AIStuff> toCheck = new ArrayList(list);
        AIStuff entityAI = null;
        for (AIStuff aistuff : toCheck)
        {
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

    /** Adds the custom logic runnable for the given entity
     * 
     * @param entity
     * @param logic */
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

    /** Checks if task can run, given the tasks in tasks.
     * 
     * @param task
     * @param tasks
     * @return */
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

    /** Clears things for world unload */
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

    /** Removes the AI entry for the entity.
     * 
     * @param entity */
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

    @SubscribeEvent
    public void tickEvent(WorldTickEvent evt)
    {
        // At the start, refresh the player lists.
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
        else try// At the end, apply all of the paths, targets, moves and
                // states.
        {
            ArrayList<AIStuff> todo = new ArrayList();

            for (Vector<AIStuff> v : aiStuffLists)
            {
                todo.addAll(v);
                for (AIStuff stuff : todo)
                {
                    if (stuff.entity.isDead)
                    {
                        v.remove(stuff);
                    }
                    else
                    {
                        stuff.runServerThreadTasks(evt.world);
                    }
                }
                todo.clear();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /** AI Ticks at the end of the server tick.
     * 
     * @param evt */
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
}
