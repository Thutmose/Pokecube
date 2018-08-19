package pokecube.core.ai.thread.aiRunnables;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.Stats;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.interfaces.pokemob.ai.LogicStates;
import thut.api.entity.ai.AIThreadManager;
import thut.api.entity.ai.IAIRunnable;
import thut.api.maths.Vector3;
import thut.lib.ItemStackTools;

public abstract class AIBase implements IAIRunnable
{
    /** Thread safe sound playing. */
    public static class PlaySound implements IRunnable
    {
        final int           dim;
        final Vector3       loc;
        final SoundEvent    sound;
        final SoundCategory cat;
        final float         volume;
        final float         pitch;

        public PlaySound(int dim, Vector3 loc, SoundEvent sound, SoundCategory cat, float volume, float pitch)
        {
            this.dim = dim;
            this.sound = sound;
            this.volume = volume;
            this.loc = loc;
            this.pitch = pitch;
            this.cat = cat;
        }

        @Override
        public boolean run(World world)
        {
            if (dim != world.provider.getDimension()) return false;
            world.playSound(null, loc.x, loc.y, loc.z, sound, cat, volume, pitch);
            return true;
        }

    }

    /** Thread safe inventory setting for pokemobs. */
    public static class InventoryChange implements IRunnable
    {
        public final int       entity;
        public final int       dim;
        public final int       slot;
        public final int       minSlot;
        public final ItemStack stack;

        public InventoryChange(Entity entity, int slot, ItemStack stack, boolean min)
        {
            this.entity = entity.getEntityId();
            this.dim = entity.dimension;
            this.stack = stack;
            if (min)
            {
                minSlot = slot;
                this.slot = -1;
            }
            else
            {
                this.slot = slot;
                minSlot = 0;
            }
        }

        @Override
        public boolean run(World world)
        {
            if (dim != world.provider.getDimension()) return false;
            Entity e = world.getEntityByID(entity);
            IPokemob pokemob = CapabilityPokemob.getPokemobFor(e);
            if (e == null || pokemob == null) return false;
            if (slot > 0) pokemob.getPokemobInventory().setInventorySlotContents(slot, stack);
            else if (!ItemStackTools.addItemStackToInventory(stack, pokemob.getPokemobInventory(), minSlot))
            {
                e.entityDropItem(stack, 0);
            }
            return true;
        }

    }

    public static interface IRunnable
    {
        /** @param world
         * @return task ran sucessfully */
        boolean run(World world);
    }

    /** A thread-safe object used to set which move a pokemob is to use.
     * 
     * @author Thutmose */
    public static class MoveInfo implements IRunnable
    {
        public static final Comparator<MoveInfo> compare = new Comparator<MoveInfo>()
                                                         {
                                                             @Override
                                                             public int compare(MoveInfo o1, MoveInfo o2)
                                                             {
                                                                 if (o1.dim != o2.dim) return 0;
                                                                 if (FMLCommonHandler.instance()
                                                                         .getSide() == Side.SERVER)
                                                                 {
                                                                     WorldServer world = FMLCommonHandler.instance()
                                                                             .getMinecraftServerInstance()
                                                                             .getWorld(o1.dim);
                                                                     IPokemob e1 = CapabilityPokemob.getPokemobFor(
                                                                             world.getEntityByID(o1.attacker));
                                                                     IPokemob e2 = CapabilityPokemob.getPokemobFor(
                                                                             world.getEntityByID(o2.attacker));
                                                                     if (e2 != null
                                                                             && e1 != null) { return pokemobComparator
                                                                                     .compare(e1, e2); }
                                                                 }
                                                                 return 0;
                                                             }
                                                         };

        public final int                         attacker;
        public final int                         targetEnt;
        public final int                         dim;
        public final Vector3                     target;
        public final float                       distance;

        public MoveInfo(int _attacker, int _targetEnt, int dimension, Vector3 _target, float _distance)
        {
            attacker = _attacker;
            targetEnt = _targetEnt;
            target = _target;
            distance = _distance;
            dim = dimension;
        }

        @Override
        public boolean run(World world)
        {
            if (dim != world.provider.getDimension()) return false;
            Entity e = world.getEntityByID(attacker);
            if (e == null || !(e instanceof EntityLiving)) return false;

            EntityLiving mob = (EntityLiving) e;
            List<?> unloadedMobs = world.unloadedEntityList;
            if (!mob.getEntityWorld().loadedEntityList.contains(mob) || unloadedMobs.contains(mob)) return false;
            IPokemob pokemob = CapabilityPokemob.getPokemobFor(mob);
            if (!(mob.isDead || mob.getHealth() <= 0 || pokemob == null))
            {
                pokemob.executeMove(world.getEntityByID(targetEnt), target, distance);
            }
            return true;
        }
    }

    /** A thread-safe object used to set the current path for an entity.
     * 
     * @author Thutmose */
    public static class PathInfo implements IRunnable
    {
        public final Path   path;
        public final int    pather;
        public final int    dim;
        public final double speed;

        public PathInfo(int _pather, int dimension, Path _path, double _speed)
        {
            path = _path;
            pather = _pather;
            speed = _speed;
            dim = dimension;
        }

        @Override
        public boolean run(World world)
        {
            if (dim != world.provider.getDimension()) return false;
            Entity e = world.getEntityByID(pather);
            if (e == null || !(e instanceof EntityLiving)) { return false; }
            EntityLiving mob = (EntityLiving) e;
            List<?> unloadedMobs = world.unloadedEntityList;
            if (!mob.getEntityWorld().loadedEntityList.contains(mob) || unloadedMobs.contains(mob)) { return false; }

            if (!(mob.isDead || mob.getHealth() <= 0))
            {
                mob.getNavigator().setPath(path, speed);
            }
            return true;
        }
    }

    /** A thread safe object used to set the attack target of an entity.
     * 
     * @author Thutmose */
    public static class TargetInfo implements IRunnable
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

        @Override
        public boolean run(World world)
        {
            if (dim != world.provider.getDimension()) return false;
            Entity e = world.getEntityByID(attacker);
            Entity e1 = world.getEntityByID(target);
            if (e == null || !(e instanceof EntityLiving)) { return false; }
            if (!(e1 instanceof EntityLivingBase))
            {
                e1 = null;
            }
            EntityLiving mob = (EntityLiving) e;
            List<?> unloadedMobs = world.unloadedEntityList;

            boolean mobExists = !(unloadedMobs.contains(e));

            if (!mobExists || (!(e1 instanceof EntityPlayer)
                    && ((e1 != null && !e1.getEntityWorld().loadedEntityList.contains(e1))
                            || (e1 != null && unloadedMobs.contains(e1))))) { return false; }
            if (!(mob.isDead || mob.getHealth() <= 0))
            {
                mob.setAttackTarget((EntityLivingBase) e1);
            }
            return true;
        }
    }

    public static class PathManager
    {
        protected boolean  set = false;
        protected PathInfo path;

        protected void reset()
        {
            path = null;
            set = false;
        }

        protected boolean addEntityPath(Entity entity, Path path, double speed)
        {
            if (set) return false;
            this.path = new PathInfo(entity.getEntityId(), entity.dimension, path, speed);
            set = true;
            return set;
        }
    }

    /** Sorts pokemobs by move order. */
    public static final Comparator<IPokemob> pokemobComparator = new Comparator<IPokemob>()
                                                               {
                                                                   @Override
                                                                   public int compare(IPokemob o1, IPokemob o2)
                                                                   {
                                                                       int speed1 = o1.getStat(Stats.VIT, true);
                                                                       int speed2 = o2.getStat(Stats.VIT, true);
                                                                       return speed2 - speed1;
                                                                   }
                                                               };

    protected IBlockAccess                   world;

    int                                      priority          = 0;

    int                                      mutex             = 0;

    protected Vector<IRunnable>              toRun             = new Vector<IRunnable>();

    protected Vector<IRunnable>              moves             = new Vector<IRunnable>();

    private PathManager                      pathManager;

    /** Set the pathmanager for this mob. This is only really needed if the
     * AIBase is intended to manage prioritized pathfinding.
     * 
     * @param manager
     * @return */
    public AIBase setPathManager(PathManager manager)
    {
        this.pathManager = manager;
        return this;
    }

    /** Returns the path manager. This is final, as to ensure that a pathmanager
     * has infact been set before continuing to run.
     * 
     * @return */
    public final PathManager getPathManager()
    {
        // This shouldn't be the case, please use setPathManager after
        // constructing the AIBase, if you are intending to use pathing.
        if (pathManager == null) pathManager = new PathManager();
        return pathManager;
    }

    /** Threadsafe path setting. This also enforces that only the highest
     * priority path is set, if you do not want to enforce this, manually add a
     * PathInfo to the toRun vector.
     * 
     * @param entity
     * @param path
     * @param speed
     * @return */
    protected boolean addEntityPath(Entity entity, Path path, double speed)
    {
        boolean set = getPathManager().addEntityPath(entity, path, speed) && getPathManager().path != null;
        if (set)
        {
            IPokemob pokemob = CapabilityPokemob.getPokemobFor(entity);
            if (pokemob != null) pokemob.setLogicState(LogicStates.PATHING, path != null);
            toRun.add(getPathManager().path);
        }
        return set;
    }

    /** Thread safe attack setting */
    protected void addMoveInfo(int attacker, int targetEnt, int dim, Vector3 target, float distance)
    {
        if (!moves.isEmpty())
        {
            if (PokecubeMod.debug)
                PokecubeMod.log(Level.WARNING, "adding duplicate move", new IllegalArgumentException());
        }
        else moves.add(new MoveInfo(attacker, targetEnt, dim, target, distance));
    }

    protected void addTargetInfo(Entity attacker, Entity target)
    {
        int targetId = target == null ? -1 : target.getEntityId();
        addTargetInfo(attacker.getEntityId(), targetId, attacker.dimension);
    }

    /** Thread safe target swapping information.
     * 
     * @param attacker
     * @param target
     * @param dim */
    protected void addTargetInfo(int attacker, int target, int dim)
    {
        toRun.add(new TargetInfo(attacker, target, dim));
    }

    @Override
    public void doMainThreadTick(World world)
    {
        ArrayList<IRunnable> runs = new ArrayList<IRunnable>(toRun.size());
        runs.addAll(toRun);
        for (IRunnable run : runs)
        {
            boolean ran = run == null || run.run(world);
            if (ran)
            {
                toRun.remove(run);
            }
        }
        // Ensure path manager has infact been set.
        getPathManager();
        synchronized (pathManager)
        {
            pathManager.reset();
        }
        runs = new ArrayList<IRunnable>(moves.size());
        runs.addAll(moves);
        for (IRunnable run : runs)
        {
            boolean ran = run.run(world);
            if (ran)
            {
                moves.remove(run);
            }
        }
    }

    protected List<EntityPlayer> getPlayersWithinDistance(Entity source, float distance)
    {
        Vector<Object> entities = AIThreadManager.worldPlayers.get(source.dimension);
        List<EntityPlayer> list = new ArrayList<EntityPlayer>();
        double dsq = distance * distance;
        if (entities != null)
        {
            List<?> temp = new ArrayList<Object>(entities);
            for (Object o : temp)
            {
                if (source.getDistanceSq((Entity) o) < dsq)
                {
                    list.add((EntityPlayer) o);
                }
            }
        }
        final BlockPos pos = source.getPosition();
        Collections.sort(list, new Comparator<EntityPlayer>()
        {

            @Override
            public int compare(EntityPlayer o1, EntityPlayer o2)
            {
                return (int) (o1.getDistanceSq(pos) - o2.getDistanceSq(pos));
            }
        });
        return list;
    }

    protected List<Entity> getEntitiesWithinDistance(Entity source, float distance, Class<?>... targetClass)
    {
        Vector<Entity> entities = AIThreadManager.worldEntities.get(source.dimension);
        List<Entity> list = new ArrayList<Entity>();
        double dsq = distance * distance;
        if (entities != null)
        {
            List<Entity> temp = new ArrayList<Entity>(entities);
            for (Entity o : temp)
            {
                boolean correctClass = true;
                for (Class<?> claz : targetClass)
                {
                    correctClass = correctClass && claz.isInstance(o);
                }
                if (correctClass)
                {
                    if (source.getDistanceSq((Entity) o) < dsq)
                    {
                        list.add(o);
                    }
                }
            }
        }
        return list;
    }

    protected List<Object> getEntitiesWithinDistance(Vector3 source, int dimension, float distance,
            Class<?>... targetClass)
    {
        Vector<?> entities = AIThreadManager.worldEntities.get(dimension);
        List<Object> list = new ArrayList<Object>();
        if (entities != null)
        {
            List<?> temp = new ArrayList<Object>(entities);
            for (Object o : temp)
            {
                boolean correctClass = true;
                for (Class<?> claz : targetClass)
                {
                    correctClass = correctClass && claz.isInstance(o);
                }
                if (correctClass)
                {
                    if (source.distToEntity(((Entity) o)) < distance)
                    {
                        list.add(o);
                    }
                }
            }
        }
        return list;
    }

    @Override
    public int getMutex()
    {
        return mutex;
    }

    @Override
    public int getPriority()
    {
        return priority;
    }

    @Override
    public IAIRunnable setMutex(int mutex)
    {
        this.mutex = mutex;
        return this;
    }

    protected void setCombatState(IPokemob pokemob, CombatStates state, boolean value)
    {
        pokemob.setCombatState(state, value);
    }

    protected void setGeneralState(IPokemob pokemob, GeneralStates state, boolean value)
    {
        pokemob.setGeneralState(state, value);
    }

    protected void setLogicState(IPokemob pokemob, LogicStates state, boolean value)
    {
        pokemob.setLogicState(state, value);
    }

    @Override
    public IAIRunnable setPriority(int prior)
    {
        priority = prior;
        return this;
    }

}
