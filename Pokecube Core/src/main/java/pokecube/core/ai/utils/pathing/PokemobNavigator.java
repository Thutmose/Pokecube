package pokecube.core.ai.utils.pathing;

import java.util.logging.Level;

import net.minecraft.entity.Entity;
import net.minecraft.pathfinding.FlyingNodeProcessor;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.pathfinding.SwimNodeProcessor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import pokecube.core.ai.utils.pathing.node.WalkNodeLadderProcessor;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.interfaces.pokemob.ai.LogicStates;
import thut.api.maths.Vector3;

/** This is overridden from the vanilla one to allow using a custom,
 * multi-threaded pathfinder. It also does some pokemob specific checks for
 * whether the pokemob can navigate, as well as checks to see if the pathing
 * should terminate early in certain situations, such as "low priority" paths
 * which are close enough to the end, like when a mob is just idling around. */
public class PokemobNavigator extends PathNavigate2
{
    private Vector3        v                 = Vector3.getNewVector();
    private Vector3        v1                = Vector3.getNewVector();
    private boolean        canDive;
    private boolean        canFly;
    private final IPokemob pokemob;
    private PokedexEntry   lastEntry;
    private PathNavigate2  wrapped;
    private boolean        lastGroundedState = false;

    public PokemobNavigator(IPokemob pokemob, World world)
    {
        super(pokemob.getEntity(), world);
        this.entity = pokemob.getEntity();
        this.world = world;
        this.pokemob = pokemob;
        canDive = pokemob.swims();
    }

    @Override
    public void updateCache()
    {
        if (wrapped != null) this.wrapped.updateCache();
    }

    @Override
    public void onUpdateNavigation()
    {
        updateCache();
        super.onUpdateNavigation();
    }

    private PathNavigate2 makeSwimingNavigator()
    {
        return new MultiNodeNavigator(entity, world, new SwimNodeProcessor(), new WalkNodeLadderProcessor(), canFly);
    }

    private PathNavigate2 makeFlyingNavigator()
    {
        return new MultiNodeNavigator(entity, world, new FlyingNodeProcessor(), new WalkNodeLadderProcessor(), canFly);
    }

    private void checkValues()
    {
        PokedexEntry entry = pokemob.getPokedexEntry();
        IPokemob transformed = CapabilityPokemob.getPokemobFor(pokemob.getTransformedTo());
        if (transformed != null)
        {
            entry = transformed.getPokedexEntry();
        }
        if (entry != lastEntry || lastGroundedState != pokemob.isGrounded() || wrapped == null)
        {
            lastGroundedState = pokemob.isGrounded();
            lastEntry = entry;
            this.canFly = pokemob.flys() || pokemob.floats();
            this.canFly = this.canFly && !lastGroundedState;
            this.canDive = entry.swims();
            if (this.canDive && this.canFly) wrapped = new MultiNodeNavigator(entity, world, new FlyingNodeProcessor(),
                    makeSwimingNavigator().getNodeProcessor(), canFly);
            if (this.canFly && !canDive) wrapped = makeFlyingNavigator();
            else if (canDive) wrapped = makeSwimingNavigator();
            else
            {
                wrapped = new LadderWalkNavigator(entity, world, canFly);
            }
        }
        wrapped.getNodeProcessor().setCanEnterDoors(true);
        wrapped.getNodeProcessor().setCanSwim(true);
        wrapped.getNodeProcessor().init(world, entity);
        wrapped.setSpeed(speed);
    }

    private boolean shouldPath(BlockPos pos)
    {
        Path current = noPath() ? null : getPath();
        if (current != null && !pokemob.getCombatState(CombatStates.ANGRY))
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
        if (wrapped == null) checkValues();
        if (pokemob.getLogicState(LogicStates.SLEEPING) || (pokemob.getStatus() & IPokemob.STATUS_SLP) > 0
                || (pokemob.getStatus() & IPokemob.STATUS_FRZ) > 0
                || pokemob.getGeneralState(GeneralStates.CONTROLLED)
                || pokemob.getLogicState(LogicStates.NOPATHING))
            return false;
        if (pokemob.getLogicState(LogicStates.SITTING)) return false;
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

        // TODO check to see that all blocks in direction have same path
        // weighting
        if (ground) { return false; }
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
        if (!this.canNavigate() || entityIn == null) { return null; }
        checkValues();
        try
        {
            return wrapped.getPathToEntityLiving(entityIn);
        }
        catch (Exception e)
        {
            PokecubeMod.log(Level.SEVERE, "Error making path for " + this.entity + " to " + entityIn + " " + wrapped,
                    e);
            return null;
        }
    }

    @Override
    public Path getPathToPos(BlockPos pos)
    {
        if (!this.canNavigate() || pos == null) { return null; }
        checkValues();
        if (shouldPath(pos))
        {
            try
            {
                return wrapped.getPathToPos(pos);
            }
            catch (Exception e)
            {
                PokecubeMod.log(Level.SEVERE, "Error making path for " + this.entity + " to " + pos + " " + wrapped, e);
                return null;
            }
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
