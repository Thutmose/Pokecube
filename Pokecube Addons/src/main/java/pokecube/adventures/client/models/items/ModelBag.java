package pokecube.adventures.client.models.items;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.FMLClientHandler;

public class ModelBag extends ModelBase {
	// fields
	ModelRenderer Pack_pt1;
	ModelRenderer Pack_pt2;
	ModelRenderer Pack_pt3;
	ModelRenderer Pack_pt4;
	ModelRenderer Pack_pt5;

	public static ModelBag model = new ModelBag();

	public ModelBag() {
		textureWidth = 64;
		textureHeight = 32;

		Pack_pt1 = new ModelRenderer(this, 0, 0);
		Pack_pt1.addBox(-5.5F, -6F, 0F, 11, 12, 7);
		Pack_pt1.setRotationPoint(0F, 0F, 0F);
		Pack_pt1.setTextureSize(64, 32);
		Pack_pt1.mirror = true;
		setRotation(Pack_pt1, 0F, 0F, 0F);
		Pack_pt2 = new ModelRenderer(this, 0, 19);
		Pack_pt2.addBox(-4.5F, -7.5F, 0F, 9, 2, 6);
		Pack_pt2.setRotationPoint(0F, 0F, 0F);
		Pack_pt2.setTextureSize(64, 32);
		Pack_pt2.mirror = true;
		setRotation(Pack_pt2, 0F, 0F, 0F);
		Pack_pt3 = new ModelRenderer(this, 48, 0);
		Pack_pt3.addBox(-3.5F, -1F, 6.1F, 7, 6, 1);
		Pack_pt3.setRotationPoint(0F, 0F, 0F);
		Pack_pt3.setTextureSize(64, 32);
		Pack_pt3.mirror = true;
		setRotation(Pack_pt3, 0F, 0F, 0F);
		Pack_pt4 = new ModelRenderer(this, 36, 0);
		Pack_pt4.addBox(-5.6F, -4F, 1F, 1, 9, 5);
		Pack_pt4.setRotationPoint(0F, 0F, 0F);
		Pack_pt4.setTextureSize(64, 32);
		Pack_pt4.mirror = true;
		setRotation(Pack_pt4, -0.1745329F, 0F, 0F);
		Pack_pt5 = new ModelRenderer(this, 36, 0);
		Pack_pt5.addBox(4.6F, -4F, 1F, 1, 9, 5);
		Pack_pt5.setRotationPoint(0F, 0F, 0F);
		Pack_pt5.setTextureSize(64, 32);
		Pack_pt5.mirror = true;
		setRotation(Pack_pt5, -0.1745329F, 0F, 0F);
	}

	public void render(float f5) {
		GL11.glPushMatrix();
		GL11.glTranslated(0.0, 0.4, 0.1);
		GL11.glScaled(0.1, 0.1, 0.1);

		ResourceLocation texture = new ResourceLocation(
				"pokecube_adventures:textures/Bag.png");
		FMLClientHandler.instance().getClient().renderEngine
				.bindTexture(texture);
		Pack_pt1.render(f5);
		Pack_pt2.render(f5);
		Pack_pt3.render(f5);
		Pack_pt4.render(f5);
		Pack_pt5.render(f5);

		GL11.glPopMatrix();
	}

	private void setRotation(ModelRenderer model, float x, float y, float z) {
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}
}
