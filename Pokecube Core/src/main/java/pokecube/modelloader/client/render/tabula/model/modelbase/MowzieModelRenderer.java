package pokecube.modelloader.client.render.tabula.model.modelbase;

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
import pokecube.core.interfaces.IMobColourable;
import pokecube.modelloader.client.render.model.IAnimationChanger;
import pokecube.modelloader.client.render.model.IPartTexturer;
import pokecube.modelloader.client.render.model.IRetexturableModel;

/** @author BobMowzie, gegy1000, FiskFille, Thutmose
 * @since 0.1.0 */
@SideOnly(Side.CLIENT)
public class MowzieModelRenderer extends ModelRenderer implements IRetexturableModel
{
    static final float       ratio       = 180f / (float) Math.PI;

    public float             initRotateAngleX;
    public float             initRotateAngleY;
    public float             initRotateAngleZ;

    public float             initOffsetX;
    public float             initOffsetY;
    public float             initOffsetZ;

    public float             initRotationPointX;
    public float             initRotationPointY;
    public float             initRotationPointZ;

    public float             initScaleX  = 1f;
    public float             initScaleY  = 1f;
    public float             initScaleZ  = 1f;
    public float             scaleX      = 1f;
    public float             scaleY      = 1f;
    public float             scaleZ      = 1f;
    public ModelRenderer     parent;
    public boolean           hasInitPose;
    private boolean          compiled;
    private int              displayList;
    public String            name;
    public String            identifier;

    public IAnimationChanger set;
    IPartTexturer            texturer;
    double[]                 texOffsets  = { 0, 0 };
    boolean                  offset      = true;

    boolean                  rotate      = true;

    boolean                  translate   = true;

    boolean                  shouldScale = true;

    public MowzieModelRenderer(ModelBase modelBase)
    {
        super(modelBase);
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

    public MowzieModelRenderer(ModelBase modelBase, String name)
    {
        super(modelBase, name);
    }

    @Override
    public void addChild(ModelRenderer renderer)
    {
        super.addChild(renderer);

        if (renderer instanceof MowzieModelRenderer)
        {
            ((MowzieModelRenderer) renderer).setParent(this);
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

    /** Returns the parent of this ModelRenderer */
    public ModelRenderer getParent()
    {
        return parent;
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

    @SideOnly(Side.CLIENT)
    public void render(float scale, Entity entity)
    {
        if (set == null) return;
        GL11.glPushMatrix();
        // Allows specific part hiding based on entity state. should probably be
        // somehow moved over to this class somewhere
        isHidden = set.isPartHidden(identifier, entity, isHidden);
        if (!isHidden && showModel)
        {
            if (!compiled)
            {
                translate = rotationPointX != 0 || rotationPointY != 0 || rotationPointZ != 0;
                rotate = rotateAngleX != 0 || rotateAngleY != 0 || rotateAngleZ != 0;
                offset = offsetX != 0 || offsetY != 0 || offsetZ != 0;
                shouldScale = scaleX != 1 || scaleY != 1 || scaleZ != 1;
                shouldScale = shouldScale && scaleX != 0 && scaleY != 0 && scaleZ != 0;
                compileDisplayList(scale);
            }
            float f5 = 0.0625F;
            if (translate) GL11.glTranslatef(rotationPointX * f5, rotationPointY * f5, rotationPointZ * f5);
            if (offset) GL11.glTranslatef(offsetX, offsetY, offsetZ);
            if (shouldScale) GL11.glScalef(scaleX, scaleY, scaleZ);
            if (translate) GL11.glTranslatef(-rotationPointX * f5, -rotationPointY * f5, -rotationPointZ * f5);
            int i;

            /** Rotate the head */
            if (set.isHeadRoot(identifier))
            {
                rotateHead(entity, set.getHeadInfo(), scale);
            }

            GL11.glPushMatrix();
            if (translate) GL11.glTranslatef(rotationPointX * scale, rotationPointY * scale, rotationPointZ * scale);

            if (rotate)
            {
                if (rotateAngleZ != 0f)
                {
                    GL11.glRotatef(rotateAngleZ * ratio, 0f, 0f, 1f);
                }
                if (rotateAngleY != 0f)
                {
                    GL11.glRotatef(rotateAngleY * ratio, 0f, 1f, 0f);
                }
                if (rotateAngleX != 0f)
                {
                    GL11.glRotatef(rotateAngleX * ratio, 1f, 0f, 0f);
                }
            }

            GL11.glPushMatrix();

            // Allows specific part recolouring,

            int rgba = 0;
            if (entity instanceof IMobColourable)
            {
                int[] cols = ((IMobColourable) entity).getRGBA();
                rgba += cols[2];
                rgba += cols[1] << 8;
                rgba += cols[2] << 16;
                rgba += cols[3] << 24;
            }
            else
            {
                rgba = 0xFFFFFFFF;
            }

            rgba = set.getColourForPart(identifier, entity, rgba);

            float alpha = ((rgba >> 24) & 255) / 255f;
            float red = ((rgba >> 16) & 255) / 255f;
            float green = ((rgba >> 8) & 255) / 255f;
            float blue = (rgba & 255) / 255f;

            // Apply Colour
            GL11.glColor4f(red, green, blue, alpha);
            boolean animateTex = false;
            // Apply Texture
            if (texturer != null)
            {
                texturer.applyTexture(name);
                texturer.shiftUVs(name, texOffsets);
                if (texOffsets[0] != 0 || texOffsets[1] != 0) animateTex = true;
                if (animateTex)
                {
                    GL11.glMatrixMode(GL11.GL_TEXTURE);
                    GL11.glLoadIdentity();
                    GL11.glTranslated(texOffsets[0], texOffsets[1], 0.0F);
                    GL11.glMatrixMode(GL11.GL_MODELVIEW);
                }
            }
            GL11.glCallList(displayList);
            if (animateTex)
            {
                GL11.glMatrixMode(GL11.GL_TEXTURE);
                GL11.glLoadIdentity();
                GL11.glMatrixMode(GL11.GL_MODELVIEW);
            }
            GL11.glPopMatrix();

            if (childModels != null)
            {
                for (i = 0; i < childModels.size(); ++i)
                {
                    ((MowzieModelRenderer) childModels.get(i)).render(scale, entity);
                }
            }

            GL11.glPopMatrix();

            if (offset) GL11.glTranslatef(-offsetX, -offsetY, -offsetZ);
            if (shouldScale) GL11.glScalef(1f / scaleX, 1f / scaleY, 1f / scaleZ);
        }

        GL11.glPopMatrix();

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

    /** Resets all rotation points. */
    public void resetAllRotationPoints()
    {
        rotationPointX = initRotationPointX;
        rotationPointY = initRotationPointY;
        rotationPointZ = initRotationPointZ;
    }

    /** Resets all rotations. */
    public void resetAllRotations()
    {
        rotateAngleX = initRotateAngleX;
        rotateAngleY = initRotateAngleY;
        rotateAngleZ = initRotateAngleZ;
    }

    /** Resets X rotation point. */
    public void resetXRotationPoints()
    {
        rotationPointX = initRotationPointX;
    }

    /** Resets X rotation. */
    public void resetXRotations()
    {
        rotateAngleX = initRotateAngleX;
    }

    /** Resets Y rotation point. */
    public void resetYRotationPoints()
    {
        rotationPointY = initRotationPointY;
    }

    /** Resets Y rotation. */
    public void resetYRotations()
    {
        rotateAngleY = initRotateAngleY;
    }

    /** Resets Z rotation point. */
    public void resetZRotationPoints()
    {
        rotationPointZ = initRotationPointZ;
    }

    /** Resets Z rotation. */
    public void resetZRotations()
    {
        rotateAngleZ = initRotateAngleZ;
    }

    private void rotateHead(Entity entity, float[] headInfo, float scale)
    {
        float ang;
        float head = (entity.getRotationYawHead()) % 360 + 180;
        float diff = 0;
        float body = (entity.rotationYaw) % 360;
        if (headInfo[2] == 1) body *= -1;
        else head *= -1;

        diff = (head + body) % 360;

        diff = (diff + 360) % 360;
        diff = (diff - 180) % 360;
        diff = Math.max(diff, headInfo[0]);
        diff = Math.min(diff, headInfo[1]);

        ang = diff;

        float ang2 = Math.max(entity.rotationPitch, headInfo[3]);
        ang2 = Math.min(ang2, headInfo[4]);

        GL11.glTranslatef(rotationPointX * scale, rotationPointY * scale, rotationPointZ * scale);

        rotateToParent();

        if (headInfo[5] == 2) GlStateManager.rotate(ang, 0, 0, 1);
        else GlStateManager.rotate(ang, 0, 1, 0);
        GlStateManager.rotate(ang2, 1, 0, 0);

        unRotateToParent();

        GL11.glTranslatef(-rotationPointX * scale, -rotationPointY * scale, -rotationPointZ * scale);
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

    /** Sets the parent of this ModelRenderer */
    private void setParent(ModelRenderer modelRenderer)
    {
        parent = modelRenderer;
    }

    public void setRotationAngles(float x, float y, float z)
    {
        rotateAngleX = x;
        rotateAngleY = y;
        rotateAngleZ = z;
    }

    public void setScale(float x, float y, float z)
    {
        scaleX = x;
        scaleY = y;
        scaleZ = z;
    }

    @Override
    public void setTexturer(IPartTexturer texturer)
    {
        this.texturer = texturer;
        if (childModels != null)
        {
            for (Object part : childModels)
            {
                if (part instanceof IRetexturableModel) ((IRetexturableModel) part).setTexturer(texturer);
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

    @Override
    public void setAnimationChanger(IAnimationChanger changer)
    {
        this.set = changer;
        if (childModels != null) for (ModelRenderer r : childModels)
        {
            if (r instanceof IRetexturableModel)
            {
                ((IRetexturableModel) r).setAnimationChanger(changer);
            }
        }
    }
}
