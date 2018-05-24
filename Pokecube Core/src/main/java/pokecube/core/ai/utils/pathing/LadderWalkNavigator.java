package pokecube.core.ai.utils.pathing;

import net.minecraft.entity.EntityLiving;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import pokecube.core.ai.utils.pathing.node.WalkNodeLadderProcessor;
import thut.api.maths.Vector3;

public class LadderWalkNavigator extends PathNavigate2
{
    private final boolean canFly;

    public LadderWalkNavigator(EntityLiving entityIn, World worldIn, boolean canFly)
    {
        super(entityIn, worldIn);
        this.canFly = canFly;
    }

    @Override
    protected PathFinder getPathFinder()
    {
        this.nodeProcessor = new WalkNodeLadderProcessor();
        this.nodeProcessor.setCanEnterDoors(true);
        return new PathFinder(this.nodeProcessor);
    }

    @Override
    protected Vec3d getEntityPosition()
    {
        return new Vec3d(this.entity.posX, this.entity.posY, this.entity.posZ);
    }

    @Override
    protected boolean canNavigate()
    {
        return true;
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
}
