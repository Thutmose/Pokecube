package pokecube.modelloader.client.tabula.model.modelbase;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.interfaces.IPokemob;
import pokecube.modelloader.client.custom.RenderAdvancedPokemobModel;
import pokecube.modelloader.client.tabula.TabulaPackLoader;
import pokecube.modelloader.client.tabula.TabulaPackLoader.TabulaModelSet;

/** @author BobMowzie, gegy1000, FiskFille
 * @since 0.1.0 */
@SideOnly(Side.CLIENT)
public class MowzieModelRenderer extends ModelRenderer
{
    public float initRotateAngleX;
    public float initRotateAngleY;
    public float initRotateAngleZ;

    public float initOffsetX;
    public float initOffsetY;
    public float initOffsetZ;

    public float initRotationPointX;
    public float initRotationPointY;
    public float initRotationPointZ;

    public float          initScaleX = 1f;
    public float          initScaleY = 1f;
    public float          initScaleZ = 1f;

    public float          scaleX = 1f;
    public float          scaleY = 1f;
    public float          scaleZ = 1f;
    public ModelRenderer  parent;
    public boolean        hasInitPose;
    private boolean       compiled;
    private int           displayList;
    public String         name;
    public String         identifier;
    public TabulaModelSet set;

    public MowzieModelRenderer(ModelBase modelBase, String name)
    {
        super(modelBase, name);
    }

    public MowzieModelRenderer(ModelBase modelBase, int x, int y)
    {
        super(modelBase, x, y);
        if (modelBase instanceof MowzieModelBase)
        {
            MowzieModelBase mowzieModelBase = (MowzieModelBase) modelBase;

            mowzieModelBase.addPart(this);
        }
    }

    public MowzieModelRenderer(ModelBase modelBase)
    {
        super(modelBase);
    }

    public void addChild(ModelRenderer renderer)
    {
        super.addChild(renderer);

        if (renderer instanceof MowzieModelRenderer)
        {
            ((MowzieModelRenderer) renderer).setParent(this);
        }
    }

    public void postRenderParentChain(float par1)
    {
        if (parent instanceof MowzieModelRenderer)
        {
            ((MowzieModelRenderer) parent).postRenderParentChain(par1);
        }
        else if (parent != null)
        {
            parent.postRender(par1);
        }

        postRender(par1);
    }

    /** Returns the parent of this ModelRenderer */
    public ModelRenderer getParent()
    {
        return parent;
    }

    /** Sets the parent of this ModelRenderer */
    private void setParent(ModelRenderer modelRenderer)
    {
        parent = modelRenderer;
    }

    /** Set the initialization pose to the current pose */
    public void setInitValuesToCurrentPose()
    {
        initRotateAngleX = rotateAngleX;
        initRotateAngleY = rotateAngleY;
        initRotateAngleZ = rotateAngleZ;

        initRotationPointX = rotationPointX;
        initRotationPointY = rotationPointY;
        initRotationPointZ = rotationPointZ;

        initOffsetX = offsetX;
        initOffsetY = offsetY;
        initOffsetZ = offsetZ;
        
        initScaleX = scaleX;
        initScaleY = scaleY;
        initScaleZ = scaleZ;

        hasInitPose = true;
    }

    /** Resets the pose to init pose */
    public void setCurrentPoseToInitValues()
    {
        if (hasInitPose)
        {
            rotateAngleX = initRotateAngleX;
            rotateAngleY = initRotateAngleY;
            rotateAngleZ = initRotateAngleZ;

            rotationPointX = initRotationPointX;
            rotationPointY = initRotationPointY;
            rotationPointZ = initRotationPointZ;

            offsetX = initOffsetX;
            offsetY = initOffsetY;
            offsetZ = initOffsetZ;
            
            scaleX = initScaleX;
            scaleY = initScaleY;
            scaleZ = initScaleZ;
        }
    }

    public void setRotationAngles(float x, float y, float z)
    {
        rotateAngleX = x;
        rotateAngleY = y;
        rotateAngleZ = z;
    }

    /** Resets all rotation points. */
    public void resetAllRotationPoints()
    {
        rotationPointX = initRotationPointX;
        rotationPointY = initRotationPointY;
        rotationPointZ = initRotationPointZ;
    }

    /** Resets X rotation point. */
    public void resetXRotationPoints()
    {
        rotationPointX = initRotationPointX;
    }

    /** Resets Y rotation point. */
    public void resetYRotationPoints()
    {
        rotationPointY = initRotationPointY;
    }

    /** Resets Z rotation point. */
    public void resetZRotationPoints()
    {
        rotationPointZ = initRotationPointZ;
    }

    /** Resets all rotations. */
    public void resetAllRotations()
    {
        rotateAngleX = initRotateAngleX;
        rotateAngleY = initRotateAngleY;
        rotateAngleZ = initRotateAngleZ;
    }

    /** Resets X rotation. */
    public void resetXRotations()
    {
        rotateAngleX = initRotateAngleX;
    }

    /** Resets Y rotation. */
    public void resetYRotations()
    {
        rotateAngleY = initRotateAngleY;
    }

    /** Resets Z rotation. */
    public void resetZRotations()
    {
        rotateAngleZ = initRotateAngleZ;
    }

    /** Copies the rotation point coordinates. */
    public void copyAllRotationPoints(MowzieModelRenderer target)
    {
        rotationPointX = target.rotationPointX;
        rotationPointY = target.rotationPointY;
        rotationPointZ = target.rotationPointZ;
    }

    /** Copies X rotation point. */
    public void copyXRotationPoint(MowzieModelRenderer target)
    {
        rotationPointX = target.rotationPointX;
    }

    /** Copies Y rotation point. */
    public void copyYRotationPoint(MowzieModelRenderer target)
    {
        rotationPointY = target.rotationPointY;
    }

    /** Copies Z rotation point. */
    public void copyZRotationPoint(MowzieModelRenderer target)
    {
        rotationPointZ = target.rotationPointZ;
    }

    public void renderWithParents(float partialTicks)
    {
        if (parent instanceof MowzieModelRenderer)
        {
            ((MowzieModelRenderer) parent).renderWithParents(partialTicks);
        }
        else if (parent != null)
        {
            parent.render(partialTicks);
        }

        render(partialTicks);
    }

    public void setScale(float x, float y, float z)
    {
        scaleX = x;
        scaleY = y;
        scaleZ = z;
    }

    @SideOnly(Side.CLIENT)
    public void render(float scale, Entity entity)
    {
        GL11.glPushMatrix();

        if (set == null) set = TabulaPackLoader.modelMap.get(((IPokemob) entity).getPokedexEntry());

        // Allows specific part recolouring, this could possibly be moved over
        // to a method inside this class somehow
        int rgba = RenderAdvancedPokemobModel.getColour(identifier, set, (IPokemob) entity, 0xFFFFFFFF);

        float alpha = ((rgba >> 24) & 255) / 255f;
        float red = ((rgba >> 16) & 255) / 255f;
        float green = ((rgba >> 8) & 255) / 255f;
        float blue = (rgba & 255) / 255f;

        // Allows specific part hiding based on entity state. should probably be
        // somehow moved over to this class somewhere
        isHidden = RenderAdvancedPokemobModel.isHidden(identifier, set, (IPokemob) entity, isHidden);

        if (!isHidden)
        {
            if (showModel)
            {
                if (!compiled)
                {
                    compileDisplayList(scale);
                }

                float f5 = 0.0625F;
                GL11.glTranslatef(rotationPointX * f5, rotationPointY * f5, rotationPointZ * f5);
                GL11.glTranslatef(offsetX, offsetY, offsetZ);
                GL11.glScalef(scaleX, scaleY, scaleZ);
                GL11.glTranslatef(-rotationPointX * f5, -rotationPointY * f5, -rotationPointZ * f5);
                int i;

                TabulaModelSet set = TabulaPackLoader.modelMap.get(((IPokemob) entity).getPokedexEntry());
                /** Rotate the head, this should probably also be moved to a
                 * seperate method. */
                if (set.isHeadRoot(identifier) && entity instanceof IPokemob)
                {
                    float head = (entity.getRotationYawHead() + 360) % 360;
                    float body = (entity.rotationYaw + 360) % 360;
                    // TODO improve on these caps.;
                    float rot = Math.min(set.headCap[1], head - body);
                    float headRot = Math.max(rot, set.headCap[0]);
                    headRot *= set.headDir;

                    GL11.glTranslatef(rotationPointX * scale, rotationPointY * scale, rotationPointZ * scale);

                    rotateToParent();

                    if (set.headAxis == 2) GlStateManager.rotate(headRot, 0, 0, 1);
                    else GlStateManager.rotate(headRot, 0, 1, 0);
                    GlStateManager.rotate(entity.rotationPitch, 1, 0, 0);

                    unRotateToParent();

                    GL11.glTranslatef(-rotationPointX * scale, -rotationPointY * scale, -rotationPointZ * scale);

                }

                if (rotateAngleX == 0f && rotateAngleY == 0f && rotateAngleZ == 0f)
                {
                    if (rotationPointX == 0f && rotationPointY == 0f && rotationPointZ == 0f)
                    {

                        GL11.glPushMatrix();
                        // Apply Colour
                        GL11.glColor4f(red, green, blue, alpha);
                        GL11.glCallList(displayList);
                        GL11.glColor4f(1, 1, 1, 1);
                        GL11.glPopMatrix();
                        if (childModels != null)
                        {
                            for (i = 0; i < childModels.size(); ++i)
                            {
                                ((MowzieModelRenderer) childModels.get(i)).render(scale, entity);
                            }
                        }
                    }
                    else
                    {
                        GL11.glTranslatef(rotationPointX * scale, rotationPointY * scale, rotationPointZ * scale);

                        GL11.glPushMatrix();
                        // Apply Colour
                        GL11.glColor4f(red, green, blue, alpha);
                        GL11.glCallList(displayList);
                        GL11.glColor4f(1, 1, 1, 1);
                        GL11.glPopMatrix();

                        if (childModels != null)
                        {
                            for (i = 0; i < childModels.size(); ++i)
                            {
                                ((MowzieModelRenderer) childModels.get(i)).render(scale, entity);
                            }
                        }

                        GL11.glTranslatef(-rotationPointX * scale, -rotationPointY * scale, -rotationPointZ * scale);
                    }
                }
                else
                {
                    GL11.glPushMatrix();
                    GL11.glTranslatef(rotationPointX * scale, rotationPointY * scale, rotationPointZ * scale);

                    if (rotateAngleZ != 0f)
                    {
                        GL11.glRotatef(rotateAngleZ * (180f / (float) Math.PI), 0f, 0f, 1f);
                    }

                    if (rotateAngleY != 0f)
                    {
                        GL11.glRotatef(rotateAngleY * (180f / (float) Math.PI), 0f, 1f, 0f);
                    }

                    if (rotateAngleX != 0f)
                    {
                        GL11.glRotatef(rotateAngleX * (180f / (float) Math.PI), 1f, 0f, 0f);
                    }

                    GL11.glPushMatrix();
                    // Apply Colour
                    GL11.glColor4f(red, green, blue, alpha);
                    GL11.glCallList(displayList);
                    GL11.glColor4f(1, 1, 1, 1);
                    GL11.glPopMatrix();

                    if (childModels != null)
                    {
                        for (i = 0; i < childModels.size(); ++i)
                        {
                            ((MowzieModelRenderer) childModels.get(i)).render(scale, entity);
                        }
                    }

                    GL11.glPopMatrix();
                }

                GL11.glTranslatef(-offsetX, -offsetY, -offsetZ);
                GL11.glScalef(1f / scaleX, 1f / scaleY, 1f / scaleZ);
            }
        }

        GL11.glPopMatrix();
    }

    private void rotateToParent()
    {
        if (parent != null)
        {
            if (parent instanceof MowzieModelRenderer) ((MowzieModelRenderer) parent).rotateToParent();

            if (parent.rotateAngleZ != 0f)
            {
                GL11.glRotatef(parent.rotateAngleZ * (180f / (float) Math.PI), 0f, 0f, -1f);
            }

            if (parent.rotateAngleY != 0f)
            {
                GL11.glRotatef(parent.rotateAngleY * (180f / (float) Math.PI), 0f, -1f, 0f);
            }

            if (parent.rotateAngleX != 0f)
            {
                GL11.glRotatef(parent.rotateAngleX * (180f / (float) Math.PI), -1f, 0f, 0f);
            }
        }
    }

    private void unRotateToParent()
    {

        if (parent != null)
        {
            if (parent instanceof MowzieModelRenderer) ((MowzieModelRenderer) parent).unRotateToParent();

            if (parent.rotateAngleZ != 0f)
            {
                GL11.glRotatef(parent.rotateAngleZ * (180f / (float) Math.PI), 0f, 0f, 1f);
            }

            if (parent.rotateAngleY != 0f)
            {
                GL11.glRotatef(parent.rotateAngleY * (180f / (float) Math.PI), 0f, 1f, 0f);
            }

            if (parent.rotateAngleX != 0f)
            {
                GL11.glRotatef(parent.rotateAngleX * (180f / (float) Math.PI), 1f, 0f, 0f);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    private void compileDisplayList(float scale)
    {
        displayList = GLAllocation.generateDisplayLists(1);
        GL11.glNewList(displayList, GL11.GL_COMPILE);
        for (Object object : cubeList)
        {
            ((ModelBox) object).render(Tessellator.getInstance().getWorldRenderer(), scale);
        }
        GL11.glEndList();
        compiled = true;
    }
}
