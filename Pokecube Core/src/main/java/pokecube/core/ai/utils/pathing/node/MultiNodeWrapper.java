package pokecube.core.ai.utils.pathing.node;

import net.minecraft.entity.EntityLiving;
import net.minecraft.pathfinding.NodeProcessor;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.world.IBlockAccess;
import pokecube.core.ai.utils.pathing.MultiNodeNavigator;

public class MultiNodeWrapper extends NodeProcessor
{
    final MultiNodeNavigator  navi;
    private final PathPoint[] pathOptionsA = new PathPoint[32];
    private final PathPoint[] pathOptionsB = new PathPoint[32];

    public MultiNodeWrapper(MultiNodeNavigator multiNodeNavigator)
    {
        this.navi = multiNodeNavigator;
    }

    @Override
    public void init(IBlockAccess sourceIn, EntityLiving mob)
    {
        super.init(sourceIn, mob);
        navi.a.init(sourceIn, mob);
        navi.b.init(sourceIn, mob);
    }

    @Override
    public void postProcess()
    {
        super.postProcess();
        navi.a.postProcess();
        navi.b.postProcess();
    }

    @Override
    public PathPoint getStart()
    {
        PathPoint a = navi.a.getStart();
        return a;
    }

    @Override
    public PathPoint getPathPointToCoords(double x, double y, double z)
    {
        PathPoint a = navi.a.getPathPointToCoords(x, y, z);
        if (a == null) a = navi.b.getPathPointToCoords(x, y, z);
        return a;
    }

    @Override
    public int findPathOptions(PathPoint[] pathOptions, PathPoint currentPoint, PathPoint targetPoint,
            float maxDistance)
    {
        int a = navi.a.findPathOptions(pathOptionsA, currentPoint, targetPoint, maxDistance);
        int b = navi.b.findPathOptions(pathOptionsB, currentPoint, targetPoint, maxDistance);
        int num = Math.min(a + b, 32);
        for (int i = 0; i < a; i++)
        {
            pathOptions[i] = pathOptionsA[i];
            if (pathOptions[i] == null) throw new IllegalArgumentException("Error with finding Path Options!");
        }
        for (int i = a; i < num; i++)
        {
            pathOptions[i] = pathOptionsB[i - a];
            if (pathOptions[i] == null) throw new IllegalArgumentException("Error with finding Path Options!");
        }
        return num;
    }

    @Override
    public PathNodeType getPathNodeType(IBlockAccess blockaccessIn, int x, int y, int z, EntityLiving entitylivingIn,
            int xSize, int ySize, int zSize, boolean canBreakDoorsIn, boolean canEnterDoorsIn)
    {
        PathNodeType a = navi.a.getPathNodeType(blockaccessIn, x, y, z, entitylivingIn, xSize, ySize, zSize,
                canBreakDoorsIn, canEnterDoorsIn);
        if (a == PathNodeType.WALKABLE || a == PathNodeType.OPEN) { return a; }
        PathNodeType b = navi.b.getPathNodeType(blockaccessIn, x, y, z, entitylivingIn, xSize, ySize, zSize,
                canBreakDoorsIn, canEnterDoorsIn);
        if (b == PathNodeType.WALKABLE || b == PathNodeType.OPEN) { return b; }
        return a;
    }

    @Override
    public PathNodeType getPathNodeType(IBlockAccess blockaccessIn, int x, int y, int z)
    {
        PathNodeType a = navi.a.getPathNodeType(blockaccessIn, x, y, z);
        if (a == PathNodeType.WALKABLE) { return a; }
        PathNodeType b = navi.b.getPathNodeType(blockaccessIn, x, y, z);
        if (b == PathNodeType.WALKABLE) { return b; }
        if (a == PathNodeType.OPEN) { return a; }
        if (b == PathNodeType.OPEN) { return b; }
        return null;
    }

}
