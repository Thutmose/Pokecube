package pokecube.core.ai.utils.pathing.node;

import net.minecraft.block.state.IBlockState;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.pathfinding.WalkNodeProcessor;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class WalkNodeLadderProcessor extends WalkNodeProcessor
{

    @Override
    public int findPathOptions(PathPoint[] pathOptions, PathPoint currentPoint, PathPoint targetPoint,
            float maxDistance)
    {
        int i = super.findPathOptions(pathOptions, currentPoint, targetPoint, maxDistance);
        PathPoint pathpoint = this.getPoint(currentPoint.x, currentPoint.y + 1, currentPoint.z, EnumFacing.UP);
        if (pathpoint != null && !pathpoint.visited && pathpoint.distanceTo(targetPoint) < maxDistance)
        {
            pathOptions[i++] = pathpoint;
        }
        pathpoint = this.getPoint(currentPoint.x, currentPoint.y - 1, currentPoint.z, EnumFacing.DOWN);
        if (pathpoint != null && !pathpoint.visited && pathpoint.distanceTo(targetPoint) < maxDistance)
        {
            pathOptions[i++] = pathpoint;
        }
        for (EnumFacing side : EnumFacing.HORIZONTALS)
        {
            pathpoint = this.getPoint(currentPoint.x + side.getFrontOffsetX(), currentPoint.y - 1,
                    currentPoint.z + side.getFrontOffsetZ(), EnumFacing.DOWN);
            if (pathpoint != null && !pathpoint.visited && pathpoint.distanceTo(targetPoint) < maxDistance)
            {
                pathOptions[i++] = pathpoint;
            }
        }
        return i;
    }

    private PathPoint getPoint(int x, int y, int z, EnumFacing direction)
    {
        if (direction == EnumFacing.UP) return getLadder(x, y, z);
        else if (direction == EnumFacing.DOWN) return getJumpOff(x, y, z);
        return null;
    }

    private PathPoint getJumpOff(int x, int y, int z)
    {
        for (int i = x; i < x + this.entitySizeX; ++i)
        {
            for (int j = y; j < y + this.entitySizeY; ++j)
            {
                for (int k = z; k < z + this.entitySizeZ; ++k)
                {
                    PathNodeType type = getPathNodeTypeRaw(blockaccess, i, j, k);
                    if (type != PathNodeType.OPEN && type != PathNodeType.WALKABLE) { return null; }
                }
            }
        }
        PathPoint point = openPoint(x, y, z);

        boolean laddar = false;
        for (EnumFacing dir : EnumFacing.HORIZONTALS)
        {
            laddar = laddar || getLadder(x + dir.getFrontOffsetX(), y, z + dir.getFrontOffsetZ()) != null;
        }
        point.nodeType = PathNodeType.OPEN;
        point.costMalus += laddar ? 1 : 5;
        return point;
    }

    private PathPoint getLadder(int x, int y, int z)
    {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int i = x; i < x + this.entitySizeX; ++i)
        {
            for (int j = y; j < y + this.entitySizeY; ++j)
            {
                for (int k = z; k < z + this.entitySizeZ; ++k)
                {
                    IBlockState iblockstate = this.blockaccess.getBlockState(pos.setPos(i, j, k));

                    if (iblockstate.getBlock().isLadder(iblockstate, blockaccess, pos, entity))
                    {
                        PathPoint point = openPoint(x, y, z);
                        point.nodeType = PathNodeType.OPEN;
                        return point;
                    }
                }
            }
        }
        return null;
    }
}
