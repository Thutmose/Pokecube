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
public class ModelPichu extends APokemobModel
{
    public ModelPichu()
    {
        float f = 0.0F;
        headMain = new ModelRenderer(this, 0, 0);
        headMain.addBox(-4F, -3F, -2F, 8, 6, 6, f);
        body = new ModelRenderer(this, 0, 12);
        body.addBox(-2.5F, -4F, -3F, 5, 6, 5, f);
        footRight = new ModelRenderer(this, 36, 0);
        footRight.addBox(-1F, 0.0F, -1F, 2, 4, 2, f);
        footLeft = new ModelRenderer(this, 36, 0);
        footLeft.addBox(-1F, 0.0F, -1F, 2, 4, 2, f);
        handRight = new ModelRenderer(this, 28, 0);
        handRight.addBox(-1F, 0.0F, -1F, 1, 4, 1, f);
        handLeft = new ModelRenderer(this, 28, 0);
        handLeft.addBox(-1F, 0.0F, -1F, 1, 4, 1, f);
        tail = new ModelRenderer(this, 28, 9);
        tail.addBox(-1F, -2F, -1F, 10, 16, 1, f);
        rightEar = new ModelRenderer(this, 48, 0);
        rightEar.addBox(-6F, -9F, 1F, 5, 7, 1, f);
        leftEar = new ModelRenderer(this, 48, 0);
        leftEar.addBox(1F, -9F, 1F, 5, 7, 1, f);
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

            if (entity.isRiding())
            {
                xOffset = 12;
            }

            body.setRotationPoint(-1F + xOffset, 21F, 0.0F);
            body.rotateAngleX = bodyAngle;
            tail.setRotationPoint(-5F + xOffset, 21F, 2F);
            footRight.setRotationPoint(-3F + xOffset, 23F, -2F);
            footLeft.setRotationPoint(1F + xOffset, 23F, -2F);
            handRight.setRotationPoint(-3F + xOffset, 18F, -2F);
            handLeft.setRotationPoint(1F + xOffset, 18F, -2F);
            footRight.rotateAngleX = ((float)Math.PI * 3F / 2F);
            footLeft.rotateAngleX = ((float)Math.PI * 3F / 2F);
            footRight.rotateAngleY = 0.2F;
            handLeft.rotateAngleY = -1F;
            handRight.rotateAngleY = 1F;
            footLeft.rotateAngleY = -0.2F;
            handRight.rotateAngleX = 5.811947F;
            handLeft.rotateAngleX = 5.811947F;
            headMain.setRotationPoint(-1F + xOffset, 14F, -2F);
            rightEar.setRotationPoint(-0.5F + xOffset, 12.5F, -2F);
            leftEar.setRotationPoint(-1F + xOffset, 12.5F, -2F);
        }
        else
        {
            float bodyAngle = 0.15F;
            body.setRotationPoint(-1F, 18F, 0.0F);
            body.rotateAngleX = bodyAngle;
            tail.setRotationPoint(-5F, 18F, 2F);
            footRight.setRotationPoint(-3F, 20F, -0.5F);
            footLeft.setRotationPoint(1F, 20F, -0.5F);
            handRight.setRotationPoint(-3F, 15F, -2F);
            handLeft.setRotationPoint(1F, 15F, -2F);
            footRight.rotateAngleX = MathHelper.cos(f * 0.6662F) * 1.4F * f1 - 0.3F;
            footLeft.rotateAngleX = MathHelper.cos(f * 0.6662F + (float)Math.PI) * 1.4F * f1 - 0.3F;
            handRight.rotateAngleX = 5.811947F;
            handLeft.rotateAngleX = 5.811947F;
            headMain.setRotationPoint(-1F, 11F, -2.5F);
            rightEar.setRotationPoint(-1F, 9.5F, -2.5F);
            leftEar.setRotationPoint(-1F, 9.5F, -2.5F);
        }

        float f3 = entity.getInterestedAngle(f2) + entity.getShakeAngle(f2, 0.0F);
        headMain.rotateAngleZ = f3;
        rightEar.rotateAngleZ = f3;
        leftEar.rotateAngleZ = f3;
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
        rightEar.rotateAngleZ -= 0.1 * MathHelper.sin(f3 / 8) + 0.2;
        leftEar.rotateAngleZ += 0.1 * MathHelper.sin(f3 / 8) + 0.2;
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
    ModelRenderer tail;
}
