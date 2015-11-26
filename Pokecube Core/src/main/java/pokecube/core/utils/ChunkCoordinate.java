package pokecube.core.utils;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import thut.api.maths.Vector3;

public class ChunkCoordinate extends BlockPos {

	public int dim;
	
	public static ChunkCoordinate getChunkCoordFromWorldCoord(int x, int y, int z, int dim)
	{
	       int i = MathHelper.floor_double(x / 16.0D);
	        int j = MathHelper.floor_double(y / 16.0D);
	        int k = MathHelper.floor_double(z / 16.0D);
	        return new ChunkCoordinate(i, j, k, dim);
	}
	
	public ChunkCoordinate(int p_i1354_1_, int p_i1354_2_, int p_i1354_3_, int dim) {
		super(p_i1354_1_, p_i1354_2_, p_i1354_3_);
		this.dim = dim;
	}

	public ChunkCoordinate(Vector3 v, int dim) {
		super(v.intX(), v.intY(), v.intZ());
		this.dim = dim;
	}

    public ChunkCoordinate(BlockPos pos, int dimension) {
		this(pos.getX(), pos.getY(), pos.getZ(), dimension);
	}

	@Override
	public boolean equals(Object p_equals_1_)
    {
        if (!(p_equals_1_ instanceof ChunkCoordinate))
        {
            return false;
        }
        else
        {
            ChunkCoordinate BlockPos = (ChunkCoordinate)p_equals_1_;
            return this.getX() == BlockPos.getX() 
            		&& this.getY() == BlockPos.getY() 
            		&& this.getZ() == BlockPos.getZ()
            		&& this.dim == BlockPos.dim;
        }
    }

    public void writeToBuffer(ByteBuf buffer)
    {
    	buffer.writeInt(getX());
    	buffer.writeInt(getY());
    	buffer.writeInt(getZ());
    	buffer.writeInt(dim);
    }
    
    @Override
	public int hashCode()
    {
        return this.getX() + this.getZ() << 8 + this.getY() << 16 + this.dim << 24;
    }

	public static ChunkCoordinate getChunkCoordFromWorldCoord(BlockPos pos, int dimension) {
		return getChunkCoordFromWorldCoord(pos.getX(), pos.getY(), pos.getZ(), dimension);
	}

//    @Override
//	public int compareTo(BlockPos p_compareTo_1_)
//    {
//    	if(p_compareTo_1_ instanceof ChunkCoordinate)
//    	{
//    		ChunkCoordinate c = (ChunkCoordinate) p_compareTo_1_;
//    		if(this.dim!=c.dim)
//    			return this.dim - c.dim;
//    	}
//    	
//        return this.getY() == p_compareTo_1_.getY() ? 
//        		(this.getZ() == p_compareTo_1_.getZ() ? 
//        				this.getX() - p_compareTo_1_.getX() : this.getZ() - p_compareTo_1_.getZ()) : this.getY() - p_compareTo_1_.getY();
//    }
}
