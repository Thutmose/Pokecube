package pokecube.core.ai.thread.aiRunnables.idle;

import java.util.List;
import java.util.Random;

import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.pathfinding.Path;
import net.minecraft.world.IBlockAccess;
import pokecube.core.ai.thread.aiRunnables.AIBase;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IMoveConstants.AIRoutine;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.interfaces.pokemob.ai.LogicStates;
import thut.api.TickHandler;
import thut.api.maths.Vector3;

/** This IAIRunnable makes the mobs randomly wander around if they have nothing
 * better to do. */
public class AIIdle extends AIBase
{
    public static int          IDLETIMER = 20;

    final private EntityLiving entity;
    final IPokemob             mob;
    final PokedexEntry         entry;
    private double             x;
    private double             y;
    private double             z;
    private double             speed;
    private double             maxLength = 16;

    Vector3                    v         = Vector3.getNewVector();
    Vector3                    v1        = Vector3.getNewVector();

    public AIIdle(IPokemob pokemob)
    {
        this.entity = pokemob.getEntity();
        this.setMutex(2);
        mob = pokemob;
        entry = mob.getPokedexEntry();
        this.speed = entity.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue();
    }

    /** Floating things try to stay their preferedHeight from the ground. */
    private void doFloatingIdle()
    {
        v.set(x, y, z);
        Vector3 temp = Vector3.getNextSurfacePoint(world, v, Vector3.secondAxisNeg, v.y);
        if (temp == null || !mob.isRoutineEnabled(AIRoutine.AIRBORNE)) return;
        y = temp.y + entry.preferedHeight;
    }

    /** Flying things will path to air, so long as not airborne, somethimes they
     * will decide to path downwards, the height they path to will be centered
     * around players, to prevent them from all flying way up, or way down */
    private void doFlyingIdle()
    {
        boolean grounded = !mob.isRoutineEnabled(AIRoutine.AIRBORNE);
        boolean tamed = mob.getGeneralState(GeneralStates.TAMED) && !mob.getGeneralState(GeneralStates.STAYING);
        boolean up = Math.random() < 0.9;
        if (grounded && up && !tamed)
        {
            mob.setRoutineState(AIRoutine.AIRBORNE, true);
        }
        else if (!tamed)
        {
            mob.setRoutineState(AIRoutine.AIRBORNE, false);
            v.set(x, y, z);
            v.set(Vector3.getNextSurfacePoint(world, v, Vector3.secondAxisNeg, v.y));
            if (v != null) y = v.y;
        }
        List<EntityPlayer> players = getPlayersWithinDistance(entity, Integer.MAX_VALUE);
        if (!players.isEmpty())
        {
            EntityPlayer player = players.get(0);
            double diff = Math.abs(player.posY - y);
            if (diff > 5)
            {
                y = player.posY + 5 * (1 - Math.random());
            }
        }
    }

    /** Grounded things will path to surface points. */
    private void doGroundIdle()
    {
        v.set(x, y, z);
        v.set(Vector3.getNextSurfacePoint(world, v, Vector3.secondAxisNeg, v.y));
        if (v != null) y = v.y;
    }

    /** Stationary things will not idle path at all */
    public void doStationaryIdle()
    {
        x = entity.posX;
        y = entity.posY;
        z = entity.posZ;
    }

    /** Water things will not idle path out of water. */
    public void doWaterIdle()
    {
        v.set(this.x, this.y, this.z);
        if (world.getBlockState(v.getPos()).getMaterial() != Material.WATER)
        {
            x = entity.posX;
            y = entity.posY;
            z = entity.posZ;
        }
    }

    @Override
    public void reset()
    {
    }

    @Override
    public void run()
    {
        if (mob.getPokedexEntry().flys())
        {
            doFlyingIdle();
        }
        else if (mob.getPokedexEntry().floats())
        {
            doFloatingIdle();
        }
        else if (entry.swims() && entity.isInWater())
        {
            doWaterIdle();
        }
        else if (entry.isStationary)
        {
            doStationaryIdle();
        }
        else
        {
            doGroundIdle();
        }
        v1.set(entity);
        v.set(this.x, this.y, this.z);

        if (v1.distToSq(v) <= 1 || mob.getLogicState(LogicStates.SITTING)) return;

        mob.setGeneralState(GeneralStates.IDLE, true);
        Path path = this.entity.getNavigator().getPathToXYZ(this.x, this.y, this.z);
        if (path != null && path.getCurrentPathLength() > maxLength) path = null;
        addEntityPath(entity, path, speed);
        mob.setGeneralState(GeneralStates.IDLE, false);
    }

    @Override
    public boolean shouldRun()
    {
        if (!mob.isRoutineEnabled(AIRoutine.WANDER)) return false;
        Path current = null;
        world = TickHandler.getInstance().getWorldCache(entity.dimension);

        if (world == null || mob.getPokedexEntry().isStationary || mob.getCombatState(CombatStates.EXECUTINGMOVE)
                || entity.getAttackTarget() != null || mob.getLogicState(LogicStates.PATHING)
                || mob.getGeneralState(GeneralStates.CONTROLLED))
            return false;
        if ((current = entity.getNavigator().getPath()) != null && entity.getNavigator().noPath())
        {
            addEntityPath(entity, null, speed);
            current = null;
        }
        if (current != null && entity.getAttackTarget() == null)
        {
            v.set(current.getFinalPathPoint());
            v1.set(entity);
            // TODO refine this to using length too
            double diff = 4 * entity.width;
            diff = Math.max(2, diff);
            if (v.distToSq(v1) < diff)
            {
                addEntityPath(entity, null, speed);
                return false;
            }
            return false;
        }
        if (entity.getAttackTarget() != null || !entity.getNavigator().noPath()) return false;

        if (mob.getLogicState(LogicStates.SITTING) && mob.getGeneralState(GeneralStates.TAMED)
                && !mob.getGeneralState(GeneralStates.STAYING))
            return false;
        int idleTimer = IDLETIMER;
        if (mob.getLogicState(LogicStates.SLEEPING) || (mob.getStatus() & IPokemob.STATUS_SLP) > 0) return false;
        else if (mob.getGeneralState(GeneralStates.CONTROLLED) || current != null)
        {
            return false;
        }
        else if ((entity.ticksExisted + new Random(mob.getRNGValue()).nextInt(idleTimer))
                % (idleTimer) == new Random(mob.getRNGValue()).nextInt(idleTimer))
        {
            boolean tameFactor = mob.getGeneralState(GeneralStates.TAMED)
                    && !mob.getGeneralState(GeneralStates.STAYING);
            int distance = (int) (maxLength = tameFactor ? 8 : 16);
            if (!tameFactor)
            {
                if (mob.getHome() == null
                        || (mob.getHome().getX() == 0 && mob.getHome().getY() == 0 & mob.getHome().getZ() == 0))
                {
                    v1.set(entity);
                    mob.setHome(v1.intX(), v1.intY(), v1.intZ(), 16);
                }
                distance = (int) Math.min(distance, mob.getHomeDistance());
                v.set(mob.getHome());
            }
            else
            {
                EntityLivingBase setTo = entity;
                if (mob.getPokemonOwner() != null) setTo = mob.getPokemonOwner();
                v.set(setTo);
            }
            Vector3 v = getRandomPointNear(world, mob, v1, distance);
            double diff = Math.max(mob.getPokedexEntry().length * mob.getSize(),
                    mob.getPokedexEntry().width * mob.getSize());
            diff = Math.max(2, diff);
            if (v == null || this.v.distToSq(v) < diff) { return false; }
            this.x = v.x;
            this.y = Math.round(v.y);
            this.z = v.z;
            return true;
        }
        return false;
    }

    public static Vector3 getRandomPointNear(IBlockAccess world, IPokemob mob, Vector3 v, int distance)
    {
        Vector3 ret = v;
        Vector3 temp = ret.copy();
        int rand = Math.abs(new Random().nextInt());
        if (distance % 2 == 0) distance++;
        int num = distance * distance * distance;
        for (int i = 0; i < num; i++)
        {
            int j = (i + rand) % num;
            int x = j % (distance) - distance / 2;
            int y = (j / distance) % (distance) - distance / 2;
            int z = (j / (distance * distance)) % (distance) - distance / 2;
            y = Math.min(Math.max(1, y), 2);
            temp.set(ret).addTo(x, y, z);
            if (temp.isClearOfBlocks(world) && mob.getBlockPathWeight(world, temp) <= 40) { return temp; }
        }
        return null;
    }

}