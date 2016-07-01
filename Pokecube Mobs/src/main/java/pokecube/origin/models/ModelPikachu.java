/**
 *
 */
package pokecube.origin.models;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.MathHelper;
import pokecube.core.entity.pokemobs.EntityPokemob;
import pokecube.core.interfaces.IPokemob;
import thut.api.entity.IMobColourable;

/**
 * @author Manchou
 *
 */
public class ModelPikachu extends APokemobModel
{
    public ModelPikachu()
    {
        float f = 0.0F;
        headMain = new ModelRenderer(this, 0, 0);
        headMain.addBox(-4F, -3F, -2F, 8, 6, 6, f);
        body = new ModelRenderer(this, 0, 12);
        body.addBox(-5F, -9F, -3F, 8, 12, 6, f);
        footRight = new ModelRenderer(this, 28, 0);
        footRight.addBox(-1F, 0.0F, -1F, 2, 4, 2, f);
        footLeft = new ModelRenderer(this, 28, 0);
        footLeft.addBox(-1F, 0.0F, -1F, 2, 4, 2, f);
        handRight = new ModelRenderer(this, 28, 0);
        handRight.addBox(-1F, 0.0F, -1F, 2, 4, 2, f);
        handLeft = new ModelRenderer(this, 28, 0);
        handLeft.addBox(-1F, 0.0F, -1F, 2, 4, 2, f);
        tail = new ModelRenderer(this, 28, 9);
        tail.addBox(-1F, -2F, -1F, 10, 16, 1, f);
        rightEar = new ModelRenderer(this, 56, 0);
        rightEar.addBox(-4F, -7F, 1F, 2, 7, 2, f);
        leftEar = new ModelRenderer(this, 48, 0);
        leftEar.addBox(2.0F, -7F, 1F, 2, 7, 2, f);
        snout = new ModelRenderer(this, 28, 26);
        snout.addBox(-4F, 0F, -2.5F, 8, 3, 1, f);
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5)
    {
        super.render(entity, f, f1, f2, f3, f4, f5);
        setRotationAngles(f, f1, f2, f3, f4, f5, entity);
        GL11.glPushMatrix();
        if(entity instanceof IMobColourable)
        {
            IMobColourable mob = (IMobColourable) entity;
            int[] cols = mob.getRGBA();
            GL11.glColor4f(cols[0]/255f, cols[1]/255f, cols[2]/255f, cols[3]/255f);
        }
        IPokemob mob = (IPokemob) entity;
        GL11.glTranslated(0, 0.9 + (1-mob.getSize()) * 0.5 , 0);
        GL11.glScaled(0.4 * mob.getSize(), 0.4 * mob.getSize(), 0.4 * mob.getSize());
        headMain.render(f5);
        body.render(f5);
        footRight.render(f5);
        footLeft.render(f5);
        handRight.render(f5);
        handLeft.render(f5);
        rightEar.render(f5);
        leftEar.render(f5);
        snout.renderWithRotation(f5);
        tail.renderWithRotation(f5);
        GL11.glPopMatrix();
    }

    @Override
    public void setLivingAnimations(EntityLivingBase entityliving, float f, float f1, float f2)
    {
        EntityPokemob entity = (EntityPokemob)entityliving;

        if (entity.getPokemonAIState(pokecube.core.interfaces.IPokemob.SITTING) || f1 < 0.001 || entity.isRiding())
        {
            float bodyAngle = 0.1F;
            float xOffset = 0;

            body.setRotationPoint(0.0F + xOffset, 21F, 0.0F);
            body.rotateAngleX = bodyAngle;
            tail.setRotationPoint(-5F + xOffset, 21F, 3F);
            footRight.setRotationPoint(-3F + xOffset, 23F, -2F);
            footLeft.setRotationPoint(1F + xOffset, 23F, -2F);
            handRight.setRotationPoint(-4F + xOffset, 15F, -3F);
            handLeft.setRotationPoint(2F + xOffset, 15F, -3F);
            footRight.rotateAngleX = ((float)Math.PI * 3F / 2F);
            footLeft.rotateAngleX = ((float)Math.PI * 3F / 2F);
            footRight.rotateAngleY = 0.5F;
            footLeft.rotateAngleY = -0.5F;
            handRight.rotateAngleX = 5.811947F;
            handLeft.rotateAngleX = 5.811947F;
            headMain.setRotationPoint(-1F + xOffset, 9.5F, -2F);
            snout.setRotationPoint(-1F + xOffset, 9.5F, -2F);
            rightEar.setRotationPoint(-1F + xOffset, 6.5F, -2F);
            leftEar.setRotationPoint(-1F + xOffset, 6.5F, -2F);
        }
        else
        {
            body.setRotationPoint(0.0F, 18F, 2.0F);
            body.rotateAngleX = ((float)Math.PI / 2F);
            tail.setRotationPoint(-5F, 16F, 4F);
            footRight.setRotationPoint(-4F, 20F, 5F);
            footLeft.setRotationPoint(1.5F, 20F, 5F);
            handRight.setRotationPoint(-3.5F, 20F, -4F);
            handLeft.setRotationPoint(1.5F, 20F, -4F);
            footRight.rotateAngleX = MathHelper.cos(f * 0.6662F) * 1.4F * f1;
            footLeft.rotateAngleX = MathHelper.cos(f * 0.6662F + (float)Math.PI) * 1.4F * f1;
            handRight.rotateAngleX = MathHelper.cos(f * 0.6662F + (float)Math.PI) * 1.4F * f1;
            handLeft.rotateAngleX = MathHelper.cos(f * 0.6662F) * 1.4F * f1;
            headMain.setRotationPoint(-1F, 14, -7.5F);
            snout.setRotationPoint(-1F, 14, -7F);
            rightEar.setRotationPoint(-1F, 11, -7F);
            leftEar.setRotationPoint(-1F, 11, -7F);
        }

        float f3 = entity.getInterestedAngle(f2) + entity.getShakeAngle(f2, 0.0F);
        headMain.rotateAngleZ = f3;
        rightEar.rotateAngleZ = f3;
        leftEar.rotateAngleZ = f3;
        snout.rotateAngleZ = f3;
        body.rotateAngleZ = entity.getShakeAngle(f2, -0.16F);
        tail.rotateAngleZ = entity.getShakeAngle(f2, -0.2F);
    }

    @Override
    public void setRotationAngles(float f, float f1, float f2, float f3, float f4, float f5, Entity entity)
    {
        super.setRotationAngles(f, f1, f2, f3, f4, f5, entity);
        headMain.rotateAngleX = f4 / (180F / (float)Math.PI);
        headMain.rotateAngleY = f3 / (180F / (float)Math.PI);
        rightEar.rotateAngleY = headMain.rotateAngleY;
        rightEar.rotateAngleX = headMain.rotateAngleX;
        leftEar.rotateAngleY = headMain.rotateAngleY;
        leftEar.rotateAngleX = headMain.rotateAngleX;

        if (f3 >= 0)
        {
            rightEar.rotateAngleZ -= 0.5 * MathHelper.sin(f3 / 8) + 0.5;
        }
        else
        {
            leftEar.rotateAngleZ += 0.5 * MathHelper.sin(f3 / 8) + 0.5;
        }

        snout.rotateAngleY = headMain.rotateAngleY;
        snout.rotateAngleX = headMain.rotateAngleX;
        tail.rotateAngleX = 2.8F;
    }

    public ModelRenderer headMain;
    public ModelRenderer body;
    public ModelRenderer footRight;
    public ModelRenderer footLeft;
    public ModelRenderer handRight;
    public ModelRenderer handLeft;
    ModelRenderer rightEar;
    ModelRenderer leftEar;
    ModelRenderer snout;
    ModelRenderer tail;
}
