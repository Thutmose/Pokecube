
package pokecube.adventures.client.models.blocks;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;

public class ModelPC extends ModelBase
{
    // fields
    ModelRenderer Computer_base;
    ModelRenderer Computer_foot;
    ModelRenderer Pokeball_tray;
    ModelRenderer Moniter_and_keyboard_base;
    ModelRenderer Moniter_screen;
    ModelRenderer Moniter_top_and_rear;
    ModelRenderer Fill_1;
    ModelRenderer Fill_2;

    public ModelPC()
    {
        textureWidth = 140;
        textureHeight = 70;

        Computer_base = new ModelRenderer(this, 0, 0);
        Computer_base.addBox(-8F, 0F, -5F, 16, 16, 13);
        Computer_base.setRotationPoint(0F, 8F, 0F);
        Computer_base.setTextureSize(140, 70);
        Computer_base.mirror = true;
        setRotation(Computer_base, 0F, 0F, 0F);
        Computer_foot = new ModelRenderer(this, 0, 29);
        Computer_foot.addBox(-7F, 12F, -8F, 14, 4, 3);
        Computer_foot.setRotationPoint(0F, 8F, 0F);
        Computer_foot.setTextureSize(140, 70);
        Computer_foot.mirror = true;
        setRotation(Computer_foot, 0F, 0F, 0F);
        Pokeball_tray = new ModelRenderer(this, 0, 36);
        Pokeball_tray.addBox(-5F, 1F, -9.5F, 10, 5, 3);
        Pokeball_tray.setRotationPoint(0F, 8F, 0F);
        Pokeball_tray.setTextureSize(140, 70);
        Pokeball_tray.mirror = true;
        setRotation(Pokeball_tray, 0.5585054F, 0F, 0F);
        Moniter_and_keyboard_base = new ModelRenderer(this, 58, 0);
        Moniter_and_keyboard_base.addBox(-8F, -3F, -8F, 16, 3, 16);
        Moniter_and_keyboard_base.setRotationPoint(0F, 8F, 0F);
        Moniter_and_keyboard_base.setTextureSize(140, 70);
        Moniter_and_keyboard_base.mirror = true;
        setRotation(Moniter_and_keyboard_base, 0F, 0F, 0F);
        Moniter_screen = new ModelRenderer(this, 58, 19);
        Moniter_screen.addBox(-8F, -8F, -2.5F, 16, 3, 13);
        Moniter_screen.setRotationPoint(0F, 8F, 0F);
        Moniter_screen.setTextureSize(140, 70);
        Moniter_screen.mirror = true;
        setRotation(Moniter_screen, 0.9250245F, 0F, 0F);
        Moniter_top_and_rear = new ModelRenderer(this, 58, 35);
        Moniter_top_and_rear.addBox(-8F, -13.2F, 0F, 16, 11, 8);
        Moniter_top_and_rear.setRotationPoint(0F, 8F, 0F);
        Moniter_top_and_rear.setTextureSize(140, 70);
        Moniter_top_and_rear.mirror = true;
        setRotation(Moniter_top_and_rear, 0F, 0F, 0F);
        Fill_1 = new ModelRenderer(this, 58, 56);
        Fill_1.addBox(-8F, -7F, -4F, 16, 4, 7);
        Fill_1.setRotationPoint(0F, 8F, 0F);
        Fill_1.setTextureSize(140, 70);
        Fill_1.mirror = true;
        setRotation(Fill_1, 0F, 0F, 0F);
        Fill_2 = new ModelRenderer(this, 24, 56);
        Fill_2.addBox(-8F, -9F, -1F, 16, 2, 1);
        Fill_2.setRotationPoint(0F, 8F, 0F);
        Fill_2.setTextureSize(140, 70);
        Fill_2.mirror = true;
        setRotation(Fill_2, 0F, 0F, 0F);
    }

    public void renderTop(float f5)
    {
        Moniter_and_keyboard_base.render(f5);
        Moniter_screen.render(f5);
        Moniter_top_and_rear.render(f5);
        Fill_1.render(f5);
        Fill_2.render(f5);
    }

    public void renderBase(float f5)
    {
        Computer_base.render(f5);
        Computer_foot.render(f5);
        Pokeball_tray.render(f5);
    }

    private void setRotation(ModelRenderer model, float x, float y, float z)
    {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }

}
