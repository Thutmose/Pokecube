package pokecube.core.ai.utils;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.init.Blocks;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.pathfinding.PathNavigate;
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

    public PokeNavigator(IPokemob pokemob, World world)
    {
        super(pokemob.getEntity(), world);
        this.entity = pokemob.getEntity();
        this.world = world;
        this.pathSearchRange = pokemob.getEntity().getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE);
        this.pokemob = pokemob;
        canSwim = true;
        canDive = pokemob.swims();
        pathfinder = new Paths(world);
    }

    /** If on ground or swimming and can swim */
    @Override
    public boolean canNavigate()
    {
        if (pokemob.getPokemonAIState(IPokemob.SLEEPING) || pokemob.getStatus() == IPokemob.STATUS_SLP
                || pokemob.getStatus() == IPokemob.STATUS_FRZ || pokemob.getPokemonAIState(IMoveConstants.CONTROLLED))
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
        Path current = noPath() ? null : getPath();
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
        Vector3 v1 = Vector3.getNewVector().set(start);
        Vector3 v2 = Vector3.getNewVector().set(end);
        boolean ground = !canFly;
        if (ground && ((int) start.y) != ((int) end.y)) { return false; }
        double dx = sizeX / 2d;
        double dy = sizeY;
        double dz = sizeZ / 2d;

        v1.set(start).addTo(0, 0, 0);
        v2.set(end).addTo(0, 0, 0);
        if (!v1.isVisible(world, v2)) return false;
        v1.set(start).addTo(0, dy, 0);
        v2.set(end).addTo(0, dy, 0);
        if (!v1.isVisible(world, v2)) return false;

        v1.set(start).addTo(dx, 0, 0);
        v2.set(end).addTo(dx, 0, 0);
        if (!v1.isVisible(world, v2)) return false;
        v1.set(start).addTo(-dx, 0, 0);
        v2.set(end).addTo(-dx, 0, 0);
        if (!v1.isVisible(world, v2)) return false;
        v1.set(start).addTo(0, 0, dz);
        v2.set(end).addTo(0, 0, dz);
        if (!v1.isVisible(world, v2)) return false;
        v1.set(start).addTo(0, 0, -dz);
        v2.set(end).addTo(0, 0, -dz);
        return true;
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
        super.onUpdateNavigation();
    }

    @Override
    public void pathFollow()
    {
        super.pathFollow();
    }

    public void refreshCache()
    {
    }

    /** Trims path data from the end to the first sun covered block */
    @Override
    public void removeSunnyPath()
    {
        super.removeSunnyPath();
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
