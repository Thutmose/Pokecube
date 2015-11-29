package pokecube.modelloader.client.custom.tbl;

import java.util.HashMap;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.model.ModelRenderer;
import pokecube.core.utils.Vector4;
import pokecube.modelloader.client.custom.IExtendedModelPart;
import thut.api.maths.Vector3;

public class TblObject implements IExtendedModelPart
{
	public HashMap<String, IExtendedModelPart>	childParts	= new HashMap<String, IExtendedModelPart>();
	public final String							name;

	public ModelRenderer model;

	public IExtendedModelPart parent = null;

	public Vector4	preRot		= new Vector4();
	public Vector4	postRot		= new Vector4();
	public Vector4	postRot2	= new Vector4();
	public Vector3	preTrans	= Vector3.getNewVectorFromPool();
	public Vector3	postTrans	= Vector3.getNewVectorFromPool();

	public Vector3	offset		= Vector3.getNewVectorFromPool();
	public Vector4	rotations	= new Vector4();
	public Vector3	scale		= Vector3.getNewVectorFromPool();

	public TblObject(String _name)
	{
		name = _name;
	}

	public int	red			= 255, green = 255, blue = 255, alpha = 255;
	public int	brightness	= 15728640;

	@Override
	public void addChild(IExtendedModelPart subPart)
	{
		if(subPart==this)
			return;
		this.childParts.put(subPart.getName(), subPart);
		subPart.setParent(this);
	}

	@Override
	public void setParent(IExtendedModelPart parent)
	{
		this.parent = parent;
	}

	@Override
	public void setPreTranslations(Vector3 point)
	{
		preTrans.set(point);
	}

	@Override
	public void setPostRotations(Vector4 angles)
	{
		postRot = angles;
	}

	@Override
	public void setPostTranslations(Vector3 point)
	{
		postTrans.set(point);
	}

	@Override
	public void setPreRotations(Vector4 angles)
	{
		preRot = angles;
	}

	public void render()
	{
		float factor = 0.0625f;

        Vector4 sum = null;
		if(getParent()!=null && getParent() instanceof TblObject)
		{
            
            ModelRenderer parents = ((TblObject)getParent()).model;
            
            float rX = parents.rotateAngleX, rY = parents.rotateAngleY, rZ = parents.rotateAngleZ;
            float pX = parents.rotationPointX * factor, pY = parents.rotationPointY * factor, pZ = parents.rotationPointZ * factor;

            GL11.glTranslatef(pX, pY, pZ);
            
            if(rZ!=0)
            {
            	Vector4 angleZ = new Vector4(0, 0, 1, (float) (rZ * 180/Math.PI));
            	sum = angleZ.copy();
            }
            if(rY!=0)
            {
            	Vector4 angleY = new Vector4(0, 1, 0, (float) (rY * 180/Math.PI));
            	if(sum==null)
            		sum = angleY.copy();
            	else
            		sum = sum.addAngles(angleY);
            }
            if(rX!=0)
            {
            	Vector4 angleX = new Vector4(1, 0, 0, (float) (rX * 180/Math.PI));
            	if(sum==null)
            		sum = angleX.copy();
            	else
            		sum = sum.addAngles(angleX);
            }
            
            if(sum!=null)
            {
                sum.glRotate();
            }
		}
		if(getParent()!=null && getParent() instanceof TblObject)
		{
            float pX = model.rotationPointX * factor, pY = model.rotationPointY * factor, pZ = model.rotationPointZ * factor;

            GL11.glTranslatef(pX, pY, pZ);
            
		}
		
		GL11.glTranslated(offset.x, offset.y, offset.z);
		GL11.glTranslated(preTrans.x, preTrans.y, preTrans.z);
		preRot.glRotate();
		GL11.glTranslated(postTrans.x, postTrans.y, postTrans.z);
		GL11.glTranslated(-offset.x, -offset.y, -offset.z);
        if(sum!=null)
        {
            sum.glRotateMinus();
        }
		postRot.glRotate();
		postRot2.glRotate();
        if(sum!=null)
        {
            sum.glRotate();
        }
		if(getParent()!=null && getParent() instanceof TblObject)
		{
            float pX = model.rotationPointX * factor, pY = model.rotationPointY * factor, pZ = model.rotationPointZ * factor;

            GL11.glTranslatef(-pX, -pY, -pZ);
            
		}
		
		GL11.glPushMatrix();		
		if(!scale.isEmpty())
			GL11.glScaled(scale.x, scale.y, scale.z);
		model.render(factor);
		GL11.glPopMatrix();
		
	}

	@Override
	public String getType()
	{
		return "tbl";
	}

	@Override
	public void renderAll()
	{
		render();
		for (IExtendedModelPart o : childParts.values())
		{
			GL11.glPushMatrix();
			GL11.glTranslated(offset.x, offset.y, offset.z);
			o.renderAll();
			GL11.glPopMatrix();
		}
	}

	@Override
	public void renderOnly(String... groupNames)
	{
		for (IExtendedModelPart o : childParts.values())
		{
			for (String s : groupNames)
			{
				if (s.equalsIgnoreCase(o.getName()))
				{
					o.renderOnly(groupNames);
				}
			}
		}
		for (String s : groupNames)
		{
			if (s.equalsIgnoreCase(name)) render();
		}
	}

	@Override
	public void renderPart(String partName)
	{
		if (this.name.equalsIgnoreCase(partName)) render();
		if (childParts.containsKey(partName)) childParts.get(partName).renderPart(partName);
	}

	@Override
	public void renderAllExcept(String... excludedGroupNames)
	{
		for (String s : childParts.keySet())
		{
			for (String s1 : excludedGroupNames)
				if (!s.equalsIgnoreCase(s1))
				{
					childParts.get(s).renderAllExcept(excludedGroupNames);
				}
		}
		for (String s1 : excludedGroupNames)
			if (s1.equalsIgnoreCase(name)) render();
	}

	@Override
	public int[] getRGBAB()
	{
		return new int[] { red, green, blue, alpha, brightness };
	}

	@Override
	public void setRGBAB(int[] array)
	{
		red = array[0];
		blue = array[1];
		green = array[2];
		alpha = array[3];
		brightness = array[4];
	}

	@Override
	public HashMap<String, IExtendedModelPart> getSubParts()
	{
		return childParts;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public Vector3 getDefaultTranslations()
	{
		return offset;
	}

	@Override
	public Vector4 getDefaultRotations()
	{
		return rotations;
	}

	@Override
	public IExtendedModelPart getParent()
	{
		return parent;
	}

	@Override
	public void setPostRotations2(Vector4 rotations)
	{
		postRot2 = rotations;
	}
}
