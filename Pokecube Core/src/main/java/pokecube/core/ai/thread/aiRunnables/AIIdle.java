package pokecube.core.ai.thread.aiRunnables;

import java.util.Random;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.pathfinding.PathEntity;
import pokecube.core.database.PokedexEntry;
import pokecube.core.events.handlers.SpawnHandler;
import pokecube.core.interfaces.IPokemob;
import thut.api.TickHandler;
import thut.api.maths.Vector3;

public class AIIdle extends AIBase
{
    final private EntityLiving entity;
    final IPokemob             mob;
    final PokedexEntry         entry;
    private double             xPosition;
    private double             yPosition;
    private double             zPosition;
    private double             speed;

    Vector3                    v  = Vector3.getNewVector();
    Vector3                    v1 = Vector3.getNewVector();

    public AIIdle(EntityLiving entity)
    {
        this.entity = entity;
        this.setMutex(2);
        mob = (IPokemob) entity;
        entry = mob.getPokedexEntry();
        this.speed = entity.getEntityAttribute(SharedMonsterAttributes.movementSpeed).getAttributeValue() * 0.4;
    }

    @Override
    public boolean shouldRun()
    {
        PathEntity current = null;
        world = TickHandler.getInstance().getWorldCache(entity.dimension);

        if (world == null || mob.getPokedexEntry().isStationary || mob.getPokemonAIState(IPokemob.EXECUTINGMOVE)
                || entity.getAttackTarget() != null)
            return false;
        if ((current = entity.getNavigator().getPath()) != null && entity.getNavigator().noPath())
        {
            addEntityPath(entity.getEntityId(), entity.dimension, null, speed);
            current = null;
        }
        if (current != null && entity.getAttackTarget() == null)
        {
            v.set(current.getFinalPathPoint());
            v1.set(entity);
            double diff = 4 * entity.width;// TODO refine tis to using length
                                           // too
            diff = Math.max(2, diff);
            if (v.distToSq(v1) < diff)
            {
                addEntityPath(entity.getEntityId(), entity.dimension, null, speed);
                return false;
            }
            else
            {
                return false;
            }
        }
        if (entity.getAttackTarget() != null || !entity.getNavigator().noPath()) return false;

        mob.getPokedexEntry().flys();
        if (mob.getPokemonAIState(IPokemob.SITTING)) return false;
        if (mob.getPokemonAIState(IPokemob.SLEEPING)) return false;
        else if (this.entity.riddenByEntity != null || current != null)
        {
            return false;
        }
        else if (entity.ticksExisted % (50 + new Random().nextInt(50)) == 0)
        {
            boolean tameFactor = mob.getPokemonAIState(IPokemob.TAMED) && !mob.getPokemonAIState(IPokemob.GUARDING)
                    && !mob.getPokemonAIState(IPokemob.STAYING);
            int distance = tameFactor ? 8 : 16;
            v.clear();
            if (!tameFactor)
            {
                if (mob.getHome() == null
                        || (mob.getHome().getX() == 0 && mob.getHome().getY() == 0 & mob.getHome().getZ() == 0))
                {
                    v1.set(mob);
                    mob.setHome(v1.intX(), v1.intY(), v1.intZ(), 16);
                }
                distance = (int) Math.min(distance, mob.getHomeDistance());
                v.set(mob.getHome());
            }
            else
            {
                v.set(entity);
            }
            v1.set((v.isEmpty()
                    && (mob.getPokemonAIState(IPokemob.GUARDING) || mob.getPokemonAIState(IPokemob.STAYING))) ? entity
                            : v);
            Vector3 v = SpawnHandler.getRandomPointNear(world, v1, distance);

            double diff = Math.max(mob.getPokedexEntry().length * mob.getSize(),
                    mob.getPokedexEntry().width * mob.getSize());
            diff = Math.max(2, diff);
            if (v == null || this.v.distToSq(v) < diff)
            {
                return false;
            }
            else
            {
                this.xPosition = v.x;
                this.yPosition = v.y;
                this.zPosition = v.z;
                return true;
            }
        }
        return false;
    }

    @Override
    public void run()
    {
        if (entry.flys())
        {
            doFlyingIdle();
        }
        else if (entry.floats())
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
        v.set(this.xPosition, this.yPosition, this.zPosition);

        if (v.isEmpty() || v1.distToSq(v) <= 1) return;
        // if (true) return;

        mob.setPokemonAIState(IPokemob.IDLE, true);
        PathEntity path = this.entity.getNavigator().getPathToXYZ(this.xPosition, this.yPosition, this.zPosition);
        addEntityPath(entity.getEntityId(), entity.dimension, path, speed);
        // System.out.println("should run");
        mob.setPokemonAIState(IPokemob.IDLE, false);
    }

    private void doFlyingIdle()
    {
        if (Math.random() > 0.75)
        {
            doGroundIdle();
        }
    }

    private void doFloatingIdle()
    {
        v.set(xPosition, yPosition, zPosition);
        Vector3 temp = Vector3.getNextSurfacePoint(world, v, Vector3.secondAxisNeg, v.y);
        if (temp == null) return;
        yPosition = temp.y + entry.preferedHeight;
    }

    private void doGroundIdle()
    {
        v.set(xPosition, yPosition, zPosition);
        Vector3 temp = Vector3.getNextSurfacePoint(world, v, Vector3.secondAxisNeg, v.y);
        if (temp == null) return;
        yPosition = temp.y;
    }

    public void doWaterIdle()
    {

    }

    public void doStationaryIdle()
    {
        xPosition = entity.posX;
        yPosition = entity.posY;
        zPosition = entity.posZ;
    }

    @Override
    public void reset()
    {
    }

}
