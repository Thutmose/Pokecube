package pokecube.core.ai.utils;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.init.Blocks;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.World;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod.Type;
import thut.api.maths.Vector3;
import thut.api.pathing.IPathingMob;
import thut.api.pathing.Paths;

public class PokeNavigator extends PathNavigate {
    private final EntityLiving theEntity;
    private final World worldObj;
    /** The PathEntity being followed. */
    private PathEntity currentPath;
    private double speed;
	Vector3 v = Vector3.getNewVectorFromPool();
	Vector3 v1 = Vector3.getNewVectorFromPool();
	Vector3 v2 = Vector3.getNewVectorFromPool();
	Vector3 v3 = Vector3.getNewVectorFromPool();
    /**
     * The number of blocks (extra) +/- in each axis that get pulled out as cache for the pathfinder's search space
     */
    private final IAttributeInstance pathSearchRange;
    private boolean noSunPathfind;
    /** Time, in number of ticks, following the current path */
    private int totalTicks;
    /**
     * The time when the last position check was done (to detect successful movement)
     */
    private int ticksAtLastPos;
    /**
     * Coordinates of the entity's position last time a check was done (part of monitoring getting 'stuck')
     */
    private Vec3 lastPosCheck = new Vec3(0.0D, 0.0D, 0.0D);
    /**
     * Specifically, if a wooden door block is even considered to be passable by the pathfinder
     */
    private boolean canPassOpenWoodenDoors = true;
    /** If door blocks are considered passable even when closed */
    private boolean canPassClosedWoodenDoors;
    /** If water blocks are avoided (at least by the pathfinder) */
    private boolean avoidsWater;
    /**
     * If the entity can swim. Swimming AI enables this and the pathfinder will also cause the entity to swim straight
     * upwards when underwater
     */
    private boolean canSwim;
    
    private boolean canDive;
    
    private boolean canFly;
    public final Paths pathfinder;
    
    final IPokemob pokemob;
    
    long lastCacheUpdate = 0;

    public PokeNavigator(EntityLiving entity, World world)
    {
    	super(entity, world);
        this.theEntity = entity;
        this.worldObj = world;
        this.pathSearchRange = entity.getEntityAttribute(SharedMonsterAttributes.followRange);
        pokemob = (IPokemob) entity;
        canSwim = true;
        canDive = ((IPathingMob)entity).swims();
        pathfinder = new Paths(world);
    }
    
    public void refreshCache()
    {
    	if(pathfinder.cacheLock[1])
    		return;
        long time = System.nanoTime();
        pathfinder.cacheLock[0] = true;
        if(lastCacheUpdate==0||(time - lastCacheUpdate > 1000000000))
        {
            int i = MathHelper.floor_double(theEntity.posX);
            int j = MathHelper.floor_double(theEntity.posY + 1.0D);
            int k = MathHelper.floor_double(theEntity.posZ);
            int l = (int)(pathSearchRange.getAttributeValue() + 16.0F);
            int i1 = i - l;
            int j1 = j - l;
            int k1 = k - l;
            int l1 = i + l;
            int i2 = j + l;
            int j2 = k + l;
        	pathfinder.chunks = new ChunkCache(worldObj, new BlockPos(i1, j1, k1), new BlockPos(l1, i2, j2), 0);
        	lastCacheUpdate = System.nanoTime();
        	
        }
        pathfinder.cacheLock[0] = false;
    }
    /**
     * Sets the speed
     */
    @Override
	public void setSpeed(double speed)
    {
        this.speed = speed;
    }

    /**
     * Gets the maximum distance that the path finding will search in.
     */
    @Override
	public float getPathSearchRange()
    {
        return (float)this.pathSearchRange.getAttributeValue();
    }
    
    @Override
    public PathEntity getPathToPos(BlockPos pos)
    {
      PokedexEntry entry = pokemob.getPokedexEntry();
      this.canFly = entry.mobType == Type.FLYING || entry.mobType == Type.FLOATING;
      this.canDive = entry.mobType == Type.WATER;
      PathEntity current = currentPath;
	  	if(current!=null && !pokemob.getPokemonAIState(IPokemob.ANGRY))
	  	{
		    	Vector3 p = v.set(current.getFinalPathPoint());
				Vector3 v = v1.set(pos);
				if(p.distToSq(v)<=1)
				{
					return current;
				}
	  	}
      return pathfinder.getEntityPathToXYZ(this.theEntity, pos.getX(), pos.getY(), pos.getZ(), this.getPathSearchRange(), this.canPassOpenWoodenDoors, this.canPassClosedWoodenDoors, this.avoidsWater, this.canFly, this.canSwim);
 
    }

    /**
     * Try to find and set a path to XYZ. Returns true if successful.
     */
    @Override
	public boolean tryMoveToXYZ(double x, double y, double z, double speed)
    {
        PathEntity pathentity = this.getPathToXYZ(MathHelper.floor_double(x), ((int)y), MathHelper.floor_double(z));
        return this.setPath(pathentity, speed);
    }

    /**
     * Returns the path to the given EntityLiving
     */
    @Override
	public PathEntity getPathToEntityLiving(Entity entity)
    {
        PokedexEntry entry = pokemob.getPokedexEntry();
        this.canFly = entry.mobType == Type.FLYING || entry.mobType == Type.FLOATING;
        this.canDive = entry.mobType == Type.WATER;
        return !this.canNavigate() ? null : pathfinder.getPathEntityToEntity(this.theEntity, entity, this.getPathSearchRange(), this.canPassOpenWoodenDoors, this.canPassClosedWoodenDoors, this.avoidsWater, this.canFly, this.canSwim);
    }

    /**
     * Try to find and set a path to EntityLiving. Returns true if successful.
     */
    @Override
	public boolean tryMoveToEntityLiving(Entity entity, double speed)
    {
        PathEntity pathentity = this.getPathToEntityLiving(entity);
        return pathentity != null ? this.setPath(pathentity, speed) : false;
    }

    /**
     * sets the active path data if path is 100% unique compared to old path, checks to adjust path for sun avoiding
     * ents and stores end coords
     */
    @Override
	public boolean setPath(PathEntity path, double speed)
    {

		if(path==currentPath)
			return true;
        if (path == null)
        {
            this.currentPath = null;
            return false;
        }
        else
        {
            if (!path.isSamePath(this.currentPath))
            {
                this.currentPath = path;
            }

            if (this.noSunPathfind)
            {
                this.removeSunnyPath();
            }

            if (this.currentPath.getCurrentPathLength() == 0)
            {
                return false;
            }
            else
            {
                this.speed = speed;
                Vec3 vec3 = this.getEntityPosition();
                this.ticksAtLastPos = this.totalTicks;
                lastPosCheck = vec3;
                return true;
            }
        }
    }

    /**
     * gets the actively used PathEntity
     */
    @Override
	public PathEntity getPath()
    {
        return this.currentPath;
    }

    @Override
	public void onUpdateNavigation()
    {
        ++this.totalTicks;

        if (!this.noPath())
        {
            if (this.canNavigate())
            {
                try
				{
					this.pathFollow();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
            }

            if (!this.noPath())
            {
                Vec3 vec3 = this.currentPath.getPosition(this.theEntity);
                
                float f = this.theEntity.width;
                f = Math.max(f, 0.5f);
                v.set(theEntity);
                v1.set(currentPath.getFinalPathPoint());

//            	System.out.println(vec3+" "+v.distTo(v1));
                if(v.distTo(v1)<f)
                {                
                	this.clearPathEntity();
                	return;
                }
                
                if (vec3 != null)
                {
                	double speed = this.speed;
                	if(canDive && this.isInLiquid())
                	{
                		speed *= 2;
                	}
                    this.theEntity.getMoveHelper().setMoveTo(vec3.xCoord, vec3.yCoord, vec3.zCoord, speed);
                }
            }
        }
    }

    @Override
    public void pathFollow()
    {
        Vec3 vec3 = this.getEntityPosition();
        int i = this.currentPath.getCurrentPathLength();
        
        float f = this.theEntity.width * this.theEntity.width;
        f = Math.max(f, 0.5f);
        v.set(theEntity);
        v1.set(currentPath.getFinalPathPoint());
        if(v.distTo(v1)<f)
        {                
        	this.clearPathEntity();
        	return;
        }
        f = Math.max(f,  1.5f);
        this.currentPath.setCurrentPathIndex(getNextPoint());
        
        if(!((canFly || canSwim && theEntity.isInWater()) && !theEntity.onGround))
        for (int j = this.currentPath.getCurrentPathIndex(); j < this.currentPath.getCurrentPathLength(); ++j)
        {
            if (this.currentPath.getPathPointFromIndex(j).yCoord != (int)vec3.yCoord)
            {
                i = j;
                break;
            }
        }
        
        int k;

        for (k = this.currentPath.getCurrentPathIndex(); k < i; ++k)
        {
            if (vec3.squareDistanceTo(this.currentPath.getVectorFromIndex(this.theEntity, k)) < f)
            {
                this.currentPath.setCurrentPathIndex(k + 1);
            }
        }
        

        k = MathHelper.ceiling_float_int(this.theEntity.width);
        int l = (int)this.theEntity.height + 1;
        int i1 = k;
        for (int j1 = i - 1; j1 >= this.currentPath.getCurrentPathIndex(); --j1)
        {
        	if (this.isDirectPathBetweenPoints(vec3, this.currentPath.getVectorFromIndex(this.theEntity, j1), k, l, i1))
            {
            	this.currentPath.setCurrentPathIndex(j1);
                break;
            }
        }
        
        if(lastPosCheck.distanceTo(vec3)>f)
        {
            this.ticksAtLastPos = this.totalTicks;
            lastPosCheck = vec3;
        }
        
        int tickDiff = this.totalTicks - this.ticksAtLastPos;
        
        if (tickDiff > (pokemob.getPokemonAIState(IMoveConstants.IDLE)?25 * speed:50 * speed))
        {
//        	if(theEntity.isInWater())
//        		f /= 5;
            if (vec3.squareDistanceTo(this.lastPosCheck) < f)
            {
            	sticks ++;
                this.clearPathEntity();
            }

            this.ticksAtLastPos = this.totalTicks;
            lastPosCheck = vec3;
        }
        
        if(sticks > 100)
        {
        	//System.err.println(theEntity+" Has been stuck trying to path quite a lot");
        	sticks = 0;
      //  	new Exception().printStackTrace();
        }
        
    }
    
    int sticks = 0;

    /**
     * If null path or reached the end
     */
    @Override
	public boolean noPath()
    {
        return this.currentPath == null || this.currentPath.isFinished();
    }

    /**
     * sets active PathEntity to null
     */
    @Override
	public synchronized void clearPathEntity()
    {
//    	if(!((IPokemob)theEntity).getPokemonAIState(IPokemob.TAMED))
//    	new Exception().printStackTrace();
        this.currentPath = null;
    }

    @Override
    public Vec3 getEntityPosition()
    {
        return new Vec3(this.theEntity.posX, this.getPathableYPos(), this.theEntity.posZ);
    }

    /**
     * Gets the safe pathing Y position for the entity depending on if it can path swim or not
     */
    private int getPathableYPos()
    {
    	boolean inWater = this.theEntity.isInWater();
        if(canDive && inWater)
        {
        	return (int)(this.theEntity.posY + 0.5D);
        }
        else if (canFly && !inWater)
        {
        	return (int)(this.theEntity.posY + 0.5D);
        }
        else if (inWater && this.canSwim)
        {
            int i = (int)this.theEntity.posY;
            Block block = this.worldObj.getBlockState(new BlockPos(MathHelper.floor_double(this.theEntity.posX), i, MathHelper.floor_double(this.theEntity.posZ))).getBlock();
            int j = 0;

            do
            {
                if (block != Blocks.flowing_water && block != Blocks.water)
                {
                    return i;
                }

                ++i;
                block = this.worldObj.getBlockState(new BlockPos(MathHelper.floor_double(this.theEntity.posX), i, MathHelper.floor_double(this.theEntity.posZ))).getBlock();
                ++j;
            }
            while (j <= 16);

            return (int)this.theEntity.posY;
        }
        return (int)(this.theEntity.posY + 0.5D);
        
    }

    /**
     * If on ground or swimming and can swim
     */
    public boolean canNavigate()
    {
        return this.theEntity.onGround || this.canSwim && this.isInLiquid() || this.canFly;
    }

    /**
     * Returns true if the entity is in water or lava, false otherwise
     */
    public boolean isInLiquid()
    {
        return this.theEntity.isInWater();// || worldObj.isMaterialInBB(theEntity.boundingBox.expand(-0.10000000149011612D, -0.4000000059604645D, -0.10000000149011612D), Material.lava);
    }

    /**
     * Trims path data from the end to the first sun covered block
     */
    public void removeSunnyPath()
    {
        if (!this.worldObj.canBlockSeeSky(new BlockPos(MathHelper.floor_double(this.theEntity.posX), (int)(this.theEntity.posY + 0.5D), MathHelper.floor_double(this.theEntity.posZ))))
        {
            for (int i = 0; i < this.currentPath.getCurrentPathLength(); ++i)
            {
                PathPoint pathpoint = this.currentPath.getPathPointFromIndex(i);

                if (this.worldObj.canBlockSeeSky(new BlockPos(pathpoint.xCoord, pathpoint.yCoord, pathpoint.zCoord)))
                {
                    this.currentPath.setCurrentPathLength(i - 1);
                    return;
                }
            }
        }
    }

    /**
     * Returns true when an entity of specified size could safely walk in a straight line between the two points. Args:
     * pos1, pos2, entityXSize, entityYSize, entityZSize
     */
    public boolean isDirectPathBetweenPoints(Vec3 start, Vec3 end, int sizeX, int sizeY, int sizeZ)
    {
        double d0 = end.xCoord - start.xCoord;
        double d1 = end.zCoord - start.zCoord;
        double dy = end.yCoord - start.yCoord;
        double d2 = d0 * d0 + d1 * d1 + dy * dy;

        if (d2 < 1.0E0D || !canFly)
        {
            return true;
        }
        else
        {
            v.set(start);
            v1.set(end);//TODO re-do this using safe checks
            return v1.isVisible(worldObj, v);
        }
    }
    
    private int getNextPoint()
    {
    	int index = currentPath.getCurrentPathIndex();
    	
    	if(index + 1 >= currentPath.getCurrentPathLength() || index == 0)
    		return index;
    	
    	PathPoint current = currentPath.getPathPointFromIndex(index-1);
    	PathPoint next = currentPath.getPathPointFromIndex(index);
    	v.set(current);
    	v1.set(next);
    	v2.set(v.subtractFrom(v1));
    	while(index + 1 < currentPath.getCurrentPathLength())
    	{
    		current = currentPath.getPathPointFromIndex(index);
    		next = currentPath.getPathPointFromIndex(index+1);
    		if(!v2.equals(v.set(current).subtractFrom(v1.set(next))))
    		{
    			return index;
    		}
    		index++;
    	}
    	return index;
    }
    
    @Override
	protected PathFinder getPathFinder() {
		// TODO Auto-generated method stub
		return null;
	}

}
