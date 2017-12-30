package pokecube.core.ai.utils.pathing;

import net.minecraft.entity.Entity;
import net.minecraft.pathfinding.FlyingNodeProcessor;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNavigateFlying;
import net.minecraft.pathfinding.SwimNodeProcessor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import pokecube.core.ai.utils.pathing.node.WalkNodeLadderProcessor;
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
public class PokemobNavigator extends PathNavigate
{
    private Vector3      v               = Vector3.getNewVector();
    private Vector3      v1              = Vector3.getNewVector();

    private boolean      canDive;

    private boolean      canFly;
    public final Paths   pathfinder;

    final IPokemob       pokemob;
    private PokedexEntry lastEntry;
    private PathNavigate wrapped;

    long                 lastCacheUpdate = 0;

    int                  sticks          = 0;

    public PokemobNavigator(IPokemob pokemob, World world)
    {
        super(pokemob.getEntity(), world);
        this.entity = pokemob.getEntity();
        this.world = world;
        this.pokemob = pokemob;
        canDive = pokemob.swims();
        pathfinder = new Paths(world);
    }

    private PathNavigate makeSwimingNavigator()
    {
        return new MultiNodeNavigator(entity, world, new SwimNodeProcessor(), new WalkNodeLadderProcessor(), canFly);
    }

    private void checkValues()
    {
        PokedexEntry entry = pokemob.getPokedexEntry();
        IPokemob transformed = CapabilityPokemob.getPokemobFor(pokemob.getTransformedTo());
        if (transformed != null)
        {
            entry = transformed.getPokedexEntry();
        }
        if (entry != lastEntry)
        {
            lastEntry = entry;
            this.canFly = entry.flys() || entry.floats();
            this.canDive = entry.swims();
            if (this.canDive && this.canFly) wrapped = new MultiNodeNavigator(entity, world, new FlyingNodeProcessor(),
                    makeSwimingNavigator().getNodeProcessor(), canFly);
            if (this.canFly && !canDive) wrapped = new PathNavigateFlying(entity, world);
            else if (canDive) wrapped = makeSwimingNavigator();
            else
            {
                wrapped = new LadderWalkNavigator(entity, world, canFly);
            }
            wrapped.getNodeProcessor().setCanEnterDoors(true);
            wrapped.getNodeProcessor().setCanSwim(true);
            wrapped.setSpeed(speed);
        }
    }

    private boolean shouldPath(BlockPos pos)
    {
        Path current = noPath() ? null : getPath();
        if (current != null && !pokemob.getPokemonAIState(IMoveConstants.ANGRY))
        {
            Vector3 p = v.set(current.getFinalPathPoint());
            Vector3 v = v1.set(pos);
            if (p.distToSq(v) <= 1) { return false; }
        }
        return true;
    }

    /** If on ground or swimming and can swim */
    @Override
    public boolean canNavigate()
    {
        if (pokemob.getPokemonAIState(IPokemob.SLEEPING) || pokemob.getStatus() == IPokemob.STATUS_SLP
                || pokemob.getStatus() == IPokemob.STATUS_FRZ || pokemob.getPokemonAIState(IMoveConstants.CONTROLLED)
                || pokemob.getPokemonAIState(IMoveConstants.NOPATHING))
            return false;
        if (pokemob.getPokemonAIState(IPokemob.SITTING)) return false;
        return this.entity.onGround || this.isInLiquid() || this.canFly;
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

    @Override
    public Path getPathToEntityLiving(Entity entityIn)
    {
        checkValues();
        return wrapped.getPathToEntityLiving(entityIn);
    }

    @Override
    public Path getPathToPos(BlockPos pos)
    {
        checkValues();
        if (shouldPath(pos))
        {
            return wrapped.getPathToPos(pos);
        }
        else return getPath();
    }

    @Override
    protected PathFinder getPathFinder()
    {
        if (pokemob != null) checkValues();
        if (wrapped != null) return wrapped.pathFinder;
        return null;
    }

    @Override
    protected Vec3d getEntityPosition()
    {
        return new Vec3d(this.entity.posX, this.entity.posY, this.entity.posZ);
    }
}