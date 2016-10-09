package pokecube.core.ai.thread;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Queue;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.collect.Maps;
import com.google.common.collect.Queues;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import pokecube.core.ai.utils.AIEventHandler;
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
    public static class AIStuff
    {
        public final EntityLiving              entity;
        public final ArrayList<IAIRunnable>    aiTasks = new ArrayList<IAIRunnable>();
        public final ArrayList<ILogicRunnable> aiLogic = new ArrayList<ILogicRunnable>();

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

        public void tick()
        {
            ArrayList list;
            synchronized (aiTasks)
            {
                list = aiTasks;
                if (list != null) for (int i = 0; i < list.size(); i++)
                {
                    IAIRunnable ai = (IAIRunnable) list.get(i);
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
            }
            list = aiLogic;
            if (list != null) for (int i = 0; i < list.size(); i++)
            {
                ILogicRunnable runnable = (ILogicRunnable) list.get(i);
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

    public static class AIThread extends Thread
    {
        public static int                        threadCount = 0;

        public static HashMap<Integer, AIThread> threads     = Maps.newHashMap();

        public static void createThreads()
        {
            threadCount = Math.max(1, PokecubeMod.core.getConfig().maxAIThreads);
            threadCount = Math.min(threadCount, Runtime.getRuntime().availableProcessors());
            aiStuffLists = new Queue[threadCount];
            System.out.println("Creating and starting Pokemob AI Threads.");
            for (int i = 0; i < threadCount; i++)
            {
                Queue<AIStuff> set = Queues.newConcurrentLinkedQueue();
                AIThread thread = new AIThread(i, set, new Object());
                aiStuffLists[i] = set;
                thread.setPriority(8);
                thread.start();
            }
            new AIEventHandler();
        }

        public final Queue<AIStuff> aiStuff;
        public final Object         lock;
        final int                   id;

        public AIThread(final int number, final Queue<AIStuff> aiStuff, final Object lock)
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
                        synchronized (lock)
                        {
                            try
                            {
                                lock.wait();
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        }
                        synchronized (aiStuff)
                        {
                            while (!aiStuff.isEmpty())
                                aiStuff.remove().tick();
                        }
                    }
                }
            });
            this.lock = lock;
            id = number;
            this.aiStuff = aiStuff;
            this.setName("Netty Server IO - Pokemob AI Thread-" + id);
            threads.put(id, this);
        }

    }

    /** Lists of the AI stuff for each thread. */
    private static Queue<AIStuff>[]                                aiStuffLists;
    /** Map of dimension to players, used for thread-safe player access. */
    public static final HashMap<Integer, Vector<Object>>           worldPlayers      = new HashMap<Integer, Vector<Object>>();

    public static final ConcurrentHashMap<Integer, Vector<Entity>> worldEntities     = new ConcurrentHashMap<Integer, Vector<Entity>>();

    /** Used for sorting the AI runnables for run order. */
    public static final Comparator<IAIRunnable>                    aiComparator      = new Comparator<IAIRunnable>()
                                                                                     {
                                                                                         @Override
                                                                                         public int compare(
                                                                                                 IAIRunnable o1,
                                                                                                 IAIRunnable o2)
                                                                                         {
                                                                                             return o1.getPriority()
                                                                                                     - o2.getPriority();
                                                                                         }
                                                                                     };

    /** Sorts pokemobs by move order. */
    public static final Comparator<IPokemob>                       pokemobComparator = new Comparator<IPokemob>()
                                                                                     {
                                                                                         @Override
                                                                                         public int compare(IPokemob o1,
                                                                                                 IPokemob o2)
                                                                                         {
                                                                                             int speed1 = Tools.getStat(
                                                                                                     o1.getBaseStats()[5],
                                                                                                     o1.getIVs()[5],
                                                                                                     o1.getEVs()[5],
                                                                                                     o1.getLevel(),
                                                                                                     o1.getModifiers()[5],
                                                                                                     o1.getNature()
                                                                                                             .getStatsMod()[5]);
                                                                                             int speed2 = Tools.getStat(
                                                                                                     o2.getBaseStats()[5],
                                                                                                     o2.getIVs()[5],
                                                                                                     o2.getEVs()[5],
                                                                                                     o2.getLevel(),
                                                                                                     o2.getModifiers()[5],
                                                                                                     o2.getNature()
                                                                                                             .getStatsMod()[5]);
                                                                                             return speed2 - speed1;
                                                                                         }
                                                                                     };

    static
    {
        AIThread.createThreads();
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
        for (int i = 0; i < tasks.size(); i++)
        {
            IAIRunnable ai = tasks.get(i);
            if (ai.getPriority() < prior && (mutex & ai.getMutex()) != 0 && ai.shouldRun()) { return false; }
        }
        return task.shouldRun();
    }

    /** Clears things for world unload */
    public static void clear()
    {
        for (Queue v : aiStuffLists)
        {
            v.clear();
        }
        worldPlayers.clear();
        TickHandler.getInstance().worldCaches.clear();
    }

    /** Sets the AIStuff to tick on correct thread.
     * 
     * @param blockEntity
     * @param task */
    public static void scheduleAITick(AIStuff ai)
    {
        IPokemob pokemob = (IPokemob) ai.entity;
        int id = pokemob.getPokemonUID() % AIThread.threadCount;
        AIThread thread = AIThread.threads.get(id);
        thread.aiStuff.add(ai);
    }

    @SubscribeEvent
    public void tickEvent(WorldTickEvent evt)
    {
        // At the start, refresh the player lists.
        if (evt.phase == Phase.START)
        {
            Vector players = worldPlayers.get(evt.world.provider.getDimension());
            if (players == null)
            {
                players = new Vector();
            }
            players.clear();
            players.addAll(evt.world.playerEntities);
            worldPlayers.put(evt.world.provider.getDimension(), players);
            Vector<Entity> entities = worldEntities.get(evt.world.provider.getDimension());
            if (entities == null)
            {
                entities = new Vector<Entity>();
            }
            entities.clear();
            entities.addAll(evt.world.loadedEntityList);
            worldEntities.put(evt.world.provider.getDimension(), entities);
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
            for (AIThread thread : AIThread.threads.values())
            {
                try
                {
                    synchronized (thread.lock)
                    {
                        thread.lock.notify();
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
    }
}
