package pokecube.adventures.client.models;

import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import pokecube.core.client.models.APokemobModel;

public class ModelMegaGallade extends APokemobModel
{
    // fields
    ModelRenderer hair;
    ModelRenderer haed;
    ModelRenderer horn_head;
    ModelRenderer horn_chest;
    ModelRenderer body_bottom;
    ModelRenderer leg_top_left;
    ModelRenderer Shape5;
    ModelRenderer leg_bottom_right;
    ModelRenderer leg_top_right;
    ModelRenderer body;
    ModelRenderer arm_blade_left;
    ModelRenderer arm_left;
    ModelRenderer arm_right;
    ModelRenderer arm_blade_right;
    ModelRenderer arm_blade_Tip_right;
    ModelRenderer arm_blade_tip_right;
    ModelRenderer Cape;

    public ModelMegaGallade()
    {
        textureWidth = 200;
        textureHeight = 100;

        hair = new ModelRenderer(this, 39, 0);
        hair.addBox(-4.5F, -8.5F, -4.5F, 9, 9, 9);
        hair.setRotationPoint(0F, -12F, 0F);
        hair.setTextureSize(200, 100);
        hair.mirror = true;
        setRotation(hair, 0F, 0F, 0F);
        haed = new ModelRenderer(this, 0, 0);
        haed.addBox(-4F, -8F, -4F, 8, 8, 8);
        haed.setRotationPoint(0F, -12F, 0F);
        haed.setTextureSize(200, 100);
        haed.mirror = true;
        setRotation(haed, 0F, 0F, 0F);
        horn_head = new ModelRenderer(this, 63, 22);
        horn_head.addBox(0F, -16F, -2F, 1, 15, 9);
        horn_head.setRotationPoint(0F, -12F, 0F);
        horn_head.setTextureSize(200, 100);
        horn_head.mirror = true;
        setRotation(horn_head, 0F, 0F, 0F);
        horn_chest = new ModelRenderer(this, 28, 22);
        horn_chest.addBox(0F, 1F, -10F, 1, 6, 14);
        horn_chest.setRotationPoint(0F, -12F, 0F);
        horn_chest.setTextureSize(200, 100);
        horn_chest.mirror = true;
        setRotation(horn_chest, 0F, 0F, 0F);
        body_bottom = new ModelRenderer(this, 22, 48);
        body_bottom.addBox(-3.5F, 8F, -3.5F, 7, 5, 7);
        body_bottom.setRotationPoint(0F, -12F, 0F);
        body_bottom.setTextureSize(200, 100);
        body_bottom.mirror = true;
        setRotation(body_bottom, 0F, 0F, 0F);
        leg_top_left = new ModelRenderer(this, 0, 78);
        leg_top_left.addBox(-2F, 9F, -2F, 4, 15, 4);
        leg_top_left.setRotationPoint(3F, 0F, 0F);
        leg_top_left.setTextureSize(200, 100);
        leg_top_left.mirror = true;
        setRotation(leg_top_left, 0F, 0F, 0F);
        leg_top_left.mirror = false;
        Shape5 = new ModelRenderer(this, 0, 62);
        Shape5.addBox(-1.5F, 0F, -1.5F, 3, 9, 3);
        Shape5.setRotationPoint(3F, 0F, 0F);
        Shape5.setTextureSize(200, 100);
        Shape5.mirror = true;
        setRotation(Shape5, 0F, 0F, 0F);
        Shape5.mirror = false;
        leg_bottom_right = new ModelRenderer(this, 0, 78);
        leg_bottom_right.addBox(-2F, 9F, -2F, 4, 15, 4);
        leg_bottom_right.setRotationPoint(-3F, 0F, 0F);
        leg_bottom_right.setTextureSize(200, 100);
        leg_bottom_right.mirror = true;
        setRotation(leg_bottom_right, 0F, 0F, 0F);
        leg_top_right = new ModelRenderer(this, 0, 62);
        leg_top_right.addBox(-1.5F, 0F, -1.5F, 3, 9, 3);
        leg_top_right.setRotationPoint(-3F, 0F, 0F);
        leg_top_right.setTextureSize(200, 100);
        leg_top_right.mirror = true;
        setRotation(leg_top_right, 0F, 0F, 0F);
        body = new ModelRenderer(this, 0, 28);
        body.addBox(-2.5F, 0F, -2.5F, 5, 8, 5);
        body.setRotationPoint(0F, -12F, 0F);
        body.setTextureSize(200, 100);
        body.mirror = true;
        setRotation(body, 0F, 0F, 0F);
        arm_blade_left = new ModelRenderer(this, 25, 64);
        arm_blade_left.addBox(-1.5F, 9F, -12F, 3, 6, 27);
        arm_blade_left.setRotationPoint(2F, -9F, 0F);
        arm_blade_left.setTextureSize(200, 100);
        arm_blade_left.mirror = true;
        setRotation(arm_blade_left, 0.3490659F, 0F, -0.7330383F);
        arm_blade_left.mirror = false;
        arm_left = new ModelRenderer(this, 0, 47);
        arm_left.addBox(-1F, 0F, -1F, 2, 10, 2);
        arm_left.setRotationPoint(2F, -9F, 0F);
        arm_left.setTextureSize(200, 100);
        arm_left.mirror = true;
        setRotation(arm_left, 0F, 0F, -0.7330383F);
        arm_left.mirror = false;
        arm_right = new ModelRenderer(this, 0, 47);
        arm_right.addBox(-1F, 0F, -1F, 2, 10, 2);
        arm_right.setRotationPoint(-2F, -9F, 0F);
        arm_right.setTextureSize(200, 100);
        arm_right.mirror = true;
        setRotation(arm_right, 0F, 0F, 0.7330383F);
        arm_blade_right = new ModelRenderer(this, 25, 64);
        arm_blade_right.addBox(-1.5F, 9F, -12F, 3, 6, 27);
        arm_blade_right.setRotationPoint(-2F, -9F, 0F);
        arm_blade_right.setTextureSize(200, 100);
        arm_blade_right.mirror = true;
        setRotation(arm_blade_right, 0.3490659F, 0F, 0.7330383F);
        arm_blade_Tip_right = new ModelRenderer(this, 126, 53);
        arm_blade_Tip_right.addBox(-1F, 12F, -9F, 2, 4, 29);
        arm_blade_Tip_right.setRotationPoint(-2F, -9F, 0F);
        arm_blade_Tip_right.setTextureSize(200, 100);
        arm_blade_Tip_right.mirror = true;
        setRotation(arm_blade_Tip_right, 0.3490659F, 0F, 0.7330383F);
        arm_blade_tip_right = new ModelRenderer(this, 126, 53);
        arm_blade_tip_right.addBox(-1F, 12F, -9F, 2, 4, 29);
        arm_blade_tip_right.setRotationPoint(2F, -9F, 0F);
        arm_blade_tip_right.setTextureSize(200, 100);
        arm_blade_tip_right.mirror = true;
        setRotation(arm_blade_tip_right, 0.3490659F, 0F, -0.7330383F);
        Cape = new ModelRenderer(this, 87, 24);
        Cape.addBox(-7.5F, 3F, 1F, 15, 34, 1);
        Cape.setRotationPoint(0F, -12F, 0F);
        Cape.setTextureSize(200, 100);
        Cape.mirror = true;
        setRotation(Cape, 0.9250245F, 0F, 0F);
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5)
    {
        super.render(entity, f, f1, f2, f3, f4, f5);
        setRotationAngles(f, f1, f2, f3, f4, f5, entity);
        hair.render(f5);
        haed.render(f5);
        horn_head.render(f5);
        horn_chest.render(f5);
        body_bottom.render(f5);
        leg_top_left.render(f5);
        Shape5.render(f5);
        leg_bottom_right.render(f5);
        leg_top_right.render(f5);
        body.render(f5);
        arm_blade_left.render(f5);
        arm_left.render(f5);
        arm_right.render(f5);
        arm_blade_right.render(f5);
        arm_blade_Tip_right.render(f5);
        arm_blade_tip_right.render(f5);
        Cape.render(f5);
    }

    @Override
    public void setRotationAngles(float f, float f1, float f2, float f3, float f4, float f5, Entity e)
    {
        super.setRotationAngles(f, f1, f2, f3, f4, f5, e);
    }

}
