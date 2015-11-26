package pokecube.adventures.client.models;

import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;
import pokecube.core.client.models.APokemobModel;
import pokecube.core.entity.pokemobs.EntityPokemob;

public class ModelMegaGardevoir extends APokemobModel
{
    // fields
    ModelRenderer hair;
    ModelRenderer head;
    ModelRenderer leg_left;
    ModelRenderer arm_left;
    ModelRenderer leg_right;
    ModelRenderer arm_right;
    ModelRenderer chest_horn_Right;
    ModelRenderer body;
    ModelRenderer Dress_Bottom;
    ModelRenderer Dress_Middle;
    ModelRenderer Dress_Top;
    ModelRenderer chest_horn_Left;

    public ModelMegaGardevoir()
    {
        textureWidth = 150;
        textureHeight = 120;

        hair = new ModelRenderer(this, 34, 0);
        hair.addBox(-4.5F, -8.5F, -4.5F, 9, 9, 9);
        hair.setRotationPoint(0F, -9F, 0F);
        hair.setTextureSize(150, 120);
        hair.mirror = true;
        setRotation(hair, 0F, 0F, 0F);
        head = new ModelRenderer(this, 0, 0);
        head.addBox(-4F, -8F, -4F, 8, 8, 8);
        head.setRotationPoint(0F, -9F, 0F);
        head.setTextureSize(150, 120);
        head.mirror = true;
        setRotation(head, 0F, 0F, 0F);
        leg_left = new ModelRenderer(this, 0, 41);
        leg_left.addBox(-1F, 0F, -1F, 2, 23, 2);
        leg_left.setRotationPoint(1F, -1F, 0F);
        leg_left.setTextureSize(150, 120);
        leg_left.mirror = true;
        setRotation(leg_left, 0F, 0F, -0.0523599F);
        arm_left = new ModelRenderer(this, 0, 21);
        arm_left.addBox(-1F, 0F, -1F, 2, 14, 2);
        arm_left.setRotationPoint(2F, -7F, 0F);
        arm_left.setTextureSize(150, 120);
        arm_left.mirror = true;
        setRotation(arm_left, 0F, 0F, -1.117011F);
        leg_right = new ModelRenderer(this, 0, 41);
        leg_right.addBox(-1F, 0F, -1F, 2, 23, 2);
        leg_right.setRotationPoint(-1F, -1F, 0F);
        leg_right.setTextureSize(150, 120);
        leg_right.mirror = true;
        setRotation(leg_right, 0F, 0F, 0.0523599F);
        arm_right = new ModelRenderer(this, 0, 21);
        arm_right.addBox(-1F, 0F, -1F, 2, 14, 2);
        arm_right.setRotationPoint(-2F, -7F, 0F);
        arm_right.setTextureSize(150, 120);
        arm_right.mirror = true;
        setRotation(arm_right, 0F, 0F, 1.117011F);
        chest_horn_Right = new ModelRenderer(this, 51, 41);
        chest_horn_Right.addBox(0F, 2F, -6.5F, 1, 4, 9);
        chest_horn_Right.setRotationPoint(0F, -9F, 0F);
        chest_horn_Right.setTextureSize(150, 120);
        chest_horn_Right.mirror = true;
        setRotation(chest_horn_Right, 0F, 0.4712389F, 0F);
        body = new ModelRenderer(this, 63, 22);
        body.addBox(-2.5F, 0F, -2.5F, 5, 8, 5);
        body.setRotationPoint(0F, -9F, 0F);
        body.setTextureSize(150, 120);
        body.mirror = true;
        setRotation(body, 0F, 0F, 0F);
        Dress_Bottom = new ModelRenderer(this, 0, 82);
        Dress_Bottom.addBox(-12F, 20F, -12F, 24, 12, 24);
        Dress_Bottom.setRotationPoint(0F, -9F, 3F);
        Dress_Bottom.setTextureSize(150, 120);
        Dress_Bottom.mirror = true;
        setRotation(Dress_Bottom, 0F, 0F, 0F);
        Dress_Middle = new ModelRenderer(this, 82, 48);
        Dress_Middle.addBox(-8F, 13F, -8F, 16, 7, 16);
        Dress_Middle.setRotationPoint(0F, -9F, 0F);
        Dress_Middle.setTextureSize(150, 120);
        Dress_Middle.mirror = true;
        setRotation(Dress_Middle, 0F, 0F, 0F);
        Dress_Top = new ModelRenderer(this, 90, 30);
        Dress_Top.addBox(-5F, 7F, -5F, 10, 5, 10);
        Dress_Top.setRotationPoint(0F, -8F, 0F);
        Dress_Top.setTextureSize(150, 120);
        Dress_Top.mirror = true;
        setRotation(Dress_Top, 0F, 0F, 0F);
        chest_horn_Left = new ModelRenderer(this, 51, 41);
        chest_horn_Left.addBox(0F, 2F, -6.5F, 1, 4, 9);
        chest_horn_Left.setRotationPoint(0F, -9F, 0F);
        chest_horn_Left.setTextureSize(150, 120);
        chest_horn_Left.mirror = true;
        setRotation(chest_horn_Left, 0F, -0.4712389F, 0F);
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5)
    {
        super.render(entity, f, f1, f2, f3, f4, f5);
        setRotationAngles(f, f1, f2, f3, f4, f5, entity);
        hair.render(f5);
        head.render(f5);
        leg_left.render(f5);
        arm_left.render(f5);
        leg_right.render(f5);
        arm_right.render(f5);
        chest_horn_Right.render(f5);
        body.render(f5);
        Dress_Bottom.render(f5);
        Dress_Middle.render(f5);
        Dress_Top.render(f5);
        chest_horn_Left.render(f5);
    }

    @Override
    public void setRotationAngles(float f, float f1, float f2, float f3, float f4, float f5, Entity entity)
    {
        super.setRotationAngles(f, f1, f2, f3, f4, f5, entity);
        head.rotateAngleY = f3 / (180F / (float) Math.PI);
        head.rotateAngleX = f4 / (180F / (float) Math.PI);
        hair.rotateAngleY = f3 / (180F / (float) Math.PI);
        hair.rotateAngleX = f4 / (180F / (float) Math.PI);
    }

    @Override
    public void setLivingAnimations(EntityLivingBase entityliving, float f, float f1, float f2)
    {
//        {
//            EntityPokemob entity = (EntityPokemob) entityliving;
//            if (entity.getPokemonAIState(pokecube.core.interfaces.IPokemob.SITTING))
//            {
//
//                // hair.setRotationPoint(0F, 0F, 0F);
//            }
//            if (!entity.getPokemonAIState(pokecube.core.interfaces.IPokemob.SITTING))
//            {
//
//                leg_left.rotateAngleX = MathHelper.cos(f * 0.6662F) * -0.35F * f1;
//                leg_right.rotateAngleX = -leg_left.rotateAngleX;
//                arm_left.rotateAngleX = leg_right.rotateAngleX * 4;
//                arm_right.rotateAngleX = leg_left.rotateAngleX * 4;
//            }
//
//        }
    }

}
