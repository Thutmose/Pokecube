package pokecube.core.utils;

import org.lwjgl.opengl.GL11;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;

public class Vector4 
{
	public float x,y,z,w;
	
	public Vector4()
	{
		y=z=w=0;
		x = 1;
	}
	
	public Vector4(double posX, double posY, double posZ, float w)
	{
		this.x = (float) posX;
		this.y = (float) posY;
		this.z = (float) posZ;
		this.w = w;
	}
	
	public Vector4(String toParse)
	{
		String[] vals = toParse.split(" ");
		if(vals.length==4)
		{
			this.x = Float.parseFloat(vals[0]);
			this.y = Float.parseFloat(vals[1]);
			this.z = Float.parseFloat(vals[2]);
			this.w = Float.parseFloat(vals[3]);
		}
	}
	
	public Vector4 set(float x, float y, float z, float w)
	{
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
		return this;
	}
	
	public Vector4 addAngles(Vector4 toAdd)
	{
		Vector4 ret = copy();
		Vector4 temp = new Vector4(toAdd.x, toAdd.y, toAdd.z, toAdd.w);
		
		if(Float.isNaN(temp.x)||Float.isNaN(temp.y)||Float.isNaN(temp.z))
		{
			temp.x = 0; temp.y = 0; temp.z = 1; temp.w = 0;
		}
		temp.toQuaternion();
		ret.toQuaternion();

		return (ret.add(temp)).toAxisAngle();
	}
	
	public Vector4 add(Vector4 b)
	{
		Vector4 quat = new Vector4();
		quat.w = w*b.w - x*b.x - y*b.y - z*b.z;
		
		quat.x	= b.w*x + b.x*w + b.y*z - b.z*y;
		quat.y	= b.w*y + b.y*w + b.z*x - b.x*z;
		quat.z	= b.w*z + b.z*w + b.x*y - b.y*x;
		
		
		return quat;
	}
	
	public Vector4 subtractAngles(Vector4 toAdd)
	{
		Vector4 temp = new Vector4(toAdd.x, toAdd.y, toAdd.z, -toAdd.w);
		return addAngles(temp);
	}
	
	public Vector4 scalarMult(float scalar)
	{
		Vector4 ret = new Vector4(x,y,z,w);
		ret.w = w*scalar;
		return ret;
	}
	
	public Vector4 copy()
	{
		return new Vector4(x,y,z,w);
	}
	
	public void glRotate()
	{
		GL11.glRotatef(w, x, y, z);
	}
	
	public void glRotateMinus()
	{
		GL11.glRotatef(-w, x, y, z);
	}
	
	public Vector4 toQuaternion()
	{//q = cos(angle/2) + i ( x * sin(angle/2)) + j (y * sin(angle/2)) + k ( z * sin(angle/2))
		
		double aw = Math.toRadians(w);
		float ax = x;
		float ay = y;
		float az = z;
		
		this.w = (float) Math.cos(aw/2);
		this.x = (float) (ax*Math.sin(aw/2));
		this.y = (float) (ay*Math.sin(aw/2));
		this.z = (float) (az*Math.sin(aw/2));
		
		return this;
	}
	
	public Vector4 normalize()
	{
		float s = x*x+y*y+z*z+w*w;
		s = (float) Math.sqrt(s);
		x /= s;
		y /= s;
		z /= s;
		w /= s;
		
		return this;
	}
	
	/**
	 * The default is axis angle for use with openGL
	 * @return
	 */
	public Vector4 toAxisAngle()
	{
		float qw = w;
		float qx = x;
		float qy = y;
		float qz = z;
		
		if(w == 0)
		{
			x = 1; y = 0; z = 0;
			return this;
		}
		
		w = (float) Math.toDegrees((2 * Math.acos(qw)));
		float s = (float) Math.sqrt(1-qw*qw);
		
		if(s==0)
		{
		//	System.err.println("Error "+this);
		//	new Exception().printStackTrace();
		}
		
		if(s>0.0001f)
		{
			x = qx / s;
			y = qy / s;
			z = qz / s;
		}

		x = (float) (x/Math.sqrt(x*x+y*y+z*z));
		y = (float) (y/Math.sqrt(x*x+y*y+z*z));
		z = (float) (z/Math.sqrt(x*x+y*y+z*z));
		
		
		return this;
	}
	
	public void writeToNBT(NBTTagCompound nbt)
	{
		nbt.setFloat("x", x);
		nbt.setFloat("y", y);
		nbt.setFloat("z", z);
		nbt.setFloat("w", w);
	}
	
	public Vector4(Entity e)
	{
		this(e.posX, e.posY, e.posZ, e.dimension);
	}
	
	public Vector4(NBTTagCompound nbt)
	{
		this();
		x = nbt.getFloat("x");
		y = nbt.getFloat("y");
		z = nbt.getFloat("z");
		w = nbt.getFloat("w");
	}
	
	public boolean withinDistance(float distance, Vector4 toCheck)
	{
		if (	  
				(int)w == (int)toCheck.w
				&& toCheck.x >= x - distance
				&& toCheck.z >= z - distance
				&& toCheck.y >= y - distance
				&& toCheck.y <= y + distance
				&& toCheck.x <= x + distance
				&& toCheck.z <= z + distance
				) {
			return true;
		}
		
		return false;
	}
	
	@Override
	public boolean equals(Object o)
	{
		if(o instanceof Vector4)
		{
			Vector4 v = (Vector4) o;
			return v.x == x && v.y == y && v.z == z && v.w == w;
		}
		
		return super.equals(o);
	}
	
	public boolean isEmpty()
	{
		return x == 0 && z == 0 && y == 0;
	}
	
	@Override
	public String toString() {
		return "x:" + x + " y:" + y + " z:" + z +" w:"+w;
	}
	
	public String toIntString() {
		return "x:" + MathHelper.floor_double(x) + " y:" + MathHelper.floor_double(y) + " z:" + MathHelper.floor_double(z) + " w:" + MathHelper.floor_double(w);
	}
}
