package pokecube.core.ai.utils.pathing;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.World;

public abstract class PathNavigate2 extends PathNavigate
{
    private ChunkCache cache;
    private BlockPos   lastCachePos;
    private long       lastCacheTime = -1;
    private Object     lock          = new Object();

    public PathNavigate2(EntityLiving entityIn, World worldIn)
    {
        super(entityIn, worldIn);
    }

    public void updateCache()
    {
        synchronized (lock)
        {
            BlockPos blockpos = new BlockPos(this.entity);

            // Only update the cache if the mob has moved more than 4 blocks, or
            // it has been more than a second.
            if (lastCachePos != null && cache != null)
            {
                long dt = this.world.getTotalWorldTime() - lastCacheTime;
                if (dt < 20 && lastCachePos.distanceSq(blockpos) < 16) { return; }
            }
            float f = this.getPathSearchRange();
            int i = (int) (f + 16.0F);
            lastCachePos = blockpos;
            lastCacheTime = this.world.getTotalWorldTime();
            cache = new ChunkCache(this.world, blockpos.add(-i, -i, -i), blockpos.add(i, i, i), 0);
        }
    }

    /** Returns path to given BlockPos */
    @Nullable
    @Override
    public Path getPathToPos(BlockPos pos)
    {
        if (!this.canNavigate() || cache == null)
        {
            return null;
        }
        else if (this.currentPath != null && !this.currentPath.isFinished() && pos.equals(this.targetPos))
        {
            return this.currentPath;
        }
        else
        {
            this.targetPos = pos;
            float f = this.getPathSearchRange();
            this.world.profiler.startSection("pathfind");
            Path path = null;
            synchronized (lock)
            {
                path = this.pathFinder.findPath(cache, this.entity, this.targetPos, f);
            }
            this.world.profiler.endSection();
            return path;
        }
    }

    /** Returns the path to the given EntityLiving. Args : entity */
    @Nullable
    @Override
    public Path getPathToEntityLiving(Entity entityIn)
    {
        if (!this.canNavigate() || cache == null)
        {
            return null;
        }
        else
        {
            BlockPos blockpos = new BlockPos(entityIn);
            if (this.currentPath != null && !this.currentPath.isFinished() && blockpos.equals(this.targetPos))
            {
                return this.currentPath;
            }
            else
            {
                this.targetPos = blockpos;
                float f = this.getPathSearchRange();
                this.world.profiler.startSection("pathfind");
                Path path = null;
                synchronized (lock)
                {
                    path = this.pathFinder.findPath(cache, this.entity, entityIn, f);
                }
                this.world.profiler.endSection();
                return path;
            }
        }
    }

}
