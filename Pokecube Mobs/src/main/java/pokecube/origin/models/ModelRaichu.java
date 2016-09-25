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

/** @author Manchou */
public class ModelRaichu extends APokemobModel
{
    public ModelRaichu()
    {
        float f = 0.0F;

        setTextureOffset("tail.main", 28, 9);
        setTextureOffset("tailEnd.main", 28, 18);

        headMain = new ModelRenderer(this, 0, 0);
        headMain.addBox(-4F, -3F, -2F, 8, 6, 6, f);
        body = new ModelRenderer(this, 0, 12);
        body.addBox(-5F, -9F, -3F, 8, 12, 6, f);
        footRight = new ModelRenderer(this, 36, 0);
        footRight.addBox(-1F, 0.0F, -1F, 2, 7, 2, f);
        footLeft = new ModelRenderer(this, 36, 0);
        footLeft.addBox(-1F, 0.0F, -1F, 2, 7, 2, f);
        handRight = new ModelRenderer(this, 28, 0);
        handRight.addBox(-1F, 0.0F, -1F, 2, 4, 2, f);
        handLeft = new ModelRenderer(this, 28, 0);
        handLeft.addBox(-1F, 0.0F, -1F, 2, 4, 2, f);

        rightEar = new ModelRenderer(this, 48, 5);
        rightEar.addBox(-9F, -6F, 1F, 6, 4, 1, f);
        leftEar = new ModelRenderer(this, 48, 0);
        leftEar.addBox(3F, -6F, 1F, 6, 4, 1, f);
        snout = new ModelRenderer(this, 28, 26);
        snout.addBox(-4F, 0F, -2.5F, 8, 3, 1, f);

        tail = new ModelRenderer(this, "tail");
        tail.setRotationPoint(0F, 20.5F, 4F);
        tail.addBox("main", -0.5F, 0F, -0.5F, 1, 4, 1);
        setRotation(tail, 0F, 0F, 0F);

        ModelRenderer lastTailPiece = tail;
        for (int i = 0; i < 8; i++)
        {
            ModelRenderer tailPiece = new ModelRenderer(this, "tail");
            tailPiece.setRotationPoint(0F, 2.9F, 0F);
            setRotation(tailPiece, 0F, 0F, 0F);
            tailPiece.addBox("main", -0.5F, 0F, -0.5F, 1, 3, 1);

            lastTailPiece.addChild(tailPiece);
            lastTailPiece = tailPiece;
        }

        ModelRenderer tailEnd = new ModelRenderer(this, "tailEnd");
        tailEnd.setRotationPoint(0F, 2.9F, 0F);
        setRotation(tailEnd, 0F, 0F, 0F);
        tailEnd.addBox("main", -0.5F, 0F, -0.5F, 10, 7, 1);

        lastTailPiece.addChild(tailEnd);
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5)
    {
        super.render(entity, f, f1, f2, f3, f4, f5);
        setRotationAngles(f, f1, f2, f3, f4, f5, entity);
        GL11.glPushMatrix();
        if (entity instanceof IMobColourable)
        {
            IMobColourable mob = (IMobColourable) entity;
            int[] cols = mob.getRGBA();
            GL11.glColor4f(cols[0] / 255f, cols[1] / 255f, cols[2] / 255f, cols[3] / 255f);
        }
        IPokemob mob = (IPokemob) entity;
        GL11.glTranslated(0, 0.5+ (1-mob.getSize()) * 0.5 , 0);
        GL11.glScaled(0.6 * mob.getSize(), 0.6 * mob.getSize(), 0.6 * mob.getSize());
        headMain.render(f5);
        body.render(f5);
        footRight.render(f5);
        footLeft.render(f5);
        handRight.render(f5);
        handLeft.render(f5);
        rightEar.render(f5);
        leftEar.render(f5);
        snout.renderWithRotation(f5);
        tail.render(f5);
        GL11.glPopMatrix();
    }

    @Override
    public void setLivingAnimations(EntityLivingBase entityliving, float f, float f1, float f2)
    {
        EntityPokemob entity = (EntityPokemob) entityliving;

        if (entity.getPokemonAIState(pokecube.core.interfaces.IPokemob.SITTING) || f1 < 0.001 || entity.isRiding())
        {
            float bodyAngle = 0.1F;
            float xOffset = 0;

            if (entity.isRiding())
            {
                xOffset = 8;
            }

            body.setRotationPoint(0.0F + xOffset, 21F, 0.0F);
            body.rotateAngleX = bodyAngle;
            tail.setRotationPoint(-0.5F + xOffset, 23F, 3F);
            footRight.setRotationPoint(-3F + xOffset, 23F, -2F);
            footLeft.setRotationPoint(1F + xOffset, 23F, -2F);
            handRight.setRotationPoint(-4F + xOffset, 15F, -3F);
            handLeft.setRotationPoint(2F + xOffset, 15F, -3F);
            footRight.rotateAngleX = ((float) Math.PI * 3F / 2F);
            footLeft.rotateAngleX = ((float) Math.PI * 3F / 2F);
            footRight.rotateAngleY = 0.2F;
            handLeft.rotateAngleY = -1F;
            handRight.rotateAngleY = 1F;
            footLeft.rotateAngleY = -0.2F;
            handRight.rotateAngleX = 5.811947F;
            handLeft.rotateAngleX = 5.811947F;
            headMain.setRotationPoint(-1F + xOffset, 9.5F, -2F);
            snout.setRotationPoint(-1F + xOffset, 9.5F, -2F);
            rightEar.setRotationPoint(-1F + xOffset, 8.5F, -2F);
            leftEar.setRotationPoint(-1F + xOffset, 8.5F, -2F);
        }
        else
        {
            float bodyAngle = 0.15F;
            body.setRotationPoint(0.0F, 15F, 0.0F);
            body.rotateAngleX = bodyAngle;
            tail.setRotationPoint(-0.5F, 15F, 3F);
            footRight.setRotationPoint(-3F, 17F, -0.5F);
            footLeft.setRotationPoint(1F, 17F, -0.5F);
            handRight.setRotationPoint(-4F, 9F, -3.5F);
            handLeft.setRotationPoint(2F, 9F, -3.5F);
            footRight.rotateAngleX = MathHelper.cos(f * 0.6662F) * 1.4F * f1 - 0.3F;
            footLeft.rotateAngleX = MathHelper.cos(f * 0.6662F + (float) Math.PI) * 1.4F * f1 - 0.3F;
            handRight.rotateAngleX = 5.811947F;
            handLeft.rotateAngleX = 5.811947F;
            headMain.setRotationPoint(-1F, 3.5F, -2.5F);
            snout.setRotationPoint(-1F, 3.5F, -2.5F);
            rightEar.setRotationPoint(-1F, 2.5F, -2.5F);
            leftEar.setRotationPoint(-1F, 2.5F, -2.5F);
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
        headMain.rotateAngleX = f4 / (180F / (float) Math.PI);
        headMain.rotateAngleY = f3 / (180F / (float) Math.PI);
        rightEar.rotateAngleY = headMain.rotateAngleY;
        rightEar.rotateAngleX = headMain.rotateAngleX;
        leftEar.rotateAngleY = headMain.rotateAngleY;
        leftEar.rotateAngleX = headMain.rotateAngleX;
        rightEar.rotateAngleZ -= 0.1 * MathHelper.sin(f3 / 8) + 0.2;
        leftEar.rotateAngleZ += 0.1 * MathHelper.sin(f3 / 8) + 0.2;
        snout.rotateAngleY = headMain.rotateAngleY;
        snout.rotateAngleX = headMain.rotateAngleX;

        float speed = 0.06F;
        setRotationFloating(tail, f2, 2.1F, 0.0F, 0.0F, 0.3F, speed);
        ModelRenderer nextTailPiece = tail.childModels.get(0);
        int i = 0;
        while (nextTailPiece != null)
        {
            if (i < 4) setRotationFloating(nextTailPiece, f2, 0.3F, 0.F, 0.0F, 0.0F, speed);
            else if (i == 4) setRotationFloating(nextTailPiece, f2, 0.1F, 0.F, 0.0F, 0.0F, speed);
            else if (i == 5) setRotationFloating(nextTailPiece, f2, -0.2F, 0.F, 0.0F, 0.0F, speed);
            else setRotationFloating(nextTailPiece, f2, -0.5F, 0.F, 0.0F, 0.0F, speed);
            i++;
            if (nextTailPiece.childModels == null || nextTailPiece.childModels.isEmpty()) nextTailPiece = null;
            else nextTailPiece = nextTailPiece.childModels.get(0);
        }
    }

    public ModelRenderer headMain;
    public ModelRenderer body;
    public ModelRenderer footRight;
    public ModelRenderer footLeft;
    public ModelRenderer handRight;
    public ModelRenderer handLeft;
    ModelRenderer        rightEar;
    ModelRenderer        leftEar;
    ModelRenderer        snout;
    ModelRenderer        tail;
}
