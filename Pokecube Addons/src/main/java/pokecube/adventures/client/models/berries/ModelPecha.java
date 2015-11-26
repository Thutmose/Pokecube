package pokecube.adventures.client.models.berries;

import net.minecraft.client.model.ModelRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.FMLClientHandler;

public class ModelPecha extends ModelBerry {
	  //fields
    ModelRenderer Leftfruit;
    ModelRenderer Rightfruit;
    ModelRenderer Midfruit;
    ModelRenderer Rightfruit_round;
    ModelRenderer Rightfruit_round_low;
    ModelRenderer Leftfruit_round;
    ModelRenderer Leftfruit_round_low;
    ModelRenderer Leftfruit_lumps;
    ModelRenderer Rightfruit_lumps;
    ModelRenderer Right_leaf;
    ModelRenderer Left_leaf;
  
  public ModelPecha()
  {
    textureWidth = 64;
    textureHeight = 64;
    
      Leftfruit = new ModelRenderer(this, 0, 0);
      Leftfruit.addBox(0F, -4F, -4F, 8, 8, 8);
      Leftfruit.setRotationPoint(0F, 18F, 0F);
      Leftfruit.setTextureSize(64, 64);
      Leftfruit.mirror = true;
      setRotation(Leftfruit, 0F, 0F, -0.8203047F);
      Rightfruit = new ModelRenderer(this, 0, 0);
      Rightfruit.addBox(-8F, -4F, -4F, 8, 8, 8);
      Rightfruit.setRotationPoint(0F, 18F, 0F);
      Rightfruit.setTextureSize(64, 64);
      Rightfruit.mirror = true;
      setRotation(Rightfruit, 0F, 0F, 0.8203047F);
      Midfruit = new ModelRenderer(this, 0, 16);
      Midfruit.addBox(-4F, -4F, -3.5F, 8, 8, 7);
      Midfruit.setRotationPoint(0F, 18F, 0F);
      Midfruit.setTextureSize(64, 64);
      Midfruit.mirror = true;
      setRotation(Midfruit, 0F, 0F, 0.7853982F);
      Rightfruit_round = new ModelRenderer(this, 32, 0);
      Rightfruit_round.addBox(-9F, -3.5F, -3.5F, 1, 7, 7);
      Rightfruit_round.setRotationPoint(0F, 18F, 0F);
      Rightfruit_round.setTextureSize(64, 64);
      Rightfruit_round.mirror = true;
      setRotation(Rightfruit_round, 0F, 0F, 0.8203047F);
      Rightfruit_round_low = new ModelRenderer(this, 32, 0);
      Rightfruit_round_low.addBox(-5F, -7.5F, -3.5F, 1, 7, 7);
      Rightfruit_round_low.setRotationPoint(0F, 18F, 0F);
      Rightfruit_round_low.setTextureSize(64, 64);
      Rightfruit_round_low.mirror = true;
      setRotation(Rightfruit_round_low, 0F, 0F, -0.7679449F);
      Leftfruit_round = new ModelRenderer(this, 32, 0);
      Leftfruit_round.addBox(8F, -3.5F, -3.5F, 1, 7, 7);
      Leftfruit_round.setRotationPoint(0F, 18F, 0F);
      Leftfruit_round.setTextureSize(64, 64);
      Leftfruit_round.mirror = true;
      setRotation(Leftfruit_round, 0F, 0F, -0.8203047F);
      Leftfruit_round_low = new ModelRenderer(this, 32, 0);
      Leftfruit_round_low.addBox(4F, -7.5F, -3.5F, 1, 7, 7);
      Leftfruit_round_low.setRotationPoint(0F, 18F, 0F);
      Leftfruit_round_low.setTextureSize(64, 64);
      Leftfruit_round_low.mirror = true;
      setRotation(Leftfruit_round_low, 0F, 0F, 0.7679449F);
      Leftfruit_lumps = new ModelRenderer(this, 0, 31);
      Leftfruit_lumps.addBox(0F, -3.5F, -5F, 7, 7, 10);
      Leftfruit_lumps.setRotationPoint(0F, 18F, 0F);
      Leftfruit_lumps.setTextureSize(64, 64);
      Leftfruit_lumps.mirror = true;
      setRotation(Leftfruit_lumps, 0F, 0F, -0.8203047F);
      Rightfruit_lumps = new ModelRenderer(this, 0, 31);
      Rightfruit_lumps.addBox(-7F, -3.5F, -5F, 7, 7, 10);
      Rightfruit_lumps.setRotationPoint(0F, 18F, 0F);
      Rightfruit_lumps.setTextureSize(64, 64);
      Rightfruit_lumps.mirror = true;
      setRotation(Rightfruit_lumps, 0F, 0F, 0.8203047F);
      Right_leaf = new ModelRenderer(this, 0, 48);
      Right_leaf.addBox(-17F, -4.1F, -4F, 13, 1, 8);
      Right_leaf.setRotationPoint(0F, 18F, 0F);
      Right_leaf.setTextureSize(64, 64);
      Right_leaf.mirror = true;
      setRotation(Right_leaf, 0F, 0F, 0.8203047F);
      Left_leaf = new ModelRenderer(this, 0, 48);
      Left_leaf.addBox(4F, -4.1F, -4F, 13, 1, 8);
      Left_leaf.setRotationPoint(0F, 18F, 0F);
      Left_leaf.setTextureSize(64, 64);
      Left_leaf.mirror = true;
      setRotation(Left_leaf, 0F, 0F, -0.8203047F);
  }
  
  @Override
public void render(float f5)
  {

	  ResourceLocation texture = new ResourceLocation("pokecube_adventures:textures/blocks/Pecha.png");
	  FMLClientHandler.instance().getClient().renderEngine.bindTexture(texture);
	  
    Leftfruit.render(f5);
    Rightfruit.render(f5);
    Midfruit.render(f5);
    Rightfruit_round.render(f5);
    Rightfruit_round_low.render(f5);
    Leftfruit_round.render(f5);
    Leftfruit_round_low.render(f5);
    Leftfruit_lumps.render(f5);
    Rightfruit_lumps.render(f5);
    Right_leaf.render(f5);
    Left_leaf.render(f5);
  }
  
  private void setRotation(ModelRenderer model, float x, float y, float z)
  {
    model.rotateAngleX = x;
    model.rotateAngleY = y;
    model.rotateAngleZ = z;
  }
  

}
