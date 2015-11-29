package pokecube.core.client.models;

import java.util.ArrayList;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.Tessellator;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.utils.Vector4;

public class ModelRendererQuat extends ModelRenderer{

    private boolean compiled;
    /** The GL display list rendered by the Tessellator for this model */
    private int displayList;
    public ModelRendererQuat(ModelBase p_i1172_1_, String p_i1172_2_)
    {
    	this(p_i1172_1_);
    }

    public ModelRendererQuat(ModelBase p_i1173_1_)
    {
        super(p_i1173_1_);
        this.textureWidth = 64.0F;
        this.textureHeight = 32.0F;
        this.showModel = true;
        this.cubeList = new ArrayList<ModelBox>();
        p_i1173_1_.boxList.add(this);
        this.setTextureSize(p_i1173_1_.textureWidth, p_i1173_1_.textureHeight);
        
    }

    public ModelRendererQuat(ModelBase p_i1174_1_, int p_i1174_2_, int p_i1174_3_)
    {
    	this(p_i1174_1_);
        this.setTextureOffset(p_i1174_2_, p_i1174_3_);
    	
    }

    @Override
	@SideOnly(Side.CLIENT)
    public void render(float p_78785_1_)
    {
        if (!this.isHidden)
        {
            if (this.showModel)
            {
                if (!this.compiled)
                {
                    this.compileDisplayList(p_78785_1_);
                }

                GL11.glTranslatef(this.offsetX, this.offsetY, this.offsetZ);
                int i;

                if (this.rotateAngleX == 0.0F && this.rotateAngleY == 0.0F && this.rotateAngleZ == 0.0F)
                {
                    if (this.rotationPointX == 0.0F && this.rotationPointY == 0.0F && this.rotationPointZ == 0.0F)
                    {
                        GL11.glCallList(this.displayList);

                        if (this.childModels != null)
                        {
                            for (i = 0; i < this.childModels.size(); ++i)
                            {
                                ((ModelRenderer)this.childModels.get(i)).render(p_78785_1_);
                            }
                        }
                    }
                    else
                    {
                        GL11.glTranslatef(this.rotationPointX * p_78785_1_, this.rotationPointY * p_78785_1_, this.rotationPointZ * p_78785_1_);
                        GL11.glCallList(this.displayList);

                        if (this.childModels != null)
                        {
                            for (i = 0; i < this.childModels.size(); ++i)
                            {
                                ((ModelRenderer)this.childModels.get(i)).render(p_78785_1_);
                            }
                        }

                        GL11.glTranslatef(-this.rotationPointX * p_78785_1_, -this.rotationPointY * p_78785_1_, -this.rotationPointZ * p_78785_1_);
                    }
                }
                else
                {
                    GL11.glPushMatrix();
                    GL11.glTranslatef(this.rotationPointX * p_78785_1_, this.rotationPointY * p_78785_1_, this.rotationPointZ * p_78785_1_);

                    Vector4 sum = null;
                    if(rotateAngleZ!=0)
                    {
                    	Vector4 angleZ = new Vector4(0, 0, 1, (float) (rotateAngleZ * 180/Math.PI));
                    	sum = angleZ.copy();
                    }
                    if(rotateAngleY!=0)
                    {
                    	Vector4 angleY = new Vector4(0, 1, 0, (float) (rotateAngleY * 180/Math.PI));
                    	if(sum==null)
                    		sum = angleY.copy();
                    	else
                    		sum = sum.addAngles(angleY);
                    }
                    if(rotateAngleX!=0)
                    {
                    	Vector4 angleX = new Vector4(1, 0, 0, (float) (rotateAngleX * 180/Math.PI));
                    	if(sum==null)
                    		sum = angleX.copy();
                    	else
                    		sum = sum.addAngles(angleX);
                    }
                    
                    if(sum!=null)
                    {
	                    sum.glRotate();
                    }
                    GL11.glCallList(this.displayList);

                    if (this.childModels != null)
                    {
                        for (i = 0; i < this.childModels.size(); ++i)
                        {
                            ((ModelRenderer)this.childModels.get(i)).render(p_78785_1_);
                        }
                    }

                    GL11.glPopMatrix();
                }

                GL11.glTranslatef(-this.offsetX, -this.offsetY, -this.offsetZ);
            }
        }
    }

    @Override
	@SideOnly(Side.CLIENT)
    public void renderWithRotation(float p_78791_1_)
    {
        if (!this.isHidden)
        {
            if (this.showModel)
            {
                if (!this.compiled)
                {
                    this.compileDisplayList(p_78791_1_);
                }

                GL11.glPushMatrix();
                GL11.glTranslatef(this.rotationPointX * p_78791_1_, this.rotationPointY * p_78791_1_, this.rotationPointZ * p_78791_1_);


                Vector4 sum = null;
                if(rotateAngleZ!=0)
                {
                	Vector4 angleZ = new Vector4(0, 0, 1, rotateAngleZ);
                	sum = angleZ.copy();
                }
                if(rotateAngleY!=0)
                {
                	Vector4 angleY = new Vector4(0, 1, 0, rotateAngleY);
                	if(sum==null)
                		sum = angleY.copy();
                	else
                		sum = sum.addAngles(angleY);
                }
                if(rotateAngleX!=0)
                {
                	Vector4 angleX = new Vector4(1, 0, 0, rotateAngleX);
                	if(sum==null)
                		sum = angleX.copy();
                	else
                		sum = sum.addAngles(angleX);
                }
                
                if(sum!=null)
                    sum.glRotate();
                GL11.glCallList(this.displayList);
                GL11.glPopMatrix();
            }
        }
    }

    /**
     * Allows the changing of Angles after a box has been rendered
     */
    @Override
	@SideOnly(Side.CLIENT)
    public void postRender(float p_78794_1_)
    {
        if (!this.isHidden)
        {
            if (this.showModel)
            {
                if (!this.compiled)
                {
                    this.compileDisplayList(p_78794_1_);
                }

                if (this.rotateAngleX == 0.0F && this.rotateAngleY == 0.0F && this.rotateAngleZ == 0.0F)
                {
                    if (this.rotationPointX != 0.0F || this.rotationPointY != 0.0F || this.rotationPointZ != 0.0F)
                    {
                        GL11.glTranslatef(this.rotationPointX * p_78794_1_, this.rotationPointY * p_78794_1_, this.rotationPointZ * p_78794_1_);
                    }
                }
                else
                {
                    GL11.glTranslatef(this.rotationPointX * p_78794_1_, this.rotationPointY * p_78794_1_, this.rotationPointZ * p_78794_1_);


                    Vector4 sum = null;
                    if(rotateAngleZ!=0)
                    {
                    	Vector4 angleZ = new Vector4(0, 0, 1, rotateAngleZ);
                    	sum = angleZ.copy();
                    }
                    if(rotateAngleY!=0)
                    {
                    	Vector4 angleY = new Vector4(0, 1, 0, rotateAngleY);
                    	if(sum==null)
                    		sum = angleY.copy();
                    	else
                    		sum = sum.addAngles(angleY);
                    }
                    if(rotateAngleX!=0)
                    {
                    	Vector4 angleX = new Vector4(1, 0, 0, rotateAngleX);
                    	if(sum==null)
                    		sum = angleX.copy();
                    	else
                    		sum = sum.addAngles(angleX);
                    }
                    
                    if(sum!=null)
	                    sum.glRotate();
                }
            }
        }
    }


    /**
     * Compiles a GL display list for this model
     */
    @SideOnly(Side.CLIENT)
    private void compileDisplayList(float p_78788_1_)
    {
        this.displayList = GLAllocation.generateDisplayLists(1);
        GL11.glNewList(this.displayList, GL11.GL_COMPILE);
        Tessellator tessellator = Tessellator.getInstance();

        for (int i = 0; i < this.cubeList.size(); ++i)
        {
            ((ModelBox)this.cubeList.get(i)).render(tessellator.getWorldRenderer(), p_78788_1_);
        }

        GL11.glEndList();
        this.compiled = true;
    }

    /**
     * Returns the model renderer with the new texture parameters.
     */
    @Override
	public ModelRenderer setTextureSize(int p_78787_1_, int p_78787_2_)
    {
        this.textureWidth = p_78787_1_;
        this.textureHeight = p_78787_2_;
        return this;
    }
}
