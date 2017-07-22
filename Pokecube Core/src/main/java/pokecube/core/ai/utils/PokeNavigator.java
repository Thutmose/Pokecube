package pokecube.core.ai.utils;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.init.Blocks;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import thut.api.maths.Vector3;
import thut.api.pathing.Paths;

/** This is overridden from the vanilla one to allow using a custom,
 * multi-threaded pathfinder. It also does some pokemob specific checks for
 * whether the pokemob can navigate, as well as checks to see if the pathing
 * should terminate early in certain situations, such as "low priority" paths
 * which are close enough to the end, like when a mob is just idling around. */
public class PokeNavigator extends PathNavigate
{
    Vector3                          v               = Vector3.getNewVector();
    Vector3                          v1              = Vector3.getNewVector();
    Vector3                          v2              = Vector3.getNewVector();
    Vector3                          v3              = Vector3.getNewVector();
    /** The number of blocks (extra) +/- in each axis that get pulled out as
     * cache for the pathfinder's search space */
    private final IAttributeInstance pathSearchRange;
    private boolean                  noSunPathfind;
    /** If the entity can swim. Swimming AI enables this and the pathfinder will
     * also cause the entity to swim straight upwards when underwater */
    private boolean                  canSwim;

    private boolean                  canDive;

    private boolean                  canFly;
    public final Paths               pathfinder;

    final IPokemob                   pokemob;

    long                             lastCacheUpdate = 0;

    int                              sticks          = 0;

    public PokeNavigator(EntityLiving entity, World world)
    {
        super(entity, world);
        this.entity = entity;
        this.world = world;
        this.pathSearchRange = entity.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE);
        pokemob = CapabilityPokemob.getPokemobFor(entity);
        canSwim = true;
        canDive = pokemob.swims();
        pathfinder = new Paths(world);
    }

    /** If on ground or swimming and can swim */
    @Override
    public boolean canNavigate()
    {
        if (pokemob.getPokemonAIState(IPokemob.SLEEPING) || pokemob.getStatus() == IPokemob.STATUS_SLP
                || pokemob.getStatus() == IPokemob.STATUS_FRZ)
            return false;
        if (pokemob.getPokemonAIState(IPokemob.SITTING)) return false;
        return this.entity.onGround || this.canSwim && this.isInLiquid() || this.canFly;
    }

    /** sets active PathHeap to null */
    @Override
    public synchronized void clearPathEntity()
    {
        this.currentPath = null;
    }

    @Override
    protected Vec3d getEntityPosition()
    {
        return new Vec3d(this.entity.posX, this.getPathableYPos(), this.entity.posZ);
    }

    /** gets the actively used PathHeap */
    @Override
    public Path getPath()
    {
        return this.currentPath;
    }

    /** Gets the safe pathing Y position for the entity depending on if it can
     * path swim or not */
    private int getPathableYPos()
    {
        boolean inWater = this.entity.isInWater();
        if (canDive && inWater)
        {
            return (int) (this.entity.posY + 0.5D);
        }
        else if (canFly && !inWater)
        {
            return (int) (this.entity.posY + 0.5D);
        }
        else if (inWater && this.canSwim)
        {
            int i = (int) this.entity.posY;
            Block block = this.world
                    .getBlockState(
                            new BlockPos(MathHelper.floor(this.entity.posX), i, MathHelper.floor(this.entity.posZ)))
                    .getBlock();
            int j = 0;

            do
            {
                if (block != Blocks.FLOWING_WATER && block != Blocks.WATER) { return i; }

                ++i;
                block = this.world
                        .getBlockState(
                                new BlockPos(MathHelper.floor(this.entity.posX), i, MathHelper.floor(this.entity.posZ)))
                        .getBlock();
                ++j;
            }
            while (j <= 16);

            return (int) this.entity.posY;
        }
        return (int) (this.entity.posY + 0.5D);

    }

    @Override
    protected PathFinder getPathFinder()
    {
        return null;
    }

    /** Gets the maximum distance that the path finding will search in. */
    @Override
    public float getPathSearchRange()
    {
        return (float) this.pathSearchRange.getAttributeValue();
    }

    /** Returns the path to the given EntityLiving */
    @Override
    public Path getPathToEntityLiving(Entity entity)
    {
        PokedexEntry entry = pokemob.getPokedexEntry();
        IPokemob transformed = CapabilityPokemob.getPokemobFor(pokemob.getTransformedTo());
        if (transformed != null)
        {
            entry = transformed.getPokedexEntry();
        }
        this.canFly = entry.flys() || entry.floats();
        this.canDive = entry.swims();

        Path ret = null;

        if (this.canNavigate())
        {
            ret = pathfinder.getPathHeapToEntity(this.entity, entity, this.getPathSearchRange());
        }
        return ret;
    }

    @Override
    public Path getPathToPos(BlockPos pos)
    {
        PokedexEntry entry = pokemob.getPokedexEntry();
        IPokemob transformed = CapabilityPokemob.getPokemobFor(pokemob.getTransformedTo());
        if (transformed != null)
        {
            entry = transformed.getPokedexEntry();
        }
        this.canFly = entry.flys() || entry.floats();
        this.canDive = entry.swims();
        Path current = getPath();
        if (current != null && !pokemob.getPokemonAIState(IMoveConstants.ANGRY))
        {
            Vector3 p = v.set(current.getFinalPathPoint());
            Vector3 v = v1.set(pos);
            if (p.distToSq(v) <= 1) { return current; }
        }
        Path ret = null;

        if (this.canNavigate())
        {
            ret = pathfinder.getEntityPathToXYZ(this.entity, pos.getX(), pos.getY(), pos.getZ(),
                    this.getPathSearchRange());
        }
        return ret;

    }

    /** Returns true when an entity of specified size could safely walk in a
     * straight line between the two points. Args: pos1, pos2, entityXSize,
     * entityYSize, entityZSize */
    @Override
    public boolean isDirectPathBetweenPoints(Vec3d start, Vec3d end, int sizeX, int sizeY, int sizeZ)
    {
        // if (true)
        return false;
        // v.set(start);
        // v1.set(end);
        // Vector3 dir = v1.subtract(v);
        // double dist = dir.mag();
        // dir.scalarMultBy(1 / dist);
        // IPathingMob pather = (IPathingMob) pokemob;
        // for (int i = 0; i < dist; i++)
        // {
        // v1.set(v).add(dir.x * i, dir.y * i - 1, dir.z * i);
        // if ((!canFly && pather.getBlockPathWeight(world, v1) >= 40)
        // || !pather.fits(world, v1.addTo(0, 1, 0), null)) { return false; }
        // }
        // return true;
    }

    /** Returns true if the entity is in water or lava, false otherwise */
    @Override
    public boolean isInLiquid()
    {
        return entity.isInWater() || entity.isInLava();
    }

    /** If null path or reached the end */
    @Override
    public boolean noPath()
    {
        return this.getPath() == null || this.getPath().isFinished();
    }

    @Override
    public void onUpdateNavigation()
    {
        ++this.totalTicks;
        if (this.tryUpdatePath)
        {
            this.updatePath();
        }
        if (!this.noPath())
        {
            if (this.canNavigate())
            {
                this.pathFollow();
            }
            else if (this.currentPath != null
                    && this.currentPath.getCurrentPathIndex() < this.currentPath.getCurrentPathLength())
            {
                Vec3d vec3d = this.getEntityPosition();
                Vec3d vec3d1 = this.currentPath.getVectorFromIndex(this.entity, this.currentPath.getCurrentPathIndex());

                if (vec3d.y > vec3d1.y && !this.entity.onGround
                        && MathHelper.floor(vec3d.x) == MathHelper.floor(vec3d1.x)
                        && MathHelper.floor(vec3d.z) == MathHelper.floor(vec3d1.z))
                {
                    this.currentPath.setCurrentPathIndex(this.currentPath.getCurrentPathIndex() + 1);
                }
            }
            if (!this.noPath())
            {
                Vec3d vec3d2 = this.currentPath.getPosition(this.entity);
                if (vec3d2 != null)
                {
                    BlockPos blockpos = (new BlockPos(vec3d2)).down();
                    PathPoint point = this.currentPath.getPathPointFromIndex(this.currentPath.getCurrentPathIndex());
                    double dx = 0, dy = 0, dz = 0;
                    if (point instanceof thut.api.pathing.PathPoint)
                    {
                        thut.api.pathing.PathPoint pointT = (thut.api.pathing.PathPoint) point;
                        dx = pointT.x - pointT.x;
                        dy = pointT.y - pointT.y;
                        dz = pointT.z - pointT.z;
                    }
                    AxisAlignedBB axisalignedbb = this.world.getBlockState(blockpos).getBoundingBox(this.world,
                            blockpos);
                    vec3d2 = vec3d2.subtract(0.0D, 1.0D - axisalignedbb.maxY, 0.0D);
                    this.entity.getMoveHelper().setMoveTo(vec3d2.x + dx, vec3d2.y + dy, vec3d2.z + dz, this.speed);
                }
            }
        }
    }

    @Override
    public void pathFollow()
    {
        Vec3d vec3d = this.getEntityPosition();
        int i = this.currentPath.getCurrentPathLength();

        for (int j = this.currentPath.getCurrentPathIndex(); j < this.currentPath.getCurrentPathLength(); ++j)
        {
            if ((double) this.currentPath.getPathPointFromIndex(j).y != Math.floor(vec3d.y))
            {
                i = j;
                break;
            }
        }

        this.maxDistanceToWaypoint = this.entity.width > 0.75F ? this.entity.width / 2.0F : 1f;
        Vec3d vec3d1 = this.currentPath.getCurrentPos();

        if (MathHelper.abs((float) (this.entity.posX - (vec3d1.x + 0.5D))) < this.maxDistanceToWaypoint
                && MathHelper.abs((float) (this.entity.posZ - (vec3d1.z + 0.5D))) < this.maxDistanceToWaypoint
                && Math.abs(this.entity.posY - vec3d1.y) < 0.75D)
        {
            this.currentPath.setCurrentPathIndex(this.currentPath.getCurrentPathIndex() + 1);
        }

        int k = MathHelper.ceil(this.entity.width);
        int l = MathHelper.ceil(this.entity.height);
        int i1 = k;

        for (int j1 = i - 1; j1 >= this.currentPath.getCurrentPathIndex(); --j1)
        {
            if (this.isDirectPathBetweenPoints(vec3d, this.currentPath.getVectorFromIndex(this.entity, j1), k, l, i1))
            {
                this.currentPath.setCurrentPathIndex(j1);
                break;
            }
        }

        this.checkForStuck(vec3d);
    }

    public void refreshCache()
    {
    }

    /** Trims path data from the end to the first sun covered block */
    @Override
    public void removeSunnyPath()
    {
        if (!this.world.canBlockSeeSky(new BlockPos(MathHelper.floor(this.entity.posX), (int) (this.entity.posY + 0.5D),
                MathHelper.floor(this.entity.posZ))))
        {
            for (int i = 0; i < this.getPath().getCurrentPathLength(); ++i)
            {
                PathPoint pathpoint = this.getPath().getPathPointFromIndex(i);

                if (this.world.canBlockSeeSky(new BlockPos(pathpoint.x, pathpoint.y, pathpoint.z)))
                {
                    this.getPath().setCurrentPathLength(i - 1);
                    return;
                }
            }
        }
    }

    /** sets the active path data if path is 100% unique compared to old path,
     * checks to adjust path for sun avoiding ents and stores end coords */
    @Override
    public boolean setPath(Path path, double speed)
    {
        if (path == getPath()) return true;
        if (path == null)
        {
            clearPathEntity();
            return false;
        }
        if (!path.isSamePath(this.getPath()))
        {
            this.currentPath = path;
        }

        if (this.noSunPathfind)
        {
            this.removeSunnyPath();
        }

        if (this.getPath().getCurrentPathLength() == 0) { return false; }
        this.speed = speed;
        return true;
    }

    /** Sets the speed */
    @Override
    public void setSpeed(double speed)
    {
        this.speed = speed;
    }

    /** Try to find and set a path to EntityLiving. Returns true if
     * successful. */
    @Override
    public boolean tryMoveToEntityLiving(Entity entity, double speed)
    {
        Path PathHeap = this.getPathToEntityLiving(entity);
        return PathHeap != null ? this.setPath(PathHeap, speed) : false;
    }

    /** Try to find and set a path to XYZ. Returns true if successful. */
    @Override
    public boolean tryMoveToXYZ(double x, double y, double z, double speed)
    {
        Path PathHeap = this.getPathToXYZ(MathHelper.floor(x), ((int) y), MathHelper.floor(z));
        return this.setPath(PathHeap, speed);
    }

}
