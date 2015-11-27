package pokecube.core.client.models.block.berries;

import net.minecraft.client.model.ModelRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.FMLClientHandler;

public class ModelLeppa extends ModelBerry {
	// fields
	ModelRenderer Berry;
	ModelRenderer Leaf_base;
	ModelRenderer Left_leaf_pt2;
	ModelRenderer Left_leaf_pt3;
	ModelRenderer Right_leaf_pt2;
	ModelRenderer Right_leaf_pt3;

	public ModelLeppa() {
		textureWidth = 64;
		textureHeight = 64;

		Berry = new ModelRenderer(this, 0, 0);
		Berry.addBox(-5F, -5F, -5F, 10, 10, 10);
		Berry.setRotationPoint(0F, 19F, 0F);
		Berry.setTextureSize(64, 64);
		Berry.mirror = true;
		setRotation(Berry, 0F, 0F, 0F);
		Leaf_base = new ModelRenderer(this, 0, 20);
		Leaf_base.addBox(-1.5F, -6F, -1.5F, 3, 1, 3);
		Leaf_base.setRotationPoint(0F, 19F, 0F);
		Leaf_base.setTextureSize(64, 64);
		Leaf_base.mirror = true;
		setRotation(Leaf_base, 0F, 0F, 0F);
		Left_leaf_pt2 = new ModelRenderer(this, 31, 0);
		Left_leaf_pt2.addBox(-1F, -11F, -1F, 1, 5, 2);
		Left_leaf_pt2.setRotationPoint(0F, 19F, 0F);
		Left_leaf_pt2.setTextureSize(64, 64);
		Left_leaf_pt2.mirror = true;
		setRotation(Left_leaf_pt2, 0F, 0F, 0.2443461F);
		Left_leaf_pt3 = new ModelRenderer(this, 40, 0);
		Left_leaf_pt3.addBox(7F, -8F, -2.5F, 1, 14, 5);
		Left_leaf_pt3.setRotationPoint(0F, 19F, 0F);
		Left_leaf_pt3.setTextureSize(64, 64);
		Left_leaf_pt3.mirror = true;
		setRotation(Left_leaf_pt3, 0F, 0F, -0.5934119F);
		Right_leaf_pt2 = new ModelRenderer(this, 31, 0);
		Right_leaf_pt2.addBox(0F, -11F, -1F, 1, 5, 2);
		Right_leaf_pt2.setRotationPoint(0F, 19F, 0F);
		Right_leaf_pt2.setTextureSize(64, 64);
		Right_leaf_pt2.mirror = true;
		setRotation(Right_leaf_pt2, 0F, 0F, -0.2443461F);
		Right_leaf_pt3 = new ModelRenderer(this, 52, 0);
		Right_leaf_pt3.addBox(-8F, -8F, -2.5F, 1, 14, 5);
		Right_leaf_pt3.setRotationPoint(0F, 19F, 0F);
		Right_leaf_pt3.setTextureSize(64, 64);
		Right_leaf_pt3.mirror = true;
		setRotation(Right_leaf_pt3, 0F, 0F, 0.5934119F);
	}

	  @Override
	public void render(float f5)
	  {

		  ResourceLocation texture = new ResourceLocation("pokecube_adventures:textures/blocks/Leppa.png");
		  FMLClientHandler.instance().getClient().renderEngine.bindTexture(texture);
		Berry.render(f5);
		Leaf_base.render(f5);
		Left_leaf_pt2.render(f5);
		Left_leaf_pt3.render(f5);
		Right_leaf_pt2.render(f5);
		Right_leaf_pt3.render(f5);
	}

	private void setRotation(ModelRenderer model, float x, float y, float z) {
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}

}
